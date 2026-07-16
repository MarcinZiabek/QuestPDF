package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class SkipOnceExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    size(500f, 500f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            val terms = arrayOf(
                                "Repository" to "A centralized storage location for source code and related files, typically managed using version control systems like Git. Repositories allow multiple developers to collaborate on projects, track changes, and maintain version history.",
                                "Version Control" to "A system that tracks changes to code over time, enabling developers to collaborate efficiently, revert to previous versions, and maintain a structured development workflow. Popular version control tools include Git, Mercurial, and Subversion.",
                                "Abstraction" to "A programming concept that hides complex implementation details and exposes only the necessary parts. Abstraction helps simplify code and allows developers to focus on high-level design rather than low-level implementation details.",
                                "Namespace" to "A container that groups related identifiers, such as variables, functions, and classes, to prevent naming conflicts in a program. Namespaces are commonly used in large projects to organize code efficiently.",
                            )

                            spacing(15f)

                            for (term in terms) {
                                item().decoration {
                                    before()
                                        .defaultTextStyle { fontSize(24f).bold().fontColor(Colors.Blue.Darken2) }
                                        .column {
                                            item().showOnce().text(term.first)

                                            item().skipOnce().text {
                                                span(term.first)
                                                span(" (continued)").light().italic()
                                            }
                                        }

                                    content().text(term.second)
                                }
                            }
                        }
                }
            }
            .generateImages({ index -> output("skip-once-$index.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
