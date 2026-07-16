package questpdf.docexamples.text;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.IContainer;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

import java.util.function.Function;

public class ParagraphStyleExamples extends DocExample {

    @Test
    public void defaultTextStyle() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(text -> {
                            text.defaultTextStyle(style -> style.light().letterSpacing(-0.1f).wordSpacing(0.1f));

                            text.span("Changing typography settings helps creating ");
                            text.span("significant").letterSpacing(0.2f).black().backgroundColor(Colors.Grey.getLighten2());
                            text.span(" visual contrast.");
                        });
                });
            })
            .generateImages(index -> output("text-paragraph-default-style.webp"), settings);
    }

    @Test
    public void textAlignment() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(400f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(20f);

                            Function<IContainer, IContainer> cellStyle = container ->
                                container.background(Colors.Grey.getLighten3()).padding(10f);

                            cellStyle.apply(column.item())
                                .text("This is an example of left-aligned text, showcasing how the text starts from the left margin and continues naturally across the container.")
                                .alignLeft();

                            cellStyle.apply(column.item())
                                .text("This text is centered within its container, creating a balanced look, especially for titles or headers.")
                                .alignCenter();

                            cellStyle.apply(column.item())
                                .text("This example demonstrates right-aligned text, often used for dates, numbers, or aligning text to the right margin.")
                                .alignRight();

                            cellStyle.apply(column.item())
                                .text("Justified text adjusts the spacing between words so that both the left and right edges of the text block are aligned, creating a clean, newspaper-like look.")
                                .justify();
                        });
                });
            })
            .generateImages(index -> output("text-paragraph-alignment.webp"), settings);
    }

    @Test
    public void firstLineIndentation() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(500f, 1200f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(Placeholders.paragraphs())
                        .paragraphFirstLineIndentation(40f);
                });
            })
            .generateImages(index -> output("text-paragraph-first-line-indentation.webp"), settings);
    }

    @Test
    public void spacing() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(500f, 1200f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(Placeholders.paragraphs())
                        .paragraphSpacing(10f);
                });
            })
            .generateImages(index -> output("text-paragraph-spacing.webp"), settings);
    }

    @Test
    public void clampLines() {
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

                            var paragraph = Placeholders.paragraph();

                            column.item()
                                .background(Colors.Grey.getLighten3())
                                .padding(5f)
                                .text(paragraph);

                            column.item()
                                .background(Colors.Grey.getLighten3())
                                .padding(5f)
                                .text(paragraph)
                                .clampLines(3);
                        });
                });
            })
            .generateImages(index -> output("text-paragraph-clamp-lines.webp"), settings);
    }

    @Test
    public void clampLinesWithCustomEllipsis() {
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
                        .text(Placeholders.paragraph())
                        .clampLines(3, " [...]");
                });
            })
            .generateImages(index -> output("text-paragraph-clamp-lines-custom-ellipsis.webp"), settings);
    }
}
