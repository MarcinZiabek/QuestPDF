package questpdf.docexamples.codepatterns;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

public class CodePatternExecutionOrderExample extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(400f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(25f);

                            column.item()
                                .border(1f)
                                .background(Colors.Blue.getLighten4())
                                .padding(15f)
                                .text("border → background → padding");

                            column.item()
                                .border(1f)
                                .padding(15f)
                                .background(Colors.Blue.getLighten4())
                                .text("border → padding → background");

                            column.item()
                                .background(Colors.Blue.getLighten4())
                                .padding(15f)
                                .border(1f)
                                .text("background → padding → border");

                            column.item()
                                .padding(15f)
                                .border(1f)
                                .background(Colors.Blue.getLighten4())
                                .text("padding → border → background");
                        });
                });
            })
            .generateImages(index -> output("code-pattern-execution-order.webp"), settings);
    }
}
