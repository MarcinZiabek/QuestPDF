package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class RoundedCornersExamples : DocExample() {

    @Test
    fun consistent() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .border(1f, Colors.Black)
                        .background(Colors.Grey.Lighten3)
                        .cornerRadius(25f)
                        .padding(25f)
                        .text("Container with consistently rounded corners")
                }
            }
            .generateImages({ output("rounded-corners-consistent.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun various() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .border(1f, Colors.Black)
                        .background(Colors.Grey.Lighten3)
                        .cornerRadiusTopLeft(5f)
                        .cornerRadiusTopRight(10f)
                        .cornerRadiusBottomRight(20f)
                        .cornerRadiusBottomLeft(40f)
                        .padding(25f)
                        .text("Container with rounded corners")
                }
            }
            .generateImages({ output("rounded-corners-various.webp") }, ImageGenerationSettings().apply {
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
                    maxSize(PageSize(550f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .border(1f, Colors.Black)
                        .cornerRadius(15f)
                        .table {
                            columnsDefinition {
                                constantColumn(100f)
                                relativeColumn()
                                constantColumn(150f)
                            }

                            header {
                                // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                                // overload, which is not bridged; an extension function is the direct equivalent.
                                fun IContainer.style(): IContainer {
                                    return border(1f, Colors.Grey.Darken2)
                                        .background(Colors.Grey.Lighten3)
                                        .paddingVertical(10f)
                                        .paddingHorizontal(15f)
                                        .defaultTextStyle { bold() }
                                }

                                cell().style().text("Index")
                                cell().style().text("Label")
                                cell().style().text("Price")
                            }

                            for (index in 1..5) {
                                fun IContainer.style(): IContainer {
                                    return border(1f, Colors.Grey.Darken2)
                                        .paddingVertical(10f)
                                        .paddingHorizontal(15f)
                                }

                                cell().style().text(index.toString())
                                cell().style().text(Placeholders.label())
                                cell().style().text(Placeholders.price())
                            }
                        }
                }
            }
            .generateImages({ output("rounded-corners-complex.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun image() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(450f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .cornerRadius(25f)
                        .image(resource("landscape.jpg"))
                }
            }
            .generateImages({ output("rounded-corners-image.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
