package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.BoxShadowStyle;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ShadowExamples extends DocExample {

    @Test
    public void simple() {
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
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    var shadowStyle = new BoxShadowStyle();
                    shadowStyle.setColor(Colors.Grey.getMedium());
                    shadowStyle.setBlur(5f);
                    shadowStyle.setSpread(5f);
                    shadowStyle.setOffsetX(5f);
                    shadowStyle.setOffsetY(5f);

                    page.content()
                        .border(1f, Colors.getBlack())
                        .shadow(shadowStyle)
                        .background(Colors.getWhite())
                        .padding(15f)
                        .text("Important content");
                });
            })
            .generateImages(index -> output("shadow-simple.webp"), settings);
    }

    @Test
    public void offsetX() {
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
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(50f);

                            for (var offsetX : new float[] { -10f, 0f, 10f }) {
                                var shadowStyle = new BoxShadowStyle();
                                shadowStyle.setColor(Colors.Grey.getDarken1());
                                shadowStyle.setBlur(10f);
                                shadowStyle.setOffsetX(offsetX);

                                row.constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(shadowStyle)
                                    .background(Colors.getWhite());
                            }
                        });
                });
            })
            .generateImages(index -> output("shadow-offset-x.webp"), settings);
    }

    @Test
    public void offsetY() {
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
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(50f);

                            for (var offsetY : new float[] { -10f, 0f, 10f }) {
                                var shadowStyle = new BoxShadowStyle();
                                shadowStyle.setColor(Colors.Grey.getDarken2());
                                shadowStyle.setBlur(10f);
                                shadowStyle.setOffsetY(offsetY);

                                row.constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(shadowStyle)
                                    .background(Colors.getWhite());
                            }
                        });
                });
            })
            .generateImages(index -> output("shadow-offset-y.webp"), settings);
    }

    @Test
    public void color() {
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
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(50f);

                            var colors = new Color[] {
                                Colors.Red.getDarken2(),
                                Colors.Green.getDarken2(),
                                Colors.Blue.getDarken2(),
                            };

                            for (var color : colors) {
                                var shadowStyle = new BoxShadowStyle();
                                shadowStyle.setColor(color);
                                shadowStyle.setBlur(10f);

                                row.constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(shadowStyle)
                                    .background(Colors.getWhite());
                            }
                        });
                });
            })
            .generateImages(index -> output("shadow-color.webp"), settings);
    }

    @Test
    public void blur() {
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
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(50f);

                            for (var blur : new float[] { 5f, 10f, 20f }) {
                                var shadowStyle = new BoxShadowStyle();
                                shadowStyle.setColor(Colors.Grey.getDarken1());
                                shadowStyle.setBlur(blur);

                                row.constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(shadowStyle)
                                    .background(Colors.getWhite());
                            }
                        });
                });
            })
            .generateImages(index -> output("shadow-blur.webp"), settings);
    }

    @Test
    public void spread() {
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
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(50f);

                            for (var spread : new float[] { 0f, 5f, 10f }) {
                                var shadowStyle = new BoxShadowStyle();
                                shadowStyle.setColor(Colors.Grey.getDarken1());
                                shadowStyle.setBlur(5f);
                                shadowStyle.setSpread(spread);

                                row.constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(shadowStyle)
                                    .background(Colors.getWhite());
                            }
                        });
                });
            })
            .generateImages(index -> output("shadow-spread.webp"), settings);
    }

    @Test
    public void noBlur() {
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
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(50f);

                            var firstShadowStyle = new BoxShadowStyle();
                            firstShadowStyle.setColor(Colors.Grey.getLighten1());
                            firstShadowStyle.setBlur(0f);
                            firstShadowStyle.setOffsetX(8f);
                            firstShadowStyle.setOffsetY(8f);

                            row.constantItem(100f)
                                .aspectRatio(1f)
                                .shadow(firstShadowStyle)
                                .border(1f, Colors.getBlack())
                                .background(Colors.getWhite());

                            var secondShadowStyle = new BoxShadowStyle();
                            secondShadowStyle.setColor(Colors.Grey.getLighten1());
                            secondShadowStyle.setBlur(0f);
                            secondShadowStyle.setOffsetX(8f);
                            secondShadowStyle.setOffsetY(8f);

                            row.constantItem(100f)
                                .aspectRatio(1f)
                                .shadow(secondShadowStyle)
                                .border(1f, Colors.getBlack())
                                .cornerRadius(16f)
                                .background(Colors.getWhite());
                        });
                });
            })
            .generateImages(index -> output("shadow-no-blur.webp"), settings);
    }
}
