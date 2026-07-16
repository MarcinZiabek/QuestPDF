package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class DebugAreaExamples : DocExample() {

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
                        .height(250f)
                        .padding(25f)
                        .debugArea("Grid example", Colors.Blue.Medium)
                        .grid {
                            columns(3)
                            spacing(5f)

                            repeat(8) {
                                item().height(50f).placeholder()
                            }
                        }
                }
            }
            .generateImages({ output("debug-area.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 216
            })
    }
}
