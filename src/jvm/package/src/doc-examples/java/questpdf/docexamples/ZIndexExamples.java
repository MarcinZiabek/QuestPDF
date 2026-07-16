package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ZIndexExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(650f, 0f));
                    page.maxSize(new PageSize(650f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .paddingVertical(15f)
                        .border(2f)
                        .row(row -> {
                            row.relativeItem()
                                .background(Colors.Grey.getLighten3())
                                .element(c -> { addPricingItem(c, "Community", "Free"); });

                            row.relativeItem()
                                .zIndex(1) // -1 or 0 or 1
                                .padding(-15f)
                                .border(1f)
                                .background(Colors.Grey.getLighten1())
                                .paddingTop(15f)
                                .element(c -> { addPricingItem(c, "Professional", "$699"); });

                            row.relativeItem()
                                .background(Colors.Grey.getLighten3())
                                .element(c -> { addPricingItem(c, "Enterprise", "$1999"); });
                        });
                });
            })
            .generateImages(index -> output("zindex-positive.webp"), settings);
    }

    private static void addPricingItem(IContainer container, String name, String formattedPrice) {
        container
            .padding(25f)
            .column(column -> {
                column.item().alignCenter().text(name).fontSize(24f).black();
                column.item().alignCenter().text(formattedPrice).fontSize(20f).semiBold();

                column.item().paddingHorizontal(-25f).paddingVertical(10f).lineHorizontal(1f);

                for (var i = 1; i <= 4; i++) {
                    column.item()
                        .paddingTop(10f)
                        .alignCenter()
                        .text(Placeholders.label())
                        .fontSize(16f)
                        .light();
                }
            });
    }
}
