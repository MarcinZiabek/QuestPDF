package questpdf.docexamples.codepatterns

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import com.questpdf.infrastructure.TextStyle
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class CodePatternContentStylingExample : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(650f, 0f))
                    maxSize(PageSize(650f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .table {
                            columnsDefinition {
                                constantColumn(50f)
                                relativeColumn(1f)
                                relativeColumn(2f)
                            }

                            header {
                                // The chainable Element(Func<IContainer, IContainer>) overload is not bridged;
                                // the style helper is applied as a local extension function instead.
                                fun IContainer.style(): IContainer {
                                    return background(Colors.Blue.Lighten5)
                                        .padding(10f)
                                        .defaultTextStyle(TextStyle.Default.fontColor(Colors.Blue.Darken4).bold())
                                }

                                cell().style().text("#")
                                cell().style().text("Product Name")
                                cell().style().text("Description")
                            }

                            fun IContainer.style(): IContainer {
                                return borderTop(2f)
                                    .borderColor(Colors.Blue.Lighten3)
                                    .padding(10f)
                            }

                            for (i in 1..5) {
                                cell().style().text(i.toString())
                                cell().style().text(Placeholders.label())
                                cell().style().text(Placeholders.sentence())
                            }
                        }
                }
            }
            .generateImages({ output("code-pattern-content-styling.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
