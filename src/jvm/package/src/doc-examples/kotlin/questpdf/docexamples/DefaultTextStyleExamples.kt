package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class DefaultTextStyleExamples : DocExample() {

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
                        .padding(25f)
                        .defaultTextStyle { bold().underline() }
                        .column {
                            spacing(10f)

                            item().text("Inherited bold and underline")
                            item().text("Disabled underline, inherited bold and adjusted font color").underline(false).fontColor(Colors.Green.Darken2)

                            item()
                                .defaultTextStyle { decorationWavy().fontColor(Colors.LightBlue.Darken3) }
                                .text("Changed underline type and adjusted font color")
                        }
                }
            }
            .generateImages({ output("default-text-style.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
