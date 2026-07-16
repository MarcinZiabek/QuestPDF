package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class PaddingExamples : DocExample() {

    @Test
    fun simpleExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(250f)
                        .paddingVertical(10f)
                        .paddingLeft(20f)
                        .paddingRight(40f)
                        .background(Colors.Grey.Lighten2)
                        .text("Sample text")
                }
            }
            .generateImages({ output("padding-simple.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun negativeExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(250f)
                        .padding(50f)
                        .background(Colors.Grey.Lighten2)
                        .paddingHorizontal(-25f)
                        .text("Sample text with negative padding")
                }
            }
            .generateImages({ output("padding-negative.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
