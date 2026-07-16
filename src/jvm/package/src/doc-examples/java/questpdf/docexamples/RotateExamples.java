package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.fluent.RowDescriptor;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class RotateExamples extends DocExample {

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
                    page.maxSize(new PageSize(500f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .row(row -> {
                            row.autoItem()
                                .rotateLeft()
                                .alignCenter()
                                .text("Definition")
                                .bold().fontColor(Colors.Blue.getDarken2());

                            row.autoItem()
                                .paddingHorizontal(15f)
                                .lineVertical(2f).lineColor(Colors.Blue.getMedium());

                            row.relativeItem()
                                .background(Colors.Blue.getLighten5())
                                .padding(15f)
                                .text(text -> {
                                    text.span("A variable").bold();
                                    text.span(" is a named storage location in memory that holds a value which can be modified during program execution.");
                                });
                        });
                });
            })
            .generateImages(index -> output("rotate.webp"), settings);
    }

    @Test
    public void freeExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));

                    page.content()
                        .background(Colors.Grey.getLighten2())
                        .padding(25f)
                        .row(row -> {
                            row.spacing(25f);

                            addIcon(row, 0f);
                            addIcon(row, 30f);
                            addIcon(row, 45f);
                            addIcon(row, 80f);
                        });
                });
            })
            .generateImages(index -> output("rotate-free.webp"), settings);
    }

    private static void addIcon(RowDescriptor row, float angle) {
        var itemSize = 100f;

        row.autoItem()
            .width(itemSize)
            .aspectRatio(1f)

            .offsetX(itemSize / 2)
            .offsetY(itemSize / 2)

            .rotate(angle)

            .offsetX(-itemSize / 2)
            .offsetY(-itemSize / 2)

            .svg(resource("compass.svg"));
    }
}
