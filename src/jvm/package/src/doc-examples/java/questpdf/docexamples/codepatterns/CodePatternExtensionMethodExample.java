package questpdf.docexamples.codepatterns;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

public class CodePatternExtensionMethodExample extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(600f, 0f));
                    page.maxSize(new PageSize(600f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(14f));
                    page.margin(25f);

                    page.content()
                        .border(1f)
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                columns.relativeColumn(2f);
                                columns.relativeColumn(3f);
                                columns.relativeColumn(2f);
                                columns.relativeColumn(3f);
                            });

                            tableLabelCell(table.cell(), "Product name");
                            tableValueCell(table.cell()).text(Placeholders.label());

                            tableLabelCell(table.cell(), "Description");
                            tableValueCell(table.cell()).text(Placeholders.sentence());

                            tableLabelCell(table.cell(), "Price");
                            tableValueCell(table.cell()).text(Placeholders.price());

                            tableLabelCell(table.cell(), "Date of production");
                            tableValueCell(table.cell()).text(Placeholders.shortDate());

                            tableLabelCell(table.cell().columnSpan(2), "Photo of the product");
                            // Image(Func<ImageSize, byte[]>) is not bridged; the payload-based delegate overload is used instead.
                            tableValueCell(table.cell().columnSpan(2)).aspectRatio(16 / 9f).image(payload -> Placeholders.image(payload.getImageSize()));
                        });
                });
            })
            .generateImages(index -> output("code-pattern-extension-methods.webp"), settings);
    }

    // The C# extension methods on IContainer become private static methods taking the container.
    private static IContainer tableCellStyle(IContainer container, Color backgroundColor) {
        return container
            .border(1f)
            .borderColor(Colors.getBlack())
            .background(backgroundColor)
            .padding(10f);
    }

    private static void tableLabelCell(IContainer container, String text) {
        tableCellStyle(container, Colors.Grey.getLighten3())
            .text(text)
            .bold();
    }

    private static IContainer tableValueCell(IContainer container) {
        return tableCellStyle(container, Colors.getTransparent());
    }
}
