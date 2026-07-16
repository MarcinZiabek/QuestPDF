package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ZIndexExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(650f, 0f))
                    maxSize(PageSize(650f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .paddingVertical(15f)
                        .border(2f)
                        .row {
                            fun addPricingItem(container: IContainer, name: String, formattedPrice: String) {
                                container
                                    .padding(25f)
                                    .column {
                                        item().alignCenter().text(name).fontSize(24f).black()
                                        item().alignCenter().text(formattedPrice).fontSize(20f).semiBold()

                                        item().paddingHorizontal(-25f).paddingVertical(10f).lineHorizontal(1f)

                                        repeat(4) {
                                            item()
                                                .paddingTop(10f)
                                                .alignCenter()
                                                .text(Placeholders.label())
                                                .fontSize(16f)
                                                .light()
                                        }
                                    }
                            }

                            relativeItem()
                                .background(Colors.Grey.Lighten3)
                                .element { addPricingItem(this, "Community", "Free") }

                            relativeItem()
                                .zIndex(1) // -1 or 0 or 1
                                .padding(-15f)
                                .border(1f)
                                .background(Colors.Grey.Lighten1)
                                .paddingTop(15f)
                                .element { addPricingItem(this, "Professional", "$699") }

                            relativeItem()
                                .background(Colors.Grey.Lighten3)
                                .element { addPricingItem(this, "Enterprise", "$1999") }
                        }
                }
            }
            .generateImages({ output("zindex-positive.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
