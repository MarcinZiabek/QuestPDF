package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.DocumentSettings;
import com.questpdf.infrastructure.Image;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

// NOT PORTED: image-dynamic.webp — depends on .NET SkiaSharp (SKBitmap/SKCanvas synthesize the dynamic image).
public class ImageExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .grid(grid -> {
                            grid.columns(2);
                            grid.spacing(10f);

                            grid.item(2).text("My photo gallery:").bold();

                            grid.item().image(resource("Photos/photo-gallery-1.jpg"));
                            grid.item().image(resource("Photos/photo-gallery-2.jpg"));
                            grid.item().image(resource("Photos/photo-gallery-3.jpg"));
                            grid.item().image(resource("Photos/photo-gallery-4.jpg"));
                        });
                });
            })
            .generateImages(index -> output("image-example.webp"), settings);
    }

    @Test
    public void imageScaling() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1500f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.item().paddingBottom(5f).text("FitWidth").bold();
                            column.item()
                                .width(200f)
                                .height(150f)
                                .border(4f)
                                .borderColor(Colors.Red.getMedium())
                                .image(resource("Photos/photo.jpg"))
                                .fitWidth();

                            column.item().height(15f);

                            column.item().paddingBottom(5f).text("FitHeight").bold();
                            column.item()
                                .width(200f)
                                .height(100f)
                                .border(4f)
                                .borderColor(Colors.Red.getMedium())
                                .image(resource("Photos/photo.jpg"))
                                .fitHeight();

                            column.item().height(15f);

                            column.item().paddingBottom(5f).text("FitArea 1").bold();
                            column.item()
                                .width(200f)
                                .height(100f)
                                .border(4f)
                                .borderColor(Colors.Red.getMedium())
                                .image(resource("Photos/photo.jpg"))
                                .fitArea();

                            column.item().height(15f);

                            column.item().paddingBottom(5f).text("FitArea 2").bold();
                            column.item()
                                .width(200f)
                                .height(150f)
                                .border(4f)
                                .borderColor(Colors.Red.getMedium())
                                .image(resource("Photos/photo.jpg"))
                                .fitArea();

                            column.item().height(15f);

                            column.item().paddingBottom(5f).text("FitUnproportionally").bold();
                            column.item()
                                .width(200f)
                                .height(50f)
                                .border(4f)
                                .borderColor(Colors.Red.getMedium())
                                .image(resource("Photos/photo.jpg"))
                                .fitUnproportionally();
                        });
                });
            })
            .generateImages(index -> output("image-scaling.webp"), settings);
    }

    @Test
    public void dpiSetting() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(10f);

                            // lower raster dpi = lower resolution, pixelation
                            column.item()
                                .image(resource("Photos/photo.jpg"))
                                .withRasterDpi(16);

                            // higher raster dpi = higher resolution
                            column.item()
                                .image(resource("Photos/photo.jpg"))
                                .withRasterDpi(288);
                        });
                });
            })
            .generateImages(index -> output("image-dpi.webp"), settings);
    }

    @Test
    public void compressionSetting() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(10f);

                            // low quality = smaller output file
                            column.item()
                                .image(resource("Photos/photo.jpg"))
                                .withCompressionQuality(ImageCompressionQuality.VeryLow);

                            // high quality / fidelity = larger output file
                            column.item()
                                .image(resource("Photos/photo.jpg"))
                                .withCompressionQuality(ImageCompressionQuality.VeryHigh);
                        });
                });
            })
            .generateImages(index -> output("image-compression.webp"), settings);
    }

    @Test
    public void globalSettings() {
        var settings = new DocumentSettings();

        // default: ImageCompressionQuality.High;
        settings.setImageCompressionQuality(ImageCompressionQuality.Medium);

        // default: 288
        settings.setImageRasterDpi(14);

        Document
            .create(document -> {
                document.page(page -> {
                    page.content().image(resource("Photos/photo.jpg"));
                });
            })
            .withSettings(settings)
            .generatePdf(output("image-global-settings.pdf"));
    }

    @Test
    public void sharedImages() {
        var image = Image.fromFile(resource("checkbox.png"));

        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(350f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(15f);

                            for (var i = 0; i < 5; i++) {
                                column.item().row(row -> {
                                    row.autoItem().width(28f).image(image);
                                    row.relativeItem().paddingLeft(8f).alignMiddle().text(Placeholders.label());
                                });
                            }
                        });
                });
            })
            .generateImages(index -> output("image-shared.webp"), settings);
    }

    @Test
    public void svgSupport() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(250f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    final String svgContent;

                    try {
                        svgContent = Files.readString(Path.of(resource("pdf-icon.svg")));
                    } catch (IOException exception) {
                        throw new UncheckedIOException(exception);
                    }

                    page.content()
                        .column(column -> {
                            column.item().text("The classic PDF icon looks like this:").bold();
                            column.item().height(15f);
                            column.item().svg(svgContent);
                        });
                });
            })
            .generateImages(index -> output("image-svg.webp"), settings);
    }
}
