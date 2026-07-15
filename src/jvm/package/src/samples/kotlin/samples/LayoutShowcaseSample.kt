package samples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.BoxShadowStyle
import com.questpdf.infrastructure.IContainer

/**
 * Breadth tour of layout primitives: constraints, alignment, decoration,
 * layers, inlined flow, multi-column, rotation/scaling, styled boxes with
 * gradients/shadows/corner radii, conditional visibility, and images.
 */
object LayoutShowcaseSample {

    fun run(): ByteArray {
        val document = Document.create {
            page {
                size(PageSizes.Letter)
                margin(32f)
                defaultTextStyle { fontSize(9f) }

                header().headerBanner()

                content().paddingVertical(12f).column {
                    spacing(16f)

                    item().labelled("Constraints") {
                        row {
                            spacing(8f)
                            constantItem(120f).height(40f).background(Colors.Cyan.Lighten4)
                                .alignCenter().alignMiddle().text("120pt fixed")
                            relativeItem().minHeight(40f).maxHeight(60f).background(Colors.Cyan.Lighten3)
                                .alignCenter().alignMiddle().text("min 40 / max 60")
                            autoItem().height(40f).padding(4f).background(Colors.Cyan.Lighten2)
                                .alignMiddle().text("auto-sized")
                        }
                    }

                    item().labelled("Styled boxes") {
                        row {
                            spacing(10f)
                            relativeItem()
                                .height(52f)
                                .backgroundLinearGradient(45f, arrayOf(Colors.Purple.Lighten3, Colors.Pink.Lighten3))
                                .cornerRadius(8f)
                                .alignCenter().alignMiddle()
                                .text("gradient + radius")
                            relativeItem()
                                .height(52f)
                                .background(Colors.White)
                                .border(1f)
                                .borderColor(Colors.Grey.Lighten1)
                                .shadow(
                                    BoxShadowStyle().apply {
                                        blur = 6f
                                        offsetX = 2f
                                        offsetY = 2f
                                        color = Colors.Grey.Medium
                                    },
                                )
                                .alignCenter().alignMiddle()
                                .text("box shadow")
                            relativeItem()
                                .height(52f)
                                .borderLinearGradient(90f, arrayOf(Colors.Orange.Medium, Colors.Red.Medium))
                                .border(2f)
                                .cornerRadiusTopLeft(12f)
                                .cornerRadiusBottomRight(12f)
                                .alignCenter().alignMiddle()
                                .text("gradient border")
                        }
                    }

                    item().labelled("Layers") {
                        layers {
                            layer().aspectRatio(4f).background(Colors.BlueGrey.Lighten5)
                            primaryLayer().padding(14f).column {
                                item().text("Primary layer content sits above the tinted background layer.")
                                item().text("Watermarks and stamps are additional layers.").fontColor(Colors.Grey.Darken1)
                            }
                            layer().alignRight().alignTop().padding(6f)
                                .rotate(8f)
                                .text("DRAFT").fontSize(18f).extraBold().fontColor(Colors.Red.Lighten2)
                        }
                    }

                    item().labelled("Inlined flow") {
                        inlined {
                            spacing(6f)
                            alignLeft()
                            baselineMiddle()
                            for (index in 1..14) {
                                val shade = if (index % 3 == 0) Colors.Green.Lighten2 else Colors.Green.Lighten4
                                item()
                                    .width(30f + (index % 5) * 14f)
                                    .height(18f)
                                    .background(shade)
                                    .alignCenter().alignMiddle()
                                    .text("$index")
                            }
                        }
                    }

                    item().labelled("Multi-column") {
                        multiColumn {
                            columns(3)
                            spacing(12f)
                            content().column {
                                spacing(6f)
                                repeat(3) {
                                    item().text(Placeholders.sentence())
                                }
                            }
                        }
                    }

                    item().labelled("Transforms") {
                        row {
                            spacing(20f)
                            constantItem(90f).aspectRatio(1f).scale(0.8f).background(Colors.Amber.Lighten3)
                                .alignCenter().alignMiddle().text("scaled 80%")
                            constantItem(90f).aspectRatio(1f).rotateLeft().background(Colors.Lime.Lighten3)
                                .alignCenter().alignMiddle().text("rotated left")
                            constantItem(90f).aspectRatio(1f).flipHorizontal().background(Colors.Teal.Lighten4)
                                .alignCenter().alignMiddle().text("flipped")
                            relativeItem().unconstrained().translateForDemo()
                        }
                    }

                    item().labelled("Conditional + media") {
                        row {
                            spacing(10f)
                            relativeItem().showIf { it.pageNumber == 1 }.background(Colors.Blue.Lighten5)
                                .padding(6f).text("Visible on page 1 only (predicate).")
                            relativeItem().showIf(true).padding(6f).text("Always shown (boolean).")
                            constantItem(120f).image(Placeholders.image(240, 120)).fitWidth()
                            constantItem(60f).svg(TICK_SVG).fitArea()
                        }
                    }
                }

                footer().alignRight().text {
                    span("Layout showcase — ")
                    currentPageNumber()
                    span("/")
                    totalPages()
                }
            }
        }

        return document.generatePdf()
    }

    private const val TICK_SVG =
        """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M4 12l5 5L20 6"/></svg>"""

    private fun IContainer.headerBanner() {
        background(Colors.BlueGrey.Darken3)
            .padding(12f)
            .row {
                relativeItem().text("Layout Showcase").fontSize(15f).semiBold().fontColor(Colors.White)
                relativeItem().alignRight().alignMiddle()
                    .text(Placeholders.shortDate()).fontColor(Colors.BlueGrey.Lighten4)
            }
    }

    private fun IContainer.labelled(title: String, block: IContainer.() -> Unit) {
        column {
            item().text(title).semiBold().fontSize(10f).fontColor(Colors.BlueGrey.Darken2)
            item().paddingTop(4f).element(block)
        }
    }

    private fun IContainer.translateForDemo(): IContainer =
        padding(10f).background(Colors.DeepOrange.Lighten4).padding(4f)
}

fun main() {
    println("LayoutShowcaseSample ran to completion: ${LayoutShowcaseSample.run().size} bytes")
}
