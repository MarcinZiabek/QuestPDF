package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ContentDirectionExamples extends DocExample {

    @Test
    public void leftToRightExample() {
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
                        .contentFromLeftToRight()
                        .row(row -> {
                            row.spacing(5f);

                            row.autoItem().height(50f).width(50f).background(Colors.Red.getLighten1());
                            row.autoItem().height(50f).width(50f).background(Colors.Green.getLighten1());
                            row.autoItem().height(50f).width(75f).background(Colors.Blue.getLighten1());
                        });
                });
            })
            .generateImages(index -> output("content-direction-ltr.webp"), settings);
    }

    @Test
    public void rightToLeftExample() {
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
                        .contentFromRightToLeft()
                        .row(row -> {
                            row.spacing(5f);

                            row.autoItem().height(50f).width(50f).background(Colors.Red.getLighten1());
                            row.autoItem().height(50f).width(50f).background(Colors.Green.getLighten1());
                            row.autoItem().height(50f).width(75f).background(Colors.Blue.getLighten1());
                        });
                });
            })
            .generateImages(index -> output("content-direction-rtl.webp"), settings);
    }
}
