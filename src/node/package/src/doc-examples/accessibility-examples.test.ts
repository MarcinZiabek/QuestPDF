// Port of src/dotnet/library/QuestPDF.DocumentationExamples/AccessibilityExamples.cs.
import { test } from 'node:test';
import { Colors, Document, DocumentMetadata, DocumentSettings, PDFA_Conformance, PDFUA_Conformance, PageSizes, Placeholders } from '../index';
import { output, resource } from './doc-example';

test('AccessibilityExamples.MinimalExample', () => {
    const metadata = new DocumentMetadata();
    metadata.language = 'en-US';
    metadata.title = 'Accessibility Test';
    metadata.subject = 'This document shows how easy it is to create accessible PDF documents with QuestPDF';

    const settings = new DocumentSettings();
    settings.pdfaConformance = PDFA_Conformance.PDFA_3A;
    settings.pdfuaConformance = PDFUA_Conformance.PDFUA_1;

    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.margin(30);

                page.header()
                    .paddingBottom(15)
                    .semanticHeader1()
                    .text('Accessibility Test Document')
                    .fontColor(Colors.Blue.Darken3)
                    .fontSize(24)
                    .bold();

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        column.item()
                            .semanticSection()
                            .column((column) => {
                                column.item()
                                    .paddingBottom(10)
                                    .semanticHeader2()
                                    .text('Section with text content')
                                    .fontColor(Colors.Blue.Darken1)
                                    .fontSize(16);

                                column.item()
                                    .text(Placeholders.paragraphs())
                                    .fontSize(12)
                                    .paragraphSpacing(8);
                            });

                        column.item()
                            .preventPageBreak()
                            .semanticSection()
                            .column((column) => {
                                column.item()
                                    .paddingBottom(10)
                                    .semanticHeader2()
                                    .text('Section with image')
                                    .fontColor(Colors.Blue.Darken1)
                                    .fontSize(16);

                                column.item()
                                    .width(250)
                                    .semanticImage('Image showing a laptop')
                                    .image(resource('product.jpg'));
                            });
                    });
            });
        })
        .withMetadata(metadata)
        .withSettings(settings)
        .generatePdf(output('accessibility-minimal-example.pdf'));
});
