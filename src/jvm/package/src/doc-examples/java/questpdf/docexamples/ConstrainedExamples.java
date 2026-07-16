package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.AspectRatioOption;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ConstrainedExamples extends DocExample {

    @Test
    public void widthExample() {
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
                        .padding(25f)
                        .column(column -> {
                            column.spacing(25f);

                            column.item()
                                .minWidth(200f)
                                .background(Colors.Grey.getLighten3())
                                .text("Lorem ipsum");

                            column.item()
                                .maxWidth(100f)
                                .background(Colors.Grey.getLighten3())
                                .text("dolor sit amet");
                        });
                });
            })
            .generateImages(index -> output("width.webp"), settings);
    }

    @Test
    public void heightExample() {
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
                        .padding(25f)
                        .height(100f)
                        .aspectRatio(2f, AspectRatioOption.FitHeight)
                        .background(Colors.Grey.getLighten1());
                });
            })
            .generateImages(index -> output("height.webp"), settings);
    }
}
