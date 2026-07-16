package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class RowExamples extends DocExample {

    @Test
    public void simpleExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.margin(25f);

                    page.content()
                        .padding(25f)
                        .width(325f)
                        .row(row -> {
                            row.constantItem(100f)
                                .background(Colors.Grey.getMedium())
                                .padding(10f)
                                .text("100pt");

                            row.relativeItem()
                                .background(Colors.Grey.getLighten1())
                                .padding(10f)
                                .text("75pt");

                            row.relativeItem(2f)
                                .background(Colors.Grey.getLighten2())
                                .padding(10f)
                                .text("150pt");
                        });
                });
            })
            .generateImages(index -> output("row-simple.webp"), settings);
    }

    @Test
    public void spacingExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.margin(25f);

                    page.content()
                        .padding(25f)
                        .width(220f)
                        .height(50f)
                        .row(row -> {
                            row.spacing(10f);

                            row.relativeItem(2f).background(Colors.Grey.getMedium());
                            row.relativeItem(3f).background(Colors.Grey.getLighten1());
                            row.relativeItem(5f).background(Colors.Grey.getLighten2());
                        });
                });
            })
            .generateImages(index -> output("row-spacing.webp"), settings);
    }

    @Test
    public void customSpacingExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(250f, 0f));
                    page.maxSize(new PageSize(250f, 1000f));
                    page.margin(25f);

                    page.content()
                        .height(50f)
                        .row(row -> {
                            row.relativeItem().background(Colors.Grey.getDarken1());
                            row.constantItem(10f);
                            row.relativeItem().background(Colors.Grey.getMedium());
                            row.constantItem(20f);
                            row.relativeItem().background(Colors.Grey.getLighten1());
                            row.constantItem(30f);
                            row.relativeItem().background(Colors.Grey.getLighten2());
                        });
                });
            })
            .generateImages(index -> output("row-spacing-custom.webp"), settings);
    }

    @Test
    public void disableUniformItemsHeightExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(700f, 0f));
                    page.maxSize(new PageSize(700f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .row(row -> {
                            row.spacing(15f);

                            labelStyle(row.relativeItem())
                                .text("Programming is both a science and an art — it demands precision, creativity, and patience. At its core, it’s about understanding how to break down complex problems into small, logical steps that a computer can execute.");

                            labelStyle(row.relativeItem())
                                .text("Programming is the art of turning ideas into logic, logic into code, and code into something that solves real problems.");
                        });
                });
            })
            .generateImages(index -> output("row-uniform-height-enabled.webp"), settings);
    }

    // The C# original routes LabelStyle through the Element(Func<IContainer, IContainer>)
    // overload, which is not bridged; a static helper method is the direct equivalent.
    private static IContainer labelStyle(IContainer container) {
        return container
            .shrinkVertical()
            .background(Colors.Grey.getLighten3())
            .cornerRadius(15f)
            .padding(15f);
    }
}
