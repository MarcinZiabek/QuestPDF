// Port of src/dotnet/library/QuestPDF.DocumentationExamples/PlaceholderExamples.cs.
import { test } from 'node:test';
import { Document, ImageCompressionQuality, ImageFormat, PageSize, PageSizes, Placeholders } from '../index';
import { imageSettings, output } from './doc-example';

test('PlaceholderExamples.TextExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(15);

                        addItem('Name', Placeholders.name());
                        addItem('Email', Placeholders.email());
                        addItem('Phone', Placeholders.phoneNumber());
                        addItem('Date', Placeholders.shortDate());
                        addItem('Time', Placeholders.time());

                        function addItem(label: string, value: string): void {
                            column.item().text((text) => {
                                text.span(`${label}: `).bold();
                                text.span(value);
                            });
                        }
                    });
            });
        })
        .generateImages(() => output('placeholders-text.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('PlaceholderExamples.BackgroundColorExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(320, 0));
                page.maxSize(new PageSize(320, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .grid((grid) => {
                        grid.columns(5);
                        grid.spacing(5);

                        for (let i = 0; i < 25; i++) {
                            grid.item()
                                .height(50)
                                .width(50)
                                .background(Placeholders.backgroundColor());
                        }
                    });
            });
        })
        .generateImages(() => output('placeholders-color-background.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('PlaceholderExamples.ColorExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        for (let i = 0; i < 5; i++) {
                            column.item()
                                .text(Placeholders.sentence())
                                .fontColor(Placeholders.color());
                        }
                    });
            });
        })
        .generateImages(() => output('placeholders-color.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('PlaceholderExamples.ImageExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .width(200)
                    .column((column) => {
                        column.spacing(10);

                        // provide an exact image resolution
                        column.item()
                            .image(Placeholders.image(100, 50));

                        // specify physical width and height of the image
                        // (The C# original passes Placeholders.Image through the Image(Func<ImageSize, byte[]>)
                        // overload, which is not bridged; the payload delegate is the direct equivalent.)
                        column.item()
                            .width(200)
                            .height(150)
                            .image((payload) => Placeholders.image(payload.imageSize));

                        // specify target physical width and aspect ratio
                        column.item()
                            .width(200)
                            .aspectRatio(3 / 2)
                            .image((payload) => Placeholders.image(payload.imageSize));
                    });
            });
        })
        .generateImages(() => output('placeholders-image.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('PlaceholderExamples.ElementExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.header()
                    .height(100)
                    .placeholder('Header');

                page.content()
                    .paddingVertical(25)
                    .placeholder();

                page.footer()
                    .height(100)
                    .placeholder('Footer');
            });
        })
        .generateImages(() => output('placeholder-element.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});
