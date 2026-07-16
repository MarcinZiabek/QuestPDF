package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class MultiColumnExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(650f, 0f));
                    page.maxSize(new PageSize(650f, 650f));
                    page.defaultTextStyle(style -> style.fontSize(12f));
                    page.margin(25f);

                    page.content()
                        .multiColumn(multiColumn -> {
                            multiColumn.columns(3);
                            multiColumn.spacing(25f);

                            multiColumn.content()
                                .column(column -> {
                                    column.spacing(15f);

                                    for (var sectionId = 0; sectionId < 3; sectionId++) {
                                        for (var textId = 0; textId < 3; textId++)
                                            column.item().text(Placeholders.paragraph()).justify();

                                        column.item().aspectRatio(21 / 9f).image(payload -> Placeholders.image(payload.getImageSize()));
                                    }
                                });
                        });
                });
            })
            .generateImages(index -> output("multicolumn-example.webp"), settings);
    }

    @Test
    public void spacerExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(450f, 0f));
                    page.maxSize(new PageSize(450f, 550f));
                    page.defaultTextStyle(style -> style.fontSize(12f));
                    page.margin(25f);

                    page.content()
                        .multiColumn(multiColumn -> {
                            multiColumn.columns(2);
                            multiColumn.spacing(50f);

                            multiColumn.spacer()
                                .alignCenter()
                                .lineVertical(2f)
                                .lineColor(Colors.Grey.getMedium());

                            multiColumn.content()
                                .column(column -> {
                                    column.spacing(15f);

                                    for (var textId = 0; textId < 5; textId++)
                                        column.item().text(Placeholders.paragraph()).justify();
                                });
                        });
                });
            })
            .generateImages(index -> output("multicolumn-spacer.webp"), settings);
    }

    @Test
    public void balanceHeightWithExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA4());
                    page.defaultTextStyle(style -> style.fontSize(14f));
                    page.margin(30f);

                    page.content()
                        .multiColumn(multiColumn -> {
                            multiColumn.spacing(30f);
                            multiColumn.balanceHeight();

                            multiColumn.content()
                                .column(column -> {
                                    column.spacing(15f);

                                    for (var textId = 0; textId < 8; textId++)
                                        column.item().text(Placeholders.paragraph()).justify();
                                });
                        });
                });
            })
            .generateImages(index -> output("multicolumn-balance-height-with.webp"), settings);
    }
}
