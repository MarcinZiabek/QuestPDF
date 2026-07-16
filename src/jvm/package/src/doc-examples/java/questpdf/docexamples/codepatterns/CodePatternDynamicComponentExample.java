package questpdf.docexamples.codepatterns;

import com.questpdf.elements.DynamicContext;
import com.questpdf.elements.IDynamicElement;
import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.DynamicComponentComposeResult;
import com.questpdf.infrastructure.IDynamicComponent;
import com.questpdf.infrastructure.Size;
import com.questpdf.infrastructure.TextStyle;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class CodePatternDynamicComponentExample extends DocExample {

    @Test
    public void dynamic() {
        var items = Stream.generate(OrderItem::new).limit(25).toList();

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA4());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(50f);

                    page.content()
                        .decoration(decoration -> {
                            decoration.before()
                                .paddingBottom(10f)
                                .text(text -> {
                                    text.defaultTextStyle(TextStyle.getDefault().bold().fontColor(Colors.Blue.getDarken2()));
                                    text.span("Page ");
                                    text.currentPageNumber();
                                    text.span(" of ");
                                    text.totalPages();
                                });

                            decoration.content()
                                .dynamic(new OrdersTableWithPageSubtotalsComponent(items));
                        });
                });
            })
            .generatePdf(output("code-pattern-dynamic-component-table-with-per-page-subtotals.pdf"));
    }

    public static class OrderItem {
        public final String itemName = Placeholders.label();

        // The Java counterpart of C# Placeholders.Random.Next(minValue, maxValue)
        // (System.Random is not bridged): a random integer within [minValue, maxValue).
        public final int price = ThreadLocalRandom.current().nextInt(1, 11) * 10;
        public final int count = ThreadLocalRandom.current().nextInt(1, 11);
    }

    public static class OrdersTableWithPageSubtotalsComponentState {
        public int shownItemsCount = 0;
    }

    public static class OrdersTableWithPageSubtotalsComponent implements IDynamicComponent {

        // The Java counterpart of the Kotlin Pair<OrderItem, IDynamicElement>.
        private record OrderItemRow(OrderItem item, IDynamicElement element) {}

        private final List<OrderItem> items;
        public OrdersTableWithPageSubtotalsComponentState state;

        public OrdersTableWithPageSubtotalsComponent(List<OrderItem> items) {
            this.items = items;

            this.state = new OrdersTableWithPageSubtotalsComponentState();
            this.state.shownItemsCount = 0;
        }

        @Override
        public DynamicComponentComposeResult compose(DynamicContext context) {
            var header = composeHeader(context);
            var sampleFooter = composeFooter(context, List.of());
            var decorationHeight = header.getSize().getHeight() + sampleFooter.getSize().getHeight();

            var rows = getItemsForPage(context, decorationHeight);
            var footer = composeFooter(context, rows.stream().map(OrderItemRow::item).toList());

            var content = context.createElement(container -> {
                container.shrink().decoration(decoration -> {
                    decoration.before().element(header);

                    decoration.content().column(column -> {
                        for (var row : rows)
                            column.item().element(row.element());
                    });

                    decoration.after().element(footer);
                });
            });

            var state = new OrdersTableWithPageSubtotalsComponentState();
            state.shownItemsCount = this.state.shownItemsCount + rows.size();
            this.state = state;

            var result = new DynamicComponentComposeResult();
            result.setContent(content);
            result.setHasMoreContent(this.state.shownItemsCount < items.size());
            return result;
        }

        private IDynamicElement composeHeader(DynamicContext context) {
            return context.createElement(element -> {
                element
                    .width(context.getAvailableSize().getWidth())
                    .borderBottom(1f)
                    .borderColor(Colors.Grey.getDarken2())
                    .padding(10f)
                    .defaultTextStyle(TextStyle.getDefault().semiBold())
                    .row(row -> {
                        row.constantItem(50f).text("#");
                        row.relativeItem().text("Item name");
                        row.constantItem(75f).alignRight().text("Count");
                        row.constantItem(75f).alignRight().text("Price");
                        row.constantItem(75f).alignRight().text("Total");
                    });
            });
        }

        private IDynamicElement composeFooter(DynamicContext context, List<OrderItem> items) {
            var total = items.stream().mapToInt(x -> x.count * x.price).sum();

            return context.createElement(element -> {
                element
                    .width(context.getAvailableSize().getWidth())
                    .padding(10f)
                    .alignRight()
                    .text("Subtotal: " + total + "$")
                    .bold();
            });
        }

        private List<OrderItemRow> getItemsForPage(DynamicContext context, float decorationHeight) {
            var rows = new ArrayList<OrderItemRow>();
            var totalHeight = decorationHeight;

            for (var index = state.shownItemsCount; index < items.size(); index++) {
                var item = items.get(index);
                var itemNumber = index + 1;

                var element = context.createElement(content -> {
                    content
                        .width(context.getAvailableSize().getWidth())
                        .borderBottom(1f)
                        .borderColor(Colors.Grey.getLighten2())
                        .padding(10f)
                        .row(row -> {
                            row.constantItem(50f).text(String.valueOf(itemNumber));
                            row.relativeItem().text(item.itemName);
                            row.constantItem(75f).alignRight().text(String.valueOf(item.count));
                            row.constantItem(75f).alignRight().text(item.price + "$");
                            row.constantItem(75f).alignRight().text(item.count * item.price + "$");
                        });
                });

                var elementHeight = element.getSize().getHeight();

                // it is important to use the Size.Epsilon constant to avoid floating point comparison issues
                if (totalHeight + elementHeight > context.getAvailableSize().getHeight() + Size.Epsilon)
                    break;

                totalHeight += elementHeight;
                rows.add(new OrderItemRow(item, element));
            }

            return rows;
        }
    }
}
