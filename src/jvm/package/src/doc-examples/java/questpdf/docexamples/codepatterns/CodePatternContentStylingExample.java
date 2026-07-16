package questpdf.docexamples.codepatterns;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import com.questpdf.infrastructure.TextStyle;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

public class CodePatternContentStylingExample extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(650f, 0f));
                    page.maxSize(new PageSize(650f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                columns.constantColumn(50f);
                                columns.relativeColumn(1f);
                                columns.relativeColumn(2f);
                            });

                            table.header(header -> {
                                // The chainable Element(Func<IContainer, IContainer>) overload is not bridged;
                                // the style helper is applied as a private static method instead.
                                headerStyle(header.cell()).text("#");
                                headerStyle(header.cell()).text("Product Name");
                                headerStyle(header.cell()).text("Description");
                            });

                            for (var i = 1; i <= 5; i++) {
                                contentStyle(table.cell()).text(String.valueOf(i));
                                contentStyle(table.cell()).text(Placeholders.label());
                                contentStyle(table.cell()).text(Placeholders.sentence());
                            }
                        });
                });
            })
            .generateImages(index -> output("code-pattern-content-styling.webp"), settings);
    }

    private static IContainer headerStyle(IContainer container) {
        return container
            .background(Colors.Blue.getLighten5())
            .padding(10f)
            .defaultTextStyle(TextStyle.getDefault().fontColor(Colors.Blue.getDarken4()).bold());
    }

    private static IContainer contentStyle(IContainer container) {
        return container
            .borderTop(2f)
            .borderColor(Colors.Blue.getLighten3())
            .padding(10f);
    }
}
