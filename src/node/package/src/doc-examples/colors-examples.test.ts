// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ColorsExamples.cs.
import { test } from 'node:test';
import { Color, Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('ColorsExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(175)
                    .padding(20)
                    .border(1)
                    .borderColor(Color.from('#03A9F4'))
                    .background(Colors.LightBlue.Lighten5)
                    .padding(20)
                    .text('Blue text')
                    .bold()
                    .fontColor(Colors.LightBlue.Darken4)
                    .underline()
                    .decorationWavy()
                    .decorationColor(Color.from(0xFF0000));
            });
        })
        .generateImages(() => output('colors.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
