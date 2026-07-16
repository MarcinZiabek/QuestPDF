package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ScaleExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(350f)
                        .padding(25f)
                        .column {
                            spacing(10f)

                            val scales = arrayOf(0.75f, 1f, 1.25f, 1.5f)

                            for (scale in scales) {
                                item()
                                    .background(Colors.Grey.Lighten3)
                                    .scale(scale)
                                    .padding(10f)
                                    // removeSuffix matches the .NET float-to-string formatting ("1", not "1.0")
                                    .text("Content scale: ${scale.toString().removeSuffix(".0")}")
                                    .fontSize(20f)
                            }
                        }
                }
            }
            .generateImages({ output("scale.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
