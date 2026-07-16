package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.infrastructure.TextStyle
import com.questpdf.infrastructure.Unit
import org.junit.jupiter.api.Test

class MergingDocumentsExamples : DocExample() {

    @Test
    fun useOriginalPageNumbersExample() {
        Document
            .merge(
                generateReport("Short Document 1", 5),
                generateReport("Medium Document 2", 10),
                generateReport("Long Document 3", 15))
            .useOriginalPageNumbers()
            .generatePdf(output("merged.pdf"))
    }

    @Test
    fun useContinuousPageNumbersExample() {
        Document
            .merge(
                generateReport("Short Document 1", 5),
                generateReport("Medium Document 2", 10),
                generateReport("Long Document 3", 15))
            .useContinuousPageNumbers()
            .generatePdf(output("merged.pdf"))
    }

    private fun generateReport(title: String, itemsCount: Int): Document {
        return Document.create {
            page {
                size(PageSizes.A5)
                margin(0.5f, Unit.Inch)

                header()
                    .text(title)
                    .bold()
                    .fontSize(24f)
                    .fontColor(Colors.Blue.Accent2)

                content()
                    .paddingVertical(20f)
                    .column {
                        spacing(10f)

                        for (i in 0 until itemsCount) {
                            item()
                                .width(200f)
                                .height(50f)
                                .background(Colors.Grey.Lighten3)
                                .alignMiddle()
                                .alignCenter()
                                .text("Item $i")
                                .fontSize(16f)
                        }
                    }

                footer()
                    .alignCenter()
                    .paddingVertical(20f)
                    .text {
                        defaultTextStyle(TextStyle.Default.fontSize(16f))

                        currentPageNumber()
                        span(" / ")
                        totalPages()
                    }
            }
        }
    }
}
