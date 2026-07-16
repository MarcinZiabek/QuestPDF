package questpdf.docexamples

import com.questpdf.Settings
import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.PageSizes
import com.questpdf.infrastructure.Color
import com.questpdf.infrastructure.DocumentMetadata
import com.questpdf.infrastructure.IContainer
import org.junit.jupiter.api.Test
import java.io.File

class SemanticExamples : DocExample() {

    @Test
    fun headerAndFooter() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 250f))
                    defaultTextStyle { fontSize(16f) }
                    margin(25f)

                    content()
                        .border(1f)
                        .borderColor(Colors.Grey.Lighten1)
                        .semanticTable()
                        .table {
                            data class NamedPageSize(val name: String, val width: Double, val height: Double)

                            val pageSizes = listOf(
                                NamedPageSize("Letter (ANSI A)", 8.5, 11.0),
                                NamedPageSize("Legal", 8.5, 14.0),
                                NamedPageSize("Ledger (ANSI B)", 11.0, 17.0),
                                NamedPageSize("Tabloid (ANSI B)", 17.0, 11.0),
                                NamedPageSize("ANSI C", 22.0, 17.0),
                                NamedPageSize("ANSI D", 34.0, 22.0),
                                NamedPageSize("ANSI E", 44.0, 34.0),
                            )

                            val inchesToPoints = 72

                            fun defaultCellStyle(container: IContainer, backgroundColor: Color): IContainer {
                                return container
                                    .border(1f)
                                    .borderColor(Colors.Grey.Lighten1)
                                    .background(backgroundColor)
                                    .paddingVertical(5f)
                                    .paddingHorizontal(10f)
                                    .alignCenter()
                                    .alignMiddle()
                            }

                            columnsDefinition {
                                relativeColumn()

                                constantColumn(80f)
                                constantColumn(80f)

                                constantColumn(80f)
                                constantColumn(80f)
                            }

                            header {
                                // you can extend existing styles by creating additional methods
                                fun IContainer.cellStyle(): IContainer =
                                    defaultCellStyle(this, Colors.Grey.Lighten3)

                                // please be sure to call the 'header' handler!

                                cell().rowSpan(2u).cellStyle().extendHorizontal().alignLeft()
                                    .text("Document type").bold()

                                cell().columnSpan(2u).cellStyle().text("Inches").bold()
                                cell().columnSpan(2u).cellStyle().text("Points").bold()

                                cell().cellStyle().text("Width")
                                cell().cellStyle().text("Height")

                                cell().cellStyle().text("Width")
                                cell().cellStyle().text("Height")
                            }

                            for (page in pageSizes) {
                                fun IContainer.cellStyle(): IContainer =
                                    defaultCellStyle(this, Colors.White).showOnce()

                                cell().cellStyle().extendHorizontal().alignLeft().text(page.name)

                                // inches
                                cell().cellStyle().text(formatNumber(page.width))
                                cell().cellStyle().text(formatNumber(page.height))

                                // points
                                cell().cellStyle().text(formatNumber(page.width * inchesToPoints))
                                cell().cellStyle().text(formatNumber(page.height * inchesToPoints))
                            }
                        }
                }
            }
            .generatePdf()
    }

    class BookTermModel(
        val term: String,
        val description: String,
        val firstLevelCategory: String,
        val secondLevelCategory: String,
        val thirdLevelCategory: String,
    )

    @Test
    fun generateBook() {
        Settings.enableCaching = false
        Settings.enableDebugging = false

        // The .NET original configures System.Text.Json for camelCase keys;
        // the minimal reader below consumes the camelCase keys directly.
        val bookData = File(resource("semantic-book-content.json")).readText()
        val terms = parseBookTerms(bookData)
        val categories = terms
            .groupBy { x -> x.firstLevelCategory }
            .map { x ->
                TermCategory(
                    category = x.key,
                    terms = x.value
                        .groupBy { y -> y.secondLevelCategory }
                        .map { y ->
                            TermCategory(
                                category = y.key,
                                terms = y.value
                                    .groupBy { z -> z.thirdLevelCategory }
                                    .map { z ->
                                        TermCategory(
                                            category = z.key,
                                            terms = z.value
                                        )
                                    }
                            )
                        }
                )
            }

        Document
            .create {
                page {
                    size(PageSizes.A4)
                    defaultTextStyle { fontSize(20f) }
                    margin(50f)
                    pageColor(Colors.White)

                    header()
                        .text("Programming Terms")
                        .bold()
                        .fontSize(36f)

                    content()
                        .paddingVertical(24f)
                        .column {
                            for (category1 in categories) {
                                item()
                                    .semanticSection()
                                    .ensureSpace(100f)
                                    .column {
                                        spacing(24f)

                                        item()
                                            .paddingBottom(8f)
                                            .semanticHeader1()
                                            .text(category1.category)
                                            .fontSize(24f)
                                            .fontColor(Colors.Blue.Darken4)
                                            .bold()

                                        for (category2 in category1.terms) {
                                            item()
                                                .semanticSection()
                                                .ensureSpace(100f)
                                                .column {
                                                    spacing(8f)

                                                    item()
                                                        .paddingBottom(8f)
                                                        .semanticHeader2()
                                                        .text(category2.category)
                                                        .fontSize(20f)
                                                        .fontColor(Colors.Blue.Darken2)
                                                        .bold()

                                                    for (category3 in category2.terms) {
                                                        item()
                                                            .semanticSection()
                                                            .ensureSpace(100f)
                                                            .column {
                                                                spacing(8f)

                                                                item()
                                                                    .paddingBottom(8f)
                                                                    .semanticHeader3()
                                                                    .text(category3.category)
                                                                    .fontSize(16f)
                                                                    .fontColor(Colors.Blue.Medium)
                                                                    .bold()

                                                                for (term in category3.terms) {
                                                                    item()
                                                                        .semanticParagraph()
                                                                        .text {
                                                                            span(term.term).bold()
                                                                            span(" - ")
                                                                            span(term.description)
                                                                        }
                                                                }
                                                            }
                                                    }
                                                }
                                        }
                                    }

                                item().pageBreak()
                            }
                        }

                    footer()
                        .alignCenter()
                        .text {
                            span("Page ")
                            currentPageNumber()
                            span(" of ")
                            totalPages()
                        }
                }
            }
            .withMetadata(DocumentMetadata().apply {
                title = "Programming Terms"
                language = "en-US"
            })
            .generatePdf()
    }

    /** Counterpart of the anonymous types the .NET original creates while grouping terms. */
    private class TermCategory<T>(val category: String, val terms: List<T>)

    /** The string .NET produces implicitly for a double passed to Text(object). */
    private fun formatNumber(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

    /**
     * Minimal JSON reader for the semantic-book-content resource (an array of flat,
     * string-valued objects); the JVM port has no counterpart of System.Text.Json.
     */
    private fun parseBookTerms(json: String): List<BookTermModel> {
        val reader = JsonReader(json)
        val terms = mutableListOf<BookTermModel>()

        reader.expect('[')

        do {
            val values = mutableMapOf<String, String>()
            reader.expect('{')

            do {
                val key = reader.readString()
                reader.expect(':')
                values[key] = reader.readString()
            } while (reader.tryConsume(','))

            reader.expect('}')

            terms.add(BookTermModel(
                term = values.getValue("term"),
                description = values.getValue("description"),
                firstLevelCategory = values.getValue("firstLevelCategory"),
                secondLevelCategory = values.getValue("secondLevelCategory"),
                thirdLevelCategory = values.getValue("thirdLevelCategory"),
            ))
        } while (reader.tryConsume(','))

        reader.expect(']')

        return terms
    }

    private class JsonReader(private val text: String) {
        private var position = 0

        fun expect(character: Char) {
            skipWhitespace()
            check(position < text.length && text[position] == character) {
                "Expected '$character' at position $position."
            }
            position++
        }

        fun tryConsume(character: Char): Boolean {
            skipWhitespace()

            if (position < text.length && text[position] == character) {
                position++
                return true
            }

            return false
        }

        fun readString(): String {
            expect('"')
            val builder = StringBuilder()

            while (true) {
                when (val character = text[position++]) {
                    '"' -> return builder.toString()
                    '\\' -> when (val escaped = text[position++]) {
                        '"', '\\', '/' -> builder.append(escaped)
                        'b' -> builder.append('\b')
                        'f' -> builder.append('\u000C')
                        'n' -> builder.append('\n')
                        'r' -> builder.append('\r')
                        't' -> builder.append('\t')
                        'u' -> {
                            builder.append(text.substring(position, position + 4).toInt(16).toChar())
                            position += 4
                        }
                        else -> error("Unsupported escape sequence '\\$escaped'.")
                    }
                    else -> builder.append(character)
                }
            }
        }

        private fun skipWhitespace() {
            while (position < text.length && text[position].isWhitespace())
                position++
        }
    }
}
