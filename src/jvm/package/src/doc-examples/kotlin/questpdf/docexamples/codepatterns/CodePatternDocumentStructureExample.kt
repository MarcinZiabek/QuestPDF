package questpdf.docexamples.codepatterns

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.IContainer
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample
import java.io.File

class CodePatternDocumentStructureExample : DocExample() {

    @Test
    fun example() {
        val content = generateReport()
        File(output("code-pattern-document-structure.pdf")).writeBytes(content)
    }

    fun generateReport(): ByteArray {
        return Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .paddingBottom(15f)
                        .column {
                            item().element(::reportTitle)
                            item().pageBreak()
                            item().element(::redSection)
                            item().pageBreak()
                            item().element(::greenSection)
                            item().pageBreak()
                            item().element(::blueSection)
                        }

                    footer().alignCenter().text { currentPageNumber() }
                }
            }
            .generatePdf()
    }

    private fun reportTitle(container: IContainer) {
        container.extend()
            .alignCenter()
            .alignMiddle()
            .text("Multi-section report")
            .fontSize(48f)
            .bold()
    }

    private fun redSection(container: IContainer) {
        container.grid {
            columns(3)
            spacing(15f)

            item(3).text("Red section")
                .fontColor(Colors.Red.Darken2).fontSize(32f).bold()

            item(3).text(Placeholders.paragraph()).light()

            for (i in 0 until 6)
                item().aspectRatio(4 / 3f).background(Colors.Red.Lighten4)
        }
    }

    private fun greenSection(container: IContainer) {
        container.grid {
            columns(3)
            spacing(15f)

            item(3).text("Green section")
                .fontColor(Colors.Green.Darken2).fontSize(32f).bold()

            item(3).text(Placeholders.paragraph()).light()

            for (i in 0 until 12)
                item().aspectRatio(4 / 3f).background(Colors.Green.Lighten4)
        }
    }

    private fun blueSection(container: IContainer) {
        container.grid {
            columns(3)
            spacing(15f)

            item(3).text("Blue section")
                .fontColor(Colors.Blue.Darken2).fontSize(32f).bold()

            item(3).text(Placeholders.paragraph()).light()

            for (i in 0 until 18)
                item().aspectRatio(4 / 3f).background(Colors.Blue.Lighten4)
        }
    }
}
