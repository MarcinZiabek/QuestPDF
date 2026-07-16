package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

public class PlaceholderExamples extends DocExample {

    @Test
    public void textExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(15f);

                            BiConsumer<String, String> addItem = (label, value) -> {
                                column.item().text(text -> {
                                    text.span(label + ": ").bold();
                                    text.span(value);
                                });
                            };

                            addItem.accept("Name", Placeholders.name());
                            addItem.accept("Email", Placeholders.email());
                            addItem.accept("Phone", Placeholders.phoneNumber());
                            addItem.accept("Date", Placeholders.shortDate());
                            addItem.accept("Time", Placeholders.time());
                        });
                });
            })
            .generateImages(index -> output("placeholders-text.webp"), settings);
    }

    @Test
    public void backgroundColorExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(320f, 0f));
                    page.maxSize(new PageSize(320f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .grid(grid -> {
                            grid.columns(5);
                            grid.spacing(5f);

                            for (var i = 0; i < 25; i++) {
                                grid.item()
                                    .height(50f)
                                    .width(50f)
                                    .background(Placeholders.backgroundColor());
                            }
                        });
                });
            })
            .generateImages(index -> output("placeholders-color-background.webp"), settings);
    }

    @Test
    public void colorExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(500f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(10f);

                            for (var i = 0; i < 5; i++) {
                                column.item()
                                    .text(Placeholders.sentence())
                                    .fontColor(Placeholders.color());
                            }
                        });
                });
            })
            .generateImages(index -> output("placeholders-color.webp"), settings);
    }

    @Test
    public void imageExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .width(200f)
                        .column(column -> {
                            column.spacing(10f);

                            // provide an exact image resolution
                            column.item()
                                .image(Placeholders.image(100, 50));

                            // specify physical width and height of the image
                            // (The C# original passes Placeholders.Image through the Image(Func<ImageSize, byte[]>)
                            // overload, which is not bridged; the payload delegate is the direct equivalent.)
                            column.item()
                                .width(200f)
                                .height(150f)
                                .image(payload -> Placeholders.image(payload.getImageSize()));

                            // specify target physical width and aspect ratio
                            column.item()
                                .width(200f)
                                .aspectRatio(3 / 2f)
                                .image(payload -> Placeholders.image(payload.getImageSize()));
                        });
                });
            })
            .generateImages(index -> output("placeholders-image.webp"), settings);
    }

    @Test
    public void elementExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.header()
                        .height(100f)
                        .placeholder("Header");

                    page.content()
                        .paddingVertical(25f)
                        .placeholder();

                    page.footer()
                        .height(100f)
                        .placeholder("Footer");
                });
            })
            .generateImages(index -> output("placeholder-element.webp"), settings);
    }
}
