// Port of src/dotnet/library/QuestPDF.DocumentationExamples/DecorationExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('DecorationExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(350, 0));
                page.maxSize(new PageSize(350, 300));
                page.margin(25);
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .background(Colors.Grey.Lighten3)
                    .padding(15)
                    .decoration((decoration) => {
                        decoration
                            .before()
                            .defaultTextStyle((style) => style.bold())
                            .column((column) => {
                                column.item().showOnce().text('Customer Instructions:');
                                column.item().skipOnce().text('Customer Instructions [continued]:');
                            });

                        decoration
                            .content()
                            .paddingTop(10)
                            .text('Please wrap the item in elegant gift paper and include a small blank card for a personal message. If possible, remove any price tags or invoices from the package. Make sure the wrapping is secure but easy to open without damaging the contents.');
                    });
            });
        })
        .generateImages((index) => output(`decoration-${index}.webp`), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
