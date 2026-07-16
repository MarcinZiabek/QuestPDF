package questpdf.docexamples;

import com.questpdf.Settings;
import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.PageSizes;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.DocumentMetadata;
import com.questpdf.infrastructure.IContainer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SemanticExamples extends DocExample {

    @Test
    public void headerAndFooter() {
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
                        .semanticTable()
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
                                // you can extend existing styles by creating additional methods
                                Function<IContainer, IContainer> cellStyle = container ->
                                    defaultCellStyle.apply(container, Colors.Grey.getLighten3());

                                // please be sure to call the 'header' handler!

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
            .generatePdf();
    }

    public record BookTermModel(
        String term,
        String description,
        String firstLevelCategory,
        String secondLevelCategory,
        String thirdLevelCategory) {}

    @Test
    public void generateBook() throws IOException {
        Settings.setEnableCaching(false);
        Settings.setEnableDebugging(false);

        // The .NET original configures System.Text.Json for camelCase keys;
        // the minimal reader below consumes the camelCase keys directly.
        var bookData = Files.readString(Path.of(resource("semantic-book-content.json")));
        var terms = parseBookTerms(bookData);

        // Counterpart of the Kotlin groupBy chain: LinkedHashMap keeps the first-occurrence key order.
        var categories = terms.stream()
            .collect(Collectors.groupingBy(x -> x.firstLevelCategory(), LinkedHashMap::new, Collectors.toList()))
            .entrySet().stream()
            .map(x -> new TermCategory<>(
                x.getKey(),
                x.getValue().stream()
                    .collect(Collectors.groupingBy(y -> y.secondLevelCategory(), LinkedHashMap::new, Collectors.toList()))
                    .entrySet().stream()
                    .map(y -> new TermCategory<>(
                        y.getKey(),
                        y.getValue().stream()
                            .collect(Collectors.groupingBy(z -> z.thirdLevelCategory(), LinkedHashMap::new, Collectors.toList()))
                            .entrySet().stream()
                            .map(z -> new TermCategory<>(
                                z.getKey(),
                                z.getValue()))
                            .toList()))
                    .toList()))
            .toList();

        var metadata = new DocumentMetadata();
        metadata.setTitle("Programming Terms");
        metadata.setLanguage("en-US");

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA4());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(50f);
                    page.pageColor(Colors.getWhite());

                    page.header()
                        .text("Programming Terms")
                        .bold()
                        .fontSize(36f);

                    page.content()
                        .paddingVertical(24f)
                        .column(column -> {
                            for (var category1 : categories) {
                                column.item()
                                    .semanticSection()
                                    .ensureSpace(100f)
                                    .column(column1 -> {
                                        column1.spacing(24f);

                                        column1.item()
                                            .paddingBottom(8f)
                                            .semanticHeader1()
                                            .text(category1.category())
                                            .fontSize(24f)
                                            .fontColor(Colors.Blue.getDarken4())
                                            .bold();

                                        for (var category2 : category1.terms()) {
                                            column1.item()
                                                .semanticSection()
                                                .ensureSpace(100f)
                                                .column(column2 -> {
                                                    column2.spacing(8f);

                                                    column2.item()
                                                        .paddingBottom(8f)
                                                        .semanticHeader2()
                                                        .text(category2.category())
                                                        .fontSize(20f)
                                                        .fontColor(Colors.Blue.getDarken2())
                                                        .bold();

                                                    for (var category3 : category2.terms()) {
                                                        column2.item()
                                                            .semanticSection()
                                                            .ensureSpace(100f)
                                                            .column(column3 -> {
                                                                column3.spacing(8f);

                                                                column3.item()
                                                                    .paddingBottom(8f)
                                                                    .semanticHeader3()
                                                                    .text(category3.category())
                                                                    .fontSize(16f)
                                                                    .fontColor(Colors.Blue.getMedium())
                                                                    .bold();

                                                                for (var term : category3.terms()) {
                                                                    column3.item()
                                                                        .semanticParagraph()
                                                                        .text(text -> {
                                                                            text.span(term.term()).bold();
                                                                            text.span(" - ");
                                                                            text.span(term.description());
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
                        .text(text -> {
                            text.span("Page ");
                            text.currentPageNumber();
                            text.span(" of ");
                            text.totalPages();
                        });
                });
            })
            .withMetadata(metadata)
            .generatePdf();
    }

    /** Counterpart of the anonymous types the .NET original creates while grouping terms. */
    private record TermCategory<T>(String category, List<T> terms) {}

    /** The string .NET produces implicitly for a double passed to Text(object). */
    private static String formatNumber(double value) {
        return value == (double) (long) value ? String.valueOf((long) value) : String.valueOf(value);
    }

    /**
     * Minimal JSON reader for the semantic-book-content resource (an array of flat,
     * string-valued objects); the JVM port has no counterpart of System.Text.Json.
     */
    private static List<BookTermModel> parseBookTerms(String json) {
        var reader = new JsonReader(json);
        var terms = new ArrayList<BookTermModel>();

        reader.expect('[');

        do {
            var values = new HashMap<String, String>();
            reader.expect('{');

            do {
                var key = reader.readString();
                reader.expect(':');
                values.put(key, reader.readString());
            } while (reader.tryConsume(','));

            reader.expect('}');

            terms.add(new BookTermModel(
                values.get("term"),
                values.get("description"),
                values.get("firstLevelCategory"),
                values.get("secondLevelCategory"),
                values.get("thirdLevelCategory")));
        } while (reader.tryConsume(','));

        reader.expect(']');

        return terms;
    }

    private static class JsonReader {
        private final String text;
        private int position;

        JsonReader(String text) {
            this.text = text;
        }

        void expect(char character) {
            skipWhitespace();

            if (position >= text.length() || text.charAt(position) != character)
                throw new IllegalStateException("Expected '" + character + "' at position " + position + ".");

            position++;
        }

        boolean tryConsume(char character) {
            skipWhitespace();

            if (position < text.length() && text.charAt(position) == character) {
                position++;
                return true;
            }

            return false;
        }

        String readString() {
            expect('"');
            var builder = new StringBuilder();

            while (true) {
                var character = text.charAt(position++);

                switch (character) {
                    case '"' -> {
                        return builder.toString();
                    }

                    case '\\' -> {
                        var escaped = text.charAt(position++);

                        switch (escaped) {
                            case '"', '\\', '/' -> builder.append(escaped);
                            case 'b' -> builder.append('\b');
                            case 'f' -> builder.append('\f');
                            case 'n' -> builder.append('\n');
                            case 'r' -> builder.append('\r');
                            case 't' -> builder.append('\t');
                            case 'u' -> {
                                builder.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                                position += 4;
                            }
                            default -> throw new IllegalStateException("Unsupported escape sequence '\\" + escaped + "'.");
                        }
                    }

                    default -> builder.append(character);
                }
            }
        }

        private void skipWhitespace() {
            while (position < text.length() && Character.isWhitespace(text.charAt(position)))
                position++;
        }
    }
}
