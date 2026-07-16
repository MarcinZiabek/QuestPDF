// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ConstrainedExamples.cs.
import { test } from 'node:test';
import { AspectRatioOption, Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('ConstrainedExamples.WidthExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(300)
                    .padding(25)
                    .column((column) => {
                        column.spacing(25);

                        column.item()
                            .minWidth(200)
                            .background(Colors.Grey.Lighten3)
                            .text('Lorem ipsum');

                        column.item()
                            .maxWidth(100)
                            .background(Colors.Grey.Lighten3)
                            .text('dolor sit amet');
                    });
            });
        })
        .generateImages(() => output('width.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ConstrainedExamples.HeightExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(300)
                    .padding(25)
                    .height(100)
                    .aspectRatio(2, AspectRatioOption.FitHeight)
                    .background(Colors.Grey.Lighten1);
            });
        })
        .generateImages(() => output('height.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
