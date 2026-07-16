package questpdf.docexamples.codepatterns;

import com.questpdf.fluent.ColumnDescriptor;
import com.questpdf.fluent.Document;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.IComponent;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

public class CodePatternAddressComponentExample extends DocExample {

    @Test
    public void example() {
        var address = new Address(
            "Apple",
            "95014",
            "United States",
            "Cupertino",
            "One Apple Park Way"
        );

        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(600f, 1200f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .component(new AddressComponent(address));
                });
            })
            .generateImages(index -> output("code-pattern-component-address.webp"), settings);
    }

    public record Address(
        String companyName,
        String postalCode,
        String country,
        String city,
        String street
    ) {}

    public static class AddressComponent implements IComponent {

        private final Address address;

        public AddressComponent(Address address) {
            this.address = address;
        }

        @Override
        public void compose(IContainer container) {
            container
                .column(column -> {
                    column.spacing(10f);

                    addItem(column, "Company name", address.companyName());
                    addItem(column, "Postal code", address.postalCode());
                    addItem(column, "Country", address.country());
                    addItem(column, "City", address.city());
                    addItem(column, "Street", address.street());
                });
        }

        private static void addItem(ColumnDescriptor column, String label, String value) {
            column.item().text(text -> {
                text.span(label + ": ").bold();
                text.span(value);
            });
        }
    }
}
