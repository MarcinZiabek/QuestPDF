// Port of src/dotnet/library/QuestPDF.DocumentationExamples/DefaultTextStyleExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('DefaultTextStyleExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(400)
                    .padding(25)
                    .defaultTextStyle((style) => style.bold().underline())
                    .column((column) => {
                        column.spacing(10);

                        column.item().text('Inherited bold and underline');
                        column.item().text('Disabled underline, inherited bold and adjusted font color').underline(false).fontColor(Colors.Green.Darken2);

                        column.item()
                            .defaultTextStyle((style) => style.decorationWavy().fontColor(Colors.LightBlue.Darken3))
                            .text('Changed underline type and adjusted font color');
                    });
            });
        })
        .generateImages(() => output('default-text-style.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
