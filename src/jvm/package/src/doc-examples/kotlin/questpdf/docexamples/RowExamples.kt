package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class RowExamples : DocExample() {

    @Test
    fun simpleExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    margin(25f)

                    content()
                        .padding(25f)
                        .width(325f)
                        .row {
                            constantItem(100f)
                                .background(Colors.Grey.Medium)
                                .padding(10f)
                                .text("100pt")

                            relativeItem()
                                .background(Colors.Grey.Lighten1)
                                .padding(10f)
                                .text("75pt")

                            relativeItem(2f)
                                .background(Colors.Grey.Lighten2)
                                .padding(10f)
                                .text("150pt")
                        }
                }
            }
            .generateImages({ output("row-simple.webp") }, ImageGenerationSettings().apply {
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
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    margin(25f)

                    content()
                        .padding(25f)
                        .width(220f)
                        .height(50f)
                        .row {
                            spacing(10f)

                            relativeItem(2f).background(Colors.Grey.Medium)
                            relativeItem(3f).background(Colors.Grey.Lighten1)
                            relativeItem(5f).background(Colors.Grey.Lighten2)
                        }
                }
            }
            .generateImages({ output("row-spacing.webp") }, ImageGenerationSettings().apply {
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
                        .height(50f)
                        .row {
                            relativeItem().background(Colors.Grey.Darken1)
                            constantItem(10f)
                            relativeItem().background(Colors.Grey.Medium)
                            constantItem(20f)
                            relativeItem().background(Colors.Grey.Lighten1)
                            constantItem(30f)
                            relativeItem().background(Colors.Grey.Lighten2)
                        }
                }
            }
            .generateImages({ output("row-spacing-custom.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun disableUniformItemsHeightExample() {
        Document
            .create {
                page {
                    minSize(PageSize(700f, 0f))
                    maxSize(PageSize(700f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)
                    pageColor(Colors.White)

                    content()
                        .row {
                            spacing(15f)

                            // The C# original routes LabelStyle through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; an extension function is the direct equivalent.
                            fun IContainer.labelStyle(): IContainer =
                                shrinkVertical()
                                    .background(Colors.Grey.Lighten3)
                                    .cornerRadius(15f)
                                    .padding(15f)

                            relativeItem()
                                .labelStyle()
                                .text("Programming is both a science and an art — it demands precision, creativity, and patience. At its core, it’s about understanding how to break down complex problems into small, logical steps that a computer can execute.")

                            relativeItem()
                                .labelStyle()
                                .text("Programming is the art of turning ideas into logic, logic into code, and code into something that solves real problems.")
                        }
                }
            }
            .generateImages({ output("row-uniform-height-enabled.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }
}
