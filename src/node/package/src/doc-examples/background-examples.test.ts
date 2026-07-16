// Port of src/dotnet/library/QuestPDF.DocumentationExamples/BackgroundExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('BackgroundExamples.SolidColor', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.pageColor(Colors.White);
                page.margin(25);

                const colors = [
                    Colors.LightBlue.Darken4,
                    Colors.LightBlue.Darken3,
                    Colors.LightBlue.Darken2,
                    Colors.LightBlue.Darken1,

                    Colors.LightBlue.Medium,

                    Colors.LightBlue.Lighten1,
                    Colors.LightBlue.Lighten2,
                    Colors.LightBlue.Lighten3,
                    Colors.LightBlue.Lighten4,
                    Colors.LightBlue.Lighten5,

                    Colors.LightBlue.Accent1,
                    Colors.LightBlue.Accent2,
                    Colors.LightBlue.Accent3,
                    Colors.LightBlue.Accent4,
                ];

                page.content()
                    .height(150)
                    .width(420)
                    .row((row) => {
                        for (const color of colors)
                            row.relativeItem().background(color);
                    });
            });
        })
        .generateImages(() => output('background-solid.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BackgroundExamples.Gradient', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(350, 0));
                page.maxSize(new PageSize(350, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.pageColor(Colors.White);
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(25);

                        column.item()
                            .backgroundLinearGradient(0, [Colors.Red.Lighten2, Colors.Blue.Lighten2])
                            .aspectRatio(2);

                        column.item()
                            .backgroundLinearGradient(45, [Colors.Green.Lighten2, Colors.LightGreen.Lighten2, Colors.Yellow.Lighten2])
                            .aspectRatio(2);

                        column.item()
                            .backgroundLinearGradient(90, [Colors.Yellow.Lighten2, Colors.Amber.Lighten2, Colors.Orange.Lighten2])
                            .aspectRatio(2);
                    });
            });
        })
        .generateImages(() => output('background-gradient.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BackgroundExamples.RoundedCorners', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.pageColor(Colors.White);
                page.margin(25);

                page.content()
                    .shrink()
                    .background(Colors.Grey.Lighten2)
                    .cornerRadius(25)
                    .padding(25)
                    .text('Content with rounded corners');
            });
        })
        .generateImages(() => output('background-rounded-corners.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
