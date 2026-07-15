#!/usr/bin/env zx

// Publishes the QuestPDF JVM package into a local Maven feed, builds every
// package-test app against that feed, runs the resulting binaries, and
// verifies that the produced documents are valid.
//
// Usage:
//   zx run-tests.mjs [--version <package-version>]
//
// Without --version, the script regenerates the bindings (if missing),
// publishes the QuestPDF.Native library for the current platform, and
// publishes the package into <repo>/artifacts/maven-feed with a unique
// version, so local changes are always tested. With --version, it expects the
// feed to already contain that version together with the natives classifier
// jar for the current platform (CI).

import { execFileSync, spawn } from 'node:child_process';
import { fileURLToPath, pathToFileURL } from 'node:url';

$.verbose = true;

// Forward slashes work on every supported OS and avoid backslash escaping issues in zx commands.
const testsRoot = path.dirname(fileURLToPath(import.meta.url)).replaceAll('\\', '/');
const repoRoot = path.resolve(testsRoot, '../../..').replaceAll('\\', '/');
const packageRoot = `${repoRoot}/src/jvm/package`;
const generatorProject = `${repoRoot}/src/dotnet/port-generator/QuestPDF.Interop.Generator`;
const feedDir = `${repoRoot}/artifacts/maven-feed`;
const testOutputRoot = `${repoRoot}/artifacts/test-output`;
const resourcesDir = `${testsRoot}/shared/resources`;

// Must match TestRunner.OUTPUT_FOLDER in shared/kotlin.
const outputFolderName = 'TestOutput';

const isWindows = process.platform === 'win32';
const gradlew = `${packageRoot}/gradlew${isWindows ? '.bat' : ''}`;
const mvnw = `${testsRoot}/mvnw${isWindows ? '.cmd' : ''}`;

const apps = {
  'console-gradle': { build: buildGradleApp, executable: runnableGradleScript },
  'console-maven': { build: buildMavenApp, executable: () => jarCommand('console-maven', 'console-maven-1.0.0.jar') },
  'webapp-spring-boot': { build: buildMavenApp, executable: () => jarCommand('webapp-spring-boot', 'webapp-spring-boot-1.0.0.jar'), web: true },
};

const rid = detectRid();
let version = argv.version;

try {
  await main();
} catch (error) {
  console.error(chalk.red(error?.message ?? String(error)));
  process.exitCode = 1;
}

async function main() {
  configureJavaHome();

  if (!version)
    version = await publishPackage();
  else if (!await fs.pathExists(`${feedDir}/com/questpdf/questpdf/${version}/questpdf-${version}.pom`))
    throw new Error(`QuestPDF ${version} was not found in ${feedDir}. Omit --version to publish the package automatically.`);

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

function detectRid() {
  const arch = process.arch === 'arm64' ? 'arm64' : 'x64';

  if (process.platform === 'darwin')
    return `osx-${arch}`;

  if (process.platform === 'win32')
    return `win-${arch}`;

  const isMusl = fs.pathExistsSync('/etc/alpine-release');
  return isMusl ? `linux-musl-${arch}` : `linux-${arch}`;
}

// Gradle 9.6 requires a supported JDK (21 is the tested toolchain). CI sets
// JAVA_HOME via setup-java; locally on macOS the JDK 21 install is looked up.
function configureJavaHome() {
  if (process.env.JAVA_HOME || process.platform !== 'darwin')
    return;

  try {
    const javaHome = os.platform() === 'darwin'
      ? String(spawnSyncOutput('/usr/libexec/java_home', ['-v', '21']))
      : '';

    if (javaHome) {
      process.env.JAVA_HOME = javaHome;
      console.log(chalk.blue(`Using JAVA_HOME=${javaHome}`));
    }
  } catch {
    // Fall through: Gradle/Maven will use whatever java is on PATH.
  }
}

function spawnSyncOutput(command, args) {
  return execFileSync(command, args, { encoding: 'utf8' }).trim();
}

async function publishPackage() {
  // A unique version prevents Maven and Gradle from silently reusing a previously cached package.
  const packVersion = `0.1.0-local.${new Date().toISOString().replaceAll(/\D/g, '').slice(0, 14)}`;

  if (!await fs.pathExists(`${packageRoot}/src/generated/kotlin`)) {
    console.log(chalk.blue('Generated bindings are missing — running the interop generator.'));
    await $`dotnet run --project ${generatorProject}`;
  }

  await fs.remove(feedDir);

  console.log(chalk.blue(`Publishing QuestPDF ${packVersion} (natives: ${rid})`));
  await $({ cwd: packageRoot })`${gradlew} publishNative publishToLocalFeed -PquestpdfVersion=${packVersion}`;

  return packVersion;
}

async function runApp(name) {
  const app = apps[name];
  const appDir = `${testsRoot}/${name}`;

  await Promise.all(['build', 'target', '.gradle', outputFolderName].map(dir => fs.remove(`${appDir}/${dir}`)));

  await app.build(name);

  const command = app.executable(name);
  const environment = { ...process.env, QUESTPDF_TEST_RESOURCES: resourcesDir };

  if (app.web)
    await runWebApp(command, appDir, environment);
  else
    await runConsoleApp(command, appDir, environment);

  const outputDirectory = `${appDir}/${outputFolderName}`;
  await validateDocuments(outputDirectory);

  const collectedOutput = `${testOutputRoot}/${name}`;
  await fs.remove(collectedOutput);
  await fs.copy(outputDirectory, collectedOutput);
}

async function buildGradleApp(name) {
  const appDir = `${testsRoot}/${name}`;
  const properties = [
    `-PquestpdfVersion=${version}`,
    `-PquestpdfFeedDir=${feedDir}`,
    `-PquestpdfRid=${rid}`
  ];

  await $({ cwd: appDir })`${gradlew} --no-daemon installDist ${properties}`;
}

async function buildMavenApp(name) {
  const appDir = `${testsRoot}/${name}`;
  const properties = [
    `-Dquestpdf.version=${version}`,
    `-Dquestpdf.feed.url=${pathToFileURL(feedDir).href}`,
    `-Dquestpdf.rid=${rid}`
  ];

  await $({ cwd: appDir })`${mvnw} -B package ${properties}`;
}

function runnableGradleScript(name) {
  return [`${testsRoot}/${name}/build/install/${name}/bin/${name}${isWindows ? '.bat' : ''}`];
}

function jarCommand(name, artifact) {
  const java = process.env.JAVA_HOME ? `${process.env.JAVA_HOME.replaceAll('\\', '/')}/bin/java${isWindows ? '.exe' : ''}` : 'java';
  return [java, '-jar', `${testsRoot}/${name}/target/${artifact}`];
}

async function runConsoleApp(command, appDir, environment) {
  const [executable, ...args] = command;
  console.log(chalk.cyan(`Running ${path.basename(executable)} ${args.join(' ')}`));
  await $({ cwd: appDir, env: environment, stdio: 'inherit' })`${executable} ${args}`;
}

async function runWebApp(command, appDir, environment) {
  const baseUrl = 'http://127.0.0.1:5087';
  const [executable, ...args] = command;
  console.log(chalk.cyan(`Starting ${path.basename(executable)} at ${baseUrl}`));

  const server = spawn(executable, [...args, '--server.port=5087'], {
    cwd: appDir,
    env: environment,
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
