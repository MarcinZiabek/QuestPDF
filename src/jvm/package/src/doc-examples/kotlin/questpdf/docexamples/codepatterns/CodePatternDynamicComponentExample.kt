package questpdf.docexamples.codepatterns

import com.questpdf.elements.DynamicContext
import com.questpdf.elements.IDynamicElement
import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.DynamicComponentComposeResult
import com.questpdf.infrastructure.IDynamicComponent
import com.questpdf.infrastructure.Size
import com.questpdf.infrastructure.TextStyle
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample
import kotlin.random.Random

class CodePatternDynamicComponentExample : DocExample() {

    @Test
    fun dynamic() {
        val items = (0 until 25).map { OrderItem() }

        Document
            .create {
                page {
                    size(PageSizes.A4)
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)

                    content()
                        .decoration {
                            before()
                                .paddingBottom(10f)
                                .text {
                                    defaultTextStyle(TextStyle.Default.bold().fontColor(Colors.Blue.Darken2))
                                    span("Page ")
                                    currentPageNumber()
                                    span(" of ")
                                    totalPages()
                                }

                            content()
                                .dynamic(OrdersTableWithPageSubtotalsComponent(items))
                        }
                }
            }
            .generatePdf(output("code-pattern-dynamic-component-table-with-per-page-subtotals.pdf"))
    }

    class OrderItem {
        val itemName: String = Placeholders.label()

        // The Kotlin counterpart of C# Placeholders.Random.Next(minValue, maxValue)
        // (System.Random is not bridged): a random integer within [minValue, maxValue).
        val price: Int = Random.nextInt(1, 11) * 10
        val count: Int = Random.nextInt(1, 11)
    }

    class OrdersTableWithPageSubtotalsComponentState {
        var shownItemsCount: Int = 0
    }

    class OrdersTableWithPageSubtotalsComponent(private val items: List<OrderItem>) : IDynamicComponent {
        var state: OrdersTableWithPageSubtotalsComponentState = OrdersTableWithPageSubtotalsComponentState().apply {
            shownItemsCount = 0
        }

        override fun compose(context: DynamicContext): DynamicComponentComposeResult {
            val header = composeHeader(context)
            val sampleFooter = composeFooter(context, emptyList())
            val decorationHeight = header.size.height + sampleFooter.size.height

            val rows = getItemsForPage(context, decorationHeight).toList()
            val footer = composeFooter(context, rows.map { it.first })

            val content = context.createElement {
                shrink().decoration {
                    before().element(header)

                    content().column {
                        for (row in rows)
                            item().element(row.second)
                    }

                    after().element(footer)
                }
            }

            state = OrdersTableWithPageSubtotalsComponentState().apply {
                shownItemsCount = state.shownItemsCount + rows.size
            }

            val result = DynamicComponentComposeResult()
            result.content = content
            result.hasMoreContent = state.shownItemsCount < items.size
            return result
        }

        private fun composeHeader(context: DynamicContext): IDynamicElement {
            return context.createElement {
                width(context.availableSize.width)
                    .borderBottom(1f)
                    .borderColor(Colors.Grey.Darken2)
                    .padding(10f)
                    .defaultTextStyle(TextStyle.Default.semiBold())
                    .row {
                        constantItem(50f).text("#")
                        relativeItem().text("Item name")
                        constantItem(75f).alignRight().text("Count")
                        constantItem(75f).alignRight().text("Price")
                        constantItem(75f).alignRight().text("Total")
                    }
            }
        }

        private fun composeFooter(context: DynamicContext, items: List<OrderItem>): IDynamicElement {
            val total = items.sumOf { it.count * it.price }

            return context.createElement {
                width(context.availableSize.width)
                    .padding(10f)
                    .alignRight()
                    .text("Subtotal: ${total}$")
                    .bold()
            }
        }

        private fun getItemsForPage(context: DynamicContext, decorationHeight: Float): Sequence<Pair<OrderItem, IDynamicElement>> = sequence {
            var totalHeight = decorationHeight

            for (index in state.shownItemsCount until items.size) {
                val item = items.elementAt(index)

                val element = context.createElement {
                    width(context.availableSize.width)
                        .borderBottom(1f)
                        .borderColor(Colors.Grey.Lighten2)
                        .padding(10f)
                        .row {
                            constantItem(50f).text((index + 1).toString())
                            relativeItem().text(item.itemName)
                            constantItem(75f).alignRight().text(item.count.toString())
                            constantItem(75f).alignRight().text("${item.price}$")
                            constantItem(75f).alignRight().text("${item.count * item.price}$")
                        }
                }

                val elementHeight = element.size.height

                // it is important to use the Size.Epsilon constant to avoid floating point comparison issues
                if (totalHeight + elementHeight > context.availableSize.height + Size.Epsilon)
                    break

                totalHeight += elementHeight
                yield(item to element)
            }
        }
    }
}
