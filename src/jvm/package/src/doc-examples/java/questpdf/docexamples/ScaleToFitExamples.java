package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ScaleToFitExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            var text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";

                            for (var i = 4; i <= 8; i++) {
                                column.item()
                                    .shrink()
                                    .border(1f)
                                    .padding(15f)
                                    .width(i * 50f) // sizes from 200x100 to 450x175
                                    .height(i * 25f)
                                    .scaleToFit()
                                    .text(text);
                            }
                        });
                });
            })
            .generateImages(index -> output("scale-to-fit.webp"), settings);
    }
}
