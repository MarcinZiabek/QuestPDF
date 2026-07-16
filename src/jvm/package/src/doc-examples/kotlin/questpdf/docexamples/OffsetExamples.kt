package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class OffsetExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .padding(50f)
                        .background(Colors.Blue.Lighten3)
                        .offsetX(25f)
                        .offsetY(25f)
                        .border(4f)
                        .borderColor(Colors.Blue.Darken2)
                        .padding(50f)
                        .text("Moved content")
                        .fontSize(25f)
                }
            }
            .generateImages({ output("offset.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
