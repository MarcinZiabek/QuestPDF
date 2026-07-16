package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class BorderExamples extends DocExample {

    @Test
    public void simpleExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(450f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .border(3f, Colors.Blue.getDarken4())
                        .background(Colors.Blue.getLighten5())
                        .padding(25f)
                        .text(text -> {
                            text.defaultTextStyle(style -> style.fontColor(Colors.Blue.getDarken4()).fontSize(16f));
                            text.span("TIP: ").bold();
                            text.span("You can use borders to create visual separation between elements in your document. Borders can be applied to any element, including text, images, and containers.");
                        });
                });
            })
            .generateImages(index -> output("border-simple.webp"), settings);
    }

    @Test
    public void multiple() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .shrink()

                        .borderVertical(5f)
                        .borderColor(Colors.Green.getDarken2())
                        .borderAlignmentInside()

                        .container()

                        .borderHorizontal(10f)
                        .borderColor(Colors.Blue.getLighten1())
                        .borderAlignmentInside()

                        .background(Colors.Grey.getLighten2())
                        .paddingVertical(25f)
                        .paddingHorizontal(50f)
                        .text("Content");
                });
            })
            .generateImages(index -> output("border-multiple.webp"), settings);
    }

    @Test
    public void consistentThickness() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(550f, 0f));
                    page.maxSize(new PageSize(550f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(25f);

                            row.relativeItem()
                                .border(1f, Colors.getBlack())
                                .padding(10f)
                                .alignCenter()
                                .text("Thin");

                            row.relativeItem()
                                .border(3f, Colors.getBlack())
                                .padding(10f)
                                .alignCenter()
                                .text("Medium");

                            row.relativeItem()
                                .border(9f, Colors.getBlack())
                                .padding(10f)
                                .alignCenter()
                                .text("Bold");
                        });
                });
            })
            .generateImages(index -> output("border-thickness-consistent.webp"), settings);
    }

    @Test
    public void variousThickness() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .borderLeft(4f)
                        .borderTop(6f)
                        .borderRight(8f)
                        .borderBottom(10f)
                        .padding(25f)
                        .text("Sample text");
                });
            })
            .generateImages(index -> output("border-thickness-various.webp"), settings);
    }

    @Test
    public void alignment() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(725f, 0f));
                    page.maxSize(new PageSize(725f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(25f);

                            row.relativeItem()
                                .background(Colors.Grey.getLighten1())
                                .padding(25f)
                                .text("No Border");

                            row.relativeItem()
                                .border(10f, Colors.Grey.getDarken2())
                                .borderAlignmentInside()
                                .padding(25f)
                                .text("Border Inside");

                            row.relativeItem()
                                .border(10f, Colors.Grey.getDarken2())
                                .borderAlignmentMiddle()
                                .padding(25f)
                                .text("Border Middle");

                            row.relativeItem()
                                .border(10f, Colors.Grey.getDarken2())
                                .borderAlignmentOutside()
                                .padding(25f)
                                .text("Border Outside");
                        });
                });
            })
            .generateImages(index -> output("border-alignment.webp"), settings);
    }

    @Test
    public void roundedCorners1() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .cornerRadius(10f)
                        .border(1f, Colors.getBlack())
                        .background(Colors.Grey.getLighten2())
                        .padding(25f)
                        .text("Border with rounded corners");
                });
            })
            .generateImages(index -> output("border-rounded-corners-1.webp"), settings);
    }

    @Test
    public void roundedCorners2() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .cornerRadius(10f)
                        .borderLeft(10f)
                        .borderAlignmentInside()
                        .borderColor(Colors.Green.getDarken2())
                        .background(Colors.Green.getLighten4())
                        .padding(25f)
                        .paddingLeft(10f)
                        .defaultTextStyle(style -> style.fontColor(Colors.Green.getDarken4()))
                        .column(column -> {
                            column.item().text("Completed").bold();
                            column.item().height(5f);
                            column.item().text("The invoice has been paid in full.").fontSize(16f);
                        });
                });
            })
            .generateImages(index -> output("border-rounded-corners-2.webp"), settings);
    }

    @Test
    public void solidColor() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(450f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            var colors = new Color[] {
                                Colors.Red.getMedium(),
                                Colors.Green.getMedium(),
                                Colors.Blue.getMedium(),
                            };

                            row.spacing(25f);

                            for (var color : colors) {
                                row.relativeItem()
                                    .border(5f)
                                    .borderColor(color)
                                    .padding(15f)
                                    .text(color.toString())
                                    .fontColor(color);
                            }
                        });
                });
            })
            .generateImages(index -> output("border-color-solid.webp"), settings);
    }

    @Test
    public void gradient() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .column(column -> {
                            column.spacing(25f);

                            column.item()
                                .border(5f)
                                .borderLinearGradient(0f, new Color[] { Colors.Red.getDarken1(), Colors.Blue.getDarken1() })
                                .borderAlignmentInside()
                                .padding(25f)
                                .text("Horizontal gradient");

                            column.item()
                                .border(10f)
                                .borderLinearGradient(45f, new Color[] { Colors.Green.getDarken1(), Colors.LightGreen.getDarken1(), Colors.Yellow.getDarken1() })
                                .borderAlignmentInside()
                                .padding(25f)
                                .text("Diagonal gradient");

                            column.item()
                                .border(10f)
                                .borderLinearGradient(90f, new Color[] { Colors.Yellow.getDarken1(), Colors.Amber.getDarken1(), Colors.Orange.getDarken1() })
                                .cornerRadius(20f)
                                .padding(25f)
                                .text("Vertical gradient");
                        });
                });
            })
            .generateImages(index -> output("border-color-gradient.webp"), settings);
    }
}
