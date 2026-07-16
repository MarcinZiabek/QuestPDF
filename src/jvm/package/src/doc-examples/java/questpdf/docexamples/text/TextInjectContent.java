package questpdf.docexamples.text;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import com.questpdf.infrastructure.TextInjectedElementAlignment;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

public class TextInjectContent extends DocExample {

    @Test
    public void injectImage() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(500f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(text -> {
                            text.span("A unit test can either ");
                            text.element().paddingBottom(-4f).height(24f).image(resource("unit-test-completed-icon.png"));
                            text.span(" pass").fontColor(Colors.Green.getMedium());
                            text.span(" or ");
                            text.element().paddingBottom(-4f).height(24f).image(resource("unit-test-failed-icon.png"));
                            text.span(" fail").fontColor(Colors.Red.getMedium());
                            text.span(".");
                        });
                });
            })
            .generateImages(index -> output("text-inject-image.webp"), settings);
    }

    @Test
    public void injectSvg() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(350f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(text -> {
                            text.span("To synchronize your email inbox, please click the ");
                            text.element().paddingBottom(-4f).height(24f).svg(resource("mail-synchronize-icon.svg"));
                            text.span(" icon.");
                        });
                });
            })
            .generateImages(index -> output("text-inject-svg.webp"), settings);
    }

    @Test
    public void injectPosition() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(text -> {
                            text.span("This ");

                            text.element(TextInjectedElementAlignment.AboveBaseline)
                                .width(12f).height(12f)
                                .background(Colors.Green.getMedium());

                            text.span(" element is positioned above the baseline, while this ");

                            text.element(TextInjectedElementAlignment.BelowBaseline)
                                .width(12f).height(12f)
                                .background(Colors.Blue.getMedium());

                            text.span(" element is positioned below the baseline.");
                        });
                });
            })
            .generateImages(index -> output("text-inject-position.webp"), settings);
    }
}
