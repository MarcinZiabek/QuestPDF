package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class RotateExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .row {
                            autoItem()
                                .rotateLeft()
                                .alignCenter()
                                .text("Definition")
                                .bold().fontColor(Colors.Blue.Darken2)

                            autoItem()
                                .paddingHorizontal(15f)
                                .lineVertical(2f).lineColor(Colors.Blue.Medium)

                            relativeItem()
                                .background(Colors.Blue.Lighten5)
                                .padding(15f)
                                .text {
                                    span("A variable").bold()
                                    span(" is a named storage location in memory that holds a value which can be modified during program execution.")
                                }
                        }
                }
            }
            .generateImages({ output("rotate.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun freeExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))

                    content()
                        .background(Colors.Grey.Lighten2)
                        .padding(25f)
                        .row {
                            spacing(25f)

                            fun addIcon(angle: Float) {
                                val itemSize = 100f

                                autoItem()
                                    .width(itemSize)
                                    .aspectRatio(1f)

                                    .offsetX(itemSize / 2)
                                    .offsetY(itemSize / 2)

                                    .rotate(angle)

                                    .offsetX(-itemSize / 2)
                                    .offsetY(-itemSize / 2)

                                    .svg(resource("compass.svg"))
                            }

                            addIcon(0f)
                            addIcon(30f)
                            addIcon(45f)
                            addIcon(80f)
                        }
                }
            }
            .generateImages({ output("rotate-free.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
