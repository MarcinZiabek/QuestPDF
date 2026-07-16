// Port of src/dotnet/library/QuestPDF.DocumentationExamples/BorderExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { colorToString, imageSettings, output } from './doc-example';

test('BorderExamples.SimpleExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(450);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .border(3, Colors.Blue.Darken4)
                    .background(Colors.Blue.Lighten5)
                    .padding(25)
                    .text((text) => {
                        text.defaultTextStyle((style) => style.fontColor(Colors.Blue.Darken4).fontSize(16));
                        text.span('TIP: ').bold();
                        text.span('You can use borders to create visual separation between elements in your document. Borders can be applied to any element, including text, images, and containers.');
                    });
            });
        })
        .generateImages(() => output('border-simple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.Multiple', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .shrink()

                    .borderVertical(5)
                    .borderColor(Colors.Green.Darken2)
                    .borderAlignmentInside()

                    .container()

                    .borderHorizontal(10)
                    .borderColor(Colors.Blue.Lighten1)
                    .borderAlignmentInside()

                    .background(Colors.Grey.Lighten2)
                    .paddingVertical(25)
                    .paddingHorizontal(50)
                    .text('Content');
            });
        })
        .generateImages(() => output('border-multiple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.ConsistentThickness', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(550, 0));
                page.maxSize(new PageSize(550, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(25);

                        row.relativeItem()
                            .border(1, Colors.Black)
                            .padding(10)
                            .alignCenter()
                            .text('Thin');

                        row.relativeItem()
                            .border(3, Colors.Black)
                            .padding(10)
                            .alignCenter()
                            .text('Medium');

                        row.relativeItem()
                            .border(9, Colors.Black)
                            .padding(10)
                            .alignCenter()
                            .text('Bold');
                    });
            });
        })
        .generateImages(() => output('border-thickness-consistent.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.VariousThickness', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .borderLeft(4)
                    .borderTop(6)
                    .borderRight(8)
                    .borderBottom(10)
                    .padding(25)
                    .text('Sample text');
            });
        })
        .generateImages(() => output('border-thickness-various.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.Alignment', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(725, 0));
                page.maxSize(new PageSize(725, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(25);

                        row.relativeItem()
                            .background(Colors.Grey.Lighten1)
                            .padding(25)
                            .text('No Border');

                        row.relativeItem()
                            .border(10, Colors.Grey.Darken2)
                            .borderAlignmentInside()
                            .padding(25)
                            .text('Border Inside');

                        row.relativeItem()
                            .border(10, Colors.Grey.Darken2)
                            .borderAlignmentMiddle()
                            .padding(25)
                            .text('Border Middle');

                        row.relativeItem()
                            .border(10, Colors.Grey.Darken2)
                            .borderAlignmentOutside()
                            .padding(25)
                            .text('Border Outside');
                    });
            });
        })
        .generateImages(() => output('border-alignment.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.RoundedCorners1', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .cornerRadius(10)
                    .border(1, Colors.Black)
                    .background(Colors.Grey.Lighten2)
                    .padding(25)
                    .text('Border with rounded corners');
            });
        })
        .generateImages(() => output('border-rounded-corners-1.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.RoundedCorners2', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .cornerRadius(10)
                    .borderLeft(10)
                    .borderAlignmentInside()
                    .borderColor(Colors.Green.Darken2)
                    .background(Colors.Green.Lighten4)
                    .padding(25)
                    .paddingLeft(10)
                    .defaultTextStyle((style) => style.fontColor(Colors.Green.Darken4))
                    .column((column) => {
                        column.item().text('Completed').bold();
                        column.item().height(5);
                        column.item().text('The invoice has been paid in full.').fontSize(16);
                    });
            });
        })
        .generateImages(() => output('border-rounded-corners-2.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.SolidColor', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(450);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        const colors = [
                            Colors.Red.Medium,
                            Colors.Green.Medium,
                            Colors.Blue.Medium,
                        ];

                        row.spacing(25);

                        for (const color of colors) {
                            row.relativeItem()
                                .border(5)
                                .borderColor(color)
                                .padding(15)
                                .text(colorToString(color))
                                .fontColor(color);
                        }
                    });
            });
        })
        .generateImages(() => output('border-color-solid.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('BorderExamples.Gradient', () => {
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
                        column.spacing(25);

                        column.item()
                            .border(5)
                            .borderLinearGradient(0, [Colors.Red.Darken1, Colors.Blue.Darken1])
                            .borderAlignmentInside()
                            .padding(25)
                            .text('Horizontal gradient');

                        column.item()
                            .border(10)
                            .borderLinearGradient(45, [Colors.Green.Darken1, Colors.LightGreen.Darken1, Colors.Yellow.Darken1])
                            .borderAlignmentInside()
                            .padding(25)
                            .text('Diagonal gradient');

                        column.item()
                            .border(10)
                            .borderLinearGradient(90, [Colors.Yellow.Darken1, Colors.Amber.Darken1, Colors.Orange.Darken1])
                            .cornerRadius(20)
                            .padding(25)
                            .text('Vertical gradient');
                    });
            });
        })
        .generateImages(() => output('border-color-gradient.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
