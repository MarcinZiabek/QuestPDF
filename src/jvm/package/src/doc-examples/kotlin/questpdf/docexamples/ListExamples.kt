package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ListExamples : DocExample() {

    @Test
    fun bulletpointExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(350f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            for (i in 1..7) {
                                item().row {
                                    constantItem(26f).image(resource("bulletpoint.png"))
                                    constantItem(5f)
                                    relativeItem().text(Placeholders.label())
                                }
                            }
                        }
                }
            }
            .generateImages({ output("list-unordered.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun orderedExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            for (i in 1..11) {
                                item().row {
                                    constantItem(35f).text("$i.")
                                    relativeItem().text(Placeholders.sentence())
                                }
                            }
                        }
                }
            }
            .generateImages({ output("list-ordered.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun nested() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            val nestingSize = 25f

                            spacing(10f)

                            item()
                                .text("Algorithm: Checking if a Number is Prime")
                                .fontSize(24f).fontColor(Colors.Blue.Darken2)

                            fun addListItem(nestingLevel: Int, bulletText: String, text: String) {
                                item().row {
                                    constantItem(nestingSize * nestingLevel)
                                    constantItem(nestingSize).text(bulletText)
                                    relativeItem().text(text)
                                }
                            }

                            addListItem(0, "1.", "Handle special cases")
                            addListItem(1, "a)", "If n is less than 2, return false (not prime).")
                            addListItem(1, "b)", "If n is 2, return true (prime).")

                            addListItem(0, "2.", "Check divisibility")
                            addListItem(1, "-", "Iterate through numbers from 2 to n - 1:")
                            addListItem(2, "-", "If n is divisible by any of these numbers, return false.")

                            addListItem(0, "3.", "Return true (if no divisors were found, n is prime).")
                        }
                }
            }
            .generateImages({ output("list-nested.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
