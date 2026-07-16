package questpdf.docexamples.codepatterns

import com.questpdf.elements.DynamicContext
import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.infrastructure.DynamicComponentComposeResult
import com.questpdf.infrastructure.IDynamicComponent
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample
import java.io.File
import kotlin.random.Random

class CodePatternComponentProgressbarComponentExample : DocExample() {

    @Test
    fun example() {
        val content = generateReport()
        File(output("code-pattern-dynamic-component-progressbar.pdf")).writeBytes(content)
    }

    fun generateReport(): ByteArray {
        return Document
            .create {
                page {
                    size(PageSizes.A4)
                    margin(50f)
                    defaultTextStyle { fontSize(20f) }

                    header().column {
                        item()
                            .text("MyBrick Set")
                            .fontSize(48f).fontColor(Colors.Blue.Darken2).bold()

                        item()
                            .text("Building Instruction")
                            .fontSize(24f)

                        item().height(15f)

                        item().dynamic(PageProgressbarComponent())
                    }

                    content().paddingVertical(25f).column {
                        spacing(25f)

                        for (i in 1..30) {
                            item()
                                .background(Colors.Grey.Lighten3)
                                .height((Random.nextInt(4, 8) * 25).toFloat())
                                .alignCenter()
                                .alignMiddle()
                                .text("Step $i")
                        }
                    }

                    footer().dynamic(PageNumberSideComponent())
                }
            }
            .generatePdf()
    }

    class PageProgressbarComponent : IDynamicComponent {
        override fun compose(context: DynamicContext): DynamicComponentComposeResult {
            val content = context.createElement {
                val width = context.availableSize.width * context.pageNumber / context.totalPages

                background(Colors.Blue.Lighten3)
                    .height(5f)
                    .width(width)
                    .background(Colors.Blue.Darken2)
            }

            return DynamicComponentComposeResult().apply {
                this.content = content
                hasMoreContent = false
            }
        }
    }

    class PageNumberSideComponent : IDynamicComponent {
        override fun compose(context: DynamicContext): DynamicComponentComposeResult {
            val content = context.createElement {
                // The chainable Element(Func<IContainer, IContainer>) overload is not bridged;
                // the conditional alignment is applied inline instead.
                val aligned = if (context.pageNumber % 2 == 0) alignRight() else alignLeft()

                aligned
                    .text {
                        span("Page ")
                        currentPageNumber()
                    }
            }

            return DynamicComponentComposeResult().apply {
                this.content = content
                hasMoreContent = false
            }
        }
    }
}
