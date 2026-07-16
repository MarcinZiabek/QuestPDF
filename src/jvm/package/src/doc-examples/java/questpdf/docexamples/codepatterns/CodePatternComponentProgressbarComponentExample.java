package questpdf.docexamples.codepatterns;

import com.questpdf.elements.DynamicContext;
import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import com.questpdf.infrastructure.DynamicComponentComposeResult;
import com.questpdf.infrastructure.IDynamicComponent;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

public class CodePatternComponentProgressbarComponentExample extends DocExample {

    @Test
    public void example() throws IOException {
        var content = generateReport();
        Files.write(Path.of(output("code-pattern-dynamic-component-progressbar.pdf")), content);
    }

    public byte[] generateReport() {
        return Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA4());
                    page.margin(50f);
                    page.defaultTextStyle(style -> style.fontSize(20f));

                    page.header().column(column -> {
                        column.item()
                            .text("MyBrick Set")
                            .fontSize(48f).fontColor(Colors.Blue.getDarken2()).bold();

                        column.item()
                            .text("Building Instruction")
                            .fontSize(24f);

                        column.item().height(15f);

                        column.item().dynamic(new PageProgressbarComponent());
                    });

                    page.content().paddingVertical(25f).column(column -> {
                        column.spacing(25f);

                        for (var i = 1; i <= 30; i++) {
                            column.item()
                                .background(Colors.Grey.getLighten3())
                                .height(ThreadLocalRandom.current().nextInt(4, 8) * 25)
                                .alignCenter()
                                .alignMiddle()
                                .text("Step " + i);
                        }
                    });

                    page.footer().dynamic(new PageNumberSideComponent());
                });
            })
            .generatePdf();
    }

    public static class PageProgressbarComponent implements IDynamicComponent {
        @Override
        public DynamicComponentComposeResult compose(DynamicContext context) {
            var content = context.createElement(element -> {
                var width = context.getAvailableSize().getWidth() * context.getPageNumber() / context.getTotalPages();

                element
                    .background(Colors.Blue.getLighten3())
                    .height(5f)
                    .width(width)
                    .background(Colors.Blue.getDarken2());
            });

            var result = new DynamicComponentComposeResult();
            result.setContent(content);
            result.setHasMoreContent(false);
            return result;
        }
    }

    public static class PageNumberSideComponent implements IDynamicComponent {
        @Override
        public DynamicComponentComposeResult compose(DynamicContext context) {
            var content = context.createElement(element -> {
                // The chainable Element(Func<IContainer, IContainer>) overload is not bridged;
                // the conditional alignment is applied inline instead.
                var aligned = context.getPageNumber() % 2 == 0 ? element.alignRight() : element.alignLeft();

                aligned
                    .text(text -> {
                        text.span("Page ");
                        text.currentPageNumber();
                    });
            });

            var result = new DynamicComponentComposeResult();
            result.setContent(content);
            result.setHasMoreContent(false);
            return result;
        }
    }
}
