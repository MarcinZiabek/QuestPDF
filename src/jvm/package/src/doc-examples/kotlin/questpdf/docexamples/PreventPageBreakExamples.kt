package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import org.junit.jupiter.api.Test

class PreventPageBreakExamples : DocExample() {

    @Test
    fun enabledExample() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(30f)

                    content()
                        .column {
                            item().height(400f).background(Colors.Grey.Lighten3)
                            item().height(30f)

                            item()
                                .preventPageBreak()
                                .text {
                                    paragraphSpacing(15f)

                                    span("Optimizing Content Placement").bold().fontColor(Colors.Blue.Darken2).fontSize(24f)
                                    span("\n")
                                    span("By carefully determining where to place a page break, you can avoid awkward text separations and maintain readability. Thoughtful formatting improves the overall user experience, making complex topics easier to digest.")
                                }
                        }
                }
            }
            .generatePdf(output("prevent-page-break-enabled.pdf"))
    }

    @Test
    fun disabledExample() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(30f)

                    content()
                        .column {
                            item().height(400f).background(Colors.Grey.Lighten3)
                            item().height(30f)

                            item()
                                .text {
                                    paragraphSpacing(15f)

                                    span("Optimizing Content Placement").bold().fontColor(Colors.Blue.Darken2).fontSize(24f)
                                    span("\n")
                                    span("By carefully determining where to place a page break, you can avoid awkward text separations and maintain readability. Thoughtful formatting improves the overall user experience, making complex topics easier to digest.")
                                }
                        }
                }
            }
            .generatePdf(output("prevent-page-break-disabled.pdf"))
    }
}
