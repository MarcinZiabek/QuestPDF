package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.DocumentMetadata
import com.questpdf.infrastructure.DocumentSettings
import com.questpdf.infrastructure.PDFA_Conformance
import com.questpdf.infrastructure.PDFUA_Conformance
import org.junit.jupiter.api.Test

class AccessibilityExamples : DocExample() {

    @Test
    fun minimalExample() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    margin(30f)

                    header()
                        .paddingBottom(15f)
                        .semanticHeader1()
                        .text("Accessibility Test Document")
                        .fontColor(Colors.Blue.Darken3)
                        .fontSize(24f)
                        .bold()

                    content()
                        .column {
                            spacing(20f)

                            item()
                                .semanticSection()
                                .column {
                                    item()
                                        .paddingBottom(10f)
                                        .semanticHeader2()
                                        .text("Section with text content")
                                        .fontColor(Colors.Blue.Darken1)
                                        .fontSize(16f)

                                    item()
                                        .text(Placeholders.paragraphs())
                                        .fontSize(12f)
                                        .paragraphSpacing(8f)
                                }

                            item()
                                .preventPageBreak()
                                .semanticSection()
                                .column {
                                    item()
                                        .paddingBottom(10f)
                                        .semanticHeader2()
                                        .text("Section with image")
                                        .fontColor(Colors.Blue.Darken1)
                                        .fontSize(16f)

                                    item()
                                        .width(250f)
                                        .semanticImage("Image showing a laptop")
                                        .image(resource("product.jpg"))
                                }
                        }
                }
            }
            .withMetadata(DocumentMetadata().apply {
                language = "en-US"
                title = "Accessibility Test"
                subject = "This document shows how easy it is to create accessible PDF documents with QuestPDF"
            })
            .withSettings(DocumentSettings().apply {
                pdfaConformance = PDFA_Conformance.PDFA_3A
                pdfuaConformance = PDFUA_Conformance.PDFUA_1
            })
            .generatePdf(output("accessibility-minimal-example.pdf"))
    }
}
