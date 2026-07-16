package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ColorsExamples extends DocExample {

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
                        .width(175f)
                        .padding(20f)
                        .border(1f)
                        .borderColor(Color.from("#03A9F4"))
                        .background(Colors.LightBlue.getLighten5())
                        .padding(20f)
                        .text("Blue text")
                        .bold()
                        .fontColor(Colors.LightBlue.getDarken4())
                        .underline()
                        .decorationWavy()
                        .decorationColor(Color.from(0xFF0000));
                });
            })
            .generateImages(index -> output("colors.webp"), settings);
    }
}
