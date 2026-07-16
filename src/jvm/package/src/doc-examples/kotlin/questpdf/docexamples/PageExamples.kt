package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import com.questpdf.infrastructure.Unit
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PageExamples : DocExample() {

    @Test
    fun simple() {
        Document
            .create {
                page {
                    size(PageSizes.A5)
                    margin(2f, Unit.Centimetre)
                    defaultTextStyle { fontSize(24f) }

                    header()
                        .text("Hello, World!")
                        .fontSize(48f).bold()

                    content()
                        .paddingVertical(25f)
                        .text(Placeholders.loremIpsum())
                        .justify()

                    footer()
                        .alignCenter()
                        .text {
                            currentPageNumber()
                            span(" / ")
                            totalPages()
                        }
                }
            }
            .generateImages({ output("page-simple.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun mainSlots() {
        Document
            .create {
                page {
                    size(PageSizes.A4)
                    margin(2f, Unit.Centimetre)
                    defaultTextStyle { fontSize(24f) }

                    header()
                        .background(Colors.Grey.Lighten1)
                        .height(125f)
                        .alignCenter()
                        .alignMiddle()
                        .text("Header")

                    content()
                        .background(Colors.Grey.Lighten2)
                        .alignCenter()
                        .alignMiddle()
                        .text("Content")

                    footer()
                        .background(Colors.Grey.Lighten1)
                        .height(75f)
                        .alignCenter()
                        .alignMiddle()
                        .text("Footer")
                }
            }
            .generateImages({ output("page-main-slots.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun foreground() {
        Document
            .create {
                page {
                    size(PageSizes.A4)
                    margin(2f, Unit.Centimetre)
                    defaultTextStyle { fontSize(20f) }

                    header()
                        .paddingBottom(1f, Unit.Centimetre)
                        .text("Report")
                        .fontSize(30f)
                        .bold()

                    content()
                        .text(Placeholders.paragraphs())
                        .paragraphSpacing(1f, Unit.Centimetre)
                        .justify()

                    foreground().svg(resource("draft-foreground.svg")).fitArea()
                }
            }
            .generateImages({ output("page-foreground.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }

    @Test
    fun background() {
        Document
            .create {
                page {
                    size(PageSizes.A4.landscape())

                    background().svg(resource("certificate-background.svg")).fitArea()

                    content()
                        .paddingLeft(10f, Unit.Centimetre)
                        .paddingRight(5f, Unit.Centimetre)
                        .alignMiddle()
                        .column {
                            item().height(50f).svg(resource("questpdf-logo.svg"))

                            item().height(50f)

                            item().text("CERTIFICATE").fontSize(64f).extraBlack()

                            item().height(25f)

                            item()
                                .shrink().borderBottom(1f).padding(10f)
                                .text("Marcin Ziąbek").fontSize(32f).italic()

                            item().height(10f)

                            item()
                                .text("has successfully completed the course \"QuestPDF Basics\" on ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))}.")
                                .fontSize(20f).light()
                        }
                }
            }
            .generateImages({ output("page-background.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
