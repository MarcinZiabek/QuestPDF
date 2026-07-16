package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.Color
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ColorsExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(175f)
                        .padding(20f)
                        .border(1f)
                        .borderColor(Color.from("#03A9F4"))
                        .background(Colors.LightBlue.Lighten5)
                        .padding(20f)
                        .text("Blue text")
                        .bold()
                        .fontColor(Colors.LightBlue.Darken4)
                        .underline()
                        .decorationWavy()
                        .decorationColor(Color.from(0xFF0000u))
                }
            }
            .generateImages({ output("colors.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
