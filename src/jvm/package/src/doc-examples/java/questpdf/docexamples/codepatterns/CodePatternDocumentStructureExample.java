package questpdf.docexamples.codepatterns;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.IContainer;
import org.junit.jupiter.api.Test;
import questpdf.docexamples.DocExample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodePatternDocumentStructureExample extends DocExample {

    @Test
    public void example() throws IOException {
        var content = generateReport();
        Files.write(Path.of(output("code-pattern-document-structure.pdf")), content);
    }

    public byte[] generateReport() {
        return Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .paddingBottom(15f)
                        .column(column -> {
                            column.item().element(this::reportTitle);
                            column.item().pageBreak();
                            column.item().element(this::redSection);
                            column.item().pageBreak();
                            column.item().element(this::greenSection);
                            column.item().pageBreak();
                            column.item().element(this::blueSection);
                        });

                    page.footer().alignCenter().text(text -> { text.currentPageNumber(); });
                });
            })
            .generatePdf();
    }

    private void reportTitle(IContainer container) {
        container.extend()
            .alignCenter()
            .alignMiddle()
            .text("Multi-section report")
            .fontSize(48f)
            .bold();
    }

    private void redSection(IContainer container) {
        container.grid(grid -> {
            grid.columns(3);
            grid.spacing(15f);

            grid.item(3).text("Red section")
                .fontColor(Colors.Red.getDarken2()).fontSize(32f).bold();

            grid.item(3).text(Placeholders.paragraph()).light();

            for (var i = 0; i < 6; i++)
                grid.item().aspectRatio(4 / 3f).background(Colors.Red.getLighten4());
        });
    }

    private void greenSection(IContainer container) {
        container.grid(grid -> {
            grid.columns(3);
            grid.spacing(15f);

            grid.item(3).text("Green section")
                .fontColor(Colors.Green.getDarken2()).fontSize(32f).bold();

            grid.item(3).text(Placeholders.paragraph()).light();

            for (var i = 0; i < 12; i++)
                grid.item().aspectRatio(4 / 3f).background(Colors.Green.getLighten4());
        });
    }

    private void blueSection(IContainer container) {
        container.grid(grid -> {
            grid.columns(3);
            grid.spacing(15f);

            grid.item(3).text("Blue section")
                .fontColor(Colors.Blue.getDarken2()).fontSize(32f).bold();

            grid.item(3).text(Placeholders.paragraph()).light();

            for (var i = 0; i < 18; i++)
                grid.item().aspectRatio(4 / 3f).background(Colors.Blue.getLighten4());
        });
    }
}
