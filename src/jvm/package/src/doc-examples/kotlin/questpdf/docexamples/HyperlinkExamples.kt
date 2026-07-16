package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import org.junit.jupiter.api.Test

class HyperlinkExamples : DocExample() {

    @Test
    fun elementExample() {
        Document
            .create {
                page {
                    continuousSize(400f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(25f)

                            item()
                                .text("Clicking the NuGet logo will redirect you to the NuGet website.")

                            item()
                                .width(150f)
                                .hyperlink("https://www.nuget.org/")
                                .svg(resource("nuget-logo.svg"))
                        }
                }
            }
            .generatePdf(output("hyperlink-element.pdf"))
    }

    @Test
    fun insideTextExample() {
        Document
            .create {
                page {
                    continuousSize(300f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("Click ")
                            hyperlink("here", "https://www.nuget.org/").underline().fontColor(Colors.Blue.Darken2)
                            span(" to visit the official NuGet website.")
                        }
                }
            }
            .generatePdf(output("hyperlink-text.pdf"))
    }
}
