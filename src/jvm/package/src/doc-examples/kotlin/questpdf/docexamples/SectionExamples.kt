package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import org.junit.jupiter.api.Test

class SectionExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    size(PageSizes.A5.landscape())
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            val terms = arrayOf(
                                "Bit" to "The smallest unit of data in computing, representing either a 0 or a 1. Multiple bits are combined to form bytes, which are used to store larger data values.",
                                "Byte" to "A unit of digital information that consists of 8 bits. A byte is commonly used to store a single character of text, such as a letter or a number, in computer memory.",
                                "Binary" to "A number system that uses only two digits, 0 and 1, which are the fundamental building blocks of computer operations. Computers process and store all data in binary format, including text, images, and instructions.",
                                "Array" to "A data structure that stores a fixed-size sequence of elements, all of the same type, in a contiguous block of memory. Arrays allow quick access to elements using an index and are commonly used to manage collections of data.",
                            )

                            // title
                            item().extend().alignMiddle().alignCenter().text("Programming Glossary").fontSize(32f).bold()
                            item().pageBreak()

                            // table of contents
                            item().paddingBottom(25f).text("Table of Contents").fontSize(24f).bold().underline()

                            for (term in terms) {
                                item()
                                    .paddingBottom(10f)
                                    .sectionLink("term-$term")
                                    .text {
                                        span("Term ")
                                        span(term.first).bold()
                                        span(" on page ")
                                        beginPageNumberOfSection("term-$term")
                                    }
                            }

                            // content
                            for (term in terms) {
                                item().pageBreak()

                                item()
                                    .section("term-$term")
                                    .text {
                                        span(term.first).bold().fontColor(Colors.Blue.Darken2)
                                        span(" - ")
                                        span(term.second)
                                    }
                            }
                        }
                }
            }
            .generatePdf(output("sections.pdf"))
    }
}
