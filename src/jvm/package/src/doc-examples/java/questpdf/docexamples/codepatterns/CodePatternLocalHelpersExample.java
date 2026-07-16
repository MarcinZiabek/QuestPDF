package questpdf.docexamples.codepatterns;

import com.questpdf.fluent.ColumnDescriptor;
import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

public class CodePatternLocalHelpersExample extends DocExample {

    @Test
    public void example() {
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
                        .column(column -> {
                            column.spacing(15f);

                            column.item().text("Business details:").fontSize(24f).bold().fontColor(Colors.Blue.getDarken2());

                            addContactItem(column, resource("Icons/phone.svg"), Placeholders.phoneNumber());
                            addContactItem(column, resource("Icons/email.svg"), Placeholders.email());
                            addContactItem(column, resource("Icons/web.svg"), Placeholders.webpageUrl());
                        });
                });
            })
            .generateImages(index -> output("code-pattern-local-helpers.webp"), settings);
    }

    private static void addContactItem(ColumnDescriptor column, String iconPath, String label) {
        column.item().row(row -> {
            row.constantItem(32f).aspectRatio(1f).svg(iconPath);
            row.constantItem(15f);
            row.autoItem().alignMiddle().text(label);
        });
    }
}
