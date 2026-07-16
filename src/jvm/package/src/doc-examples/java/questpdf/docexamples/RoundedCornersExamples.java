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

public class RoundedCornersExamples extends DocExample {

    @Test
    public void consistent() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .border(1f, Colors.getBlack())
                        .background(Colors.Grey.getLighten3())
                        .cornerRadius(25f)
                        .padding(25f)
                        .text("Container with consistently rounded corners");
                });
            })
            .generateImages(index -> output("rounded-corners-consistent.webp"), settings);
    }

    @Test
    public void various() {
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
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .border(1f, Colors.getBlack())
                        .background(Colors.Grey.getLighten3())
                        .cornerRadiusTopLeft(5f)
                        .cornerRadiusTopRight(10f)
                        .cornerRadiusBottomRight(20f)
                        .cornerRadiusBottomLeft(40f)
                        .padding(25f)
                        .text("Container with rounded corners");
                });
            })
            .generateImages(index -> output("rounded-corners-various.webp"), settings);
    }

    @Test
    public void complex() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(550f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .border(1f, Colors.getBlack())
                        .cornerRadius(15f)
                        .table(table -> {
                            table.columnsDefinition(columns -> {
                                columns.constantColumn(100f);
                                columns.relativeColumn();
                                columns.constantColumn(150f);
                            });

                            table.header(header -> {
                                headerCellStyle(header.cell()).text("Index");
                                headerCellStyle(header.cell()).text("Label");
                                headerCellStyle(header.cell()).text("Price");
                            });

                            for (var index = 1; index <= 5; index++) {
                                bodyCellStyle(table.cell()).text(Integer.toString(index));
                                bodyCellStyle(table.cell()).text(Placeholders.label());
                                bodyCellStyle(table.cell()).text(Placeholders.price());
                            }
                        });
                });
            })
            .generateImages(index -> output("rounded-corners-complex.webp"), settings);
    }

    // The C# original routes Style through the Element(Func<IContainer, IContainer>)
    // overload, which is not bridged; a private static helper is the direct equivalent.
    private static IContainer headerCellStyle(IContainer container) {
        return container
            .border(1f, Colors.Grey.getDarken2())
            .background(Colors.Grey.getLighten3())
            .paddingVertical(10f)
            .paddingHorizontal(15f)
            .defaultTextStyle(style -> style.bold());
    }

    private static IContainer bodyCellStyle(IContainer container) {
        return container
            .border(1f, Colors.Grey.getDarken2())
            .paddingVertical(10f)
            .paddingHorizontal(15f);
    }

    @Test
    public void image() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(450f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);
                    page.pageColor(Colors.getWhite());

                    page.content()
                        .cornerRadius(25f)
                        .image(resource("landscape.jpg"));
                });
            })
            .generateImages(index -> output("rounded-corners-image.webp"), settings);
    }
}
