package questpdf.docexamples.codepatterns

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
import questpdf.docexamples.DocExample

class CodePatternExtensionMethodExample : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(600f, 0f))
                    maxSize(PageSize(600f, 1000f))
                    defaultTextStyle { fontSize(14f) }
                    margin(25f)

                    content()
                        .border(1f)
                        .table {
                            columnsDefinition {
                                relativeColumn(2f)
                                relativeColumn(3f)
                                relativeColumn(2f)
                                relativeColumn(3f)
                            }

                            cell().tableLabelCell("Product name")
                            cell().tableValueCell().text(Placeholders.label())

                            cell().tableLabelCell("Description")
                            cell().tableValueCell().text(Placeholders.sentence())

                            cell().tableLabelCell("Price")
                            cell().tableValueCell().text(Placeholders.price())

                            cell().tableLabelCell("Date of production")
                            cell().tableValueCell().text(Placeholders.shortDate())

                            cell().columnSpan(2u).tableLabelCell("Photo of the product")
                            // Image(Func<ImageSize, byte[]>) is not bridged; the payload-based delegate overload is used instead.
                            cell().columnSpan(2u).tableValueCell().aspectRatio(16 / 9f).image { payload -> Placeholders.image(payload.imageSize) }
                        }
                }
            }
            .generateImages({ output("code-pattern-extension-methods.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}

private fun IContainer.tableCellStyle(backgroundColor: Color): IContainer {
    return border(1f)
        .borderColor(Colors.Black)
        .background(backgroundColor)
        .padding(10f)
}

private fun IContainer.tableLabelCell(text: String) {
    tableCellStyle(Colors.Grey.Lighten3)
        .text(text)
        .bold()
}

private fun IContainer.tableValueCell(): IContainer {
    return tableCellStyle(Colors.Transparent)
}
