package samples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.FontFeatures
import com.questpdf.helpers.Fonts
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.TextStyle

/**
 * Rich-text formatting tour: span styling, weights, decorations, positions,
 * paragraph controls, injected elements, hyperlinks, sections and page-number
 * fields.
 */
object TextShowcaseSample {

    fun run(): ByteArray {
        val document = Document.create {
            page {
                size(PageSizes.A5)
                margin(30f)
                defaultTextStyle(TextStyle.Default.fontSize(11f).fontFamily(Fonts.Georgia, Fonts.TimesNewRoman))

                content().column {
                    spacing(14f)

                    item().section("intro").text {
                        alignLeft()
                        paragraphSpacing(6f)
                        paragraphFirstLineIndentation(18f)

                        span("Typography, ").fontSize(24f).bold().fontColor(Colors.DeepPurple.Darken2)
                        span("the art of arranging type, ").italic()
                        span("covers weight, ")
                        span("width, ").extraBold()
                        span("slope ").light().italic()
                        span("and much more.")
                        emptyLine()

                        line("Each line() call ends the current paragraph.")
                        span("Spans continue it: ")
                        span("underlined, ").underline()
                        span("struck through, ").strikethrough()
                        span("overlined, ").overline()
                        span("wavy-decorated, ").underline().decorationWavy().decorationColor(Colors.Red.Medium)
                        span("dotted ").underline().decorationDotted()
                        span("and doubled.").underline().decorationDouble()
                    }

                    item().text {
                        defaultTextStyle(TextStyle.Default.fontSize(11f))
                        span("Chemistry needs H")
                        span("2").subscript()
                        span("O and math needs x")
                        span("2").superscript()
                        span(". Code reads best in ")
                        span("monospace").fontFamily(Fonts.Consolas).backgroundColor(Colors.Grey.Lighten3)
                        span(" with ")
                        span("ligatures").enableFontFeature(FontFeatures.StandardLigatures)
                        span(" enabled; spacing can be ")
                        span("letter-spaced").letterSpacing(0.3f)
                        span(" or ")
                        span("word  spaced").wordSpacing(2f)
                        span(".")
                    }

                    item().background(Colors.Grey.Lighten4).padding(8f).text {
                        clampLines(3, "… [truncated]")
                        span(Placeholders.loremIpsum())
                        span(" This paragraph clamps to three lines with a custom ellipsis.")
                    }

                    item().text {
                        justify()
                        defaultTextStyle { lineHeight(1.6f) }
                        span("Justified text with generous line height: ")
                        span(Placeholders.paragraph())
                        span(" — and a trailing remark for good measure.")
                    }

                    item().text {
                        span("Links: visit the ")
                        hyperlink("QuestPDF docs", "https://www.questpdf.com").fontColor(Colors.Blue.Darken1).underline()
                        span(" or jump back to the ")
                        sectionLink("intro section", "intro").fontColor(Colors.Teal.Darken1)
                        span(".")
                    }

                    item().text {
                        span("Inline elements sit on the baseline: ")
                        // The C# API declares element(handler, alignment = …) — the lambda is
                        // not the last parameter, so Kotlin's trailing-lambda syntax cannot
                        // bind here and named arguments are required. A genuine ergonomics
                        // finding of this prototype.
                        element(handler = {
                            // A single-child container takes exactly one fluent chain;
                            // the real engine rejects a second chain on the same receiver.
                            width(9f).height(9f).background(Colors.Green.Medium)
                        })
                        span(" like that green square, or centered: ")
                        element(
                            handler = {
                                width(9f).height(9f).background(Colors.Orange.Medium)
                            },
                            alignment = com.questpdf.infrastructure.TextInjectedElementAlignment.Middle,
                        )
                        span(" the orange one.")
                    }
                }

                footer().alignCenter().text {
                    defaultTextStyle(TextStyle.Default.fontSize(8f).fontColor(Colors.Grey.Darken1))
                    span("Text showcase — page ")
                    currentPageNumber()
                    span(" of ")
                    totalPages()
                    span(", section pages ")
                    beginPageNumberOfSection("intro")
                    span("–")
                    endPageNumberOfSection("intro")
                }
            }
        }

        return document.generatePdf()
    }
}

fun main() {
    println("TextShowcaseSample ran to completion: ${TextShowcaseSample.run().size} bytes")
}
