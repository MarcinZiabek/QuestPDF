package samples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.infrastructure.IContainer

/**
 * A table-heavy quarterly report: repeated per-section tables, explicit cell
 * placement with row/column spans, zebra striping, and paging helpers
 * (ensureSpace, showEntire, pageBreak).
 */
object ReportSample {

    private data class Metric(val name: String, val values: List<Double>)

    private data class Section(val title: String, val metrics: List<Metric>)

    private val sections = listOf(
        Section(
            "Hardware division",
            listOf(
                Metric("Revenue (k€)", listOf(1240.0, 1310.5, 1422.8, 1518.0)),
                Metric("Units shipped", listOf(830.0, 905.0, 1010.0, 1150.0)),
                Metric("Returns (%)", listOf(2.1, 1.8, 1.9, 1.6)),
            ),
        ),
        Section(
            "Software division",
            listOf(
                Metric("ARR (k€)", listOf(2210.0, 2380.0, 2544.9, 2718.3)),
                Metric("Active seats", listOf(10400.0, 11250.0, 12010.0, 13100.0)),
                Metric("Churn (%)", listOf(1.4, 1.3, 1.2, 1.1)),
            ),
        ),
        Section(
            "Services division",
            listOf(
                Metric("Billable hours", listOf(5400.0, 5620.0, 5810.0, 6005.0)),
                Metric("Utilization (%)", listOf(74.0, 78.0, 81.0, 79.0)),
            ),
        ),
    )

    fun run(): ByteArray {
        val document = Document.create {
            page {
                size(PageSizes.A4.landscape())
                margin(40f)
                defaultTextStyle { fontSize(9f) }

                header().column {
                    item().row {
                        relativeItem().text("Quarterly Performance Report — FY2026").fontSize(16f).semiBold()
                        constantItem(160f).alignRight().text("CONFIDENTIAL").fontColor(Colors.Red.Darken2).semiBold()
                    }
                    item().paddingTop(6f).lineHorizontal(2f).lineColor(Colors.Indigo.Darken2)
                }

                content().paddingVertical(14f).column {
                    spacing(22f)

                    for ((sectionIndex, section) in sections.withIndex()) {
                        if (sectionIndex > 0)
                            item().pageBreak()

                        item().ensureSpace(140f).showEntire().decoration {
                            before {
                                text(section.title).fontSize(12f).semiBold().fontColor(Colors.Indigo.Darken2)
                            }
                            content().paddingTop(6f).table {
                                columnsDefinition {
                                    constantColumn(46f)
                                    relativeColumn(2f)
                                    relativeColumn()
                                    relativeColumn()
                                    relativeColumn()
                                    relativeColumn()
                                    relativeColumn()
                                }

                                header {
                                    // A merged corner cell spanning both header rows.
                                    cell().rowSpan(2u).headerCell().alignMiddle().text("#")
                                    cell().rowSpan(2u).headerCell().alignMiddle().text("Metric")
                                    cell().columnSpan(4u).headerCell().alignCenter().text("Quarter")
                                    cell().rowSpan(2u).headerCell().alignMiddle().alignRight().text("FY total")

                                    cell().row(2u).column(3u).headerCell().alignRight().text("Q1")
                                    cell().row(2u).column(4u).headerCell().alignRight().text("Q2")
                                    cell().row(2u).column(5u).headerCell().alignRight().text("Q3")
                                    cell().row(2u).column(6u).headerCell().alignRight().text("Q4")
                                }

                                for ((index, metric) in section.metrics.withIndex()) {
                                    val zebra = if (index % 2 == 0) Colors.Grey.Lighten5 else Colors.White

                                    cell().bodyCell(zebra).text("${index + 1}")
                                    cell().bodyCell(zebra).text(metric.name)

                                    for (value in metric.values)
                                        cell().bodyCell(zebra).alignRight().text(format(value))

                                    cell().bodyCell(zebra).alignRight().text(format(metric.values.sum())).semiBold()
                                }
                            }
                        }
                    }

                    item().shrink().background(Colors.Amber.Lighten4).padding(8f).text {
                        span("Note: ").semiBold()
                        span("all figures are preliminary and unaudited.")
                    }
                }

                footer().row {
                    relativeItem().text("Generated by the reporting pipeline")
                    relativeItem().alignRight().text {
                        currentPageNumber()
                        span(" / ")
                        totalPages()
                    }
                }
            }
        }

        return document.generatePdf()
    }

    private fun format(value: Double) =
        if (value == value.toLong().toDouble()) "%,d".format(value.toLong()) else "%.1f".format(value)

    private fun com.questpdf.elements.table.ITableCellContainer.headerCell(): IContainer =
        background(Colors.Indigo.Darken2)
            .border(0.5f)
            .borderColor(Colors.Indigo.Darken4)
            .padding(5f)
            .defaultTextStyle { fontColor(Colors.White).semiBold() }

    private fun com.questpdf.elements.table.ITableCellContainer.bodyCell(zebra: com.questpdf.infrastructure.Color): IContainer =
        background(zebra)
            .borderBottom(0.5f)
            .borderColor(Colors.Grey.Lighten2)
            .padding(5f)
}

fun main() {
    println("ReportSample ran to completion: ${ReportSample.run().size} bytes")
}
