package questpdf.docexamples.codepatterns

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class CodePatternExecutionOrderExample : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(400f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(25f)

                            item()
                                .border(1f)
                                .background(Colors.Blue.Lighten4)
                                .padding(15f)
                                .text("border → background → padding")

                            item()
                                .border(1f)
                                .padding(15f)
                                .background(Colors.Blue.Lighten4)
                                .text("border → padding → background")

                            item()
                                .background(Colors.Blue.Lighten4)
                                .padding(15f)
                                .border(1f)
                                .text("background → padding → border")

                            item()
                                .padding(15f)
                                .border(1f)
                                .background(Colors.Blue.Lighten4)
                                .text("padding → border → background")
                        }
                }
            }
            .generateImages({ output("code-pattern-execution-order.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
