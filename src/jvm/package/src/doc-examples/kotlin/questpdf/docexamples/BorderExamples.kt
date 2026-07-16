package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class BorderExamples : DocExample() {

    @Test
    fun simpleExample() {
        Document
            .create {
                page {
                    continuousSize(450f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .border(3f, Colors.Blue.Darken4)
                        .background(Colors.Blue.Lighten5)
                        .padding(25f)
                        .text {
                            defaultTextStyle { fontColor(Colors.Blue.Darken4).fontSize(16f) }
                            span("TIP: ").bold()
                            span("You can use borders to create visual separation between elements in your document. Borders can be applied to any element, including text, images, and containers.")
                        }
                }
            }
            .generateImages({ output("border-simple.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun multiple() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .shrink()

                        .borderVertical(5f)
                        .borderColor(Colors.Green.Darken2)
                        .borderAlignmentInside()

                        .container()

                        .borderHorizontal(10f)
                        .borderColor(Colors.Blue.Lighten1)
                        .borderAlignmentInside()

                        .background(Colors.Grey.Lighten2)
                        .paddingVertical(25f)
                        .paddingHorizontal(50f)
                        .text("Content")
                }
            }
            .generateImages({ output("border-multiple.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun consistentThickness() {
        Document
            .create {
                page {
                    minSize(PageSize(550f, 0f))
                    maxSize(PageSize(550f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(25f)

                            relativeItem()
                                .border(1f, Colors.Black)
                                .padding(10f)
                                .alignCenter()
                                .text("Thin")

                            relativeItem()
                                .border(3f, Colors.Black)
                                .padding(10f)
                                .alignCenter()
                                .text("Medium")

                            relativeItem()
                                .border(9f, Colors.Black)
                                .padding(10f)
                                .alignCenter()
                                .text("Bold")
                        }
                }
            }
            .generateImages({ output("border-thickness-consistent.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun variousThickness() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .borderLeft(4f)
                        .borderTop(6f)
                        .borderRight(8f)
                        .borderBottom(10f)
                        .padding(25f)
                        .text("Sample text")
                }
            }
            .generateImages({ output("border-thickness-various.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun alignment() {
        Document
            .create {
                page {
                    minSize(PageSize(725f, 0f))
                    maxSize(PageSize(725f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(25f)

                            relativeItem()
                                .background(Colors.Grey.Lighten1)
                                .padding(25f)
                                .text("No Border")

                            relativeItem()
                                .border(10f, Colors.Grey.Darken2)
                                .borderAlignmentInside()
                                .padding(25f)
                                .text("Border Inside")

                            relativeItem()
                                .border(10f, Colors.Grey.Darken2)
                                .borderAlignmentMiddle()
                                .padding(25f)
                                .text("Border Middle")

                            relativeItem()
                                .border(10f, Colors.Grey.Darken2)
                                .borderAlignmentOutside()
                                .padding(25f)
                                .text("Border Outside")
                        }
                }
            }
            .generateImages({ output("border-alignment.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun roundedCorners1() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .cornerRadius(10f)
                        .border(1f, Colors.Black)
                        .background(Colors.Grey.Lighten2)
                        .padding(25f)
                        .text("Border with rounded corners")
                }
            }
            .generateImages({ output("border-rounded-corners-1.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun roundedCorners2() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .cornerRadius(10f)
                        .borderLeft(10f)
                        .borderAlignmentInside()
                        .borderColor(Colors.Green.Darken2)
                        .background(Colors.Green.Lighten4)
                        .padding(25f)
                        .paddingLeft(10f)
                        .defaultTextStyle { fontColor(Colors.Green.Darken4) }
                        .column {
                            item().text("Completed").bold()
                            item().height(5f)
                            item().text("The invoice has been paid in full.").fontSize(16f)
                        }
                }
            }
            .generateImages({ output("border-rounded-corners-2.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun solidColor() {
        Document
            .create {
                page {
                    continuousSize(450f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            val colors = arrayOf(
                                Colors.Red.Medium,
                                Colors.Green.Medium,
                                Colors.Blue.Medium,
                            )

                            spacing(25f)

                            for (color in colors) {
                                relativeItem()
                                    .border(5f)
                                    .borderColor(color)
                                    .padding(15f)
                                    .text(color.toHexString())
                                    .fontColor(color)
                            }
                        }
                }
            }
            .generateImages({ output("border-color-solid.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun gradient() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .column {
                            spacing(25f)

                            item()
                                .border(5f)
                                .borderLinearGradient(0f, arrayOf(Colors.Red.Darken1, Colors.Blue.Darken1))
                                .borderAlignmentInside()
                                .padding(25f)
                                .text("Horizontal gradient")

                            item()
                                .border(10f)
                                .borderLinearGradient(45f, arrayOf(Colors.Green.Darken1, Colors.LightGreen.Darken1, Colors.Yellow.Darken1))
                                .borderAlignmentInside()
                                .padding(25f)
                                .text("Diagonal gradient")

                            item()
                                .border(10f)
                                .borderLinearGradient(90f, arrayOf(Colors.Yellow.Darken1, Colors.Amber.Darken1, Colors.Orange.Darken1))
                                .cornerRadius(20f)
                                .padding(25f)
                                .text("Vertical gradient")
                        }
                }
            }
            .generateImages({ output("border-color-gradient.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
