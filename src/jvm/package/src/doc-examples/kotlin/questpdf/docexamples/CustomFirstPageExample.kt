package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import org.junit.jupiter.api.Test

class CustomFirstPageExample : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    margin(30f)
                    defaultTextStyle { fontSize(20f) }

                    header().column {
                        item().showOnce().background(Colors.Blue.Lighten2).height(80f)
                        item().skipOnce().background(Colors.Green.Lighten2).height(60f)
                    }

                    content().paddingVertical(20f).column {
                        spacing(20f)

                        repeat(20) {
                            item().background(Colors.Grey.Lighten3).height(40f)
                        }
                    }

                    footer().alignCenter().text {
                        currentPageNumber()
                        span(" / ")
                        totalPages()
                    }
                }
            }
            .generatePdf(output("example-custom-first-page.pdf"))
    }
}
