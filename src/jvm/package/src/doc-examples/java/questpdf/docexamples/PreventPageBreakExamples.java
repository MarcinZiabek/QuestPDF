package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import org.junit.jupiter.api.Test;

public class PreventPageBreakExamples extends DocExample {

    @Test
    public void enabledExample() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(30f);

                    page.content()
                        .column(column -> {
                            column.item().height(400f).background(Colors.Grey.getLighten3());
                            column.item().height(30f);

                            column.item()
                                .preventPageBreak()
                                .text(text -> {
                                    text.paragraphSpacing(15f);

                                    text.span("Optimizing Content Placement").bold().fontColor(Colors.Blue.getDarken2()).fontSize(24f);
                                    text.span("\n");
                                    text.span("By carefully determining where to place a page break, you can avoid awkward text separations and maintain readability. Thoughtful formatting improves the overall user experience, making complex topics easier to digest.");
                                });
                        });
                });
            })
            .generatePdf(output("prevent-page-break-enabled.pdf"));
    }

    @Test
    public void disabledExample() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(30f);

                    page.content()
                        .column(column -> {
                            column.item().height(400f).background(Colors.Grey.getLighten3());
                            column.item().height(30f);

                            column.item()
                                .text(text -> {
                                    text.paragraphSpacing(15f);

                                    text.span("Optimizing Content Placement").bold().fontColor(Colors.Blue.getDarken2()).fontSize(24f);
                                    text.span("\n");
                                    text.span("By carefully determining where to place a page break, you can avoid awkward text separations and maintain readability. Thoughtful formatting improves the overall user experience, making complex topics easier to digest.");
                                });
                        });
                });
            })
            .generatePdf(output("prevent-page-break-disabled.pdf"));
    }
}
