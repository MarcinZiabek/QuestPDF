package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShowOnceExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(350f, 500f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .decoration(decoration -> {
                            decoration.before().column(column -> {
                                column.item()
                                    .showOnce()
                                    .row(row -> {
                                        row.constantItem(80f).aspectRatio(4 / 3f).placeholder();
                                        row.constantItem(10f);
                                        row.relativeItem()
                                            .alignMiddle()
                                            .column(innerColumn -> {
                                                innerColumn.item().text("Invoice #1234").fontSize(24f).bold();
                                                innerColumn.item().text("Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))).fontSize(16f).light();
                                            });
                                    });

                                column.item()
                                    .skipOnce()
                                    .text("Invoice #1234").fontSize(24f).bold();
                            });

                            // generate dummy content
                            decoration.content()
                                .paddingTop(15f)
                                .extendHorizontal()
                                .column(column -> {
                                    column.spacing(10f);

                                    for (var i = 1; i <= 15; i++) {
                                        column.item()
                                            .height(30f)
                                            .background(Colors.Grey.getLighten3())
                                            .alignCenter()
                                            .alignMiddle()
                                            .text(String.valueOf(i));
                                    }
                                });
                        });
                });
            })
            .generateImages(index -> output("show-once-" + index + ".webp"), settings);
    }
}
