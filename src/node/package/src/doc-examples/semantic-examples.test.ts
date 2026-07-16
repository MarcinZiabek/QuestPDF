// Port of src/dotnet/library/QuestPDF.DocumentationExamples/SemanticExamples.cs.
import * as fs from 'node:fs';
import { test } from 'node:test';
import { Color, Colors, Document, DocumentMetadata, IContainer, PageSize, PageSizes, Settings } from '../index';
import { resource } from './doc-example';

test('SemanticExamples.HeaderAndFooter', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 250));
                page.defaultTextStyle((style) => style.fontSize(16));
                page.margin(25);

                page.content()
                    .border(1)
                    .borderColor(Colors.Grey.Lighten1)
                    .semanticTable()
                    .table((table) => {
                        const pageSizes: { name: string, width: number, height: number }[] = [
                            { name: 'Letter (ANSI A)', width: 8.5, height: 11 },
                            { name: 'Legal', width: 8.5, height: 14 },
                            { name: 'Ledger (ANSI B)', width: 11, height: 17 },
                            { name: 'Tabloid (ANSI B)', width: 17, height: 11 },
                            { name: 'ANSI C', width: 22, height: 17 },
                            { name: 'ANSI D', width: 34, height: 22 },
                            { name: 'ANSI E', width: 44, height: 34 },
                        ];

                        const inchesToPoints = 72;

                        const defaultCellStyle = (container: IContainer, backgroundColor: Color): IContainer => {
                            return container
                                .border(1)
                                .borderColor(Colors.Grey.Lighten1)
                                .background(backgroundColor)
                                .paddingVertical(5)
                                .paddingHorizontal(10)
                                .alignCenter()
                                .alignMiddle();
                        };

                        table.columnsDefinition((columns) => {
                            columns.relativeColumn();

                            columns.constantColumn(80);
                            columns.constantColumn(80);

                            columns.constantColumn(80);
                            columns.constantColumn(80);
                        });

                        table.header((header) => {
                            // you can extend existing styles by creating additional methods
                            const cellStyle = (container: IContainer): IContainer =>
                                defaultCellStyle(container, Colors.Grey.Lighten3);

                            // please be sure to call the 'header' handler!

                            cellStyle(header.cell().rowSpan(2)).extendHorizontal().alignLeft()
                                .text('Document type').bold();

                            cellStyle(header.cell().columnSpan(2)).text('Inches').bold();
                            cellStyle(header.cell().columnSpan(2)).text('Points').bold();

                            cellStyle(header.cell()).text('Width');
                            cellStyle(header.cell()).text('Height');

                            cellStyle(header.cell()).text('Width');
                            cellStyle(header.cell()).text('Height');
                        });

                        for (const page of pageSizes) {
                            const cellStyle = (container: IContainer): IContainer =>
                                defaultCellStyle(container, Colors.White).showOnce();

                            cellStyle(table.cell()).extendHorizontal().alignLeft().text(page.name);

                            // inches
                            cellStyle(table.cell()).text(String(page.width));
                            cellStyle(table.cell()).text(String(page.height));

                            // points
                            cellStyle(table.cell()).text(String(page.width * inchesToPoints));
                            cellStyle(table.cell()).text(String(page.height * inchesToPoints));
                        }
                    });
            });
        })
        .generatePdf();
});

interface BookTermModel {
    term: string;
    description: string;
    firstLevelCategory: string;
    secondLevelCategory: string;
    thirdLevelCategory: string;
}

/** Counterpart of LINQ GroupBy: groups in first-occurrence order of the key. */
function groupBy<T, K>(items: T[], keySelector: (item: T) => K): [K, T[]][] {
    const groups = new Map<K, T[]>();

    for (const item of items) {
        const key = keySelector(item);
        const group = groups.get(key);

        if (group)
            group.push(item);
        else
            groups.set(key, [item]);
    }

    return [...groups.entries()];
}

test('SemanticExamples.GenerateBook', () => {
    Settings.enableCaching = false;
    Settings.enableDebugging = false;

    // The .NET original configures System.Text.Json for camelCase keys;
    // JSON.parse consumes the camelCase keys directly.
    const bookData = fs.readFileSync(resource('semantic-book-content.json'), 'utf8');
    const terms: BookTermModel[] = JSON.parse(bookData);
    const categories = groupBy(terms, (x) => x.firstLevelCategory)
        .map(([category1, category1Terms]) => ({
            category: category1,
            terms: groupBy(category1Terms, (y) => y.secondLevelCategory)
                .map(([category2, category2Terms]) => ({
                    category: category2,
                    terms: groupBy(category2Terms, (z) => z.thirdLevelCategory)
                        .map(([category3, category3Terms]) => ({
                            category: category3,
                            terms: category3Terms,
                        })),
                })),
        }));

    const metadata = new DocumentMetadata();
    metadata.title = 'Programming Terms';
    metadata.language = 'en-US';

    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A4);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(50);
                page.pageColor(Colors.White);

                page.header()
                    .text('Programming Terms')
                    .bold()
                    .fontSize(36);

                page.content()
                    .paddingVertical(24)
                    .column((column) => {
                        for (const category1 of categories) {
                            column.item()
                                .semanticSection()
                                .ensureSpace(100)
                                .column((column) => {
                                    column.spacing(24);

                                    column.item()
                                        .paddingBottom(8)
                                        .semanticHeader1()
                                        .text(category1.category)
                                        .fontSize(24)
                                        .fontColor(Colors.Blue.Darken4)
                                        .bold();

                                    for (const category2 of category1.terms) {
                                        column.item()
                                            .semanticSection()
                                            .ensureSpace(100)
                                            .column((column) => {
                                                column.spacing(8);

                                                column.item()
                                                    .paddingBottom(8)
                                                    .semanticHeader2()
                                                    .text(category2.category)
                                                    .fontSize(20)
                                                    .fontColor(Colors.Blue.Darken2)
                                                    .bold();

                                                for (const category3 of category2.terms) {
                                                    column.item()
                                                        .semanticSection()
                                                        .ensureSpace(100)
                                                        .column((column) => {
                                                            column.spacing(8);

                                                            column.item()
                                                                .paddingBottom(8)
                                                                .semanticHeader3()
                                                                .text(category3.category)
                                                                .fontSize(16)
                                                                .fontColor(Colors.Blue.Medium)
                                                                .bold();

                                                            for (const term of category3.terms) {
                                                                column.item()
                                                                    .semanticParagraph()
                                                                    .text((text) => {
                                                                        text.span(term.term).bold();
                                                                        text.span(' - ');
                                                                        text.span(term.description);
                                                                    });
                                                            }
                                                        });
                                                }
                                            });
                                    }
                                });

                            column.item().pageBreak();
                        }
                    });

                page.footer()
                    .alignCenter()
                    .text((text) => {
                        text.span('Page ');
                        text.currentPageNumber();
                        text.span(' of ');
                        text.totalPages();
                    });
            });
        })
        .withMetadata(metadata)
        .generatePdf();
});
