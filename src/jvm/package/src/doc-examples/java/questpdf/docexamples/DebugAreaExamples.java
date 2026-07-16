package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class DebugAreaExamples extends DocExample {

    @Test
    public void leftToRightExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(216);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));

                    page.content()
                        .width(250f)
                        .height(250f)
                        .padding(25f)
                        .debugArea("Grid example", Colors.Blue.getMedium())
                        .grid(grid -> {
                            grid.columns(3);
                            grid.spacing(5f);

                            for (var i = 0; i < 8; i++)
                                grid.item().height(50f).placeholder();
                        });
                });
            })
            .generateImages(index -> output("debug-area.webp"), settings);
    }
}
