package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class DecorationExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(350f, 0f))
                    maxSize(PageSize(350f, 300f))
                    margin(25f)
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .background(Colors.Grey.Lighten3)
                        .padding(15f)
                        .decoration {
                            before()
                                .defaultTextStyle { bold() }
                                .column {
                                    item().showOnce().text("Customer Instructions:")
                                    item().skipOnce().text("Customer Instructions [continued]:")
                                }

                            content()
                                .paddingTop(10f)
                                .text("Please wrap the item in elegant gift paper and include a small blank card for a personal message. If possible, remove any price tags or invoices from the package. Make sure the wrapping is secure but easy to open without damaging the contents.")
                        }
                }
            }
            .generateImages({ index -> output("decoration-$index.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
