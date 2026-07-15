#!/usr/bin/env zx

// Packs the QuestPDF npm package into a local feed of .tgz files, installs it
// into every package-test app, runs the apps, and verifies that the produced
// documents are valid.
//
// Usage:
//   zx run-tests.mjs [--version <package-version>]
//
// Without --version, the script regenerates the bindings (if missing),
// publishes the QuestPDF.Native library for the current platform, and packs
// the package into <repo>/artifacts/npm-feed with a unique version, so local
// changes are always tested. With --version, it expects the feed to already
// contain the main package and the native package for the current platform
// at that version (CI).

import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';

$.verbose = true;

// Forward slashes work on every supported OS and avoid backslash escaping issues in zx commands.
const testsRoot = path.dirname(fileURLToPath(import.meta.url)).replaceAll('\\', '/');
const repoRoot = path.resolve(testsRoot, '../../..').replaceAll('\\', '/');
const packageRoot = `${repoRoot}/src/node/package`;
const generatorProject = `${repoRoot}/src/dotnet/port-generator/QuestPDF.Interop.Generator`;
const feedDir = `${repoRoot}/artifacts/npm-feed`;
const testOutputRoot = `${repoRoot}/artifacts/test-output`;
const resourcesDir = `${testsRoot}/shared/resources`;

// Must match OUTPUT_FOLDER in shared/test-document.cjs and console-typescript/src/main.ts.
const outputFolderName = 'TestOutput';

const isWindows = process.platform === 'win32';
const npm = isWindows ? 'npm.cmd' : 'npm';

const apps = {
  'console-cjs': { command: ['node', 'index.js'] },
  'console-esm': { command: ['node', 'index.mjs'] },
  'console-typescript': { build: buildTypescriptApp, command: ['node', 'dist/main.js'] },
  'webapp-express': { command: ['node', 'server.js'], web: true },
};

const platformId = detectPlatformId();
let version = argv.version;

try {
  await main();
} catch (error) {
  console.error(chalk.red(error?.message ?? String(error)));
  process.exitCode = 1;
}

async function main() {
  if (!version)
    version = await packFeed();

  for (const archive of [mainArchive(), nativeArchive()]) {
    if (!await fs.pathExists(archive))
      throw new Error(`${archive} was not found. Omit --version to pack the feed automatically.`);
  }

  const failures = [];

  for (const name of Object.keys(apps)) {
    console.log(`::group::${name}`);

    try {
      await runApp(name);
      console.log(chalk.green(`${name} passed`));
    } catch (error) {
      failures.push(name);
      console.error(chalk.red(`${name} failed: ${error?.message ?? error}`));
    } finally {
      console.log('::endgroup::');
    }
  }

  if (failures.length > 0)
    throw new Error(`${failures.length} app(s) failed: ${failures.join(', ')}.`);

  console.log(chalk.green('All package tests passed.'));
}

// The npm platform-package identifier for the current machine; must match
// platformPackageId() in the package's native-bridge.ts.
function detectPlatformId() {
  const arch = process.arch === 'arm64' ? 'arm64' : 'x64';

  if (process.platform === 'darwin')
    return `darwin-${arch}`;

  if (process.platform === 'win32')
    return `win32-${arch}`;

  const isMusl = fs.pathExistsSync('/etc/alpine-release');
  return isMusl ? `linux-musl-${arch}` : `linux-${arch}`;
}

function mainArchive() {
  return `${feedDir}/questpdf-${version}.tgz`;
}

function nativeArchive() {
  return `${feedDir}/questpdf-native-${platformId}-${version}.tgz`;
}

async function packFeed() {
  // A unique version prevents npm from silently reusing a previously cached package.
  const packVersion = `0.1.0-local.${new Date().toISOString().replaceAll(/\D/g, '').slice(0, 14)}`;

  if (!await fs.pathExists(`${packageRoot}/src/generated`)) {
    console.log(chalk.blue('Generated bindings are missing — running the interop generator.'));
    await $`dotnet run --project ${generatorProject}`;
  }

  await fs.remove(feedDir);

  console.log(chalk.blue(`Packing questpdf ${packVersion} (natives: ${platformId})`));
  await $({ cwd: packageRoot })`${npm} install --no-audit --no-fund`;
  await $({ cwd: packageRoot })`node scripts/publish-native.mjs --staging`;
  await $({ cwd: packageRoot })`node scripts/pack-feed.mjs --version ${packVersion}`;

  return packVersion;
}

async function runApp(name) {
  const app = apps[name];
  const appDir = `${testsRoot}/${name}`;

  await Promise.all(['node_modules', 'package-lock.json', 'dist', outputFolderName].map(entry => fs.remove(`${appDir}/${entry}`)));

  // One install resolves the app's own dependencies together with the local
  // feed archives. The remaining @questpdf/native-* optional dependencies of
  // the main package are not in any registry; npm skips them.
  await $({ cwd: appDir })`${npm} install --no-save --no-audit --no-fund ${mainArchive()} ${nativeArchive()}`;

  if (app.build)
    await app.build(appDir);

  const environment = { ...process.env, QUESTPDF_TEST_RESOURCES: resourcesDir };

  if (app.web)
    await runWebApp(app.command, appDir, environment);
  else
    await runConsoleApp(app.command, appDir, environment);

  const outputDirectory = `${appDir}/${outputFolderName}`;
  await validateDocuments(outputDirectory);

  const collectedOutput = `${testOutputRoot}/${name}`;
  await fs.remove(collectedOutput);
  await fs.copy(outputDirectory, collectedOutput);
}

async function buildTypescriptApp(appDir) {
  await $({ cwd: appDir })`${npm} run build`;
}

async function runConsoleApp(command, appDir, environment) {
  const [executable, ...args] = command;
  console.log(chalk.cyan(`Running ${executable} ${args.join(' ')}`));
  await $({ cwd: appDir, env: environment, stdio: 'inherit' })`${executable} ${args}`;
}

async function runWebApp(command, appDir, environment) {
  const port = 5087;
  const baseUrl = `http://127.0.0.1:${port}`;
  const [executable, ...args] = command;
  console.log(chalk.cyan(`Starting ${executable} ${args.join(' ')} at ${baseUrl}`));

  const server = spawn(executable, args, {
    cwd: appDir,
    env: { ...environment, PORT: String(port) },
    stdio: ['ignore', 'inherit', 'inherit']
  });

  const serverFailure = new Promise((resolve, reject) => {
    server.once('error', reject);
    server.once('exit', (code, signal) => reject(new Error(`Web app exited prematurely (code: ${code}, signal: ${signal}).`)));
  });

  // The rejection is expected when the server is stopped after a successful test.
  serverFailure.catch(() => {});

  try {
    await Promise.race([serverFailure, waitForServer(`${baseUrl}/health`)]);

    const response = await fetch(`${baseUrl}/generate`);

    if (!response.ok)
      throw new Error(`GET /generate returned HTTP ${response.status}.`);

    const document = Buffer.from(await response.arrayBuffer());

    if (!hasHeader(document, '%PDF-'))
      throw new Error('The /generate endpoint did not return a valid PDF document.');

    console.log(chalk.green(`Downloaded generated document (${document.length} bytes)`));
  } finally {
    await stopServer(server);
  }
}

async function waitForServer(url) {
  await retry(60, '1s', async () => {
    const response = await fetch(url);

    if (!response.ok)
      throw new Error(`Health check returned HTTP ${response.status}.`);
  });

  console.log(chalk.green(`Server is ready at ${url}`));
}

async function stopServer(server) {
  for (const signal of ['SIGTERM', 'SIGKILL']) {
    if (server.exitCode !== null || server.signalCode !== null)
      return;

    server.kill(signal);
    await Promise.race([new Promise(resolve => server.once('exit', resolve)), sleep(5000)]);
  }
}

async function validateDocuments(outputDirectory) {
  await validatePdf(`${outputDirectory}/skia.pdf`);
  await validatePdf(`${outputDirectory}/qpdf.pdf`);

  if (isWindows)
    await validateXps(`${outputDirectory}/skia.xps`);
}

async function validatePdf(filePath) {
  const buffer = await readDocument(filePath);

  if (!hasHeader(buffer, '%PDF-'))
    throw new Error(`Output file has an invalid header: ${filePath}`);

  if (!buffer.subarray(-2048).includes('%%EOF'))
    throw new Error(`PDF EOF marker was not found: ${filePath}`);

  console.log(chalk.green(`Validated ${filePath} (${buffer.length} bytes)`));
}

async function validateXps(filePath) {
  const buffer = await readDocument(filePath);

  if (!hasHeader(buffer, 'PK\x03\x04'))
    throw new Error(`Output file has an invalid header: ${filePath}`);

  console.log(chalk.green(`Validated ${filePath} (${buffer.length} bytes)`));
}

async function readDocument(filePath) {
  if (!await fs.pathExists(filePath))
    throw new Error(`Expected output file was not created: ${filePath}`);

  const buffer = await fs.readFile(filePath);

  if (buffer.length < 1024)
    throw new Error(`Output file is suspiciously small: ${filePath} (${buffer.length} bytes).`);

  return buffer;
}

function hasHeader(buffer, header) {
  return buffer.subarray(0, header.length).toString('latin1') === header;
}
