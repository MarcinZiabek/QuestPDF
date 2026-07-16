package questpdf.docexamples.text;

import com.questpdf.Settings;
import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.FontFeatures;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

public class TextStyleExamples extends DocExample {

    @Test
    public void fontSize() {
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
                            column.spacing(10f);

                            column.item()
                                .text("This is small text (16pt)")
                                .fontSize(16f);

                            column.item()
                                .text("This is medium text (24pt)")
                                .fontSize(24f);

                            column.item()
                                .text("This is large text (36pt)")
                                .fontSize(36f);
                        });
                });
            })
            .generateImages(index -> output("text-font-size.webp"), settings);
    }

    @Test
    public void fontFamily() {
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
                            column.spacing(10f);

                            column.item().text("This is text with default font (Lato)");

                            column.item().text("This is text with Times New Roman font")
                                .fontFamily("Times New Roman");

                            column.item().text("This is text with Courier New font")
                                .fontFamily("Courier New");
                        });
                });
            })
            .generateImages(index -> output("text-font-family.webp"), settings);
    }

    @Test
    public void fontColor() {
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
                        .text(text -> {
                            text.span("Each pixel consists of three sub-pixels: ");
                            text.span("red").fontColor(Colors.Red.getMedium());
                            text.span(", ");
                            text.span("green").fontColor(Colors.Green.getMedium());
                            text.span(" and ");
                            text.span("blue").fontColor(Colors.Blue.getMedium());
                            text.span(".");
                        });
                });
            })
            .generateImages(index -> output("text-font-color.webp"), settings);
    }

    @Test
    public void backgroundColor() {
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
                        .text(text -> {
                            text.span("The term ");
                            text.span("algorithm").backgroundColor(Colors.Yellow.getLighten3()).bold();
                            text.span(" refers to a set of rules or steps used to solve a problem.");
                        });
                });
            })
            .generateImages(index -> output("text-font-background.webp"), settings);
    }

    @Test
    public void italic() {
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
                        .text(text -> {
                            text.span("In this sentence, the word ");
                            text.span("important").italic();
                            text.span(" is emphasized using italics.");
                        });
                });
            })
            .generateImages(index -> output("text-font-italic.webp"), settings);
    }

    @Test
    public void fontWeight() {
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
                        .text(text -> {
                            text.span("This sentence demonstrates ");
                            text.span("bold").bold();
                            text.span(", ");
                            text.span("normal").normalWeight();
                            text.span(", ");
                            text.span("light").light();
                            text.span(" and ");
                            text.span("thin").thin();
                            text.span(" font weights.");
                        });
                });
            })
            .generateImages(index -> output("text-font-weight.webp"), settings);
    }

    @Test
    public void subscript() {
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
                        .text(text -> {
                            text.span("H");
                            text.span("2").subscript();
                            text.span("O is the chemical formula for water.");
                        });
                });
            })
            .generateImages(index -> output("text-subscript.webp"), settings);
    }

    @Test
    public void superscript() {
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
                        .text(text -> {
                            text.span("E = mc");
                            text.span("2").superscript();
                            text.span(" is the equation of mass-energy equivalence.");
                        });
                });
            })
            .generateImages(index -> output("text-superscript.webp"), settings);
    }

    @Test
    public void lineHeight() {
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
                            column.spacing(20f);

                            var lineHeights = new float[] { 0.75f, 1f, 2f };
                            var paragraph = Placeholders.paragraph();

                            for (var lineHeight : lineHeights) {
                                column.item()
                                    .background(Colors.Grey.getLighten3())
                                    .padding(5f)
                                    .text(paragraph)
                                    .fontSize(16f)
                                    .lineHeight(lineHeight);
                            }
                        });
                });
            })
            .generateImages(index -> output("text-line-height.webp"), settings);
    }

    @Test
    public void letterSpacing() {
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
                            column.spacing(20f);

                            var letterSpacing = new float[] { -0.08f, 0f, 0.2f };
                            var paragraph = Placeholders.sentence();

                            for (var spacing : letterSpacing) {
                                column.item()
                                    .background(Colors.Grey.getLighten3())
                                    .padding(5f)
                                    .text(paragraph)
                                    .fontSize(18f)
                                    .letterSpacing(spacing);
                            }
                        });
                });
            })
            .generateImages(index -> output("text-letter-spacing.webp"), settings);
    }

    @Test
    public void wordSpacing() {
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
                            column.spacing(20f);

                            var wordSpacing = new float[] { -0.2f, 0f, 0.4f };
                            var paragraph = Placeholders.sentence();

                            for (var spacing : wordSpacing) {
                                column.item()
                                    .background(Colors.Grey.getLighten3())
                                    .padding(5f)
                                    .text(paragraph)
                                    .fontSize(16f)
                                    .wordSpacing(spacing);
                            }
                        });
                });
            })
            .generateImages(index -> output("text-word-spacing.webp"), settings);
    }

    @Test
    public void fontFallback() {
        Settings.setUseEnvironmentFonts(false);

        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(600f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text("The Arabic word for programming is البرمجة.")
                        .fontFamily("Lato", "Noto Sans Arabic");
                });
            })
            .generateImages(index -> output("text-font-fallback.webp"), settings);
    }

    @Test
    public void fontFallbackEmoji() {
        Settings.setUseEnvironmentFonts(false);

        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(600f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text("Popular emojis include 😊, 😂, ❤️, 👍, and 😎.")
                        .fontFamily("Lato", "Noto Emoji");
                });
            })
            .generateImages(index -> output("text-font-fallback-emoji.webp"), settings);
    }

    @Test
    public void textFontFeatures() {
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
                        .row(row -> {
                            row.spacing(25f);

                            row.relativeItem()
                                .background(Colors.Grey.getLighten3())
                                .padding(10f)
                                .column(column -> {
                                    column.item().text("Without ligatures").fontSize(16f);

                                    column.item()
                                        .text("fly and fight")
                                        .fontSize(32f)
                                        .disableFontFeature(FontFeatures.StandardLigatures);
                                });

                            row.relativeItem()
                                .background(Colors.Grey.getLighten3())
                                .padding(10f)
                                .column(column -> {
                                    column.item().text("With ligatures").fontSize(16f);

                                    column.item().text("fly and fight")
                                        .fontSize(32f)
                                        .enableFontFeature(FontFeatures.StandardLigatures);
                                });
                        });
                });
            })
            .generateImages(index -> output("text-font-features.webp"), settings);
    }

    @Test
    public void decorationTypes() {
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
                        .text(text -> {
                            text.span("There are a couple of available text decorations: ");
                            text.span("underline").underline().fontColor(Colors.Red.getMedium());
                            text.span(", ");
                            text.span("strikethrough").strikethrough().fontColor(Colors.Green.getMedium());
                            text.span(" and ");
                            text.span("overline").overline().fontColor(Colors.Blue.getMedium());
                            text.span(". ");
                        });
                });
            })
            .generateImages(index -> output("text-decoration-types.webp"), settings);
    }

    @Test
    public void decorationStyles() {
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
                        .text(text -> {
                            text.span("Moreover, the decoration can be ");

                            text.span("solid").fontColor(Colors.Indigo.getMedium()).underline().decorationSolid();
                            text.span(", ");
                            text.span("double").fontColor(Colors.Blue.getMedium()).underline().decorationDouble();
                            text.span(", ");
                            text.span("wavy").fontColor(Colors.LightBlue.getMedium()).underline().decorationWavy();
                            text.span(", ");
                            text.span("dotted").fontColor(Colors.Cyan.getMedium()).underline().decorationDotted();
                            text.span(" or ");
                            text.span("dashed").fontColor(Colors.Green.getMedium())
                                .underline().decorationDashed();
                            text.span(".");
                        });
                });
            })
            .generateImages(index -> output("text-decoration-styles.webp"), settings);
    }

    @Test
    public void decorationsAdvanced() {
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
                        .text(text -> {
                            text.span("This text contains a ");

                            text.span("seriuos")
                                .underline()
                                .decorationWavy()
                                .decorationColor(Colors.Red.getMedium())
                                .decorationThickness(2f);

                            text.span(" typo.");
                        });
                });
            })
            .generateImages(index -> output("text-decoration-advanced.webp"), settings);
    }
}
