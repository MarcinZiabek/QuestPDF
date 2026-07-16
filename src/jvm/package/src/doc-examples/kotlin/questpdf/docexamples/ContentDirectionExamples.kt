package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ContentDirectionExamples : DocExample() {

    @Test
    fun leftToRightExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(250f)
                        .contentFromLeftToRight()
                        .row {
                            spacing(5f)

                            autoItem().height(50f).width(50f).background(Colors.Red.Lighten1)
                            autoItem().height(50f).width(50f).background(Colors.Green.Lighten1)
                            autoItem().height(50f).width(75f).background(Colors.Blue.Lighten1)
                        }
                }
            }
            .generateImages({ output("content-direction-ltr.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun rightToLeftExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(250f)
                        .contentFromRightToLeft()
                        .row {
                            spacing(5f)

                            autoItem().height(50f).width(50f).background(Colors.Red.Lighten1)
                            autoItem().height(50f).width(50f).background(Colors.Green.Lighten1)
                            autoItem().height(50f).width(75f).background(Colors.Blue.Lighten1)
                        }
                }
            }
            .generateImages({ output("content-direction-rtl.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
