package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ColumnExamples extends DocExample {

    @Test
    public void simpleExample() {
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
                        .column(column -> {
                            column.item().background(Colors.Grey.getMedium()).height(50f);
                            column.item().background(Colors.Grey.getLighten1()).height(75f);
                            column.item().background(Colors.Grey.getLighten2()).height(100f);
                        });
                });
            })
            .generateImages(index -> output("column-simple.webp"), settings);
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
                    page.minSize(new PageSize(250f, 0f));
                    page.maxSize(new PageSize(250f, 1000f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(25f);

                            column.item().background(Colors.Grey.getMedium()).height(50f);
                            column.item().background(Colors.Grey.getLighten1()).height(75f);
                            column.item().background(Colors.Grey.getLighten2()).height(100f);
                        });
                });
            })
            .generateImages(index -> output("column-spacing.webp"), settings);
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
                        .column(column -> {
                            column.item().background(Colors.Grey.getDarken1()).height(50f);
                            column.item().height(10f);
                            column.item().background(Colors.Grey.getMedium()).height(50f);
                            column.item().height(20f);
                            column.item().background(Colors.Grey.getLighten1()).height(50f);
                            column.item().height(30f);
                            column.item().background(Colors.Grey.getLighten2()).height(50f);
                        });
                });
            })
            .generateImages(index -> output("column-spacing-custom.webp"), settings);
    }

    @Test
    public void disableUniformItemsWidthExample() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(400f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .column(column -> {
                            column.spacing(15f);

                            labelStyle(column.item())
                                .text("REST API");

                            labelStyle(column.item())
                                .text("Garbage Collection");

                            labelStyle(column.item())
                                .text("Object-Oriented Programming");
                        });
                });
            })
            .generateImages(index -> output("column-uniform-width-disabled.webp"), settings);
    }

    // The C# original routes LabelStyle through the Element(Func<IContainer, IContainer>)
    // overload, which is not bridged; a static helper method is the direct equivalent.
    private static IContainer labelStyle(IContainer container) {
        return container
            .shrinkHorizontal()
            .background(Colors.Grey.getLighten3())
            .cornerRadius(15f)
            .padding(15f);
    }
}
