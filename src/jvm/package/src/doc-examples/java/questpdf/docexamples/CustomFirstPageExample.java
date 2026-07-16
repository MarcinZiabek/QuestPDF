package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import org.junit.jupiter.api.Test;

public class CustomFirstPageExample extends DocExample {

    @Test
    public void example() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.margin(30f);
                    page.defaultTextStyle(style -> style.fontSize(20f));

                    page.header().column(column -> {
                        column.item().showOnce().background(Colors.Blue.getLighten2()).height(80f);
                        column.item().skipOnce().background(Colors.Green.getLighten2()).height(60f);
                    });

                    page.content().paddingVertical(20f).column(column -> {
                        column.spacing(20f);

                        for (var i = 0; i < 20; i++)
                            column.item().background(Colors.Grey.getLighten3()).height(40f);
                    });

                    page.footer().alignCenter().text(text -> {
                        text.currentPageNumber();
                        text.span(" / ");
                        text.totalPages();
                    });
                });
            })
            .generatePdf(output("example-custom-first-page.pdf"));
    }
}
