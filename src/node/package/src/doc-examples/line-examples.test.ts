// Port of src/dotnet/library/QuestPDF.DocumentationExamples/LineExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('LineExamples.VerticalLineExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.autoItem().text('Text on the left');

                        row.autoItem()
                            .paddingHorizontal(15)
                            .lineVertical(3)
                            .lineColor(Colors.Blue.Medium);

                        row.autoItem().text('Text on the right');
                    });
            });
        })
        .generateImages(() => output('line-vertical.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('LineExamples.HorizontalLineExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .column((column) => {
                        column.item().text('Text above the line');

                        column.item()
                            .paddingVertical(10)
                            .lineHorizontal(2)
                            .lineColor(Colors.Blue.Medium);

                        column.item().text('Text below the line');
                    });
            });
        })
        .generateImages(() => output('line-horizontal.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('LineExamples.Thickness', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        for (const thickness of [1, 2, 4, 8]) {
                            column.item()
                                .width(200)
                                .lineHorizontal(thickness);
                        }
                    });
            });
        })
        .generateImages(() => output('line-thickness.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('LineExamples.SolidColor', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .column((column) => {
                        const colors = [
                            Colors.Red.Medium,
                            Colors.Green.Medium,
                            Colors.Blue.Medium,
                        ];

                        column.spacing(20);

                        for (const color of colors) {
                            column.item()
                                .width(200)
                                .lineHorizontal(5)
                                .lineColor(color);
                        }
                    });
            });
        })
        .generateImages(() => output('line-color-solid.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('LineExamples.Gradient', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        column.item()
                            .width(200)
                            .lineHorizontal(5)
                            .lineGradient([Colors.Red.Medium, Colors.Orange.Medium]);

                        column.item()
                            .width(200)
                            .lineHorizontal(5)
                            .lineGradient([Colors.Orange.Medium, Colors.Yellow.Medium, Colors.Lime.Medium]);

                        column.item()
                            .width(200)
                            .lineHorizontal(5)
                            .lineGradient([Colors.Blue.Lighten2, Colors.LightBlue.Lighten1, Colors.Cyan.Medium, Colors.Teal.Darken1, Colors.Green.Darken2]);
                    });
            });
        })
        .generateImages(() => output('line-color-gradient.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('LineExamples.DashPattern', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        column.item()
                            .width(200)
                            .lineHorizontal(5)
                            .lineDashPattern([4, 4]);

                        column.item()
                            .width(200)
                            .lineHorizontal(5)
                            .lineDashPattern([12, 12]);

                        column.item()
                            .width(200)
                            .lineHorizontal(5)
                            .lineDashPattern([4, 4, 12, 4]);
                    });
            });
        })
        .generateImages(() => output('line-dash-pattern.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('LineExamples.Complex', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .width(300)
                    .lineHorizontal(8)
                    .lineDashPattern([4, 4, 8, 8, 12, 12])
                    .lineGradient([Colors.Red.Medium, Colors.Orange.Medium, Colors.Yellow.Medium]);
            });
        })
        .generateImages(() => output('line-example.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
