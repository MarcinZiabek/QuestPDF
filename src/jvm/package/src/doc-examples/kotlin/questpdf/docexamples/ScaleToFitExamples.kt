package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ScaleToFitExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."

                            for (i in 4..8) {
                                item()
                                    .shrink()
                                    .border(1f)
                                    .padding(15f)
                                    .width(i * 50f) // sizes from 200x100 to 450x175
                                    .height(i * 25f)
                                    .scaleToFit()
                                    .text(text)
                            }
                        }
                }
            }
            .generateImages({ output("scale-to-fit.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }
}
