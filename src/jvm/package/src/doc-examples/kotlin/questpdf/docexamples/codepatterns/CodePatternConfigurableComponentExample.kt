package questpdf.docexamples.codepatterns

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.EmptyContainer
import com.questpdf.infrastructure.IComponent
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class CodePatternConfigurableComponentExample : DocExample() {

    @Test
    fun example() {
        fun buildSampleSection(): IComponent {
            val section = SectionComponent()

            section.text("Product name", Placeholders.label())
            section.text("Description", Placeholders.sentence())
            section.text("Price", Placeholders.price())
            section.text("Date of production", Placeholders.shortDate())
            section.image("Photo of the product", resource("product.jpg"))
            section.custom("Status").text("Accepted").fontColor(Colors.Green.Darken2).bold()

            return section
        }

        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1200f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .component(buildSampleSection())
                }
            }
            .generateImages({ output("code-pattern-component-configurable.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    class SectionComponent : IComponent {

        private val fields = mutableListOf<Pair<String, IContainer>>()

        override fun compose(container: IContainer) {
            container
                .border(1f)
                .column {
                    for ((label, content) in fields) {
                        item().row {
                            relativeItem()
                                .border(1f)
                                .borderColor(Colors.Grey.Medium)
                                .background(Colors.Grey.Lighten3)
                                .padding(10f)
                                .text(label)

                            relativeItem(2f)
                                .border(1f)
                                .borderColor(Colors.Grey.Medium)
                                .padding(10f)
                                .element(content)
                        }
                    }
                }
        }

        fun text(label: String, text: String) {
            custom(label).text(text)
        }

        fun image(label: String, imagePath: String) {
            custom(label).image(imagePath)
        }

        fun custom(label: String): IContainer {
            val content = EmptyContainer.create()
            fields.add(label to content)
            return content
        }
    }
}
