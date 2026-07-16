'use strict';

// The document-generation scenario shared by the CJS, ESM and Express test
// apps (the TypeScript app carries its own typed copy to validate the shipped
// declarations). It mirrors the .NET package tests (src/dotnet/package-tests):
// a Skia-rendered PDF with text and an image, a qpdf document operation
// (merge + attachment), and an XPS document on Windows. The Arabic line
// renders with a font the apps deploy themselves (the fonts/ folder next to
// the entry script).
//
// The questpdf module instance is passed in by the calling app so that it
// resolves from the app's own node_modules.

const fs = require('node:fs');
const path = require('node:path');

const OUTPUT_FOLDER = 'TestOutput';

function runTests(questpdf) {
    const { Document, DocumentOperation, LicenseType, PageSizes, Settings } = questpdf;

    const resources = process.env.QUESTPDF_TEST_RESOURCES;
    if (!resources)
        throw new Error('QUESTPDF_TEST_RESOURCES must point at the shared test resources directory.');

    Settings.license = LicenseType.Community;
    Settings.useEnvironmentFonts = false;
    Settings.checkIfAllTextGlyphsAreAvailable = true;

    const outputFolder = path.resolve(OUTPUT_FOLDER);
    fs.mkdirSync(outputFolder, { recursive: true });

    const skiaPdfOutput = path.join(outputFolder, 'skia.pdf');
    const skiaXpsOutput = path.join(outputFolder, 'skia.xps');
    const pdfToMerge = path.join(outputFolder, 'to-merge.pdf');
    const qpdfOutput = path.join(outputFolder, 'qpdf.pdf');

    createMainDocument(questpdf, resources).generatePdf(skiaPdfOutput);
    createDocumentToMerge(questpdf).generatePdf(pdfToMerge);

    const attachment = new DocumentOperation.DocumentAttachment();
    attachment.filePath = path.join(resources, 'books.xml');

    DocumentOperation
        .loadFile(skiaPdfOutput)
        .mergeFile(pdfToMerge)
        .addAttachment(attachment)
        .save(qpdfOutput);

    if (process.platform === 'win32')
        createMainDocument(questpdf, resources).generateXps(skiaXpsOutput);

    return outputFolder;
}

function createMainDocument(questpdf, resources) {
    const { Document, PageSizes } = questpdf;

    return Document.create((documentContainer) => {
        documentContainer.page((page) => {
            page.margin(50);
            page.size(PageSizes.A5);
            page.defaultTextStyle((style) => style.fontSize(24));

            page.content().column((column) => {
                column.spacing(10);
                column.item().text('Lorem ipsum dolor sit amet');
                column.item().text('مرحبا بالعالم').fontFamily('Noto Sans Arabic');
                column.item().width(50).image(path.join(resources, 'questpdf-logo.png'));
            });
        });
    });
}

function createDocumentToMerge(questpdf) {
    const { Document, PageSizes } = questpdf;

    return Document.create((documentContainer) => {
        documentContainer.page((page) => {
            page.margin(50);
            page.size(PageSizes.A5);
            page.content().text('Document to merge').fontSize(24);
        });
    });
}

module.exports = { runTests, OUTPUT_FOLDER };
