// Port of src/dotnet/library/QuestPDF.DocumentationExamples/DocumentOperationExamples.cs.
import * as fs from 'node:fs';
import { test } from 'node:test';
import { Color, Colors, Document, DocumentOperation, Unit } from '../index';
import { output } from './doc-example';

test('DocumentOperationExamples.MergeFiles', () => {
    const prefix = 'document-operation-merge';

    generateSampleDocument(output(`${prefix}-source-red.pdf`), Colors.Red.Lighten3, 2);
    generateSampleDocument(output(`${prefix}-source-green.pdf`), Colors.Green.Lighten3, 3);
    generateSampleDocument(output(`${prefix}-source-blue.pdf`), Colors.Blue.Lighten3, 5);

    DocumentOperation
        .loadFile(output(`${prefix}-source-red.pdf`))
        .mergeFile(output(`${prefix}-source-green.pdf`))
        .mergeFile(output(`${prefix}-source-blue.pdf`))
        .save(output(`${prefix}-result.pdf`));
});

test('DocumentOperationExamples.SelectEvenPages', () => {
    const prefix = 'document-operation-select-even-pages';

    generateSampleDocument(output(`${prefix}-source.pdf`), Colors.Indigo.Lighten3, 11);

    DocumentOperation
        .loadFile(output(`${prefix}-source.pdf`))
        .takePages('1-z:even')
        .save(output(`${prefix}-result.pdf`));
});

test('DocumentOperationExamples.Encrypt', () => {
    const prefix = 'document-operation-encrypt';

    generateSampleDocument(output(`${prefix}-source.pdf`), Colors.Orange.Lighten3, 7);

    const encryption = new DocumentOperation.Encryption256Bit();
    encryption.userPassword = 'user-password';
    encryption.ownerPassword = 'owner-password';
    encryption.allowContentExtraction = false;
    encryption.allowPrinting = false;

    DocumentOperation
        .loadFile(output(`${prefix}-source.pdf`))
        .encrypt(encryption)
        .save(output(`${prefix}-result.pdf`));
});

test('DocumentOperationExamples.AddAttachment', () => {
    const prefix = 'document-operation-add-attachment';

    generateSampleDocument(output(`${prefix}-source.pdf`), Colors.Cyan.Lighten3, 7);
    fs.writeFileSync(output(`${prefix}-content.txt`), 'Hello, World!');

    const attachment = new DocumentOperation.DocumentAttachment();
    attachment.filePath = output(`${prefix}-content.txt`);
    attachment.attachmentName = 'Attached message';

    DocumentOperation
        .loadFile(output(`${prefix}-source.pdf`))
        .addAttachment(attachment)
        .save(output(`${prefix}-result.pdf`));
});

test('DocumentOperationExamples.Overlay', () => {
    const prefix = 'document-operation-overlay';

    generateSampleDocument(output(`${prefix}-source.pdf`), Colors.Cyan.Lighten3, 7);

    Document
        .create((document) => {
            document.page((page) => {
                page.margin(1, Unit.Centimetre);
                page.pageColor(Colors.Transparent);

                page.content().column((column) => {
                    for (let i = 0; i < 6; i++)
                        column.item().pageBreak();
                });

                page.footer().alignCenter().text((text) => {
                    text.defaultTextStyle((style) => style.fontSize(24).bold().fontColor(Colors.White));
                    text.span('Page ');
                    text.currentPageNumber();
                    text.span(' of ');
                    text.totalPages();
                });
            });
        })
        .generatePdf(output(`${prefix}-content.pdf`));

    const configuration = new DocumentOperation.LayerConfiguration();
    configuration.filePath = output(`${prefix}-content.pdf`);

    DocumentOperation
        .loadFile(output(`${prefix}-source.pdf`))
        .overlayFile(configuration)
        .save(output(`${prefix}-result.pdf`));
});

function generateSampleDocument(fileName: string, pageColor: Color, numberOfPages: number): void {
    Document
        .create((container) => {
            container.page((page) => {
                page.margin(1, Unit.Centimetre);
                page.pageColor(pageColor);

                page.content().column((column) => {
                    for (let pageNumber = 1; pageNumber <= numberOfPages; pageNumber++) {
                        column.item()
                            .extend()
                            .alignCenter().alignMiddle()
                            .text(`${pageNumber}`)
                            .fontSize(256)
                            .fontColor(Colors.White)
                            .bold();

                        if (pageNumber !== numberOfPages)
                            column.item().pageBreak();
                    }
                });
            });
        })
        .generatePdf(fileName);
}
