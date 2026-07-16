package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class FlipExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(350f, 0f));
                    page.maxSize(new PageSize(350f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(15f);

                            column.item()
                                .text("Read the message below by putting a mirror on the right side of the screen.");

                            column.item()
                                .alignLeft()
                                .background(Colors.Red.getLighten5())
                                .padding(10f)
                                .flipHorizontal()
                                .text("This is a secret message.")
                                .fontColor(Colors.Red.getDarken2());
                        });
                });
            })
            .generateImages(index -> output("flip.webp"), settings);
    }
}
