package questpdf.docexamples.text;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import com.questpdf.infrastructure.TextStyle;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

import java.util.function.Function;

public class TextBasicExamples extends DocExample {

    @Test
    public void basic() {
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
                        .text("Sample text");
                });
            })
            .generateImages(index -> output("text-basic.webp"), settings);
    }

    @Test
    public void basicWithStyle() {
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
                        .column(column -> {
                            column.spacing(10f);

                            // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; a local Function is the direct equivalent.
                            Function<IContainer, IContainer> cellStyle = container ->
                                container.background(Colors.Grey.getLighten3()).padding(10f);

                            cellStyle.apply(column.item())
                                .text("Text with blue color")
                                .fontColor(Colors.Blue.getDarken1());

                            cellStyle.apply(column.item())
                                .text("Bold and underlined text")
                                .bold()
                                .underline();

                            cellStyle.apply(column.item())
                                .text("Centered small text")
                                .fontSize(12f)
                                .alignCenter();
                        });
                });
            })
            .generateImages(index -> output("text-basic-descriptor.webp"), settings);
    }

    @Test
    public void rich() {
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
                            text.alignCenter();

                            text.span("The ");
                            text.span("chemical formula").underline();
                            text.span(" of ");
                            text.span("sulfuric acid").backgroundColor(Colors.Amber.getLighten3());
                            text.span(" is H");
                            text.span("2").subscript();
                            text.span("SO");
                            text.span("4").subscript();
                            text.span(".");
                        });
                });
            })
            .generateImages(index -> output("text-rich.webp"), settings);
    }

    @Test
    public void styleInheritance() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(600f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .defaultTextStyle(style -> style.fontSize(20f))
                        .column(column -> {
                            column.spacing(10f);

                            column.item().text("Products").extraBold().underline().decorationThickness(2f);

                            column.item().text("Comments: " + Placeholders.sentence());

                            column.item()
                                .defaultTextStyle(style -> style.fontSize(14f))
                                .table(table -> {
                                    table.columnsDefinition(columns -> {
                                        columns.constantColumn(30f);
                                        columns.relativeColumn(1f);
                                        columns.relativeColumn(2f);
                                    });

                                    table.header(header -> {
                                        // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                                        // overload, which is not bridged; a local Function is the direct equivalent.
                                        Function<IContainer, IContainer> style = container ->
                                            container.background(Colors.Grey.getLighten3())
                                                .borderBottom(1f)
                                                .paddingHorizontal(5f)
                                                .paddingVertical(10f)
                                                .defaultTextStyle(textStyle -> textStyle.bold().fontColor(Colors.Blue.getMedium()));

                                        style.apply(header.cell()).text("ID");
                                        style.apply(header.cell()).text("Name");
                                        style.apply(header.cell()).text("Description");
                                    });

                                    // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                                    // overload, which is not bridged; a local Function is the direct equivalent.
                                    Function<IContainer, IContainer> style = container ->
                                        container.padding(5f);

                                    for (var i = 0; i < 5; i++) {
                                        style.apply(table.cell()).text(String.valueOf(i)).bold();
                                        style.apply(table.cell()).text(Placeholders.label());
                                        style.apply(table.cell()).text(Placeholders.sentence());
                                    }
                                });
                        });
                });
            })
            .generateImages(index -> output("text-style-inheritance.webp"), settings);
    }

    @Test
    public void pageNumber() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .extend()
                        .placeholder();

                    page.footer()
                        .paddingTop(25f)
                        .alignCenter()
                        .text("3 / 10");
                        // .text(text -> {
                        //     text.currentPageNumber();
                        //     text.span(" / ");
                        //     text.totalPages();
                        // });
                });
            })
            .generateImages(index -> output("text-page-number.webp"), settings);
    }

    @Test
    public void pageNumberFormat() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(text -> {
                            text.currentPageNumber().format(TextBasicExamples::formatWithLeadingZeros);
                        });
                });
            })
            .generateImages(index -> output("text-page-number-format.webp"), settings);
    }

    private static String formatWithLeadingZeros(Integer pageNumber) {
        var expectedLength = 3;
        var number = pageNumber != null ? pageNumber : 1;
        var text = Integer.toString(number);
        return "0".repeat(Math.max(expectedLength - text.length(), 0)) + text;
    }

    @Test
    public void hyperlink() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA6().landscape());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(text -> {
                            var hyperlinkStyle = TextStyle.getDefault()
                                .fontColor(Colors.Blue.getMedium())
                                .underline();

                            text.span("To learn more about QuestPDF, please visit its ");
                            text.hyperlink("homepage", "https://www.questpdf.com/").style(hyperlinkStyle);
                            text.span(", ");
                            text.hyperlink("GitHub repository", "https://github.com/QuestPDF/QuestPDF").style(hyperlinkStyle);
                            text.span(" and ");
                            text.hyperlink("NuGet package page", "https://www.nuget.org/packages/QuestPDF").style(hyperlinkStyle);
                            text.span(".");
                        });
                });
            })
            .generatePdf(output("text-hyperlink.pdf"));
    }
}
