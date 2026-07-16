package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ShowEntireExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    size(500f, 500f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .decoration {
                            val terms = arrayOf(
                                "Function" to "A reusable block of code designed to perform a specific task. Functions take input parameters, process them, and return results, making code modular, readable, and maintainable. They are an essential component of all programming languages.",
                                "Recursion" to "A programming technique where a function calls itself in order to solve a problem by breaking it down into smaller, similar subproblems. Recursion is often used for complex algorithms, such as searching, sorting, and tree traversal.",
                                "Framework" to "A pre-built collection of code, tools, and best practices that provides a structured foundation for developing software. Frameworks simplify development by handling common functionalities, such as database access, user authentication, and UI rendering.",
                                "Package" to "A self-contained collection of code, typically consisting of functions, classes, and modules, that provides specific functionality. Packages help organize large projects and allow developers to reuse and distribute their code easily.",
                            )

                            before().text("Terms and their definitions:").fontSize(24f).bold().underline()

                            content().paddingTop(15f).column {
                                spacing(15f)

                                for (term in terms) {
                                    item()
                                        .showEntire()
                                        .text {
                                            span(term.first).bold().fontColor(Colors.Blue.Darken2)
                                            span(" - ${term.second}")
                                        }
                                }
                            }
                        }
                }
            }
            .generateImages({ index -> output("show-entire-with-$index.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
