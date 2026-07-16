// Shared plumbing of the ported documentation examples (the TypeScript
// counterpart of src/dotnet/library/QuestPDF.DocumentationExamples). Output
// files use exactly the same names as the .NET examples so the port-parity
// tests can compare the produced images byte-for-byte across runtimes.
import * as fs from 'node:fs';
import * as path from 'node:path';
import { Color, ImageCompressionQuality, ImageFormat, ImageGenerationSettings, LicenseType, Settings } from '../index';

// Runs at import time, before any test body: generation checks the license
// before the image-path callback (and thus output()) is ever invoked.
Settings.license = LicenseType.Community;

let outputDirectory: string | null = null;

function ensureConfigured(): string {
    if (outputDirectory !== null)
        return outputDirectory;

    const directory = process.env.QUESTPDF_DOC_EXAMPLES_OUTPUT;
    if (!directory)
        throw new Error('QUESTPDF_DOC_EXAMPLES_OUTPUT must point at the output directory.');

    fs.mkdirSync(directory, { recursive: true });
    outputDirectory = directory;
    return outputDirectory;
}

/** Absolute path for an output file; the name must match the .NET example. */
export function output(name: string): string {
    return path.join(ensureConfigured(), name);
}

/** Absolute path of a file inside the shared Resources directory (images, fonts, ...). */
export function resource(name: string): string {
    const directory = process.env.QUESTPDF_DOC_EXAMPLES_RESOURCES;
    if (!directory)
        throw new Error('QUESTPDF_DOC_EXAMPLES_RESOURCES must point at the shared Resources directory.');

    return path.join(directory, name);
}

/** The TypeScript counterpart of C# `new ImageGenerationSettings { ... }` object initializers. */
export function imageSettings(values: {
    imageFormat?: ImageFormat;
    imageCompressionQuality?: ImageCompressionQuality;
    rasterDpi?: number;
}): ImageGenerationSettings {
    const settings = new ImageGenerationSettings();

    if (values.imageFormat !== undefined)
        settings.imageFormat = values.imageFormat;

    if (values.imageCompressionQuality !== undefined)
        settings.imageCompressionQuality = values.imageCompressionQuality;

    if (values.rasterDpi !== undefined)
        settings.rasterDpi = values.rasterDpi;

    return settings;
}

/**
 * The string representation .NET produces for a Color (Color.ToString()):
 * #RRGGBB, or #AARRGGBB when the color is translucent. Used by examples that
 * pass a color where the .NET original relies on implicit ToString conversion.
 */
export function colorToString(color: Color): string {
    const hex = (value: number) => value.toString(16).toUpperCase().padStart(2, '0');
    const rgb = `${hex(color.red)}${hex(color.green)}${hex(color.blue)}`;
    return color.alpha === 0xFF ? `#${rgb}` : `#${hex(color.alpha)}${rgb}`;
}
