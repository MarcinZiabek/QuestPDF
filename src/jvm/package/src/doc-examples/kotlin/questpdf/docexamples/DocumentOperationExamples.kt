package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.fluent.DocumentOperation
import com.questpdf.helpers.Colors
import com.questpdf.infrastructure.Color
import com.questpdf.infrastructure.Unit
import org.junit.jupiter.api.Test
import java.io.File

class DocumentOperationExamples : DocExample() {

    @Test
    fun mergeFiles() {
        val prefix = "document-operation-merge"

        generateSampleDocument(output("$prefix-source-red.pdf"), Colors.Red.Lighten3, 2)
        generateSampleDocument(output("$prefix-source-green.pdf"), Colors.Green.Lighten3, 3)
        generateSampleDocument(output("$prefix-source-blue.pdf"), Colors.Blue.Lighten3, 5)

        DocumentOperation
            .loadFile(output("$prefix-source-red.pdf"))
            .mergeFile(output("$prefix-source-green.pdf"))
            .mergeFile(output("$prefix-source-blue.pdf"))
            .save(output("$prefix-result.pdf"))
    }

    @Test
    fun selectEvenPages() {
        val prefix = "document-operation-select-even-pages"

        generateSampleDocument(output("$prefix-source.pdf"), Colors.Indigo.Lighten3, 11)

        DocumentOperation
            .loadFile(output("$prefix-source.pdf"))
            .takePages("1-z:even")
            .save(output("$prefix-result.pdf"))
    }

    @Test
    fun encrypt() {
        val prefix = "document-operation-encrypt"

        generateSampleDocument(output("$prefix-source.pdf"), Colors.Orange.Lighten3, 7)

        DocumentOperation
            .loadFile(output("$prefix-source.pdf"))
            .encrypt(DocumentOperation.Encryption256Bit().apply {
                userPassword = "user-password"
                ownerPassword = "owner-password"
                allowContentExtraction = false
                allowPrinting = false
            })
            .save(output("$prefix-result.pdf"))
    }

    @Test
    fun addAttachment() {
        val prefix = "document-operation-add-attachment"

        generateSampleDocument(output("$prefix-source.pdf"), Colors.Cyan.Lighten3, 7)
        File(output("$prefix-content.txt")).writeText("Hello, World!")

        DocumentOperation
            .loadFile(output("$prefix-source.pdf"))
            .addAttachment(DocumentOperation.DocumentAttachment().apply {
                filePath = output("$prefix-content.txt")
                attachmentName = "Attached message"
            })
            .save(output("$prefix-result.pdf"))
    }

    @Test
    fun overlay() {
        val prefix = "document-operation-overlay"

        generateSampleDocument(output("$prefix-source.pdf"), Colors.Cyan.Lighten3, 7)

        Document
            .create {
                page {
                    margin(1f, Unit.Centimetre)
                    pageColor(Colors.Transparent)

                    content().column {
                        repeat(6) {
                            item().pageBreak()
                        }
                    }

                    footer().alignCenter().text {
                        defaultTextStyle { fontSize(24f).bold().fontColor(Colors.White) }
                        span("Page ")
                        currentPageNumber()
                        span(" of ")
                        totalPages()
                    }
                }
            }
            .generatePdf(output("$prefix-content.pdf"))

        DocumentOperation
            .loadFile(output("$prefix-source.pdf"))
            .overlayFile(DocumentOperation.LayerConfiguration().apply {
                filePath = output("$prefix-content.pdf")
            })
            .save(output("$prefix-result.pdf"))
    }

    private fun generateSampleDocument(fileName: String, pageColor: Color, numberOfPages: Int) {
        Document
            .create {
                page {
                    margin(1f, Unit.Centimetre)
                    pageColor(pageColor)

                    content().column {
                        for (pageNumber in 1..numberOfPages) {
                            item()
                                .extend()
                                .alignCenter().alignMiddle()
                                .text("$pageNumber")
                                .fontSize(256f)
                                .fontColor(Colors.White)
                                .bold()

                            if (pageNumber != numberOfPages)
                                item().pageBreak()
                        }
                    }
                }
            }
            .generatePdf(fileName)
    }
}
