// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ImageExamples.cs.
import * as fs from 'node:fs';
import { test } from 'node:test';
import { Colors, Document, DocumentSettings, Image, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../index';
import { imageSettings, output, resource } from './doc-example';

// NOT PORTED: image-dynamic.webp — depends on .NET SkiaSharp (SKBitmap/SKCanvas synthesize the dynamic image).

test('ImageExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(400, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .grid((grid) => {
                        grid.columns(2);
                        grid.spacing(10);

                        grid.item(2).text('My photo gallery:').bold();

                        grid.item().image(resource('Photos/photo-gallery-1.jpg'));
                        grid.item().image(resource('Photos/photo-gallery-2.jpg'));
                        grid.item().image(resource('Photos/photo-gallery-3.jpg'));
                        grid.item().image(resource('Photos/photo-gallery-4.jpg'));
                    });
            });
        })
        .generateImages(() => output('image-example.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ImageExamples.ImageScaling', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1500));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.item().paddingBottom(5).text('FitWidth').bold();
                        column.item()
                            .width(200)
                            .height(150)
                            .border(4)
                            .borderColor(Colors.Red.Medium)
                            .image(resource('Photos/photo.jpg'))
                            .fitWidth();

                        column.item().height(15);

                        column.item().paddingBottom(5).text('FitHeight').bold();
                        column.item()
                            .width(200)
                            .height(100)
                            .border(4)
                            .borderColor(Colors.Red.Medium)
                            .image(resource('Photos/photo.jpg'))
                            .fitHeight();

                        column.item().height(15);

                        column.item().paddingBottom(5).text('FitArea 1').bold();
                        column.item()
                            .width(200)
                            .height(100)
                            .border(4)
                            .borderColor(Colors.Red.Medium)
                            .image(resource('Photos/photo.jpg'))
                            .fitArea();

                        column.item().height(15);

                        column.item().paddingBottom(5).text('FitArea 2').bold();
                        column.item()
                            .width(200)
                            .height(150)
                            .border(4)
                            .borderColor(Colors.Red.Medium)
                            .image(resource('Photos/photo.jpg'))
                            .fitArea();

                        column.item().height(15);

                        column.item().paddingBottom(5).text('FitUnproportionally').bold();
                        column.item()
                            .width(200)
                            .height(50)
                            .border(4)
                            .borderColor(Colors.Red.Medium)
                            .image(resource('Photos/photo.jpg'))
                            .fitUnproportionally();
                    });
            });
        })
        .generateImages(() => output('image-scaling.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ImageExamples.DpiSetting', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(400, 1000));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        // lower raster dpi = lower resolution, pixelation
                        column
                            .item()
                            .image(resource('Photos/photo.jpg'))
                            .withRasterDpi(16);

                        // higher raster dpi = higher resolution
                        column
                            .item()
                            .image(resource('Photos/photo.jpg'))
                            .withRasterDpi(288);
                    });
            });
        })
        .generateImages(() => output('image-dpi.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ImageExamples.CompressionSetting', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(400, 1000));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        // low quality = smaller output file
                        column
                            .item()
                            .image(resource('Photos/photo.jpg'))
                            .withCompressionQuality(ImageCompressionQuality.VeryLow);

                        // high quality / fidelity = larger output file
                        column
                            .item()
                            .image(resource('Photos/photo.jpg'))
                            .withCompressionQuality(ImageCompressionQuality.VeryHigh);
                    });
            });
        })
        .generateImages(() => output('image-compression.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ImageExamples.GlobalSettings', () => {
    const settings = new DocumentSettings();

    // default: ImageCompressionQuality.High;
    settings.imageCompressionQuality = ImageCompressionQuality.Medium;

    // default: 288
    settings.imageRasterDpi = 14;

    Document
        .create((document) => {
            document.page((page) => {
                page.content().image(resource('Photos/photo.jpg'));
            });
        })
        .withSettings(settings)
        .generatePdf(output('image-global-settings.pdf'));
});

test('ImageExamples.SharedImages', () => {
    const image = Image.fromFile(resource('checkbox.png'));

    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(350, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(15);

                        for (let i = 0; i < 5; i++) {
                            column.item().row((row) => {
                                row.autoItem().width(28).image(image);
                                row.relativeItem().paddingLeft(8).alignMiddle().text(Placeholders.label());
                            });
                        }
                    });
            });
        })
        .generateImages(() => output('image-shared.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ImageExamples.SvgSupport', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(250);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                const svgContent = fs.readFileSync(resource('pdf-icon.svg'), 'utf-8');

                page.content()
                    .column((column) => {
                        column.item().text('The classic PDF icon looks like this:').bold();
                        column.item().height(15);
                        column.item().svg(svgContent);
                    });
            });
        })
        .generateImages(() => output('image-svg.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
