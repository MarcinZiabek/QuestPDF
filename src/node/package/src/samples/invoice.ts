import {
    Color,
    Colors,
    Document,
    DocumentMetadata,
    Fonts,
    IComponent,
    IContainer,
    ITableCellContainer,
    LicenseType,
    PageSizes,
    Settings,
    Unit,
} from '../index';

/**
 * A classic invoice: metadata header, seller/buyer address blocks (as a reusable
 * component), a line-item table with a footer row, totals, and page numbering.
 */

interface Item {
    name: string;
    quantity: number;
    unitPrice: number;
}

const items: Item[] = [
    { name: 'Wireless keyboard', quantity: 2, unitPrice: 89.99 },
    { name: 'USB-C dock', quantity: 1, unitPrice: 249.0 },
    { name: '27" monitor', quantity: 2, unitPrice: 429.5 },
    { name: 'HDMI cable 2m', quantity: 3, unitPrice: 12.9 },
    { name: 'Laptop stand', quantity: 1, unitPrice: 65.0 },
];

class AddressComponent implements IComponent {
    constructor(
        private readonly title: string,
        private readonly lines: string[],
    ) {}

    compose(container: IContainer): void {
        container.column((column) => {
            column.spacing(2);
            column.item().text(this.title).semiBold().fontSize(11);
            column.item().paddingBottom(4).lineHorizontal(1).lineColor(Colors.Grey.Lighten2);
            for (const line of this.lines)
                column.item().text(line);
        });
    }
}

function money(value: number): string {
    return '€' + value.toFixed(2);
}

function headerCellStyle(cell: ITableCellContainer): IContainer {
    return cell
        .background(Colors.Blue.Darken2)
        .padding(6)
        .defaultTextStyle((style) => style.fontColor(Colors.White));
}

function bodyCellStyle(cell: ITableCellContainer, background: Color): IContainer {
    return cell
        .background(background)
        .borderBottom(0.5)
        .borderColor(Colors.Grey.Lighten3)
        .padding(6);
}

export function runInvoiceSample(): Uint8Array {
    Settings.license = LicenseType.Community;

    const document = Document.create((documentContainer) => {
        documentContainer.page((page) => {
            page.size(PageSizes.A4);
            page.margin(36);
            page.pageColor(Colors.White);
            page.defaultTextStyle((style) => style.fontSize(10).fontFamily(Fonts.Lato).fontColor(Colors.Grey.Darken4));

            page.header().row((row) => {
                row.relativeItem().column((column) => {
                    column.item().text('Nordic Supplies Oy').fontSize(20).semiBold().fontColor(Colors.Blue.Darken2);
                    column.item().text('Invoice #2026-0714');
                    column.item().text('Issued 2026-07-13 — due 2026-08-12').fontColor(Colors.Grey.Darken1);
                });
                row.constantItem(90, Unit.Point)
                    .aspectRatio(1)
                    .background(Colors.Blue.Lighten4)
                    .alignCenter()
                    .alignMiddle()
                    .text('LOGO').fontSize(14).fontColor(Colors.Blue.Darken3);
            });

            page.content().paddingVertical(20).column((column) => {
                column.spacing(18);

                column.item().row((row) => {
                    row.spacing(24);
                    row.relativeItem().component(
                        new AddressComponent('From', ['Nordic Supplies Oy', 'Katariinankatu 3', '00170 Helsinki, Finland']));
                    row.relativeItem().component(
                        new AddressComponent('Bill to', ['Aurora Robotics AB', 'Storgatan 14', '114 55 Stockholm, Sweden']));
                });

                column.item().table((table) => {
                    table.columnsDefinition((columns) => {
                        columns.relativeColumn(3);
                        columns.relativeColumn();
                        columns.relativeColumn();
                        columns.relativeColumn();
                    });

                    table.header((header) => {
                        headerCellStyle(header.cell()).text('Item').semiBold();
                        headerCellStyle(header.cell()).text('Qty').semiBold();
                        headerCellStyle(header.cell()).text('Unit price').semiBold();
                        headerCellStyle(header.cell()).text('Total').semiBold();
                    });

                    items.forEach((item, index) => {
                        const zebra = index % 2 === 0 ? Colors.White : Colors.Grey.Lighten5;
                        bodyCellStyle(table.cell(), zebra).text(item.name);
                        bodyCellStyle(table.cell(), zebra).alignRight().text(`${item.quantity}`);
                        bodyCellStyle(table.cell(), zebra).alignRight().text(money(item.unitPrice));
                        bodyCellStyle(table.cell(), zebra).alignRight().text(money(item.quantity * item.unitPrice)).semiBold();
                    });
                });

                column.item().alignRight().column((totals) => {
                    totals.spacing(2);
                    const net = items.reduce((sum, item) => sum + item.quantity * item.unitPrice, 0);
                    totals.item().text((text) => {
                        text.span('Net total:  ');
                        text.span(money(net)).semiBold();
                    });
                    totals.item().text((text) => {
                        text.span('VAT 24%:  ');
                        text.span(money(net * 0.24)).semiBold();
                    });
                    totals.item().text((text) => {
                        text.span('Grand total:  ').fontSize(12);
                        text.span(money(net * 1.24)).fontSize(12).bold().fontColor(Colors.Blue.Darken2);
                    });
                });

                column.item().background(Colors.Grey.Lighten4).padding(10).column((terms) => {
                    terms.item().text('Payment terms').semiBold();
                    terms.item().text('Please transfer the amount to IBAN FI21 1234 5600 0007 85 within 30 days.');
                });
            });

            page.footer().alignCenter().text((text) => {
                text.span('Page ');
                text.currentPageNumber();
                text.span(' of ');
                text.totalPages();
            });
        });
    });

    const metadata = new DocumentMetadata();
    metadata.title = 'Invoice 2026-0714';
    metadata.author = 'Nordic Supplies Oy';

    return document.withMetadata(metadata).generatePdf();
}
