package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class FlipExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(350f, 0f))
                    maxSize(PageSize(350f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(15f)

                            item()
                                .text("Read the message below by putting a mirror on the right side of the screen.")

                            item()
                                .alignLeft()
                                .background(Colors.Red.Lighten5)
                                .padding(10f)
                                .flipHorizontal()
                                .text("This is a secret message.")
                                .fontColor(Colors.Red.Darken2)
                        }
                }
            }
            .generateImages({ output("flip.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
