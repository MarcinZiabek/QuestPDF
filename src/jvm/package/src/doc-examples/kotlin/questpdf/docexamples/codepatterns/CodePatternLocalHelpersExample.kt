package questpdf.docexamples.codepatterns

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class CodePatternLocalHelpersExample : DocExample() {

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
                            spacing(15f)

                            item().text("Business details:").fontSize(24f).bold().fontColor(Colors.Blue.Darken2)

                            fun addContactItem(iconPath: String, label: String) {
                                item().row {
                                    constantItem(32f).aspectRatio(1f).svg(iconPath)
                                    constantItem(15f)
                                    autoItem().alignMiddle().text(label)
                                }
                            }

                            addContactItem(resource("Icons/phone.svg"), Placeholders.phoneNumber())
                            addContactItem(resource("Icons/email.svg"), Placeholders.email())
                            addContactItem(resource("Icons/web.svg"), Placeholders.webpageUrl())
                        }
                }
            }
            .generateImages({ output("code-pattern-local-helpers.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
