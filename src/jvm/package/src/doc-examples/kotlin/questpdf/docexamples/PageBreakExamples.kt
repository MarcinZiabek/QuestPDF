package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.infrastructure.IContainer
import org.junit.jupiter.api.Test

class PageBreakExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    size(300f, 450f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .paddingTop(15f)
                        .column {
                            val terms = arrayOf(
                                "Garbage Collection" to "An automatic memory management feature in many programming languages that identifies and removes unused objects to free up memory, preventing memory leaks.",
                                "Constructor" to "A special method in object-oriented programming that is automatically called when an object is created. It initializes the object's properties and sets up any necessary resources.",
                                "Dependency" to "A software component or external library that a program relies on to function correctly. Dependencies can include third-party modules, frameworks, or system-level packages that provide additional functionality without requiring developers to write everything from scratch.",
                            )

                            item()
                                .extend()
                                .alignCenter().alignMiddle()
                                .text("Programming dictionary").fontSize(24f).bold()

                            fun generatePage(container: IContainer, term: String, definition: String) {
                                container.text {
                                    span(term).bold().fontColor(Colors.Blue.Darken2)
                                    span(" - $definition")
                                }
                            }

                            for (term in terms) {
                                item().pageBreak()
                                item().element { generatePage(this, term.first, term.second) }
                            }
                        }
                }
            }
            .generatePdf(output("page-break.pdf"))
    }
}
