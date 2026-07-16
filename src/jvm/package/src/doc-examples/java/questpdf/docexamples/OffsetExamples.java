package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class OffsetExamples extends DocExample {

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
                    page.maxSize(new PageSize(400f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));

                    page.content()
                        .padding(50f)
                        .background(Colors.Blue.getLighten3())
                        .offsetX(25f)
                        .offsetY(25f)
                        .border(4f)
                        .borderColor(Colors.Blue.getDarken2())
                        .padding(50f)
                        .text("Moved content")
                        .fontSize(25f);
                });
            })
            .generateImages(index -> output("offset.webp"), settings);
    }
}
