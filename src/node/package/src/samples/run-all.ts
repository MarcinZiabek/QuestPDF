import { mkdirSync, writeFileSync } from 'node:fs';
import * as path from 'node:path';
import { runInvoiceSample } from './invoice';
import { runLayoutShowcaseSample } from './layout-showcase';

/**
 * Runs every sample composition against the real QuestPDF engine (through the
 * koffi → NativeAOT bridge) and writes the resulting PDFs to the output
 * directory. Success means every document generated and starts with a valid
 * PDF header.
 */

const outputDirectory = process.env.QUESTPDF_SAMPLES_OUTPUT ?? path.resolve(__dirname, '..', '..', 'samples-output');
mkdirSync(outputDirectory, { recursive: true });

const samples: Record<string, () => Uint8Array> = {
    invoice: runInvoiceSample,
    'layout-showcase': runLayoutShowcaseSample,
};

for (const [name, sample] of Object.entries(samples)) {
    const bytes = sample();

    if (bytes.length <= 1000)
        throw new Error(`${name} produced only ${bytes.length} bytes — not a real document`);
    if (Buffer.from(bytes.subarray(0, 5)).toString('ascii') !== '%PDF-')
        throw new Error(`${name} did not produce a PDF header`);

    const target = path.join(outputDirectory, `${name}.pdf`);
    writeFileSync(target, bytes);
    console.log(`${name}.pdf written (${bytes.length} bytes) -> ${target}`);
}

console.log(`All ${Object.keys(samples).length} samples generated real PDFs.`);
