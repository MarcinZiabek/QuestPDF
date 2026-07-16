// Port of src/dotnet/library/QuestPDF.DocumentationExamples/PreventPageBreakExamples.cs.
import { test } from 'node:test';
import { Colors, Document, PageSizes } from '../index';
import { output } from './doc-example';

test('PreventPageBreakExamples.EnabledExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(30);

                page.content()
                    .column((column) => {
                        column.item().height(400).background(Colors.Grey.Lighten3);
                        column.item().height(30);

                        column.item()
                            .preventPageBreak()
                            .text((text) => {
                                text.paragraphSpacing(15);

                                text.span('Optimizing Content Placement').bold().fontColor(Colors.Blue.Darken2).fontSize(24);
                                text.span('\n');
                                text.span('By carefully determining where to place a page break, you can avoid awkward text separations and maintain readability. Thoughtful formatting improves the overall user experience, making complex topics easier to digest.');
                            });
                    });
            });
        })
        .generatePdf(output('prevent-page-break-enabled.pdf'));
});

test('PreventPageBreakExamples.DisabledExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(30);

                page.content()
                    .column((column) => {
                        column.item().height(400).background(Colors.Grey.Lighten3);
                        column.item().height(30);

                        column.item()
                            .text((text) => {
                                text.paragraphSpacing(15);

                                text.span('Optimizing Content Placement').bold().fontColor(Colors.Blue.Darken2).fontSize(24);
                                text.span('\n');
                                text.span('By carefully determining where to place a page break, you can avoid awkward text separations and maintain readability. Thoughtful formatting improves the overall user experience, making complex topics easier to digest.');
                            });
                    });
            });
        })
        .generatePdf(output('prevent-page-break-disabled.pdf'));
});
