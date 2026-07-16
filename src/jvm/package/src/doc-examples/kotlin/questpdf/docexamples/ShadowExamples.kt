package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.BoxShadowStyle
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

// BoxShadowStyle uses `also` instead of `apply`: several loop variables below
// (offsetX, color, blur, spread) share names with BoxShadowStyle properties,
// and inside `apply` the receiver's property would shadow the loop variable.
class ShadowExamples : DocExample() {

    @Test
    fun simple() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .border(1f, Colors.Black)
                        .shadow(BoxShadowStyle().also {
                            it.color = Colors.Grey.Medium
                            it.blur = 5f
                            it.spread = 5f
                            it.offsetX = 5f
                            it.offsetY = 5f
                        })
                        .background(Colors.White)
                        .padding(15f)
                        .text("Important content")
                }
            }
            .generateImages({ output("shadow-simple.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun offsetX() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(50f)

                            for (offsetX in arrayOf(-10f, 0f, 10f)) {
                                constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(BoxShadowStyle().also {
                                        it.color = Colors.Grey.Darken1
                                        it.blur = 10f
                                        it.offsetX = offsetX
                                    })
                                    .background(Colors.White)
                            }
                        }
                }
            }
            .generateImages({ output("shadow-offset-x.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun offsetY() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(50f)

                            for (offsetY in arrayOf(-10f, 0f, 10f)) {
                                constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(BoxShadowStyle().also {
                                        it.color = Colors.Grey.Darken2
                                        it.blur = 10f
                                        it.offsetY = offsetY
                                    })
                                    .background(Colors.White)
                            }
                        }
                }
            }
            .generateImages({ output("shadow-offset-y.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun color() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(50f)

                            val colors = arrayOf(
                                Colors.Red.Darken2,
                                Colors.Green.Darken2,
                                Colors.Blue.Darken2,
                            )

                            for (color in colors) {
                                constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(BoxShadowStyle().also {
                                        it.color = color
                                        it.blur = 10f
                                    })
                                    .background(Colors.White)
                            }
                        }
                }
            }
            .generateImages({ output("shadow-color.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun blur() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(50f)

                            for (blur in arrayOf(5f, 10f, 20f)) {
                                constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(BoxShadowStyle().also {
                                        it.color = Colors.Grey.Darken1
                                        it.blur = blur
                                    })
                                    .background(Colors.White)
                            }
                        }
                }
            }
            .generateImages({ output("shadow-blur.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun spread() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(50f)

                            for (spread in arrayOf(0f, 5f, 10f)) {
                                constantItem(100f)
                                    .aspectRatio(1f)
                                    .shadow(BoxShadowStyle().also {
                                        it.color = Colors.Grey.Darken1
                                        it.blur = 5f
                                        it.spread = spread
                                    })
                                    .background(Colors.White)
                            }
                        }
                }
            }
            .generateImages({ output("shadow-spread.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun noBlur() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(50f)

                            constantItem(100f)
                                .aspectRatio(1f)
                                .shadow(BoxShadowStyle().also {
                                    it.color = Colors.Grey.Lighten1
                                    it.blur = 0f
                                    it.offsetX = 8f
                                    it.offsetY = 8f
                                })
                                .border(1f, Colors.Black)
                                .background(Colors.White)

                            constantItem(100f)
                                .aspectRatio(1f)
                                .shadow(BoxShadowStyle().also {
                                    it.color = Colors.Grey.Lighten1
                                    it.blur = 0f
                                    it.offsetX = 8f
                                    it.offsetY = 8f
                                })
                                .border(1f, Colors.Black)
                                .cornerRadius(16f)
                                .background(Colors.White)
                        }
                }
            }
            .generateImages({ output("shadow-no-blur.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
