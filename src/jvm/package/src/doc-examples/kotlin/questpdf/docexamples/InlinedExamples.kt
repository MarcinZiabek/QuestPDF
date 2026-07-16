package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import kotlin.random.Random

class InlinedExamples : DocExample() {

    @Test
    fun simpleExample() {
        Document
            .create {
                page {
                    continuousSize(450f)

                    content()
                        .background(Colors.Grey.Lighten3)
                        .padding(25f)
                        .border(1f)
                        .background(Colors.White)
                        .inlined {
                            spacing(25f)
                            baselineMiddle()
                            alignCenter()

                            repeat(15) {
                                item().element(::randomBlock)
                            }
                        }
                }
            }
            .generateImages({ output("inlined.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun spacingExample() {
        Document
            .create {
                page {
                    continuousSize(450f)

                    content()
                        .background(Colors.Grey.Lighten3)
                        .padding(25f)
                        .border(1f)
                        .background(Colors.White)
                        .inlined {
                            verticalSpacing(15f)
                            horizontalSpacing(30f)

                            repeat(10) {
                                item().element(::randomBlock)
                            }
                        }
                }
            }
            .generateImages({ output("inlined-spacing.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    private fun randomBlock(container: IContainer) {
        container
            .width(Random.nextInt(1, 4) * 25f)
            .height(Random.nextInt(1, 4) * 25f)
            .border(1f)
            .borderColor(Colors.Grey.Darken2)
            .background(Placeholders.backgroundColor())
    }
}
