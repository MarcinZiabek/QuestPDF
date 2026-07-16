package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

public class InlinedExamples extends DocExample {

    @Test
    public void simpleExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(450f);

                    page.content()
                        .background(Colors.Grey.getLighten3())
                        .padding(25f)
                        .border(1f)
                        .background(Colors.getWhite())
                        .inlined(inlined -> {
                            inlined.spacing(25f);
                            inlined.baselineMiddle();
                            inlined.alignCenter();

                            for (var i = 0; i < 15; i++)
                                inlined.item().element(InlinedExamples::randomBlock);
                        });
                });
            })
            .generateImages(index -> output("inlined.webp"), settings);
    }

    @Test
    public void spacingExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(450f);

                    page.content()
                        .background(Colors.Grey.getLighten3())
                        .padding(25f)
                        .border(1f)
                        .background(Colors.getWhite())
                        .inlined(inlined -> {
                            inlined.verticalSpacing(15f);
                            inlined.horizontalSpacing(30f);

                            for (var i = 0; i < 10; i++)
                                inlined.item().element(InlinedExamples::randomBlock);
                        });
                });
            })
            .generateImages(index -> output("inlined-spacing.webp"), settings);
    }

    private static void randomBlock(IContainer container) {
        container
            .width(ThreadLocalRandom.current().nextInt(1, 4) * 25f)
            .height(ThreadLocalRandom.current().nextInt(1, 4) * 25f)
            .border(1f)
            .borderColor(Colors.Grey.getDarken2())
            .background(Placeholders.backgroundColor());
    }
}
