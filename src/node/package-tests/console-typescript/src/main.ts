// Typed copy of the shared test scenario: compiling against the package's
// shipped .d.ts declarations is part of what this app verifies.
import * as fs from 'node:fs';
import * as path from 'node:path';
import { Document, DocumentOperation, LicenseType, PageSizes, Settings } from 'questpdf';

const OUTPUT_FOLDER = 'TestOutput';

function createMainDocument(resources: string): Document {
    return Document.create((documentContainer) => {
        documentContainer.page((page) => {
            page.margin(50);
            page.size(PageSizes.A5);
            page.defaultTextStyle((style) => style.fontSize(24));

            page.content().column((column) => {
                column.spacing(10);
                column.item().text('Lorem ipsum dolor sit amet');
                column.item().width(50).image(path.join(resources, 'questpdf-logo.png'));
            });
        });
    });
}

function createDocumentToMerge(): Document {
    return Document.create((documentContainer) => {
        documentContainer.page((page) => {
            page.margin(50);
            page.size(PageSizes.A5);
            page.content().text('Document to merge').fontSize(24);
        });
    });
}

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

createMainDocument(resources).generatePdf(skiaPdfOutput);
createDocumentToMerge().generatePdf(pdfToMerge);

const attachment = new DocumentOperation.DocumentAttachment();
attachment.filePath = path.join(resources, 'books.xml');

DocumentOperation
    .loadFile(skiaPdfOutput)
    .mergeFile(pdfToMerge)
    .addAttachment(attachment)
    .save(qpdfOutput);

if (process.platform === 'win32')
    createMainDocument(resources).generateXps(skiaXpsOutput);

console.log(`Documents generated into ${outputFolder}`);
