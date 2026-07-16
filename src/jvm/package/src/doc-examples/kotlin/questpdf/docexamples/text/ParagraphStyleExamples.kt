package questpdf.docexamples.text

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class ParagraphStyleExamples : DocExample() {

    @Test
    fun defaultTextStyle() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            defaultTextStyle { light().letterSpacing(-0.1f).wordSpacing(0.1f) }

                            span("Changing typography settings helps creating ")
                            span("significant").letterSpacing(0.2f).black().backgroundColor(Colors.Grey.Lighten2)
                            span(" visual contrast.")
                        }
                }
            }
            .generateImages({ output("text-paragraph-default-style.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun textAlignment() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(20f)

                            fun cellStyle(container: IContainer): IContainer =
                                container.background(Colors.Grey.Lighten3).padding(10f)

                            cellStyle(item())
                                .text("This is an example of left-aligned text, showcasing how the text starts from the left margin and continues naturally across the container.")
                                .alignLeft()

                            cellStyle(item())
                                .text("This text is centered within its container, creating a balanced look, especially for titles or headers.")
                                .alignCenter()

                            cellStyle(item())
                                .text("This example demonstrates right-aligned text, often used for dates, numbers, or aligning text to the right margin.")
                                .alignRight()

                            cellStyle(item())
                                .text("Justified text adjusts the spacing between words so that both the left and right edges of the text block are aligned, creating a clean, newspaper-like look.")
                                .justify()
                        }
                }
            }
            .generateImages({ output("text-paragraph-alignment.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun firstLineIndentation() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1200f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text(Placeholders.paragraphs())
                        .paragraphFirstLineIndentation(40f)
                }
            }
            .generateImages({ output("text-paragraph-first-line-indentation.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }

    @Test
    fun spacing() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1200f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text(Placeholders.paragraphs())
                        .paragraphSpacing(10f)
                }
            }
            .generateImages({ output("text-paragraph-spacing.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }

    @Test
    fun clampLines() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            val paragraph = Placeholders.paragraph()

                            item()
                                .background(Colors.Grey.Lighten3)
                                .padding(5f)
                                .text(paragraph)

                            item()
                                .background(Colors.Grey.Lighten3)
                                .padding(5f)
                                .text(paragraph)
                                .clampLines(3)
                        }
                }
            }
            .generateImages({ output("text-paragraph-clamp-lines.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun clampLinesWithCustomEllipsis() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text(Placeholders.paragraph())
                        .clampLines(3, " [...]")
                }
            }
            .generateImages({ output("text-paragraph-clamp-lines-custom-ellipsis.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
