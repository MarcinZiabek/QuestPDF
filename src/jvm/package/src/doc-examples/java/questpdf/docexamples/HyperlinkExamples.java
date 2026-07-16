package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import org.junit.jupiter.api.Test;

public class HyperlinkExamples extends DocExample {

    @Test
    public void elementExample() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(400f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            column.spacing(25f);

                            column.item()
                                .text("Clicking the NuGet logo will redirect you to the NuGet website.");

                            column.item()
                                .width(150f)
                                .hyperlink("https://www.nuget.org/")
                                .svg(resource("nuget-logo.svg"));
                        });
                });
            })
            .generatePdf(output("hyperlink-element.pdf"));
    }

    @Test
    public void insideTextExample() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.continuousSize(300f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .text(text -> {
                            text.span("Click ");
                            text.hyperlink("here", "https://www.nuget.org/").underline().fontColor(Colors.Blue.getDarken2());
                            text.span(" to visit the official NuGet website.");
                        });
                });
            })
            .generatePdf(output("hyperlink-text.pdf"));
    }
}
