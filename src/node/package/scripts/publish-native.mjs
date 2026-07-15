// Publishes the QuestPDF.Native shared library (dotnet publish, NativeAOT)
// into src/node/package/native/, where the runtime loads it from by default.
import { execSync } from 'node:child_process';
import { readdirSync, rmSync } from 'node:fs';
import * as path from 'node:path';
import { fileURLToPath } from 'node:url';

const packageRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const nativeProject = path.resolve(packageRoot, '..', '..', 'dotnet', 'interop');
const outputDirectory = path.join(packageRoot, 'native');

const arch = process.arch === 'arm64' ? 'arm64' : 'x64';
const rid = process.platform === 'darwin' ? `osx-${arch}`
    : process.platform === 'win32' ? `win-${arch}`
    : `linux-${arch}`;

execSync(`dotnet publish "${nativeProject}" -c Release -r ${rid} -o "${outputDirectory}"`, { stdio: 'inherit' });

// StripSymbols=true drops a .dSYM bundle (~32 MB of DWARF) next to the dylib;
// it is debug-only and must not ship with the runtime directory.
for (const entry of readdirSync(outputDirectory)) {
    if (entry.endsWith('.dSYM'))
        rmSync(path.join(outputDirectory, entry), { recursive: true, force: true });
}

console.log(`Published QuestPDF.Native (${rid}) to ${outputDirectory}`);
