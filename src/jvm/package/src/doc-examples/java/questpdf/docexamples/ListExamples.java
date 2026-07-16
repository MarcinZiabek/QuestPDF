package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ListExamples extends DocExample {

    @Test
    public void bulletpointExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(350f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(10f);

                            for (var i = 1; i <= 7; i++) {
                                column.item().row(row -> {
                                    row.constantItem(26f).image(resource("bulletpoint.png"));
                                    row.constantItem(5f);
                                    row.relativeItem().text(Placeholders.label());
                                });
                            }
                        });
                });
            })
            .generateImages(index -> output("list-unordered.webp"), settings);
    }

    @Test
    public void orderedExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(600f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(10f);

                            for (var i = 1; i <= 11; i++) {
                                var number = i;

                                column.item().row(row -> {
                                    row.constantItem(35f).text(number + ".");
                                    row.relativeItem().text(Placeholders.sentence());
                                });
                            }
                        });
                });
            })
            .generateImages(index -> output("list-ordered.webp"), settings);
    }

    @Test
    public void nested() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(600f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            var nestingSize = 25f;

                            column.spacing(10f);

                            column.item()
                                .text("Algorithm: Checking if a Number is Prime")
                                .fontSize(24f).fontColor(Colors.Blue.getDarken2());

                            AddListItemFunction addListItem = (nestingLevel, bulletText, text) -> {
                                column.item().row(row -> {
                                    row.constantItem(nestingSize * nestingLevel);
                                    row.constantItem(nestingSize).text(bulletText);
                                    row.relativeItem().text(text);
                                });
                            };

                            addListItem.invoke(0, "1.", "Handle special cases");
                            addListItem.invoke(1, "a)", "If n is less than 2, return false (not prime).");
                            addListItem.invoke(1, "b)", "If n is 2, return true (prime).");

                            addListItem.invoke(0, "2.", "Check divisibility");
                            addListItem.invoke(1, "-", "Iterate through numbers from 2 to n - 1:");
                            addListItem.invoke(2, "-", "If n is divisible by any of these numbers, return false.");

                            addListItem.invoke(0, "3.", "Return true (if no divisors were found, n is prime).");
                        });
                });
            })
            .generateImages(index -> output("list-nested.webp"), settings);
    }

    @FunctionalInterface
    private interface AddListItemFunction {
        void invoke(int nestingLevel, String bulletText, String text);
    }
}
