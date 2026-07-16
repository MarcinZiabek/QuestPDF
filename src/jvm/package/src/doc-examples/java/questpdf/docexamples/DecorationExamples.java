package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class DecorationExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(350f, 0f));
                    page.maxSize(new PageSize(350f, 300f));
                    page.margin(25f);
                    page.defaultTextStyle(style -> style.fontSize(20f));

                    page.content()
                        .background(Colors.Grey.getLighten3())
                        .padding(15f)
                        .decoration(decoration -> {
                            decoration.before()
                                .defaultTextStyle(style -> style.bold())
                                .column(column -> {
                                    column.item().showOnce().text("Customer Instructions:");
                                    column.item().skipOnce().text("Customer Instructions [continued]:");
                                });

                            decoration.content()
                                .paddingTop(10f)
                                .text("Please wrap the item in elegant gift paper and include a small blank card for a personal message. If possible, remove any price tags or invoices from the package. Make sure the wrapping is secure but easy to open without damaging the contents.");
                        });
                });
            })
            .generateImages(index -> output("decoration-" + index + ".webp"), settings);
    }
}
