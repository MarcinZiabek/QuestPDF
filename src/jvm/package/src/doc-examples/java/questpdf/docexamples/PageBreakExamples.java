package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.infrastructure.IContainer;
import org.junit.jupiter.api.Test;

public class PageBreakExamples extends DocExample {

    @Test
    public void example() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.size(300f, 450f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .paddingTop(15f)
                        .column(column -> {
                            var terms = new String[][] {
                                { "Garbage Collection", "An automatic memory management feature in many programming languages that identifies and removes unused objects to free up memory, preventing memory leaks." },
                                { "Constructor", "A special method in object-oriented programming that is automatically called when an object is created. It initializes the object's properties and sets up any necessary resources." },
                                { "Dependency", "A software component or external library that a program relies on to function correctly. Dependencies can include third-party modules, frameworks, or system-level packages that provide additional functionality without requiring developers to write everything from scratch." },
                            };

                            column.item()
                                .extend()
                                .alignCenter().alignMiddle()
                                .text("Programming dictionary").fontSize(24f).bold();

                            for (var term : terms) {
                                column.item().pageBreak();
                                column.item().element(c -> {
                                    generatePage(c, term[0], term[1]);
                                });
                            }
                        });
                });
            })
            .generatePdf(output("page-break.pdf"));
    }

    private static void generatePage(IContainer container, String term, String definition) {
        container.text(text -> {
            text.span(term).bold().fontColor(Colors.Blue.getDarken2());
            text.span(" - " + definition);
        });
    }
}
