// Port of src/dotnet/library/QuestPDF.DocumentationExamples/MergingDocumentsExamples.cs.
import { test } from 'node:test';
import { Colors, Document, PageSizes, TextStyle, Unit } from '../index';
import { output } from './doc-example';

test('MergingDocumentsExamples.UseOriginalPageNumbersExample', () => {
    Document
        .merge(
            generateReport('Short Document 1', 5),
            generateReport('Medium Document 2', 10),
            generateReport('Long Document 3', 15))
        .useOriginalPageNumbers()
        .generatePdf(output('merged.pdf'));
});

test('MergingDocumentsExamples.UseContinuousPageNumbersExample', () => {
    Document
        .merge(
            generateReport('Short Document 1', 5),
            generateReport('Medium Document 2', 10),
            generateReport('Long Document 3', 15))
        .useContinuousPageNumbers()
        .generatePdf(output('merged.pdf'));
});

function generateReport(title: string, itemsCount: number): Document {
    return Document.create((document) => {
        document.page((page) => {
            page.size(PageSizes.A5);
            page.margin(0.5, Unit.Inch);

            page.header()
                .text(title)
                .bold()
                .fontSize(24)
                .fontColor(Colors.Blue.Accent2);

            page.content()
                .paddingVertical(20)
                .column((column) => {
                    column.spacing(10);

                    for (let i = 0; i < itemsCount; i++) {
                        column
                            .item()
                            .width(200)
                            .height(50)
                            .background(Colors.Grey.Lighten3)
                            .alignMiddle()
                            .alignCenter()
                            .text(`Item ${i}`)
                            .fontSize(16);
                    }
                });

            page.footer()
                .alignCenter()
                .paddingVertical(20)
                .text((text) => {
                    text.defaultTextStyle(TextStyle.Default.fontSize(16));

                    text.currentPageNumber();
                    text.span(' / ');
                    text.totalPages();
                });
        });
    });
}
