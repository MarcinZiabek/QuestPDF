package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class DefaultTextStyleExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));

                    page.content()
                        .width(400f)
                        .padding(25f)
                        .defaultTextStyle(style -> style.bold().underline())
                        .column(column -> {
                            column.spacing(10f);

                            column.item().text("Inherited bold and underline");
                            column.item().text("Disabled underline, inherited bold and adjusted font color").underline(false).fontColor(Colors.Green.getDarken2());

                            column.item()
                                .defaultTextStyle(style -> style.decorationWavy().fontColor(Colors.LightBlue.getDarken3()))
                                .text("Changed underline type and adjusted font color");
                        });
                });
            })
            .generateImages(index -> output("default-text-style.webp"), settings);
    }
}
