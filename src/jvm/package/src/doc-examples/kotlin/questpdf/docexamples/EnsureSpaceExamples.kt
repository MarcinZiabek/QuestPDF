package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import org.junit.jupiter.api.Test

class EnsureSpaceExamples : DocExample() {

    @Test
    fun enabledExample() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(30f)

                    content()
                        .column {
                            item().height(400f).background(Colors.Grey.Lighten3)
                            item().height(30f)

                            item()
                                .ensureSpace(100f)
                                .table {
                                    columnsDefinition {
                                        constantColumn(40f)
                                        relativeColumn()
                                    }

                                    for (i in 1..12) {
                                        cell().text("$i.")
                                        cell().showEntire().text(Placeholders.sentence())
                                    }
                                }
                        }
                }
            }
            .generatePdf(output("ensure-space-enabled.pdf"))
    }

    @Test
    fun disabledExample() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(30f)

                    content()
                        .column {
                            item().height(400f).background(Colors.Grey.Lighten3)
                            item().height(30f)

                            item()
                                .table {
                                    columnsDefinition {
                                        constantColumn(40f)
                                        relativeColumn()
                                    }

                                    for (i in 1..12) {
                                        cell().text("$i.")
                                        cell().text(Placeholders.sentence())
                                    }
                                }
                        }
                }
            }
            .generatePdf(output("ensure-space-disabled.pdf"))
    }
}
