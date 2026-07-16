#!/usr/bin/env zx

// Port-parity tests: runs the documentation-example suites of every runtime —
// .NET (the reference), Kotlin/JVM, Java/JVM and TypeScript/Node — against
// the same engine (the QuestPDF package version pinned by src/dotnet/interop)
// and verifies that every example produced its output files and that the
// bytes are identical across runtimes.
//
// Non-deterministic examples (random placeholder content, current date/time,
// PDF timestamps) are detected automatically by running the .NET suite twice:
// outputs that differ between the two .NET runs are only checked for
// presence, everything else must be byte-identical.
//
// Outputs intentionally not ported (external .NET-only libraries, network
// access) are listed in known-not-ported.txt; a listed file that suddenly
// appears in a port fails the run so the list cannot go stale.
//
// Usage:
//   zx run-tests.mjs [--skip-dotnet-restore-check]

import { createHash } from 'node:crypto';
import { fileURLToPath } from 'node:url';

$.verbose = true;

const testsRoot = path.dirname(fileURLToPath(import.meta.url)).replaceAll('\\', '/');
const repoRoot = path.resolve(testsRoot, '../..').replaceAll('\\', '/');

const dotnetProject = `${repoRoot}/src/dotnet/library/QuestPDF.DocumentationExamples`;
const interopProject = `${repoRoot}/src/dotnet/interop`;
const generatorProject = `${repoRoot}/src/dotnet/port-generator/QuestPDF.Interop.Generator`;
const jvmPackageRoot = `${repoRoot}/src/jvm/package`;
const nodePackageRoot = `${repoRoot}/src/node/package`;

const parityRoot = `${repoRoot}/artifacts/port-parity`;
const nativeDir = `${parityRoot}/native`;
const resourcesDir = `${dotnetProject}/Resources`;

const outputExtensions = new Set(['.webp', '.png', '.jpg', '.jpeg', '.pdf']);

// Example classes that stay .NET-only by design: they demonstrate integrations
// with .NET-specific libraries (ScottPlot, ZXing, SkiaSharp) or require network
// access (Mapbox). They are excluded from the .NET parity run entirely — the
// regular .NET test workflows still cover them.
const dotnetOnlyExampleClasses = [
  'ChartExamples',
  'BarcodeExamples',
  'SkiaSharpIntegrationExamples',
  'MapExample',
];

const isWindows = process.platform === 'win32';
const gradlew = `${jvmPackageRoot}/gradlew${isWindows ? '.bat' : ''}`;
const npm = isWindows ? 'npm.cmd' : 'npm';

try {
  await main();
} catch (error) {
  console.error(chalk.red(error?.message ?? String(error)));
  process.exitCode = 1;
}

async function main() {
  const engineVersion = await readPinnedEngineVersion();
  console.log(chalk.blue(`Engine: QuestPDF ${engineVersion} (the version pinned by src/dotnet/interop)`));

  await fs.remove(parityRoot);
  await fs.mkdirp(parityRoot);

  await ensureGeneratedBindings();
  await publishNativeRuntime();

  const dotnetRun1 = await runDotnetSuite(engineVersion, 1);
  const dotnetRun2 = await runDotnetSuite(engineVersion, 2);

  const ports = [
    { name: 'jvm-kotlin', outputs: await runJvmSuite('jvm-kotlin', 'docExamplesTest') },
    { name: 'jvm-java', outputs: await runJvmSuite('jvm-java', 'docExamplesJavaTest') },
    { name: 'node', outputs: await runNodeSuite() },
  ];

  const report = compare(dotnetRun1, dotnetRun2, ports);
  await fs.writeJson(`${parityRoot}/report.json`, report, { spaces: 2 });

  printReport(report);

  if (report.failures.length > 0)
    throw new Error(`Port parity failed: ${report.failures.length} problem(s). Full report: ${parityRoot}/report.json`);

  console.log(chalk.green(`Port parity passed: ${report.identical.length} identical, ${report.presenceOnly.length} presence-only, ${report.notPorted.length} known not ported.`));
}

async function readPinnedEngineVersion() {
  const csproj = await fs.readFile(`${interopProject}/QuestPDF.Native.csproj`, 'utf8');
  const match = csproj.match(/Include="QuestPDF"\s+Version="([^"]+)"/);

  if (!match)
    throw new Error('Could not find the pinned QuestPDF package version in src/dotnet/interop/QuestPDF.Native.csproj.');

  return match[1];
}

async function ensureGeneratedBindings() {
  if (await fs.pathExists(`${jvmPackageRoot}/src/generated/kotlin`) && await fs.pathExists(`${nodePackageRoot}/src/generated`))
    return;

  console.log(chalk.blue('Generated bindings are missing — running the interop generator.'));
  await $`dotnet run --project ${generatorProject}`;
}

async function publishNativeRuntime() {
  const rid = detectRid();
  console.log(chalk.blue(`Publishing QuestPDF.Native (${rid}) once for all suites`));

  await $`dotnet publish ${interopProject} -c Release -r ${rid} -o ${nativeDir}`;

  for (const entry of await fs.readdir(nativeDir)) {
    if (entry.endsWith('.dSYM'))
      await fs.remove(path.join(nativeDir, entry));
  }

  // The .NET suite auto-discovers the fonts copied next to its binaries
  // (Resources/Fonts — Noto Sans Arabic, Noto Emoji, used by the font-fallback
  // examples). The ports register every font deployed with the native runtime,
  // so the same fonts are staged there.
  await fs.copy(`${resourcesDir}/Fonts`, `${nativeDir}/DocumentationFonts`);
}

function detectRid() {
  const arch = process.arch === 'arm64' ? 'arm64' : 'x64';

  if (process.platform === 'darwin')
    return `osx-${arch}`;

  if (process.platform === 'win32')
    return `win-${arch}`;

  const isMusl = fs.pathExistsSync('/etc/alpine-release');
  return isMusl ? `linux-musl-${arch}` : `linux-${arch}`;
}

// ---- suites ----

// The .NET examples write into the test working directory (the build output
// folder). Outputs are swept into a per-run folder afterwards, so the second
// run starts clean and reveals which outputs are non-deterministic.
async function runDotnetSuite(engineVersion, runIndex) {
  const targetDir = `${parityRoot}/dotnet-run${runIndex}`;
  await fs.mkdirp(targetDir);

  console.log(`::group::dotnet run ${runIndex}`);

  try {
    // Invariant globalization pins culture-sensitive formatting (decimal
    // separators, month names) to the same output the ports produce; the CI
    // workflows set the same variable globally.
    const environment = { ...process.env, DOTNET_SYSTEM_GLOBALIZATION_INVARIANT: '1' };

    const filter = dotnetOnlyExampleClasses.map(name => `FullyQualifiedName!~${name}`).join('&');
    await $({ cwd: dotnetProject, env: environment })`dotnet test -c Release -p:QuestPdfPackageVersion=${engineVersion} --filter ${filter} --nologo`;

    const binDir = `${dotnetProject}/bin/Release/net10.0`;
    const outputs = await collectOutputs(binDir, { sweep: true, targetDir });

    if (outputs.size === 0)
      throw new Error('The .NET suite produced no output files — did the tests run?');

    return outputs;
  } finally {
    console.log('::endgroup::');
  }
}

// The JVM package carries the documentation examples twice — once in Kotlin,
// once in Java — each with its own Gradle test task and output directory.
async function runJvmSuite(name, gradleTask) {
  const outputDir = `${parityRoot}/${name}`;
  await fs.mkdirp(outputDir);

  console.log(`::group::${name}`);

  try {
    const environment = {
      ...process.env,
      QUESTPDF_NATIVE_DIR: nativeDir,
      QUESTPDF_DOC_EXAMPLES_OUTPUT: outputDir,
      QUESTPDF_DOC_EXAMPLES_RESOURCES: resourcesDir,
    };

    await $({ cwd: jvmPackageRoot, env: environment })`${gradlew} --no-daemon ${gradleTask}`;
    return await collectOutputs(outputDir, { sweep: false });
  } finally {
    console.log('::endgroup::');
  }
}

async function runNodeSuite() {
  const outputDir = `${parityRoot}/node`;
  await fs.mkdirp(outputDir);

  console.log('::group::node');

  try {
    const environment = {
      ...process.env,
      QUESTPDF_NATIVE_DIR: nativeDir,
      QUESTPDF_DOC_EXAMPLES_OUTPUT: outputDir,
      QUESTPDF_DOC_EXAMPLES_RESOURCES: resourcesDir,
    };

    await $({ cwd: nodePackageRoot })`${npm} install --no-audit --no-fund`;
    await $({ cwd: nodePackageRoot })`${npm} run build`;
    await $({ cwd: nodePackageRoot, env: environment })`node --test ${'dist/doc-examples/**/*.test.js'}`;

    return await collectOutputs(outputDir, { sweep: false });
  } finally {
    console.log('::endgroup::');
  }
}

// Returns Map<fileName, sha256>. With sweep, files are moved out of the
// source directory (the .NET build output) into targetDir first.
async function collectOutputs(directory, { sweep, targetDir }) {
  const outputs = new Map();

  for (const entry of await fs.readdir(directory, { recursive: true })) {
    const relative = String(entry).replaceAll('\\', '/');
    const extension = path.extname(relative).toLowerCase();

    if (!outputExtensions.has(extension))
      continue;

    // Test resources copied next to the .NET binaries are inputs, not outputs.
    if (relative.startsWith('Resources/'))
      continue;

    const source = path.join(directory, relative);

    if (!(await fs.stat(source)).isFile())
      continue;

    if (sweep) {
      const swept = path.join(targetDir, relative);
      await fs.mkdirp(path.dirname(swept));
      await fs.move(source, swept, { overwrite: true });
      outputs.set(relative, await hashFile(swept));
    } else {
      outputs.set(relative, await hashFile(source));
    }
  }

  return outputs;
}

async function hashFile(filePath) {
  return createHash('sha256').update(await fs.readFile(filePath)).digest('hex');
}

// ---- comparison ----

function compare(dotnetRun1, dotnetRun2, ports) {
  const knownNotPorted = readKnownNotPorted();
  const report = { identical: [], presenceOnly: [], notPorted: [], failures: [] };

  for (const [name, hash] of [...dotnetRun1].sort(([a], [b]) => a.localeCompare(b))) {
    if (knownNotPorted.has(name)) {
      report.notPorted.push(name);

      for (const port of ports) {
        if (port.outputs.has(name))
          report.failures.push(`${name}: listed in known-not-ported.txt but the ${port.name} port produced it — remove the stale entry.`);
      }

      continue;
    }

    const missing = ports.filter(port => !port.outputs.has(name));

    if (missing.length > 0) {
      report.failures.push(`${name}: missing in ${missing.map(port => port.name).join(' and ')}.`);
      continue;
    }

    const deterministic = dotnetRun2.get(name) === hash;

    if (!deterministic) {
      report.presenceOnly.push(name);
      continue;
    }

    const mismatched = ports.filter(port => port.outputs.get(name) !== hash);

    if (mismatched.length > 0)
      report.failures.push(`${name}: bytes differ in ${mismatched.map(port => port.name).join(' and ')}.`);
    else
      report.identical.push(name);
  }

  // Outputs only the second .NET run produced would hide a flaky test; surface them.
  for (const name of dotnetRun2.keys()) {
    if (!dotnetRun1.has(name))
      report.failures.push(`${name}: produced by the second .NET run only — the suite is not stable.`);
  }

  for (const port of ports) {
    for (const name of port.outputs.keys()) {
      if (!dotnetRun1.has(name))
        report.failures.push(`${name}: produced by the ${port.name} port but not by .NET — output name drift?`);
    }
  }

  return report;
}

function readKnownNotPorted() {
  const listPath = `${testsRoot}/known-not-ported.txt`;

  if (!fs.pathExistsSync(listPath))
    return new Set();

  return new Set(
    fs.readFileSync(listPath, 'utf8')
      .split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0 && !line.startsWith('#')),
  );
}

function printReport(report) {
  console.log('');
  console.log(chalk.bold('Port parity report'));
  console.log(`  byte-identical across all runtimes: ${report.identical.length}`);
  console.log(`  presence-only (non-deterministic):  ${report.presenceOnly.length}`);
  console.log(`  known not ported:                   ${report.notPorted.length}`);
  console.log(`  failures:                           ${report.failures.length}`);

  for (const failure of report.failures)
    console.error(chalk.red(`  FAIL ${failure}`));
}
