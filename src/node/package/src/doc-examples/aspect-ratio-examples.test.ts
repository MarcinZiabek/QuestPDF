// Port of src/dotnet/library/QuestPDF.DocumentationExamples/AspectRatioExamples.cs.
import { test } from 'node:test';
import { AspectRatioOption, Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('AspectRatioExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(300)
                    .height(300)
                    .aspectRatio(3 / 4, AspectRatioOption.FitArea)
                    .background(Colors.Grey.Lighten2)
                    .alignCenter()
                    .alignMiddle()
                    .text('3:4 Content Area');
            });
        })
        .generateImages(() => output('aspect-ratio.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
