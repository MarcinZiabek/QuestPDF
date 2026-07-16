package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class BackgroundExamples extends DocExample {

    @Test
    public void solidColor() {
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
                    page.pageColor(Colors.getWhite());
                    page.margin(25f);

                    var colors = new Color[] {
                        Colors.LightBlue.getDarken4(),
                        Colors.LightBlue.getDarken3(),
                        Colors.LightBlue.getDarken2(),
                        Colors.LightBlue.getDarken1(),

                        Colors.LightBlue.getMedium(),

                        Colors.LightBlue.getLighten1(),
                        Colors.LightBlue.getLighten2(),
                        Colors.LightBlue.getLighten3(),
                        Colors.LightBlue.getLighten4(),
                        Colors.LightBlue.getLighten5(),

                        Colors.LightBlue.getAccent1(),
                        Colors.LightBlue.getAccent2(),
                        Colors.LightBlue.getAccent3(),
                        Colors.LightBlue.getAccent4(),
                    };

                    page.content()
                        .height(150f)
                        .width(420f)
                        .row(row -> {
                            for (var color : colors)
                                row.relativeItem().background(color);
                        });
                });
            })
            .generateImages(index -> output("background-solid.webp"), settings);
    }

    @Test
    public void gradient() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(350f, 0f));
                    page.maxSize(new PageSize(350f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.pageColor(Colors.getWhite());
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(25f);

                            column.item()
                                .backgroundLinearGradient(0f, new Color[] { Colors.Red.getLighten2(), Colors.Blue.getLighten2() })
                                .aspectRatio(2f);

                            column.item()
                                .backgroundLinearGradient(45f, new Color[] { Colors.Green.getLighten2(), Colors.LightGreen.getLighten2(), Colors.Yellow.getLighten2() })
                                .aspectRatio(2f);

                            column.item()
                                .backgroundLinearGradient(90f, new Color[] { Colors.Yellow.getLighten2(), Colors.Amber.getLighten2(), Colors.Orange.getLighten2() })
                                .aspectRatio(2f);
                        });
                });
            })
            .generateImages(index -> output("background-gradient.webp"), settings);
    }

    @Test
    public void roundedCorners() {
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
                    page.pageColor(Colors.getWhite());
                    page.margin(25f);

                    page.content()
                        .shrink()
                        .background(Colors.Grey.getLighten2())
                        .cornerRadius(25f)
                        .padding(25f)
                        .text("Content with rounded corners");
                });
            })
            .generateImages(index -> output("background-rounded-corners.webp"), settings);
    }
}
