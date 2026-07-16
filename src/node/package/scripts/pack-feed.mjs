// Packs the questpdf npm package plus one @questpdf/native-* package per
// staged platform into a local feed of .tgz files, mirroring the artifacts a
// real npm publish would upload.
//
// Native runtime directories (dotnet publish output) are staged under
// <natives-dir>/<rid>/ — locally by scripts/publish-native.mjs, on CI by
// downloading the per-platform build artifacts.
//
// Usage:
//   node scripts/pack-feed.mjs [--version <v>] [--natives-dir <dir>] [--feed-dir <dir>] [--require-all]

import { execSync } from 'node:child_process';
import { cpSync, existsSync, mkdirSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import * as path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';

const packageRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = path.resolve(packageRoot, '..', '..', '..');

const { values } = parseArgs({
    options: {
        'version': { type: 'string' },
        'natives-dir': { type: 'string' },
        'feed-dir': { type: 'string' },
        'require-all': { type: 'boolean', default: false },
    },
});

const version = values.version
    ?? `0.1.0-local.${new Date().toISOString().replaceAll(/\D/g, '').slice(0, 14)}`;
const nativesDir = path.resolve(packageRoot, values['natives-dir'] ?? 'native-staging');
const feedDir = path.resolve(packageRoot, values['feed-dir'] ?? path.join(repoRoot, 'artifacts', 'npm-feed'));

// One entry per supported platform: dotnet RID → npm platform package descriptor.
const platforms = {
    'osx-arm64': { id: 'darwin-arm64', os: 'darwin', cpu: 'arm64' },
    'osx-x64': { id: 'darwin-x64', os: 'darwin', cpu: 'x64' },
    'win-x64': { id: 'win32-x64', os: 'win32', cpu: 'x64' },
    'win-arm64': { id: 'win32-arm64', os: 'win32', cpu: 'arm64' },
    'linux-x64': { id: 'linux-x64', os: 'linux', cpu: 'x64', libc: ['glibc'] },
    'linux-arm64': { id: 'linux-arm64', os: 'linux', cpu: 'arm64', libc: ['glibc'] },
    'linux-musl-x64': { id: 'linux-musl-x64', os: 'linux', cpu: 'x64', libc: ['musl'] },
};

function nativeLibraryName(rid) {
    if (rid.startsWith('osx-'))
        return 'QuestPDF.Native.dylib';
    if (rid.startsWith('win-'))
        return 'QuestPDF.Native.dll';
    return 'QuestPDF.Native.so';
}

function run(command, cwd) {
    execSync(command, { cwd, stdio: 'inherit' });
}

const stagedRids = Object.keys(platforms)
    .filter(rid => existsSync(path.join(nativesDir, rid, nativeLibraryName(rid))));

if (stagedRids.length === 0)
    throw new Error(`No native runtime directories found under ${nativesDir}. Run 'node scripts/publish-native.mjs --staging' first.`);

const missingRids = Object.keys(platforms).filter(rid => !stagedRids.includes(rid));

if (values['require-all'] && missingRids.length > 0)
    throw new Error(`Native runtime directories are missing for: ${missingRids.join(', ')}.`);

if (missingRids.length > 0)
    console.warn(`Native packages are NOT packed for: ${missingRids.join(', ')} (not staged).`);

if (!existsSync(path.join(packageRoot, 'node_modules', 'typescript')))
    throw new Error(`Dependencies are not installed in ${packageRoot}. Run 'npm install' first.`);

console.log(`Building the TypeScript sources`);
run('npm run build', packageRoot);

mkdirSync(feedDir, { recursive: true });

// ---- platform native packages ----

for (const rid of stagedRids) {
    const platform = platforms[rid];
    const packageDir = path.join(packageRoot, 'build', 'native-packages', platform.id);

    // The runtime registers every font deployed next to the natives; the
    // bundled Lato family (QuestPDF's default typeface) must ship with every
    // platform, like the LatoFont folder in the NuGet package.
    const latoDirectory = path.join(nativesDir, rid, 'LatoFont');
    if (!existsSync(latoDirectory) || !readdirSync(latoDirectory).some(file => file.endsWith('.ttf')))
        throw new Error(`The staged native directory for ${rid} does not contain the bundled LatoFont fonts.`);

    rmSync(packageDir, { recursive: true, force: true });
    mkdirSync(packageDir, { recursive: true });

    const manifest = {
        name: `@questpdf/native-${platform.id}`,
        version,
        description: `QuestPDF native runtime for ${platform.id}. Installed automatically as an optional dependency of the questpdf package.`,
        license: 'SEE LICENSE IN LICENSE.md',
        author: 'Marcin Ziąbek (CodeFlint)',
        homepage: 'https://www.questpdf.com',
        repository: { type: 'git', url: 'git+https://github.com/QuestPDF/QuestPDF.git' },
        bugs: { url: 'https://github.com/QuestPDF/QuestPDF/issues' },
        os: [platform.os],
        cpu: [platform.cpu],
        ...(platform.libc ? { libc: platform.libc } : {}),
        engines: { node: '>=18' },
        // Scoped packages default to restricted access on the npm registry.
        publishConfig: { access: 'public' },
        files: ['native'],
    };

    const readme =
        `# @questpdf/native-${platform.id}\n\n` +
        `The QuestPDF native runtime for ${platform.id}. This package is installed automatically ` +
        `as an optional dependency of [questpdf](https://www.npmjs.com/package/questpdf) — do not depend on it directly.\n\n` +
        `Learn more at [www.questpdf.com](https://www.questpdf.com).\n`;

    writeFileSync(path.join(packageDir, 'package.json'), JSON.stringify(manifest, null, 4) + '\n');
    writeFileSync(path.join(packageDir, 'README.md'), readme);
    cpSync(path.join(repoRoot, 'LICENSE.md'), path.join(packageDir, 'LICENSE.md'));
    cpSync(path.join(nativesDir, rid), path.join(packageDir, 'native'), { recursive: true });

    console.log(`Packing @questpdf/native-${platform.id}@${version}`);
    run(`npm pack --pack-destination "${feedDir}"`, packageDir);
}

// ---- main package ----

const manifestPath = path.join(packageRoot, 'package.json');
const originalManifest = readFileSync(manifestPath, 'utf8');
const manifest = JSON.parse(originalManifest);

manifest.version = version;

for (const name of Object.keys(manifest.optionalDependencies ?? {}))
    manifest.optionalDependencies[name] = version;

// The license text lives once at the repository root; it is copied in only
// for the duration of the pack (npm always includes LICENSE.md and README.md).
const licensePath = path.join(packageRoot, 'LICENSE.md');

try {
    writeFileSync(manifestPath, JSON.stringify(manifest, null, 4) + '\n');
    cpSync(path.join(repoRoot, 'LICENSE.md'), licensePath);

    console.log(`Packing questpdf@${version}`);
    run(`npm pack --pack-destination "${feedDir}"`, packageRoot);
} finally {
    writeFileSync(manifestPath, originalManifest);
    rmSync(licensePath, { force: true });
}

console.log(`\nPacked questpdf@${version} (natives: ${stagedRids.join(', ')}) into ${feedDir}`);
