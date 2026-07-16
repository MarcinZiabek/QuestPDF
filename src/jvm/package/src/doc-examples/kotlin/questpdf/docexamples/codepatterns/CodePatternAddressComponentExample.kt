package questpdf.docexamples.codepatterns

import com.questpdf.fluent.Document
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.IComponent
import com.questpdf.infrastructure.IContainer
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class CodePatternAddressComponentExample : DocExample() {

    @Test
    fun example() {
        val address = Address(
            companyName = "Apple",
            postalCode = "95014",
            country = "United States",
            city = "Cupertino",
            street = "One Apple Park Way",
        )

        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(600f, 1200f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .component(AddressComponent(address))
                }
            }
            .generateImages({ output("code-pattern-component-address.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    class Address(
        val companyName: String,
        val postalCode: String,
        val country: String,
        val city: String,
        val street: String,
    )

    class AddressComponent(private val address: Address) : IComponent {

        override fun compose(container: IContainer) {
            container
                .column {
                    spacing(10f)

                    fun addItem(label: String, value: String) {
                        item().text {
                            span("$label: ").bold()
                            span(value)
                        }
                    }

                    addItem("Company name", address.companyName)
                    addItem("Postal code", address.postalCode)
                    addItem("Country", address.country)
                    addItem("City", address.city)
                    addItem("Street", address.street)
                }
        }
    }
}
