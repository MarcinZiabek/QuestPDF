package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.fluent.DocumentOperation;
import com.questpdf.helpers.Colors;
import com.questpdf.infrastructure.Color;
import com.questpdf.infrastructure.Unit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DocumentOperationExamples extends DocExample {

    @Test
    public void mergeFiles() {
        var prefix = "document-operation-merge";

        generateSampleDocument(output(prefix + "-source-red.pdf"), Colors.Red.getLighten3(), 2);
        generateSampleDocument(output(prefix + "-source-green.pdf"), Colors.Green.getLighten3(), 3);
        generateSampleDocument(output(prefix + "-source-blue.pdf"), Colors.Blue.getLighten3(), 5);

        DocumentOperation
            .loadFile(output(prefix + "-source-red.pdf"))
            .mergeFile(output(prefix + "-source-green.pdf"))
            .mergeFile(output(prefix + "-source-blue.pdf"))
            .save(output(prefix + "-result.pdf"));
    }

    @Test
    public void selectEvenPages() {
        var prefix = "document-operation-select-even-pages";

        generateSampleDocument(output(prefix + "-source.pdf"), Colors.Indigo.getLighten3(), 11);

        DocumentOperation
            .loadFile(output(prefix + "-source.pdf"))
            .takePages("1-z:even")
            .save(output(prefix + "-result.pdf"));
    }

    @Test
    public void encrypt() {
        var prefix = "document-operation-encrypt";

        generateSampleDocument(output(prefix + "-source.pdf"), Colors.Orange.getLighten3(), 7);

        var encryption = new DocumentOperation.Encryption256Bit();
        encryption.setUserPassword("user-password");
        encryption.setOwnerPassword("owner-password");
        encryption.setAllowContentExtraction(false);
        encryption.setAllowPrinting(false);

        DocumentOperation
            .loadFile(output(prefix + "-source.pdf"))
            .encrypt(encryption)
            .save(output(prefix + "-result.pdf"));
    }

    @Test
    public void addAttachment() throws IOException {
        var prefix = "document-operation-add-attachment";

        generateSampleDocument(output(prefix + "-source.pdf"), Colors.Cyan.getLighten3(), 7);
        Files.writeString(Path.of(output(prefix + "-content.txt")), "Hello, World!");

        var attachment = new DocumentOperation.DocumentAttachment();
        attachment.setFilePath(output(prefix + "-content.txt"));
        attachment.setAttachmentName("Attached message");

        DocumentOperation
            .loadFile(output(prefix + "-source.pdf"))
            .addAttachment(attachment)
            .save(output(prefix + "-result.pdf"));
    }

    @Test
    public void overlay() {
        var prefix = "document-operation-overlay";

        generateSampleDocument(output(prefix + "-source.pdf"), Colors.Cyan.getLighten3(), 7);

        Document
            .create(document -> {
                document.page(page -> {
                    page.margin(1f, Unit.Centimetre);
                    page.pageColor(Colors.getTransparent());

                    page.content().column(column -> {
                        for (var i = 0; i < 6; i++)
                            column.item().pageBreak();
                    });

                    page.footer().alignCenter().text(text -> {
                        text.defaultTextStyle(style -> style.fontSize(24f).bold().fontColor(Colors.getWhite()));
                        text.span("Page ");
                        text.currentPageNumber();
                        text.span(" of ");
                        text.totalPages();
                    });
                });
            })
            .generatePdf(output(prefix + "-content.pdf"));

        var configuration = new DocumentOperation.LayerConfiguration();
        configuration.setFilePath(output(prefix + "-content.pdf"));

        DocumentOperation
            .loadFile(output(prefix + "-source.pdf"))
            .overlayFile(configuration)
            .save(output(prefix + "-result.pdf"));
    }

    private static void generateSampleDocument(String fileName, Color pageColor, int numberOfPages) {
        Document
            .create(container -> {
                container.page(page -> {
                    page.margin(1f, Unit.Centimetre);
                    page.pageColor(pageColor);

                    page.content().column(column -> {
                        for (var pageNumber = 1; pageNumber <= numberOfPages; pageNumber++) {
                            column.item()
                                .extend()
                                .alignCenter().alignMiddle()
                                .text(String.valueOf(pageNumber))
                                .fontSize(256f)
                                .fontColor(Colors.getWhite())
                                .bold();

                            if (pageNumber != numberOfPages)
                                column.item().pageBreak();
                        }
                    });
                });
            })
            .generatePdf(fileName);
    }
}
