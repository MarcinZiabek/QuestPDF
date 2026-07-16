package questpdf.docexamples.text

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import com.questpdf.infrastructure.TextStyle
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class TextBasicExamples : DocExample() {

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
                        .text("Sample text")
                }
            }
            .generateImages({ output("text-basic.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun basicWithStyle() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; an extension function is the direct equivalent.
                            fun IContainer.cellStyle(): IContainer =
                                background(Colors.Grey.Lighten3).padding(10f)

                            item()
                                .cellStyle()
                                .text("Text with blue color")
                                .fontColor(Colors.Blue.Darken1)

                            item()
                                .cellStyle()
                                .text("Bold and underlined text")
                                .bold()
                                .underline()

                            item()
                                .cellStyle()
                                .text("Centered small text")
                                .fontSize(12f)
                                .alignCenter()
                        }
                }
            }
            .generateImages({ output("text-basic-descriptor.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun rich() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            alignCenter()

                            span("The ")
                            span("chemical formula").underline()
                            span(" of ")
                            span("sulfuric acid").backgroundColor(Colors.Amber.Lighten3)
                            span(" is H")
                            span("2").subscript()
                            span("SO")
                            span("4").subscript()
                            span(".")
                        }
                }
            }
            .generateImages({ output("text-rich.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun styleInheritance() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .defaultTextStyle { fontSize(20f) }
                        .column {
                            spacing(10f)

                            item().text("Products").extraBold().underline().decorationThickness(2f)

                            item().text("Comments: " + Placeholders.sentence())

                            item()
                                .defaultTextStyle { fontSize(14f) }
                                .table {
                                    columnsDefinition {
                                        constantColumn(30f)
                                        relativeColumn(1f)
                                        relativeColumn(2f)
                                    }

                                    header {
                                        // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                                        // overload, which is not bridged; an extension function is the direct equivalent.
                                        fun IContainer.style(): IContainer =
                                            background(Colors.Grey.Lighten3)
                                                .borderBottom(1f)
                                                .paddingHorizontal(5f)
                                                .paddingVertical(10f)
                                                .defaultTextStyle { bold().fontColor(Colors.Blue.Medium) }

                                        cell().style().text("ID")
                                        cell().style().text("Name")
                                        cell().style().text("Description")
                                    }

                                    // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                                    // overload, which is not bridged; an extension function is the direct equivalent.
                                    fun IContainer.style(): IContainer =
                                        padding(5f)

                                    for (i in 0 until 5) {
                                        cell().style().text(i.toString()).bold()
                                        cell().style().text(Placeholders.label())
                                        cell().style().text(Placeholders.sentence())
                                    }
                                }
                        }
                }
            }
            .generateImages({ output("text-style-inheritance.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun pageNumber() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .extend()
                        .placeholder()

                    footer()
                        .paddingTop(25f)
                        .alignCenter()
                        .text("3 / 10")
                        // .text {
                        //     currentPageNumber()
                        //     span(" / ")
                        //     totalPages()
                        // }
                }
            }
            .generateImages({ output("text-page-number.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun pageNumberFormat() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    fun formatWithLeadingZeros(pageNumber: Int?): String {
                        val expectedLength = 3
                        val number = pageNumber ?: 1
                        return number.toString().padStart(expectedLength, '0')
                    }

                    content()
                        .text {
                            currentPageNumber().format(::formatWithLeadingZeros)
                        }
                }
            }
            .generateImages({ output("text-page-number-format.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun hyperlink() {
        Document
            .create {
                page {
                    size(PageSizes.A6.landscape())
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            val hyperlinkStyle = TextStyle.Default
                                .fontColor(Colors.Blue.Medium)
                                .underline()

                            span("To learn more about QuestPDF, please visit its ")
                            hyperlink("homepage", "https://www.questpdf.com/").style(hyperlinkStyle)
                            span(", ")
                            hyperlink("GitHub repository", "https://github.com/QuestPDF/QuestPDF").style(hyperlinkStyle)
                            span(" and ")
                            hyperlink("NuGet package page", "https://www.nuget.org/packages/QuestPDF").style(hyperlinkStyle)
                            span(".")
                        }
                }
            }
            .generatePdf(output("text-hyperlink.pdf"))
    }
}
