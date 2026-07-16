package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class LineExamples : DocExample() {

    @Test
    fun verticalLineExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            autoItem().text("Text on the left")

                            autoItem()
                                .paddingHorizontal(15f)
                                .lineVertical(3f)
                                .lineColor(Colors.Blue.Medium)

                            autoItem().text("Text on the right")
                        }
                }
            }
            .generateImages({ output("line-vertical.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun horizontalLineExample() {
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
                            item().text("Text above the line")

                            item()
                                .paddingVertical(10f)
                                .lineHorizontal(2f)
                                .lineColor(Colors.Blue.Medium)

                            item().text("Text below the line")
                        }
                }
            }
            .generateImages({ output("line-horizontal.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun thickness() {
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
                            spacing(20f)

                            for (thickness in arrayOf(1f, 2f, 4f, 8f)) {
                                item()
                                    .width(200f)
                                    .lineHorizontal(thickness)
                            }
                        }
                }
            }
            .generateImages({ output("line-thickness.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun solidColor() {
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
                            val colors = arrayOf(
                                Colors.Red.Medium,
                                Colors.Green.Medium,
                                Colors.Blue.Medium,
                            )

                            spacing(20f)

                            for (color in colors) {
                                item()
                                    .width(200f)
                                    .lineHorizontal(5f)
                                    .lineColor(color)
                            }
                        }
                }
            }
            .generateImages({ output("line-color-solid.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
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
                            spacing(20f)

                            item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineGradient(arrayOf(Colors.Red.Medium, Colors.Orange.Medium))

                            item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineGradient(arrayOf(Colors.Orange.Medium, Colors.Yellow.Medium, Colors.Lime.Medium))

                            item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineGradient(arrayOf(Colors.Blue.Lighten2, Colors.LightBlue.Lighten1, Colors.Cyan.Medium, Colors.Teal.Darken1, Colors.Green.Darken2))
                        }
                }
            }
            .generateImages({ output("line-color-gradient.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun dashPattern() {
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
                            spacing(20f)

                            item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineDashPattern(floatArrayOf(4f, 4f))

                            item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineDashPattern(floatArrayOf(12f, 12f))

                            item()
                                .width(200f)
                                .lineHorizontal(5f)
                                .lineDashPattern(floatArrayOf(4f, 4f, 12f, 4f))
                        }
                }
            }
            .generateImages({ output("line-dash-pattern.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun complex() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .width(300f)
                        .lineHorizontal(8f)
                        .lineDashPattern(floatArrayOf(4f, 4f, 8f, 8f, 12f, 12f))
                        .lineGradient(arrayOf(Colors.Red.Medium, Colors.Orange.Medium, Colors.Yellow.Medium))
                }
            }
            .generateImages({ output("line-example.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
