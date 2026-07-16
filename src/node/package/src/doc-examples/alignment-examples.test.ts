// Port of src/dotnet/library/QuestPDF.DocumentationExamples/AlignmentExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('AlignmentExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(300)
                    .height(300)
                    .alignBottom()
                    .alignCenter()
                    .background(Colors.Grey.Lighten2)
                    .padding(10)
                    .text('Lorem ipsum');
            });
        })
        .generateImages(() => output('alignment.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
