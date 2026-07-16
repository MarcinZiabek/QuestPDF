package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ColumnExamples : DocExample() {

    @Test
    fun simpleExample() {
        Document
            .create {
                page {
                    minSize(PageSize(250f, 0f))
                    maxSize(PageSize(250f, 1000f))
                    margin(25f)

                    content()
                        .column {
                            item().background(Colors.Grey.Medium).height(50f)
                            item().background(Colors.Grey.Lighten1).height(75f)
                            item().background(Colors.Grey.Lighten2).height(100f)
                        }
                }
            }
            .generateImages({ output("column-simple.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun spacingExample() {
        Document
            .create {
                page {
                    minSize(PageSize(250f, 0f))
                    maxSize(PageSize(250f, 1000f))
                    margin(25f)

                    content()
                        .column {
                            spacing(25f)

                            item().background(Colors.Grey.Medium).height(50f)
                            item().background(Colors.Grey.Lighten1).height(75f)
                            item().background(Colors.Grey.Lighten2).height(100f)
                        }
                }
            }
            .generateImages({ output("column-spacing.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun customSpacingExample() {
        Document
            .create {
                page {
                    minSize(PageSize(250f, 0f))
                    maxSize(PageSize(250f, 1000f))
                    margin(25f)

                    content()
                        .column {
                            item().background(Colors.Grey.Darken1).height(50f)
                            item().height(10f)
                            item().background(Colors.Grey.Medium).height(50f)
                            item().height(20f)
                            item().background(Colors.Grey.Lighten1).height(50f)
                            item().height(30f)
                            item().background(Colors.Grey.Lighten2).height(50f)
                        }
                }
            }
            .generateImages({ output("column-spacing-custom.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun disableUniformItemsWidthExample() {
        Document
            .create {
                page {
                    minSize(PageSize(400f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .column {
                            spacing(15f)

                            // The C# original routes LabelStyle through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; an extension function is the direct equivalent.
                            fun IContainer.labelStyle(): IContainer =
                                shrinkHorizontal()
                                    .background(Colors.Grey.Lighten3)
                                    .cornerRadius(15f)
                                    .padding(15f)

                            item()
                                .labelStyle()
                                .text("REST API")

                            item()
                                .labelStyle()
                                .text("Garbage Collection")

                            item()
                                .labelStyle()
                                .text("Object-Oriented Programming")
                        }
                }
            }
            .generateImages({ output("column-uniform-width-disabled.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
