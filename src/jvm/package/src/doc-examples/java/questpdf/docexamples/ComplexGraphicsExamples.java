package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ComplexGraphicsExamples extends DocExample {

    @Test
    public void roundedRectangleWithGradient() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .layers(layers -> {
                            layers.layer().svg(size -> """
                                <svg width="%s" height="%s" xmlns="http://www.w3.org/2000/svg">
                                    <defs>
                                      <linearGradient id="backgroundGradient" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                                        <stop stop-color="#00E5FF" offset="0%%"/>
                                        <stop stop-color="#2979FF" offset="100%%"/>
                                      </linearGradient>
                                    </defs>

                                    <rect x="0" y="0" width="%s" height="%s" rx="%s" ry="%s" fill="url(#backgroundGradient)" />
                                </svg>""".formatted(size.getWidth(), size.getHeight(), size.getWidth(), size.getHeight(), size.getHeight() / 2, size.getHeight() / 2));

                            layers.primaryLayer()
                                .paddingVertical(10f)
                                .paddingHorizontal(20f)
                                .text("QuestPDF")
                                .fontColor(Colors.getWhite())
                                .fontSize(32f)
                                .extraBlack();
                        });
                });
            })
            .generateImages(index -> output("complex-graphics-rounded-rectangle-with-gradient.webp"), settings);
    }

    @Test
    public void dottedLine() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(500f, 0f));
                    page.maxSize(new PageSize(500f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(5f);

                            for (var i = 1; i <= 5; i++) {
                                var index = i;
                                var pageNumber = i * 7 + 4;

                                column.item().row(row -> {
                                    row.autoItem().text(index + ".");
                                    row.constantItem(10f);
                                    row.autoItem().text(Placeholders.label());

                                    row.relativeItem().paddingHorizontal(3f).offsetY(20f).height(2f).svg(size -> """
                                        <svg width="%s" height="%s" xmlns="http://www.w3.org/2000/svg">
                                            <line x1="0" y1="0" x2="%s" y2="0" fill="none" stroke="black" stroke-width="2" stroke-dasharray="2 6" />
                                        </svg>""".formatted(size.getWidth(), size.getHeight(), size.getWidth()));

                                    row.autoItem().text(Integer.toString(pageNumber));
                                });
                            }
                        });
                });
            })
            .generateImages(index -> output("complex-graphics-dotted-line.webp"), settings);
    }
}
