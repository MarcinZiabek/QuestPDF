package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.AspectRatioOption;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class AspectRatioExamples extends DocExample {

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
                        .width(300f)
                        .height(300f)
                        .aspectRatio(3f / 4f, AspectRatioOption.FitArea)
                        .background(Colors.Grey.getLighten2())
                        .alignCenter()
                        .alignMiddle()
                        .text("3:4 Content Area");
                });
            })
            .generateImages(index -> output("aspect-ratio.webp"), settings);
    }
}
