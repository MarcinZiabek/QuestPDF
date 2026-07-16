package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.fluent.TableDescriptor;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TableExamples extends DocExample {

    @Test
    public void basic() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(500f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                columns.constantColumn(50f);
                                columns.relativeColumn();
                                columns.constantColumn(125f);
                            });

                            table.header(header -> {
                                header.cell().borderBottom(2f).padding(8f).text("#");
                                header.cell().borderBottom(2f).padding(8f).text("Product");
                                header.cell().borderBottom(2f).padding(8f).alignRight().text("Price");
                            });

                            for (var i = 0; i < 6; i++) {
                                // The Java counterpart of C# Math.Round(Random.Shared.NextDouble() * 100, 2).
                                var price = Math.round(ThreadLocalRandom.current().nextDouble() * 100 * 100) / 100.0;

                                table.cell().padding(8f).text(String.valueOf(i + 1));
                                table.cell().padding(8f).text(Placeholders.label());
                                table.cell().padding(8f).alignRight().text("$" + price);
                            }
                        });
                });
            })
            .generateImages(index -> output("table-simple.webp"), settings);
    }

    @Test
    public void cellStyleExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(500f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    var weatherIcons = new String[] { "cloudy.svg", "lightning.svg", "pouring.svg", "rainy.svg", "snowy.svg", "windy.svg" };

                    page.content()
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                columns.relativeColumn();
                                columns.constantColumn(125f);
                                columns.constantColumn(125f);
                            });

                            table.header(header -> {
                                // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                                // overload, which is not bridged; a local Function is the direct equivalent.
                                Function<IContainer, IContainer> cellStyle = container ->
                                    container.background(Colors.Blue.getDarken2())
                                        .defaultTextStyle(style -> style.fontColor(Colors.getWhite()).bold())
                                        .paddingVertical(8f)
                                        .paddingHorizontal(16f);

                                cellStyle.apply(header.cell()).text("Day");
                                cellStyle.apply(header.cell()).alignCenter().text("Weather");
                                cellStyle.apply(header.cell()).alignRight().text("Temp");
                            });

                            for (var i = 0; i < 7; i++) {
                                var weatherIndex = ThreadLocalRandom.current().nextInt(0, weatherIcons.length);

                                var backgroundColor = i % 2 == 0
                                    ? Colors.Blue.getLighten5()
                                    : Colors.Blue.getLighten4();

                                Function<IContainer, IContainer> cellStyle = container ->
                                    container.background(backgroundColor)
                                        .paddingVertical(8f)
                                        .paddingHorizontal(16f);

                                cellStyle.apply(table.cell())
                                    .text(LocalDate.of(2025, 2, 26).plusDays(i).format(DateTimeFormatter.ofPattern("dd MMMM", Locale.ENGLISH)));

                                cellStyle.apply(table.cell()).alignCenter().height(24f)
                                    .svg(resource("WeatherIcons/" + weatherIcons[weatherIndex]));

                                cellStyle.apply(table.cell()).alignRight()
                                    .text(ThreadLocalRandom.current().nextInt(-10, 35) + "°");
                            }
                        });
                });
            })
            .generateImages(index -> output("table-cell-style.webp"), settings);
    }

    @Test
    public void overlappingCells() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(700f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(16f));
                    page.margin(25f);

                    var dayNames = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };

                    page.content()
                        .border(1f)
                        .borderColor(Colors.Grey.getLighten1())
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                // hour column
                                columns.constantColumn(60f);

                                // day columns
                                for (var i = 0; i < 5; i++)
                                    columns.relativeColumn();
                            });

                            // even/odd columns background
                            for (var column = 0; column < 7; column++) {
                                var backgroundColor = column % 2 == 0 ? Colors.Grey.getLighten3() : Colors.getWhite();
                                table.cell().column(column).rowSpan(24).background(backgroundColor);
                            }

                            // hours and hour lines
                            for (var hour = 6; hour < 16; hour++) {
                                table.cell().column(1).row(hour)
                                    .paddingVertical(5f).paddingHorizontal(10f).alignRight()
                                    .text(String.valueOf(hour));

                                table.cell().row(hour).columnSpan(6)
                                    .border(1f).borderColor(Colors.Grey.getLighten1()).height(20f);
                            }

                            // dates and day names
                            for (var i = 0; i < 5; i++) {
                                var index = i;

                                table.cell()
                                    .column(i + 2).row(1).padding(5f)
                                    .column(column -> {
                                        column.item().alignCenter().text(String.valueOf(17 + index)).fontSize(24f).bold();
                                        column.item().alignCenter().text(dayNames[index]).light();
                                    });
                            }

                            // standup events
                            for (var i = 1; i <= 4; i++)
                                addEvent(table, i, 8, 1, "Standup", Colors.Blue.getLighten4(), Colors.Blue.getDarken3());

                            // other events
                            addEvent(table, 2, 11, 2, "Interview", Colors.Red.getLighten4(), Colors.Red.getDarken3());
                            addEvent(table, 3, 12, 3, "Demo", Colors.Red.getLighten4(), Colors.Red.getDarken3());
                            addEvent(table, 5, 5, 17, "PTO", Colors.Green.getLighten4(), Colors.Green.getDarken3());
                        });
                });
            })
            .generateImages(index -> output("table-overlapping-cells.webp"), settings);
    }

    private static void addEvent(TableDescriptor table, int day, int hour, int length, String name, Color backgroundColor, Color textColor) {
        table.cell()
            .column(day + 1).row(hour).rowSpan(length)
            .padding(5f).background(backgroundColor).padding(5f)
            .alignCenter().alignMiddle()
            .text(name).fontColor(textColor);
    }

    @Test
    public void manualCellPlacement() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(700f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(16f));
                    page.margin(25f);

                    page.content()
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                columns.constantColumn(75f);
                                columns.constantColumn(150f);
                                columns.constantColumn(200f);
                                columns.constantColumn(200f);
                            });

                            // The C# original routes the cell styles through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; local Functions are the direct equivalent.
                            BiFunction<IContainer, Color, IContainer> cellStyle = (container, color) ->
                                container.border(1f).background(color).paddingHorizontal(10f).paddingVertical(15f).alignCenter().alignMiddle();

                            Function<IContainer, IContainer> headerCellStyle = container ->
                                cellStyle.apply(container, Colors.Grey.getLighten4());

                            Function<IContainer, IContainer> goodCellStyle = container ->
                                cellStyle.apply(container, Colors.Green.getLighten4()).defaultTextStyle(style -> style.fontColor(Colors.Green.getDarken2()));

                            Function<IContainer, IContainer> badCellStyle = container ->
                                cellStyle.apply(container, Colors.Red.getLighten4()).defaultTextStyle(style -> style.fontColor(Colors.Red.getDarken2()));

                            headerCellStyle.apply(table.cell().row(1).column(3).columnSpan(2))
                                .text("Predicted condition").bold();

                            headerCellStyle.apply(table.cell().row(3).column(1).rowSpan(2)).rotateLeft()
                                .text("Actual\ncondition").bold().alignCenter();

                            headerCellStyle.apply(table.cell().row(2).column(3))
                                .text("Positive (PP)");

                            headerCellStyle.apply(table.cell().row(2).column(4))
                                .text("Negative (PN)");

                            headerCellStyle.apply(table.cell().row(3).column(2)).text("Positive (P)");

                            headerCellStyle.apply(table.cell().row(4).column(2))
                                .text("Negative (N)");

                            goodCellStyle.apply(table.cell().row(3).column(3))
                                .text("True positive (TP)");

                            badCellStyle.apply(table.cell().row(3).column(4))
                                .text("False negative (FN)");

                            badCellStyle.apply(table.cell().row(4).column(3)).text("False positive (FP)");

                            goodCellStyle.apply(table.cell().row(4).column(4)).text("True negative (TN)");
                        });
                });
            })
            .generateImages(index -> output("table-manual-cell-placement.webp"), settings);
    }

    @Test
    public void columnsDefinition() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(700f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(16f));
                    page.margin(25f);

                    page.content()
                        .width(450f)
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                columns.constantColumn(150f);
                                columns.relativeColumn(2f);
                                columns.relativeColumn(3f);
                            });

                            // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; a local Function is the direct equivalent.
                            Function<IContainer, IContainer> cellStyle = container ->
                                container.border(1f).padding(10f);

                            cellStyle.apply(table.cell().columnSpan(3).background(Colors.Grey.getLighten2()))
                                .text("Total width: 450px");

                            cellStyle.apply(table.cell()).text("Constant: 150px");
                            cellStyle.apply(table.cell()).text("Relative: 2*");
                            cellStyle.apply(table.cell()).text("Relative: 3*");

                            cellStyle.apply(table.cell()).text("150px");
                            cellStyle.apply(table.cell()).text("120px");
                            cellStyle.apply(table.cell()).text("180px");
                        });
                });
            })
            .generateImages(index -> output("table-columns-definition.webp"), settings);
    }

    @Test
    public void headerAndFooter() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(600f, 250f));
                    page.defaultTextStyle(style -> style.fontSize(16f));
                    page.margin(25f);

                    page.content()
                        .border(1f)
                        .borderColor(Colors.Grey.getLighten1())
                        .table(table -> {
                            record NamedPageSize(String name, double width, double height) {}

                            var pageSizes = List.of(
                                new NamedPageSize("Letter (ANSI A)", 8.5, 11.0),
                                new NamedPageSize("Legal", 8.5, 14.0),
                                new NamedPageSize("Ledger (ANSI B)", 11.0, 17.0),
                                new NamedPageSize("Tabloid (ANSI B)", 17.0, 11.0),
                                new NamedPageSize("ANSI C", 22.0, 17.0),
                                new NamedPageSize("ANSI D", 34.0, 22.0),
                                new NamedPageSize("ANSI E", 44.0, 34.0));

                            var inchesToPoints = 72;

                            // The C# original routes the cell styles through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; local Functions are the direct equivalent.
                            // (C# declares backgroundColor as string, relying on implicit Color<->string conversions.)
                            BiFunction<IContainer, Color, IContainer> defaultCellStyle = (container, backgroundColor) ->
                                container
                                    .border(1f)
                                    .borderColor(Colors.Grey.getLighten1())
                                    .background(backgroundColor)
                                    .paddingVertical(5f)
                                    .paddingHorizontal(10f)
                                    .alignCenter()
                                    .alignMiddle();

                            table.columnsDefinition(columns -> {
                                columns.relativeColumn();

                                columns.constantColumn(80f);
                                columns.constantColumn(80f);

                                columns.constantColumn(80f);
                                columns.constantColumn(80f);
                            });

                            table.header(header -> {
                                // please be sure to call the 'header' handler!

                                // you can extend existing styles by creating additional methods
                                Function<IContainer, IContainer> cellStyle = container ->
                                    defaultCellStyle.apply(container, Colors.Grey.getLighten3());

                                cellStyle.apply(header.cell().rowSpan(2)).extendHorizontal().alignLeft()
                                    .text("Document type").bold();

                                cellStyle.apply(header.cell().columnSpan(2)).text("Inches").bold();
                                cellStyle.apply(header.cell().columnSpan(2)).text("Points").bold();

                                cellStyle.apply(header.cell()).text("Width");
                                cellStyle.apply(header.cell()).text("Height");

                                cellStyle.apply(header.cell()).text("Width");
                                cellStyle.apply(header.cell()).text("Height");
                            });

                            for (var pageSize : pageSizes) {
                                Function<IContainer, IContainer> cellStyle = container ->
                                    defaultCellStyle.apply(container, Colors.getWhite()).showOnce();

                                cellStyle.apply(table.cell()).extendHorizontal().alignLeft().text(pageSize.name());

                                // inches
                                cellStyle.apply(table.cell()).text(formatNumber(pageSize.width()));
                                cellStyle.apply(table.cell()).text(formatNumber(pageSize.height()));

                                // points
                                cellStyle.apply(table.cell()).text(formatNumber(pageSize.width() * inchesToPoints));
                                cellStyle.apply(table.cell()).text(formatNumber(pageSize.height() * inchesToPoints));
                            }
                        });
                });
            })
            .generateImages(index -> output("table-header-and-footer-" + index + ".webp"), settings);
    }

    /**
     * The string .NET produces for a double passed to Text(object): integral values
     * print without a fractional part (e.g. 612), non-integral ones with it (e.g. 8.5).
     */
    private static String formatNumber(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
