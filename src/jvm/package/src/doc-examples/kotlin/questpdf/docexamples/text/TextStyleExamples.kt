package questpdf.docexamples.text

import com.questpdf.Settings
import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.FontFeatures
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class TextStyleExamples : DocExample() {

    @Test
    fun fontSize() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            item()
                                .text("This is small text (16pt)")
                                .fontSize(16f)

                            item()
                                .text("This is medium text (24pt)")
                                .fontSize(24f)

                            item()
                                .text("This is large text (36pt)")
                                .fontSize(36f)
                        }
                }
            }
            .generateImages({ output("text-font-size.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun fontFamily() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            item().text("This is text with default font (Lato)")

                            item().text("This is text with Times New Roman font")
                                .fontFamily("Times New Roman")

                            item().text("This is text with Courier New font")
                                .fontFamily("Courier New")
                        }
                }
            }
            .generateImages({ output("text-font-family.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun fontColor() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("Each pixel consists of three sub-pixels: ")
                            span("red").fontColor(Colors.Red.Medium)
                            span(", ")
                            span("green").fontColor(Colors.Green.Medium)
                            span(" and ")
                            span("blue").fontColor(Colors.Blue.Medium)
                            span(".")
                        }
                }
            }
            .generateImages({ output("text-font-color.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun backgroundColor() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("The term ")
                            span("algorithm").backgroundColor(Colors.Yellow.Lighten3).bold()
                            span(" refers to a set of rules or steps used to solve a problem.")
                        }
                }
            }
            .generateImages({ output("text-font-background.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun italic() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("In this sentence, the word ")
                            span("important").italic()
                            span(" is emphasized using italics.")
                        }
                }
            }
            .generateImages({ output("text-font-italic.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun fontWeight() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("This sentence demonstrates ")
                            span("bold").bold()
                            span(", ")
                            span("normal").normalWeight()
                            span(", ")
                            span("light").light()
                            span(" and ")
                            span("thin").thin()
                            span(" font weights.")
                        }
                }
            }
            .generateImages({ output("text-font-weight.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun subscript() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("H")
                            span("2").subscript()
                            span("O is the chemical formula for water.")
                        }
                }
            }
            .generateImages({ output("text-subscript.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun superscript() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("E = mc")
                            span("2").superscript()
                            span(" is the equation of mass-energy equivalence.")
                        }
                }
            }
            .generateImages({ output("text-superscript.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun lineHeight() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(20f)

                            val lineHeights = arrayOf(0.75f, 1f, 2f)
                            val paragraph = Placeholders.paragraph()

                            for (lineHeight in lineHeights) {
                                item()
                                    .background(Colors.Grey.Lighten3)
                                    .padding(5f)
                                    .text(paragraph)
                                    .fontSize(16f)
                                    .lineHeight(lineHeight)
                            }
                        }
                }
            }
            .generateImages({ output("text-line-height.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun letterSpacing() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(20f)

                            val letterSpacing = arrayOf(-0.08f, 0f, 0.2f)
                            val paragraph = Placeholders.sentence()

                            for (spacing in letterSpacing) {
                                item()
                                    .background(Colors.Grey.Lighten3)
                                    .padding(5f)
                                    .text(paragraph)
                                    .fontSize(18f)
                                    .letterSpacing(spacing)
                            }
                        }
                }
            }
            .generateImages({ output("text-letter-spacing.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun wordSpacing() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(20f)

                            val wordSpacing = arrayOf(-0.2f, 0f, 0.4f)
                            val paragraph = Placeholders.sentence()

                            for (spacing in wordSpacing) {
                                item()
                                    .background(Colors.Grey.Lighten3)
                                    .padding(5f)
                                    .text(paragraph)
                                    .fontSize(16f)
                                    .wordSpacing(spacing)
                            }
                        }
                }
            }
            .generateImages({ output("text-word-spacing.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun fontFallback() {
        Settings.useEnvironmentFonts = false

        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text("The Arabic word for programming is البرمجة.")
                        .fontFamily("Lato", "Noto Sans Arabic")
                }
            }
            .generateImages({ output("text-font-fallback.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun fontFallbackEmoji() {
        Settings.useEnvironmentFonts = false

        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text("Popular emojis include 😊, 😂, ❤️, 👍, and 😎.")
                        .fontFamily("Lato", "Noto Emoji")
                }
            }
            .generateImages({ output("text-font-fallback-emoji.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun textFontFeatures() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .row {
                            spacing(25f)

                            relativeItem()
                                .background(Colors.Grey.Lighten3)
                                .padding(10f)
                                .column {
                                    item().text("Without ligatures").fontSize(16f)

                                    item()
                                        .text("fly and fight")
                                        .fontSize(32f)
                                        .disableFontFeature(FontFeatures.StandardLigatures)
                                }

                            relativeItem()
                                .background(Colors.Grey.Lighten3)
                                .padding(10f)
                                .column {
                                    item().text("With ligatures").fontSize(16f)

                                    item().text("fly and fight")
                                        .fontSize(32f)
                                        .enableFontFeature(FontFeatures.StandardLigatures)
                                }
                        }
                }
            }
            .generateImages({ output("text-font-features.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun decorationTypes() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("There are a couple of available text decorations: ")
                            span("underline").underline().fontColor(Colors.Red.Medium)
                            span(", ")
                            span("strikethrough").strikethrough().fontColor(Colors.Green.Medium)
                            span(" and ")
                            span("overline").overline().fontColor(Colors.Blue.Medium)
                            span(". ")
                        }
                }
            }
            .generateImages({ output("text-decoration-types.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun decorationStyles() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("Moreover, the decoration can be ")

                            span("solid").fontColor(Colors.Indigo.Medium).underline().decorationSolid()
                            span(", ")
                            span("double").fontColor(Colors.Blue.Medium).underline().decorationDouble()
                            span(", ")
                            span("wavy").fontColor(Colors.LightBlue.Medium).underline().decorationWavy()
                            span(", ")
                            span("dotted").fontColor(Colors.Cyan.Medium).underline().decorationDotted()
                            span(" or ")
                            span("dashed").fontColor(Colors.Green.Medium)
                                .underline().decorationDashed()
                            span(".")
                        }
                }
            }
            .generateImages({ output("text-decoration-styles.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun decorationsAdvanced() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("This text contains a ")

                            span("seriuos")
                                .underline()
                                .decorationWavy()
                                .decorationColor(Colors.Red.Medium)
                                .decorationThickness(2f)

                            span(" typo.")
                        }
                }
            }
            .generateImages({ output("text-decoration-advanced.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
