package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class LineExamples extends DocExample {

    @Test
    public void verticalLineExample() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.autoItem().text("Text on the left");

                            row.autoItem()
                                .paddingHorizontal(15f)
                                .lineVertical(3f)
                                .lineColor(Colors.Blue.getMedium());

                            row.autoItem().text("Text on the right");
                        });
                });
            })
            .generateImages(index -> output("line-vertical.webp"), settings);
    }

    @Test
    public void horizontalLineExample() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .column(column -> {
                            column.item().text("Text above the line");

                            column.item()
                                .paddingVertical(10f)
                                .lineHorizontal(2f)
                                .lineColor(Colors.Blue.getMedium());

                            column.item().text("Text below the line");
                        });
                });
            })
            .generateImages(index -> output("line-horizontal.webp"), settings);
    }

    @Test
    public void thickness() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .column(column -> {
                            column.spacing(20f);

                            for (var thickness : new float[] { 1f, 2f, 4f, 8f }) {
                                column.item()
                                    .width(200f)
                                    .lineHorizontal(thickness);
                            }
                        });
                });
            })
            .generateImages(index -> output("line-thickness.webp"), settings);
    }

    @Test
    public void solidColor() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .column(column -> {
                            var colors = new Color[] {
                                Colors.Red.getMedium(),
                                Colors.Green.getMedium(),
                                Colors.Blue.getMedium(),
                            };

                            column.spacing(20f);

                            for (var color : colors) {
                                column.item()
                                    .width(200f)
                                    .lineHorizontal(5f)
                                    .lineColor(color);
                            }
                        });
                });
            })
            .generateImages(index -> output("line-color-solid.webp"), settings);
    }

    @Test
    public void gradient() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .column(column -> {
                            column.spacing(20f);

                            column.item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineGradient(new Color[] { Colors.Red.getMedium(), Colors.Orange.getMedium() });

                            column.item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineGradient(new Color[] { Colors.Orange.getMedium(), Colors.Yellow.getMedium(), Colors.Lime.getMedium() });

                            column.item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineGradient(new Color[] { Colors.Blue.getLighten2(), Colors.LightBlue.getLighten1(), Colors.Cyan.getMedium(), Colors.Teal.getDarken1(), Colors.Green.getDarken2() });
                        });
                });
            })
            .generateImages(index -> output("line-color-gradient.webp"), settings);
    }

    @Test
    public void dashPattern() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .column(column -> {
                            column.spacing(20f);

                            column.item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineDashPattern(new float[] { 4f, 4f });

                            column.item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineDashPattern(new float[] { 12f, 12f });

                            column.item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineDashPattern(new float[] { 4f, 4f, 12f, 4f });
                        });
                });
            })
            .generateImages(index -> output("line-dash-pattern.webp"), settings);
    }

    @Test
    public void complex() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .width(300f)
                        .lineHorizontal(8f)
                        .lineDashPattern(new float[] { 4f, 4f, 8f, 8f, 12f, 12f })
                        .lineGradient(new Color[] { Colors.Red.getMedium(), Colors.Orange.getMedium(), Colors.Yellow.getMedium() });
                });
            })
            .generateImages(index -> output("line-example.webp"), settings);
    }
}
