// Port of src/dotnet/library/QuestPDF.DocumentationExamples/PaddingExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('PaddingExamples.SimpleExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(250)
                    .paddingVertical(10)
                    .paddingLeft(20)
                    .paddingRight(40)
                    .background(Colors.Grey.Lighten2)
                    .text('Sample text');
            });
        })
        .generateImages(() => output('padding-simple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('PaddingExamples.NegativeExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(250)
                    .padding(50)
                    .background(Colors.Grey.Lighten2)
                    .paddingHorizontal(-25)
                    .text('Sample text with negative padding');
            });
        })
        .generateImages(() => output('padding-negative.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
