package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import com.questpdf.infrastructure.Unit;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PageExamples extends DocExample {

    @Test
    public void simple() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.margin(2f, Unit.Centimetre);
                    page.defaultTextStyle(style -> style.fontSize(24f));

                    page.header()
                        .text("Hello, World!")
                        .fontSize(48f).bold();

                    page.content()
                        .paddingVertical(25f)
                        .text(Placeholders.loremIpsum())
                        .justify();

                    page.footer()
                        .alignCenter()
                        .text(text -> {
                            text.currentPageNumber();
                            text.span(" / ");
                            text.totalPages();
                        });
                });
            })
            .generateImages(index -> output("page-simple.webp"), settings);
    }

    @Test
    public void mainSlots() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.VeryHigh);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA4());
                    page.margin(2f, Unit.Centimetre);
                    page.defaultTextStyle(style -> style.fontSize(24f));

                    page.header()
                        .background(Colors.Grey.getLighten1())
                        .height(125f)
                        .alignCenter()
                        .alignMiddle()
                        .text("Header");

                    page.content()
                        .background(Colors.Grey.getLighten2())
                        .alignCenter()
                        .alignMiddle()
                        .text("Content");

                    page.footer()
                        .background(Colors.Grey.getLighten1())
                        .height(75f)
                        .alignCenter()
                        .alignMiddle()
                        .text("Footer");
                });
            })
            .generateImages(index -> output("page-main-slots.webp"), settings);
    }

    @Test
    public void foreground() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.High);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA4());
                    page.margin(2f, Unit.Centimetre);
                    page.defaultTextStyle(style -> style.fontSize(20f));

                    page.header()
                        .paddingBottom(1f, Unit.Centimetre)
                        .text("Report")
                        .fontSize(30f)
                        .bold();

                    page.content()
                        .text(Placeholders.paragraphs())
                        .paragraphSpacing(1f, Unit.Centimetre)
                        .justify();

                    page.foreground().svg(resource("draft-foreground.svg")).fitArea();
                });
            })
            .generateImages(index -> output("page-foreground.webp"), settings);
    }

    @Test
    public void background() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA4().landscape());

                    page.background().svg(resource("certificate-background.svg")).fitArea();

                    page.content()
                        .paddingLeft(10f, Unit.Centimetre)
                        .paddingRight(5f, Unit.Centimetre)
                        .alignMiddle()
                        .column(column -> {
                            column.item().height(50f).svg(resource("questpdf-logo.svg"));

                            column.item().height(50f);

                            column.item().text("CERTIFICATE").fontSize(64f).extraBlack();

                            column.item().height(25f);

                            column.item()
                                .shrink().borderBottom(1f).padding(10f)
                                .text("Marcin Ziąbek").fontSize(32f).italic();

                            column.item().height(10f);

                            column.item()
                                .text("has successfully completed the course \"QuestPDF Basics\" on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)) + ".")
                                .fontSize(20f).light();
                        });
                });
            })
            .generateImages(index -> output("page-background.webp"), settings);
    }
}
