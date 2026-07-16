package questpdf.docexamples.codepatterns;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.EmptyContainer;
import com.questpdf.infrastructure.IComponent;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

import java.util.ArrayList;
import java.util.List;

public class CodePatternConfigurableComponentExample extends DocExample {

    @Test
    public void example() {
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
                        .component(buildSampleSection());
                });
            })
            .generateImages(index -> output("code-pattern-component-configurable.webp"), settings);
    }

    private static IComponent buildSampleSection() {
        var section = new SectionComponent();

        section.text("Product name", Placeholders.label());
        section.text("Description", Placeholders.sentence());
        section.text("Price", Placeholders.price());
        section.text("Date of production", Placeholders.shortDate());
        section.image("Photo of the product", resource("product.jpg"));
        section.custom("Status").text("Accepted").fontColor(Colors.Green.getDarken2()).bold();

        return section;
    }

    public static class SectionComponent implements IComponent {

        private record Field(String label, IContainer content) {}

        private final List<Field> fields = new ArrayList<>();

        @Override
        public void compose(IContainer container) {
            container
                .border(1f)
                .column(column -> {
                    for (var field : fields) {
                        column.item().row(row -> {
                            row.relativeItem()
                                .border(1f)
                                .borderColor(Colors.Grey.getMedium())
                                .background(Colors.Grey.getLighten3())
                                .padding(10f)
                                .text(field.label());

                            row.relativeItem(2f)
                                .border(1f)
                                .borderColor(Colors.Grey.getMedium())
                                .padding(10f)
                                .element(field.content());
                        });
                    }
                });
        }

        public void text(String label, String text) {
            custom(label).text(text);
        }

        public void image(String label, String imagePath) {
            custom(label).image(imagePath);
        }

        public IContainer custom(String label) {
            var content = EmptyContainer.create();
            fields.add(new Field(label, content));
            return content;
        }
    }
}
