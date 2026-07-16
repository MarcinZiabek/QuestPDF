package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class RepeatExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(600f, 0f))
                    maxSize(PageSize(600f, 600f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .decoration {
                            val terms = arrayOf(
                                "Algorithm" to "A precise set of instructions that defines a process for solving a specific problem or performing a computation. Algorithms are the foundation of programming and are used to optimize tasks efficiently.",
                                "Bug" to "An error, flaw, or unintended behavior in a program that causes it to produce incorrect or unexpected results. Debugging is the process of identifying, analyzing, and fixing these issues to improve software reliability.",
                                "Variable" to "A named storage location in memory that holds a value, which can be modified during program execution. Variables make code dynamic and flexible by allowing data manipulation and retrieval.",
                                "Compilation" to "The process of transforming human-readable source code into machine code (binary instructions) that a computer can execute. This process is performed by a compiler and often includes syntax checks, optimizations, and linking dependencies.",
                            )

                            before().text("Terms and their definitions:").bold()

                            content().paddingTop(15f).column {
                                for (term in terms) {
                                    item().row {
                                        relativeItem(2f)
                                            .border(1f)
                                            .background(Colors.Grey.Lighten3)
                                            .padding(15f)
                                            .repeat()
                                            .text(term.first)

                                        relativeItem(3f)
                                            .border(1f)
                                            .padding(15f)
                                            .text(term.second)
                                    }
                                }
                            }
                        }
                }
            }
            .generateImages({ index -> output("repeat-with-$index.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
