package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class LayersExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    continuousSize(450f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            item().paddingBottom(15f).text("Proposed Business Card Design:").bold()

                            item()
                                .aspectRatio(4 / 3f)
                                .layers {
                                    layer().image(resource("card-background.jpg")).fitUnproportionally()

                                    primaryLayer()
                                        .offsetY(75f)
                                        .column {
                                            item()
                                                .alignCenter()
                                                .text("Horizon Ventures")
                                                .bold().fontSize(32f).fontColor(Colors.Blue.Darken2)

                                            item().alignCenter().text("Your journey begins here")
                                        }
                                }
                        }
                }
            }
            .generateImages({ output("layers.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
