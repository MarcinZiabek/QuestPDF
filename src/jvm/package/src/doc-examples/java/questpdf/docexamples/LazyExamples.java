package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.infrastructure.IComponent;
import com.questpdf.infrastructure.IContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LazyExamples extends DocExample {

    private static class SimpleComponent implements IComponent {
        private final int start;
        private final int end;

        SimpleComponent(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void compose(IContainer container) {
            container.decoration(decoration -> {
                decoration.before()
                    .text("Numbers from " + start + " to " + end)
                    .fontSize(20f).bold().fontColor(Colors.Blue.getDarken2());

                decoration.content().column(column -> {
                    for (var i = start; i <= end; i++)
                        column.item().text("Number " + i).fontSize(10f);
                });
            });
        }
    }

    @Test
    @Disabled("This test is for manual testing only.")
    public void disabled() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.margin(10f);

                    page.content().column(column -> {
                        var sectionSize = 1000;

                        for (var i = 0; i < 1000; i++) {
                            column.item().component(new SimpleComponent(
                                i * sectionSize,
                                i * sectionSize + sectionSize - 1
                            ));
                        }
                    });
                });
            })
            .generatePdf(output("lazy-disabled.pdf"));
    }

    @Test
    @Disabled("This test is for manual testing only.")
    public void enabled() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.margin(10f);
                    page.content().column(column -> {
                        var sectionSize = 1000;

                        for (var i = 0; i < 1000; i++) {
                            var start = i * sectionSize;
                            var end = start + sectionSize - 1;

                            column.item().lazy(c -> {
                                c.component(new SimpleComponent(start, end));
                            });
                        }
                    });
                });
            })
            .generatePdf(output("lazy-enabled.pdf"));
    }

    @Test
    @Disabled("This test is for manual testing only.")
    public void enabledWithCache() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.margin(10f);

                    page.content().column(column -> {
                        var sectionSize = 1000;

                        for (var i = 0; i < 1000; i++) {
                            var start = i * sectionSize;
                            var end = start + sectionSize - 1;

                            column.item().lazyWithCache(c -> {
                                c.component(new SimpleComponent(start, end));
                            });
                        }
                    });
                });
            })
            .generatePdf(output("lazy-enabled-with-cache.pdf"));
    }
}
