package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ScaleExamples extends DocExample {

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
                        .width(350f)
                        .padding(25f)
                        .column(column -> {
                            column.spacing(10f);

                            var scales = new float[] { 0.75f, 1f, 1.25f, 1.5f };

                            for (var scale : scales) {
                                column.item()
                                    .background(Colors.Grey.getLighten3())
                                    .scale(scale)
                                    .padding(10f)
                                    // stripping the ".0" suffix matches the .NET float-to-string formatting ("1", not "1.0")
                                    .text("Content scale: " + Float.toString(scale).replaceAll("\\.0$", ""))
                                    .fontSize(20f);
                            }
                        });
                });
            })
            .generateImages(index -> output("scale.webp"), settings);
    }
}
