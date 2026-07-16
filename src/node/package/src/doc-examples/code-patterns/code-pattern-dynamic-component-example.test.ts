// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternDynamicComponentExample.cs.
import { test } from 'node:test';
import { Colors, Document, DynamicComponentComposeResult, DynamicContext, IDynamicComponent, IDynamicElement, PageSizes, Placeholders, Size, TextStyle } from '../../index';
import { output } from '../doc-example';

test('CodePatternDynamicComponentExample.Dynamic', () => {
    const items = Array.from({ length: 25 }, () => new OrderItem());

    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A4);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);

                page.content()
                    .decoration((decoration) => {
                        decoration
                            .before()
                            .paddingBottom(10)
                            .text((text) => {
                                text.defaultTextStyle(TextStyle.Default.bold().fontColor(Colors.Blue.Darken2));
                                text.span('Page ');
                                text.currentPageNumber();
                                text.span(' of ');
                                text.totalPages();
                            });

                        decoration
                            .content()
                            .dynamic(new OrdersTableWithPageSubtotalsComponent(items));
                    });
            });
        })
        .generatePdf(output('code-pattern-dynamic-component-table-with-per-page-subtotals.pdf'));
});

/** The TypeScript counterpart of C# Placeholders.Random.Next(minValue, maxValue) (System.Random is not bridged): a random integer within [minValue, maxValue). */
function randomNext(minValue: number, maxValue: number): number {
    return Math.floor(Math.random() * (maxValue - minValue)) + minValue;
}

class OrderItem {
    itemName: string = Placeholders.label();
    price: number = randomNext(1, 11) * 10;
    count: number = randomNext(1, 11);
}

class OrdersTableWithPageSubtotalsComponentState {
    shownItemsCount: number = 0;
}

class OrdersTableWithPageSubtotalsComponent implements IDynamicComponent {
    private readonly items: OrderItem[];
    state: OrdersTableWithPageSubtotalsComponentState;

    constructor(items: OrderItem[]) {
        this.items = items;

        this.state = new OrdersTableWithPageSubtotalsComponentState();
        this.state.shownItemsCount = 0;
    }

    compose(context: DynamicContext): DynamicComponentComposeResult {
        const header = this.composeHeader(context);
        const sampleFooter = this.composeFooter(context, []);
        const decorationHeight = header.size.height + sampleFooter.size.height;

        const rows = [...this.getItemsForPage(context, decorationHeight)];
        const footer = this.composeFooter(context, rows.map((x) => x.item));

        const content = context.createElement((container) => {
            container.shrink().decoration((decoration) => {
                decoration.before().element(header);

                decoration.content().column((column) => {
                    for (const row of rows)
                        column.item().element(row.element);
                });

                decoration.after().element(footer);
            });
        });

        const state = new OrdersTableWithPageSubtotalsComponentState();
        state.shownItemsCount = this.state.shownItemsCount + rows.length;
        this.state = state;

        const result = new DynamicComponentComposeResult();
        result.content = content;
        result.hasMoreContent = this.state.shownItemsCount < this.items.length;
        return result;
    }

    private composeHeader(context: DynamicContext): IDynamicElement {
        return context.createElement((element) => {
            element
                .width(context.availableSize.width)
                .borderBottom(1)
                .borderColor(Colors.Grey.Darken2)
                .padding(10)
                .defaultTextStyle(TextStyle.Default.semiBold())
                .row((row) => {
                    row.constantItem(50).text('#');
                    row.relativeItem().text('Item name');
                    row.constantItem(75).alignRight().text('Count');
                    row.constantItem(75).alignRight().text('Price');
                    row.constantItem(75).alignRight().text('Total');
                });
        });
    }

    private composeFooter(context: DynamicContext, items: OrderItem[]): IDynamicElement {
        const total = items.reduce((sum, x) => sum + x.count * x.price, 0);

        return context.createElement((element) => {
            element
                .width(context.availableSize.width)
                .padding(10)
                .alignRight()
                .text(`Subtotal: ${total}$`)
                .bold();
        });
    }

    private *getItemsForPage(context: DynamicContext, decorationHeight: number): Generator<{ item: OrderItem; element: IDynamicElement }> {
        let totalHeight = decorationHeight;

        for (let index = this.state.shownItemsCount; index < this.items.length; index++) {
            const item = this.items[index];

            const element = context.createElement((content) => {
                content
                    .width(context.availableSize.width)
                    .borderBottom(1)
                    .borderColor(Colors.Grey.Lighten2)
                    .padding(10)
                    .row((row) => {
                        row.constantItem(50).text((index + 1).toString());
                        row.relativeItem().text(item.itemName);
                        row.constantItem(75).alignRight().text(item.count.toString());
                        row.constantItem(75).alignRight().text(`${item.price}$`);
                        row.constantItem(75).alignRight().text(`${item.count * item.price}$`);
                    });
            });

            const elementHeight = element.size.height;

            // it is important to use the Size.Epsilon constant to avoid floating point comparison issues
            if (totalHeight + elementHeight > context.availableSize.height + Size.Epsilon)
                break;

            totalHeight += elementHeight;
            yield { item, element };
        }
    }
}
