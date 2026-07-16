package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class UnconstrainedExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(400f)
                        .height(350f)
                        .padding(25f)
                        .paddingLeft(50f)
                        .column {
                            item().width(300f).height(150f).background(Colors.Blue.Lighten3)

                            item()
                                .unconstrained()
                                .offsetX(-50f)
                                .offsetY(-50f)
                                .width(100f)
                                .height(100f)
                                .background(Colors.Blue.Darken2)

                            item().width(300f).height(150f).background(Colors.Blue.Lighten2)
                        }
                }
            }
            .generateImages({ output("unconstrained.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
