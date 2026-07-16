package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class PaddingExamples extends DocExample {

    @Test
    public void simpleExample() {
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
                        .width(250f)
                        .paddingVertical(10f)
                        .paddingLeft(20f)
                        .paddingRight(40f)
                        .background(Colors.Grey.getLighten2())
                        .text("Sample text");
                });
            })
            .generateImages(index -> output("padding-simple.webp"), settings);
    }

    @Test
    public void negativeExample() {
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
                        .width(250f)
                        .padding(50f)
                        .background(Colors.Grey.getLighten2())
                        .paddingHorizontal(-25f)
                        .text("Sample text with negative padding");
                });
            })
            .generateImages(index -> output("padding-negative.webp"), settings);
    }
}
