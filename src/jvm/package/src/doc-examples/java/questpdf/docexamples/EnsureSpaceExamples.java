package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import org.junit.jupiter.api.Test;

public class EnsureSpaceExamples extends DocExample {

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
                                .ensureSpace(100f)
                                .table(table -> {
                                    table.columnsDefinition(columns -> {
                                        columns.constantColumn(40f);
                                        columns.relativeColumn();
                                    });

                                    for (var i = 1; i <= 12; i++) {
                                        table.cell().text(i + ".");
                                        table.cell().showEntire().text(Placeholders.sentence());
                                    }
                                });
                        });
                });
            })
            .generatePdf(output("ensure-space-enabled.pdf"));
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
                                .table(table -> {
                                    table.columnsDefinition(columns -> {
                                        columns.constantColumn(40f);
                                        columns.relativeColumn();
                                    });

                                    for (var i = 1; i <= 12; i++) {
                                        table.cell().text(i + ".");
                                        table.cell().text(Placeholders.sentence());
                                    }
                                });
                        });
                });
            })
            .generatePdf(output("ensure-space-disabled.pdf"));
    }
}
