package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.infrastructure.IComponent
import com.questpdf.infrastructure.IContainer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LazyExamples : DocExample() {

    private class SimpleComponent(private val start: Int, private val end: Int) : IComponent {
        override fun compose(container: IContainer) {
            container.decoration {
                before()
                    .text("Numbers from $start to $end")
                    .fontSize(20f).bold().fontColor(Colors.Blue.Darken2)

                content().column {
                    for (i in start..end)
                        item().text("Number $i").fontSize(10f)
                }
            }
        }
    }

    @Test
    @Disabled("This test is for manual testing only.")
    fun disabled() {
        Document
            .create {
                page {
                    margin(10f)

                    content().column {
                        val sectionSize = 1000

                        for (i in 0 until 1000) {
                            item().component(SimpleComponent(
                                start = i * sectionSize,
                                end = i * sectionSize + sectionSize - 1
                            ))
                        }
                    }
                }
            }
            .generatePdf(output("lazy-disabled.pdf"))
    }

    @Test
    @Disabled("This test is for manual testing only.")
    fun enabled() {
        Document
            .create {
                page {
                    margin(10f)
                    content().column {
                        val sectionSize = 1000

                        for (i in 0 until 1000) {
                            val start = i * sectionSize
                            val end = start + sectionSize - 1

                            item().lazy {
                                component(SimpleComponent(
                                    start = start,
                                    end = end
                                ))
                            }
                        }
                    }
                }
            }
            .generatePdf(output("lazy-enabled.pdf"))
    }

    @Test
    @Disabled("This test is for manual testing only.")
    fun enabledWithCache() {
        Document
            .create {
                page {
                    margin(10f)

                    content().column {
                        val sectionSize = 1000

                        for (i in 0 until 1000) {
                            val start = i * sectionSize
                            val end = start + sectionSize - 1

                            item().lazyWithCache {
                                component(SimpleComponent(
                                    start = start,
                                    end = end
                                ))
                            }
                        }
                    }
                }
            }
            .generatePdf(output("lazy-enabled-with-cache.pdf"))
    }
}
