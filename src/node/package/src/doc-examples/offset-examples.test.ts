// Port of src/dotnet/library/QuestPDF.DocumentationExamples/OffsetExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('OffsetExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(400, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .padding(50)
                    .background(Colors.Blue.Lighten3)
                    .offsetX(25)
                    .offsetY(25)
                    .border(4)
                    .borderColor(Colors.Blue.Darken2)
                    .padding(50)
                    .text('Moved content')
                    .fontSize(25);
            });
        })
        .generateImages(() => output('offset.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
