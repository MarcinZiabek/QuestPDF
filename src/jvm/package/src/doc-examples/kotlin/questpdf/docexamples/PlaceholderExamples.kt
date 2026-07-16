package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class PlaceholderExamples : DocExample() {

    @Test
    fun textExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(15f)

                            fun addItem(label: String, value: String) {
                                item().text {
                                    span("$label: ").bold()
                                    span(value)
                                }
                            }

                            addItem("Name", Placeholders.name())
                            addItem("Email", Placeholders.email())
                            addItem("Phone", Placeholders.phoneNumber())
                            addItem("Date", Placeholders.shortDate())
                            addItem("Time", Placeholders.time())
                        }
                }
            }
            .generateImages({ output("placeholders-text.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun backgroundColorExample() {
        Document
            .create {
                page {
                    minSize(PageSize(320f, 0f))
                    maxSize(PageSize(320f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .grid {
                            columns(5)
                            spacing(5f)

                            repeat(25) {
                                item()
                                    .height(50f)
                                    .width(50f)
                                    .background(Placeholders.backgroundColor())
                            }
                        }
                }
            }
            .generateImages({ output("placeholders-color-background.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun colorExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            repeat(5) {
                                item()
                                    .text(Placeholders.sentence())
                                    .fontColor(Placeholders.color())
                            }
                        }
                }
            }
            .generateImages({ output("placeholders-color.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun imageExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .width(200f)
                        .column {
                            spacing(10f)

                            // provide an exact image resolution
                            item()
                                .image(Placeholders.image(100, 50))

                            // specify physical width and height of the image
                            // (The C# original passes Placeholders.Image through the Image(Func<ImageSize, byte[]>)
                            // overload, which is not bridged; the payload delegate is the direct equivalent.)
                            item()
                                .width(200f)
                                .height(150f)
                                .image { payload -> Placeholders.image(payload.imageSize) }

                            // specify target physical width and aspect ratio
                            item()
                                .width(200f)
                                .aspectRatio(3 / 2f)
                                .image { payload -> Placeholders.image(payload.imageSize) }
                        }
                }
            }
            .generateImages({ output("placeholders-image.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun elementExample() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    header()
                        .height(100f)
                        .placeholder("Header")

                    content()
                        .paddingVertical(25f)
                        .placeholder()

                    footer()
                        .height(100f)
                        .placeholder("Footer")
                }
            }
            .generateImages({ output("placeholder-element.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }
}
