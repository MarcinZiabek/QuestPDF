package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class UnconstrainedExamples extends DocExample {

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
                        .height(350f)
                        .padding(25f)
                        .paddingLeft(50f)
                        .column(column -> {
                            column.item().width(300f).height(150f).background(Colors.Blue.getLighten3());

                            column.item()
                                .unconstrained()
                                .offsetX(-50f)
                                .offsetY(-50f)
                                .width(100f)
                                .height(100f)
                                .background(Colors.Blue.getDarken2());

                            column.item().width(300f).height(150f).background(Colors.Blue.getLighten2());
                        });
                });
            })
            .generateImages(index -> output("unconstrained.webp"), settings);
    }
}
