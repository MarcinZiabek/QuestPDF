package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.AspectRatioOption
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class AspectRatioExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(300f)
                        .height(300f)
                        .aspectRatio(3f / 4f, AspectRatioOption.FitArea)
                        .background(Colors.Grey.Lighten2)
                        .alignCenter()
                        .alignMiddle()
                        .text("3:4 Content Area")
                }
            }
            .generateImages({ output("aspect-ratio.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
