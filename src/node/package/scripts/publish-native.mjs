// Publishes the QuestPDF.Native shared library (dotnet publish, NativeAOT)
// into src/node/package/native/, where the runtime loads it from by default.
// With --staging, publishes into native-staging/<rid>/ instead, the layout
// scripts/pack-feed.mjs packs platform packages from.
import { execSync } from 'node:child_process';
import { existsSync, readdirSync, rmSync } from 'node:fs';
import * as path from 'node:path';
import { fileURLToPath } from 'node:url';

const packageRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const nativeProject = path.resolve(packageRoot, '..', '..', 'dotnet', 'interop');

const arch = process.arch === 'arm64' ? 'arm64' : 'x64';
const muslSuffix = process.platform === 'linux' && existsSync('/etc/alpine-release') ? 'musl-' : '';
const rid = process.platform === 'darwin' ? `osx-${arch}`
    : process.platform === 'win32' ? `win-${arch}`
    : `linux-${muslSuffix}${arch}`;

const outputDirectory = process.argv.includes('--staging')
    ? path.join(packageRoot, 'native-staging', rid)
    : path.join(packageRoot, 'native');

execSync(`dotnet publish "${nativeProject}" -c Release -r ${rid} -o "${outputDirectory}"`, { stdio: 'inherit' });

// StripSymbols=true drops a .dSYM bundle (~32 MB of DWARF) next to the dylib;
// it is debug-only and must not ship with the runtime directory.
for (const entry of readdirSync(outputDirectory)) {
    if (entry.endsWith('.dSYM'))
        rmSync(path.join(outputDirectory, entry), { recursive: true, force: true });
}

console.log(`Published QuestPDF.Native (${rid}) to ${outputDirectory}`);
