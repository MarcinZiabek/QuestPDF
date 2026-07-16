package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.Color
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor
import kotlin.math.round
import kotlin.random.Random

class TableExamples : DocExample() {

    @Test
    fun basic() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .table {
                            columnsDefinition {
                                constantColumn(50f)
                                relativeColumn()
                                constantColumn(125f)
                            }

                            header {
                                cell().borderBottom(2f).padding(8f).text("#")
                                cell().borderBottom(2f).padding(8f).text("Product")
                                cell().borderBottom(2f).padding(8f).alignRight().text("Price")
                            }

                            for (i in 0 until 6) {
                                // The Kotlin counterpart of C# Math.Round(Random.Shared.NextDouble() * 100, 2).
                                val price = round(Random.nextDouble() * 100 * 100) / 100

                                cell().padding(8f).text("${i + 1}")
                                cell().padding(8f).text(Placeholders.label())
                                cell().padding(8f).alignRight().text("\$$price")
                            }
                        }
                }
            }
            .generateImages({ output("table-simple.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun cellStyleExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    val weatherIcons = arrayOf("cloudy.svg", "lightning.svg", "pouring.svg", "rainy.svg", "snowy.svg", "windy.svg")

                    content()
                        .table {
                            columnsDefinition {
                                relativeColumn()
                                constantColumn(125f)
                                constantColumn(125f)
                            }

                            header {
                                // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                                // overload, which is not bridged; an extension function is the direct equivalent.
                                fun IContainer.cellStyle(): IContainer {
                                    return background(Colors.Blue.Darken2)
                                        .defaultTextStyle { fontColor(Colors.White).bold() }
                                        .paddingVertical(8f)
                                        .paddingHorizontal(16f)
                                }

                                cell().cellStyle().text("Day")
                                cell().cellStyle().alignCenter().text("Weather")
                                cell().cellStyle().alignRight().text("Temp")
                            }

                            for (i in 0 until 7) {
                                val weatherIndex = Random.nextInt(0, weatherIcons.size)

                                fun IContainer.cellStyle(): IContainer {
                                    val backgroundColor = if (i % 2 == 0)
                                        Colors.Blue.Lighten5
                                    else
                                        Colors.Blue.Lighten4

                                    return background(backgroundColor)
                                        .paddingVertical(8f)
                                        .paddingHorizontal(16f)
                                }

                                cell().cellStyle()
                                    .text(LocalDate.of(2025, 2, 26).plusDays(i.toLong()).format(DateTimeFormatter.ofPattern("dd MMMM", Locale.ENGLISH)))

                                cell().cellStyle().alignCenter().height(24f)
                                    .svg(resource("WeatherIcons/${weatherIcons[weatherIndex]}"))

                                cell().cellStyle().alignRight()
                                    .text("${Random.nextInt(-10, 35)}°")
                            }
                        }
                }
            }
            .generateImages({ output("table-cell-style.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun overlappingCells() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(700f, 1000f))
                    defaultTextStyle { fontSize(16f) }
                    margin(25f)

                    val dayNames = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

                    content()
                        .border(1f)
                        .borderColor(Colors.Grey.Lighten1)
                        .table {
                            columnsDefinition {
                                // hour column
                                constantColumn(60f)

                                // day columns
                                repeat(5) {
                                    relativeColumn()
                                }
                            }

                            // even/odd columns background
                            for (column in 0 until 7) {
                                val backgroundColor = if (column % 2 == 0) Colors.Grey.Lighten3 else Colors.White
                                cell().column(column.toUInt()).rowSpan(24u).background(backgroundColor)
                            }

                            // hours and hour lines
                            for (hour in 6 until 16) {
                                cell().column(1u).row(hour.toUInt())
                                    .paddingVertical(5f).paddingHorizontal(10f).alignRight()
                                    .text("$hour")

                                cell().row(hour.toUInt()).columnSpan(6u)
                                    .border(1f).borderColor(Colors.Grey.Lighten1).height(20f)
                            }

                            // dates and day names
                            for (i in 0 until 5) {
                                cell()
                                    .column(i.toUInt() + 2u).row(1u).padding(5f)
                                    .column {
                                        item().alignCenter().text("${17 + i}").fontSize(24f).bold()
                                        item().alignCenter().text(dayNames[i]).light()
                                    }
                            }

                            fun addEvent(day: UInt, hour: UInt, length: UInt, name: String, backgroundColor: Color, textColor: Color) {
                                cell()
                                    .column(day + 1u).row(hour).rowSpan(length)
                                    .padding(5f).background(backgroundColor).padding(5f)
                                    .alignCenter().alignMiddle()
                                    .text(name).fontColor(textColor)
                            }

                            // standup events
                            for (i in 1..4)
                                addEvent(i.toUInt(), 8u, 1u, "Standup", Colors.Blue.Lighten4, Colors.Blue.Darken3)

                            // other events
                            addEvent(2u, 11u, 2u, "Interview", Colors.Red.Lighten4, Colors.Red.Darken3)
                            addEvent(3u, 12u, 3u, "Demo", Colors.Red.Lighten4, Colors.Red.Darken3)
                            addEvent(5u, 5u, 17u, "PTO", Colors.Green.Lighten4, Colors.Green.Darken3)
                        }
                }
            }
            .generateImages({ output("table-overlapping-cells.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun manualCellPlacement() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(700f, 1000f))
                    defaultTextStyle { fontSize(16f) }
                    margin(25f)

                    content()
                        .table {
                            columnsDefinition {
                                constantColumn(75f)
                                constantColumn(150f)
                                constantColumn(200f)
                                constantColumn(200f)
                            }

                            // The C# original routes the cell styles through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; extension functions are the direct equivalent.
                            fun IContainer.cellStyle(color: Color): IContainer =
                                border(1f).background(color).paddingHorizontal(10f).paddingVertical(15f).alignCenter().alignMiddle()

                            fun IContainer.headerCellStyle(): IContainer =
                                cellStyle(Colors.Grey.Lighten4)

                            fun IContainer.goodCellStyle(): IContainer =
                                cellStyle(Colors.Green.Lighten4).defaultTextStyle { fontColor(Colors.Green.Darken2) }

                            fun IContainer.badCellStyle(): IContainer =
                                cellStyle(Colors.Red.Lighten4).defaultTextStyle { fontColor(Colors.Red.Darken2) }

                            cell().row(1u).column(3u).columnSpan(2u)
                                .headerCellStyle()
                                .text("Predicted condition").bold()

                            cell().row(3u).column(1u).rowSpan(2u)
                                .headerCellStyle().rotateLeft()
                                .text("Actual\ncondition").bold().alignCenter()

                            cell().row(2u).column(3u)
                                .headerCellStyle()
                                .text("Positive (PP)")

                            cell().row(2u).column(4u)
                                .headerCellStyle()
                                .text("Negative (PN)")

                            cell().row(3u).column(2u)
                                .headerCellStyle().text("Positive (P)")

                            cell().row(4u).column(2u)
                                .headerCellStyle()
                                .text("Negative (N)")

                            cell()
                                .row(3u).column(3u).goodCellStyle()
                                .text("True positive (TP)")

                            cell()
                                .row(3u).column(4u).badCellStyle()
                                .text("False negative (FN)")

                            cell().row(4u).column(3u)
                                .badCellStyle().text("False positive (FP)")

                            cell().row(4u).column(4u)
                                .goodCellStyle().text("True negative (TN)")
                        }
                }
            }
            .generateImages({ output("table-manual-cell-placement.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun columnsDefinition() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(700f, 1000f))
                    defaultTextStyle { fontSize(16f) }
                    margin(25f)

                    content()
                        .width(450f)
                        .table {
                            columnsDefinition {
                                constantColumn(150f)
                                relativeColumn(2f)
                                relativeColumn(3f)
                            }

                            // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; an extension function is the direct equivalent.
                            fun IContainer.cellStyle(): IContainer =
                                border(1f).padding(10f)

                            cell().columnSpan(3u)
                                .background(Colors.Grey.Lighten2).cellStyle()
                                .text("Total width: 450px")

                            cell().cellStyle().text("Constant: 150px")
                            cell().cellStyle().text("Relative: 2*")
                            cell().cellStyle().text("Relative: 3*")

                            cell().cellStyle().text("150px")
                            cell().cellStyle().text("120px")
                            cell().cellStyle().text("180px")
                        }
                }
            }
            .generateImages({ output("table-columns-definition.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

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
                        .table {
                            val pageSizes = listOf(
                                Triple("Letter (ANSI A)", 8.5, 11.0),
                                Triple("Legal", 8.5, 14.0),
                                Triple("Ledger (ANSI B)", 11.0, 17.0),
                                Triple("Tabloid (ANSI B)", 17.0, 11.0),
                                Triple("ANSI C", 22.0, 17.0),
                                Triple("ANSI D", 34.0, 22.0),
                                Triple("ANSI E", 44.0, 34.0),
                            )

                            val inchesToPoints = 72

                            // The C# original routes the cell styles through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; extension functions are the direct equivalent.
                            // (C# declares backgroundColor as string, relying on implicit Color<->string conversions.)
                            fun IContainer.defaultCellStyle(backgroundColor: Color): IContainer {
                                return border(1f)
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
                                // please be sure to call the 'header' handler!

                                // you can extend existing styles by creating additional methods
                                fun IContainer.cellStyle(): IContainer =
                                    defaultCellStyle(Colors.Grey.Lighten3)

                                cell().rowSpan(2u).cellStyle().extendHorizontal().alignLeft()
                                    .text("Document type").bold()

                                cell().columnSpan(2u).cellStyle().text("Inches").bold()
                                cell().columnSpan(2u).cellStyle().text("Points").bold()

                                cell().cellStyle().text("Width")
                                cell().cellStyle().text("Height")

                                cell().cellStyle().text("Width")
                                cell().cellStyle().text("Height")
                            }

                            for ((name, width, height) in pageSizes) {
                                fun IContainer.cellStyle(): IContainer =
                                    defaultCellStyle(Colors.White).showOnce()

                                cell().cellStyle().extendHorizontal().alignLeft().text(name)

                                // inches
                                cell().cellStyle().text(formatNumber(width))
                                cell().cellStyle().text(formatNumber(height))

                                // points
                                cell().cellStyle().text(formatNumber(width * inchesToPoints))
                                cell().cellStyle().text(formatNumber(height * inchesToPoints))
                            }
                        }
                }
            }
            .generateImages({ index -> output("table-header-and-footer-$index.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    /**
     * The string .NET produces for a double passed to Text(object): integral values
     * print without a fractional part (e.g. 612), non-integral ones with it (e.g. 8.5).
     */
    private fun formatNumber(value: Double): String =
        if (value == floor(value)) value.toLong().toString() else value.toString()
}
