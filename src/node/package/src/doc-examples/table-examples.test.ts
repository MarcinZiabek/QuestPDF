// Port of src/dotnet/library/QuestPDF.DocumentationExamples/TableExamples.cs.
import { test } from 'node:test';
import { Color, Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../index';
import { imageSettings, output, resource } from './doc-example';

test('TableExamples.Basic', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            columns.constantColumn(50);
                            columns.relativeColumn();
                            columns.constantColumn(125);
                        });

                        table.header((header) => {
                            header.cell().borderBottom(2).padding(8).text('#');
                            header.cell().borderBottom(2).padding(8).text('Product');
                            header.cell().borderBottom(2).padding(8).alignRight().text('Price');
                        });

                        for (let i = 0; i < 6; i++) {
                            // The TypeScript counterpart of C# Math.Round(Random.Shared.NextDouble() * 100, 2).
                            const price = Math.round(Math.random() * 100 * 100) / 100;

                            table.cell().padding(8).text(`${i + 1}`);
                            table.cell().padding(8).text(Placeholders.label());
                            table.cell().padding(8).alignRight().text(`$${price}`);
                        }
                    });
            });
        })
        .generateImages(() => output('table-simple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TableExamples.CellStyleExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                const weatherIcons = ['cloudy.svg', 'lightning.svg', 'pouring.svg', 'rainy.svg', 'snowy.svg', 'windy.svg'];

                page.content()
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            columns.relativeColumn();
                            columns.constantColumn(125);
                            columns.constantColumn(125);
                        });

                        table.header((header) => {
                            cellStyle(header.cell()).text('Day');
                            cellStyle(header.cell()).alignCenter().text('Weather');
                            cellStyle(header.cell()).alignRight().text('Temp');

                            // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; a free function is the direct equivalent.
                            function cellStyle(container: IContainer): IContainer {
                                return container
                                    .background(Colors.Blue.Darken2)
                                    .defaultTextStyle((style) => style.fontColor(Colors.White).bold())
                                    .paddingVertical(8)
                                    .paddingHorizontal(16);
                            }
                        });

                        for (let i = 0; i < 7; i++) {
                            const weatherIndex = randomNext(0, weatherIcons.length);

                            cellStyle(table.cell())
                                .text(formatDayMonth(new Date(2025, 1, 26 + i)));

                            cellStyle(table.cell()).alignCenter().height(24)
                                .svg(resource(`WeatherIcons/${weatherIcons[weatherIndex]}`));

                            cellStyle(table.cell()).alignRight()
                                .text(`${randomNext(-10, 35)}°`);

                            function cellStyle(container: IContainer): IContainer {
                                const backgroundColor = i % 2 === 0
                                    ? Colors.Blue.Lighten5
                                    : Colors.Blue.Lighten4;

                                return container
                                    .background(backgroundColor)
                                    .paddingVertical(8)
                                    .paddingHorizontal(16);
                            }
                        }
                    });
            });
        })
        .generateImages(() => output('table-cell-style.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TableExamples.OverlappingCells', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(700, 1000));
                page.defaultTextStyle((style) => style.fontSize(16));
                page.margin(25);

                const dayNames = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

                page.content()
                    .border(1)
                    .borderColor(Colors.Grey.Lighten1)
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            // hour column
                            columns.constantColumn(60);

                            // day columns
                            for (let i = 0; i < 5; i++)
                                columns.relativeColumn();
                        });

                        // even/odd columns background
                        for (let column = 0; column < 7; column++) {
                            const backgroundColor = column % 2 === 0 ? Colors.Grey.Lighten3 : Colors.White;
                            table.cell().column(column).rowSpan(24).background(backgroundColor);
                        }

                        // hours and hour lines
                        for (let hour = 6; hour < 16; hour++) {
                            table.cell().column(1).row(hour)
                                .paddingVertical(5).paddingHorizontal(10).alignRight()
                                .text(`${hour}`);

                            table.cell().row(hour).columnSpan(6)
                                .border(1).borderColor(Colors.Grey.Lighten1).height(20);
                        }

                        // dates and day names
                        for (let i = 0; i < 5; i++) {
                            table.cell()
                                .column(i + 2).row(1).padding(5)
                                .column((column) => {
                                    column.item().alignCenter().text(`${17 + i}`).fontSize(24).bold();
                                    column.item().alignCenter().text(dayNames[i]).light();
                                });
                        }

                        // standup events
                        for (let i = 1; i <= 4; i++)
                            addEvent(i, 8, 1, 'Standup', Colors.Blue.Lighten4, Colors.Blue.Darken3);

                        // other events
                        addEvent(2, 11, 2, 'Interview', Colors.Red.Lighten4, Colors.Red.Darken3);
                        addEvent(3, 12, 3, 'Demo', Colors.Red.Lighten4, Colors.Red.Darken3);
                        addEvent(5, 5, 17, 'PTO', Colors.Green.Lighten4, Colors.Green.Darken3);

                        function addEvent(day: number, hour: number, length: number, name: string, backgroundColor: Color, textColor: Color): void {
                            table.cell()
                                .column(day + 1).row(hour).rowSpan(length)
                                .padding(5).background(backgroundColor).padding(5)
                                .alignCenter().alignMiddle()
                                .text(name).fontColor(textColor);
                        }
                    });
            });
        })
        .generateImages(() => output('table-overlapping-cells.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TableExamples.ManualCellPlacement', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(700, 1000));
                page.defaultTextStyle((style) => style.fontSize(16));
                page.margin(25);

                page.content()
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            columns.constantColumn(75);
                            columns.constantColumn(150);
                            columns.constantColumn(200);
                            columns.constantColumn(200);
                        });

                        headerCellStyle(table.cell().row(1).column(3).columnSpan(2))
                            .text('Predicted condition').bold();

                        headerCellStyle(table.cell().row(3).column(1).rowSpan(2)).rotateLeft()
                            .text('Actual\ncondition').bold().alignCenter();

                        headerCellStyle(table.cell().row(2).column(3))
                            .text('Positive (PP)');

                        headerCellStyle(table.cell().row(2).column(4))
                            .text('Negative (PN)');

                        headerCellStyle(table.cell().row(3).column(2)).text('Positive (P)');

                        headerCellStyle(table.cell().row(4).column(2))
                            .text('Negative (N)');

                        goodCellStyle(table.cell().row(3).column(3))
                            .text('True positive (TP)');

                        badCellStyle(table.cell().row(3).column(4))
                            .text('False negative (FN)');

                        badCellStyle(table.cell().row(4).column(3)).text('False positive (FP)');

                        goodCellStyle(table.cell().row(4).column(4)).text('True negative (TN)');

                        // The C# original routes the cell styles through the Element(Func<IContainer, IContainer>)
                        // overload, which is not bridged; free functions are the direct equivalent.
                        function cellStyle(container: IContainer, color: Color): IContainer {
                            return container.border(1).background(color).paddingHorizontal(10).paddingVertical(15).alignCenter().alignMiddle();
                        }

                        function headerCellStyle(container: IContainer): IContainer {
                            return cellStyle(container, Colors.Grey.Lighten4);
                        }

                        function goodCellStyle(container: IContainer): IContainer {
                            return cellStyle(container, Colors.Green.Lighten4).defaultTextStyle((style) => style.fontColor(Colors.Green.Darken2));
                        }

                        function badCellStyle(container: IContainer): IContainer {
                            return cellStyle(container, Colors.Red.Lighten4).defaultTextStyle((style) => style.fontColor(Colors.Red.Darken2));
                        }
                    });
            });
        })
        .generateImages(() => output('table-manual-cell-placement.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TableExamples.ColumnsDefinition', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(700, 1000));
                page.defaultTextStyle((style) => style.fontSize(16));
                page.margin(25);

                page.content()
                    .width(450)
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            columns.constantColumn(150);
                            columns.relativeColumn(2);
                            columns.relativeColumn(3);
                        });

                        cellStyle(table.cell().columnSpan(3).background(Colors.Grey.Lighten2))
                            .text('Total width: 450px');

                        cellStyle(table.cell()).text('Constant: 150px');
                        cellStyle(table.cell()).text('Relative: 2*');
                        cellStyle(table.cell()).text('Relative: 3*');

                        cellStyle(table.cell()).text('150px');
                        cellStyle(table.cell()).text('120px');
                        cellStyle(table.cell()).text('180px');

                        // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                        // overload, which is not bridged; a free function is the direct equivalent.
                        function cellStyle(container: IContainer): IContainer {
                            return container.border(1).padding(10);
                        }
                    });
            });
        })
        .generateImages(() => output('table-columns-definition.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TableExamples.HeaderAndFooter', () => {
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
                    .table((table) => {
                        const pageSizes = [
                            { name: 'Letter (ANSI A)', width: 8.5, height: 11 },
                            { name: 'Legal', width: 8.5, height: 14 },
                            { name: 'Ledger (ANSI B)', width: 11, height: 17 },
                            { name: 'Tabloid (ANSI B)', width: 17, height: 11 },
                            { name: 'ANSI C', width: 22, height: 17 },
                            { name: 'ANSI D', width: 34, height: 22 },
                            { name: 'ANSI E', width: 44, height: 34 },
                        ];

                        const inchesToPoints = 72;

                        // The C# original routes the cell styles through the Element(Func<IContainer, IContainer>)
                        // overload, which is not bridged; free functions are the direct equivalent.
                        // (C# declares backgroundColor as string, relying on implicit Color<->string conversions.)
                        function defaultCellStyle(container: IContainer, backgroundColor: Color): IContainer {
                            return container
                                .border(1)
                                .borderColor(Colors.Grey.Lighten1)
                                .background(backgroundColor)
                                .paddingVertical(5)
                                .paddingHorizontal(10)
                                .alignCenter()
                                .alignMiddle();
                        }

                        table.columnsDefinition((columns) => {
                            columns.relativeColumn();

                            columns.constantColumn(80);
                            columns.constantColumn(80);

                            columns.constantColumn(80);
                            columns.constantColumn(80);
                        });

                        table.header((header) => {
                            // please be sure to call the 'header' handler!

                            cellStyle(header.cell().rowSpan(2)).extendHorizontal().alignLeft()
                                .text('Document type').bold();

                            cellStyle(header.cell().columnSpan(2)).text('Inches').bold();
                            cellStyle(header.cell().columnSpan(2)).text('Points').bold();

                            cellStyle(header.cell()).text('Width');
                            cellStyle(header.cell()).text('Height');

                            cellStyle(header.cell()).text('Width');
                            cellStyle(header.cell()).text('Height');

                            // you can extend existing styles by creating additional methods
                            function cellStyle(container: IContainer): IContainer {
                                return defaultCellStyle(container, Colors.Grey.Lighten3);
                            }
                        });

                        for (const page of pageSizes) {
                            cellStyle(table.cell()).extendHorizontal().alignLeft().text(page.name);

                            // inches
                            cellStyle(table.cell()).text(page.width.toString());
                            cellStyle(table.cell()).text(page.height.toString());

                            // points
                            cellStyle(table.cell()).text((page.width * inchesToPoints).toString());
                            cellStyle(table.cell()).text((page.height * inchesToPoints).toString());

                            function cellStyle(container: IContainer): IContainer {
                                return defaultCellStyle(container, Colors.White).showOnce();
                            }
                        }
                    });
            });
        })
        .generateImages((index) => output(`table-header-and-footer-${index}.webp`), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

/** The TypeScript counterpart of C# Random.Shared.Next(minValue, maxValue): a random integer within [minValue, maxValue). */
function randomNext(minValue: number, maxValue: number): number {
    return Math.floor(Math.random() * (maxValue - minValue)) + minValue;
}

/** The TypeScript counterpart of C# DateTime.ToString("dd MMMM"): a zero-padded day followed by the full month name. */
function formatDayMonth(date: Date): string {
    return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'long' });
}
