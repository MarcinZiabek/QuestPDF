package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import com.questpdf.infrastructure.TextStyle;
import com.questpdf.infrastructure.Unit;
import org.junit.jupiter.api.Test;

public class MergingDocumentsExamples extends DocExample {

    @Test
    public void useOriginalPageNumbersExample() {
        Document
            .merge(
                generateReport("Short Document 1", 5),
                generateReport("Medium Document 2", 10),
                generateReport("Long Document 3", 15))
            .useOriginalPageNumbers()
            .generatePdf(output("merged.pdf"));
    }

    @Test
    public void useContinuousPageNumbersExample() {
        Document
            .merge(
                generateReport("Short Document 1", 5),
                generateReport("Medium Document 2", 10),
                generateReport("Long Document 3", 15))
            .useContinuousPageNumbers()
            .generatePdf(output("merged.pdf"));
    }

    private static Document generateReport(String title, int itemsCount) {
        return Document.create(document -> {
            document.page(page -> {
                page.size(PageSizes.getA5());
                page.margin(0.5f, Unit.Inch);

                page.header()
                    .text(title)
                    .bold()
                    .fontSize(24f)
                    .fontColor(Colors.Blue.getAccent2());

                page.content()
                    .paddingVertical(20f)
                    .column(column -> {
                        column.spacing(10f);

                        for (var i = 0; i < itemsCount; i++) {
                            column.item()
                                .width(200f)
                                .height(50f)
                                .background(Colors.Grey.getLighten3())
                                .alignMiddle()
                                .alignCenter()
                                .text("Item " + i)
                                .fontSize(16f);
                        }
                    });

                page.footer()
                    .alignCenter()
                    .paddingVertical(20f)
                    .text(text -> {
                        text.defaultTextStyle(TextStyle.getDefault().fontSize(16f));

                        text.currentPageNumber();
                        text.span(" / ");
                        text.totalPages();
                    });
            });
        });
    }
}
