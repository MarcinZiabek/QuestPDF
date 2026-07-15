package samples

import com.questpdf.Settings
import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.Fonts
import com.questpdf.helpers.PageSizes
import com.questpdf.infrastructure.DocumentMetadata
import com.questpdf.infrastructure.IComponent
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.LicenseType

/**
 * A classic invoice: metadata header, seller/buyer address blocks (as a reusable
 * component), a line-item table with a footer row, totals, and page numbering.
 */
object InvoiceSample {

    private data class Item(val name: String, val quantity: Int, val unitPrice: Double)

    private val items = listOf(
        Item("Wireless keyboard", 2, 89.99),
        Item("USB-C dock", 1, 249.00),
        Item("27\" monitor", 2, 429.50),
        Item("HDMI cable 2m", 3, 12.90),
        Item("Laptop stand", 1, 65.00),
    )

    private class AddressComponent(
        private val title: String,
        private val lines: List<String>,
    ) : IComponent {
        override fun compose(container: IContainer) {
            container.column {
                spacing(2f)
                item().text(title).semiBold().fontSize(11f)
                item().paddingBottom(4f).lineHorizontal(1f).lineColor(Colors.Grey.Lighten2)
                for (line in lines)
                    item().text(line)
            }
        }
    }

    fun run(): ByteArray {
        Settings.license = LicenseType.Community

        val document = Document.create {
            page {
                size(PageSizes.A4)
                margin(36f)
                pageColor(Colors.White)
                defaultTextStyle { fontSize(10f).fontFamily(Fonts.Lato).fontColor(Colors.Grey.Darken4) }

                header().row {
                    relativeItem().column {
                        item().text("Nordic Supplies Oy").fontSize(20f).semiBold().fontColor(Colors.Blue.Darken2)
                        item().text("Invoice #2026-0714")
                        item().text("Issued 2026-07-13 — due 2026-08-12").fontColor(Colors.Grey.Darken1)
                    }
                    constantItem(90f, com.questpdf.infrastructure.Unit.Point)
                        .aspectRatio(1f)
                        .background(Colors.Blue.Lighten4)
                        .alignCenter()
                        .alignMiddle()
                        .text("LOGO").fontSize(14f).fontColor(Colors.Blue.Darken3)
                }

                content().paddingVertical(20f).column {
                    spacing(18f)

                    item().row {
                        spacing(24f)
                        relativeItem().component(
                            AddressComponent(
                                "From",
                                listOf("Nordic Supplies Oy", "Katariinankatu 3", "00170 Helsinki, Finland"),
                            ),
                        )
                        relativeItem().component(
                            AddressComponent(
                                "Bill to",
                                listOf("Aurora Robotics AB", "Storgatan 14", "114 55 Stockholm, Sweden"),
                            ),
                        )
                    }

                    item().table {
                        columnsDefinition {
                            relativeColumn(3f)
                            relativeColumn()
                            relativeColumn()
                            relativeColumn()
                        }

                        header {
                            cell().headerCellStyle().text("Item").semiBold()
                            cell().headerCellStyle().text("Qty").semiBold()
                            cell().headerCellStyle().text("Unit price").semiBold()
                            cell().headerCellStyle().text("Total").semiBold()
                        }

                        for ((index, item) in items.withIndex()) {
                            val zebra = if (index % 2 == 0) Colors.White else Colors.Grey.Lighten5
                            cell().bodyCellStyle(zebra).text(item.name)
                            cell().bodyCellStyle(zebra).alignRight().text("${item.quantity}")
                            cell().bodyCellStyle(zebra).alignRight().text(money(item.unitPrice))
                            cell().bodyCellStyle(zebra).alignRight().text(money(item.quantity * item.unitPrice)).semiBold()
                        }
                    }

                    item().alignRight().column {
                        spacing(2f)
                        val net = items.sumOf { it.quantity * it.unitPrice }
                        item().text {
                            span("Net total:  ")
                            span(money(net)).semiBold()
                        }
                        item().text {
                            span("VAT 24%:  ")
                            span(money(net * 0.24)).semiBold()
                        }
                        item().text {
                            span("Grand total:  ").fontSize(12f)
                            span(money(net * 1.24)).fontSize(12f).bold().fontColor(Colors.Blue.Darken2)
                        }
                    }

                    item().background(Colors.Grey.Lighten4).padding(10f).column {
                        item().text("Payment terms").semiBold()
                        item().text("Please transfer the amount to IBAN FI21 1234 5600 0007 85 within 30 days.")
                    }
                }

                footer().alignCenter().text {
                    span("Page ")
                    currentPageNumber()
                    span(" of ")
                    totalPages()
                }
            }
        }

        return document
            .withMetadata(
                DocumentMetadata().apply {
                    title = "Invoice 2026-0714"
                    author = "Nordic Supplies Oy"
                },
            )
            .generatePdf()
    }

    private fun money(value: Double) = "€" + String.format("%.2f", value)

    private fun com.questpdf.elements.table.ITableCellContainer.headerCellStyle(): IContainer =
        this.background(Colors.Blue.Darken2)
            .padding(6f)
            .defaultTextStyle { fontColor(Colors.White) }

    private fun com.questpdf.elements.table.ITableCellContainer.bodyCellStyle(background: com.questpdf.infrastructure.Color): IContainer =
        this.background(background)
            .borderBottom(0.5f)
            .borderColor(Colors.Grey.Lighten3)
            .padding(6f)
}

fun main() {
    println("InvoiceSample ran to completion: ${InvoiceSample.run().size} bytes")
}
