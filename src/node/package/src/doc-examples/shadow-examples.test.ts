// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ShadowExamples.cs.
import { test } from 'node:test';
import { BoxShadowStyle, Color, Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

/** The TypeScript counterpart of the C# `new BoxShadowStyle { ... }` object initializers. */
function boxShadowStyle(values: {
    color?: Color;
    blur?: number;
    spread?: number;
    offsetX?: number;
    offsetY?: number;
}): BoxShadowStyle {
    const style = new BoxShadowStyle();

    if (values.color !== undefined)
        style.color = values.color;

    if (values.blur !== undefined)
        style.blur = values.blur;

    if (values.spread !== undefined)
        style.spread = values.spread;

    if (values.offsetX !== undefined)
        style.offsetX = values.offsetX;

    if (values.offsetY !== undefined)
        style.offsetY = values.offsetY;

    return style;
}

test('ShadowExamples.Simple', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .border(1, Colors.Black)
                    .shadow(boxShadowStyle({
                        color: Colors.Grey.Medium,
                        blur: 5,
                        spread: 5,
                        offsetX: 5,
                        offsetY: 5,
                    }))
                    .background(Colors.White)
                    .padding(15)
                    .text('Important content');
            });
        })
        .generateImages(() => output('shadow-simple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ShadowExamples.OffsetX', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(50);

                        for (const offsetX of [-10, 0, 10]) {
                            row.constantItem(100)
                                .aspectRatio(1)
                                .shadow(boxShadowStyle({
                                    color: Colors.Grey.Darken1,
                                    blur: 10,
                                    offsetX: offsetX,
                                }))
                                .background(Colors.White);
                        }
                    });
            });
        })
        .generateImages(() => output('shadow-offset-x.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ShadowExamples.OffsetY', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(50);

                        for (const offsetY of [-10, 0, 10]) {
                            row.constantItem(100)
                                .aspectRatio(1)
                                .shadow(boxShadowStyle({
                                    color: Colors.Grey.Darken2,
                                    blur: 10,
                                    offsetY: offsetY,
                                }))
                                .background(Colors.White);
                        }
                    });
            });
        })
        .generateImages(() => output('shadow-offset-y.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ShadowExamples.Color', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(50);

                        const colors = [
                            Colors.Red.Darken2,
                            Colors.Green.Darken2,
                            Colors.Blue.Darken2,
                        ];

                        for (const color of colors) {
                            row.constantItem(100)
                                .aspectRatio(1)
                                .shadow(boxShadowStyle({
                                    color: color,
                                    blur: 10,
                                }))
                                .background(Colors.White);
                        }
                    });
            });
        })
        .generateImages(() => output('shadow-color.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ShadowExamples.Blur', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(50);

                        for (const blur of [5, 10, 20]) {
                            row.constantItem(100)
                                .aspectRatio(1)
                                .shadow(boxShadowStyle({
                                    color: Colors.Grey.Darken1,
                                    blur: blur,
                                }))
                                .background(Colors.White);
                        }
                    });
            });
        })
        .generateImages(() => output('shadow-blur.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ShadowExamples.Spread', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(50);

                        for (const spread of [0, 5, 10]) {
                            row.constantItem(100)
                                .aspectRatio(1)
                                .shadow(boxShadowStyle({
                                    color: Colors.Grey.Darken1,
                                    blur: 5,
                                    spread: spread,
                                }))
                                .background(Colors.White);
                        }
                    });
            });
        })
        .generateImages(() => output('shadow-spread.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ShadowExamples.NoBlur', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(50);

                        row.constantItem(100)
                            .aspectRatio(1)
                            .shadow(boxShadowStyle({
                                color: Colors.Grey.Lighten1,
                                blur: 0,
                                offsetX: 8,
                                offsetY: 8,
                            }))
                            .border(1, Colors.Black)
                            .background(Colors.White);

                        row.constantItem(100)
                            .aspectRatio(1)
                            .shadow(boxShadowStyle({
                                color: Colors.Grey.Lighten1,
                                blur: 0,
                                offsetX: 8,
                                offsetY: 8,
                            }))
                            .border(1, Colors.Black)
                            .cornerRadius(16)
                            .background(Colors.White);
                    });
            });
        })
        .generateImages(() => output('shadow-no-blur.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
