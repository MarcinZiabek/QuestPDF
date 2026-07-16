package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class LayersExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(450f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.item().paddingBottom(15f).text("Proposed Business Card Design:").bold();

                            column.item()
                                .aspectRatio(4 / 3f)
                                .layers(layers -> {
                                    layers.layer().image(resource("card-background.jpg")).fitUnproportionally();

                                    layers.primaryLayer()
                                        .offsetY(75f)
                                        .column(innerColumn -> {
                                            innerColumn.item()
                                                .alignCenter()
                                                .text("Horizon Ventures")
                                                .bold().fontSize(32f).fontColor(Colors.Blue.getDarken2());

                                            innerColumn.item().alignCenter().text("Your journey begins here");
                                        });
                                });
                        });
                });
            })
            .generateImages(index -> output("layers.webp"), settings);
    }
}
