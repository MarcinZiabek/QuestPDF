package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import com.questpdf.helpers.Placeholders;
import com.questpdf.infrastructure.DocumentMetadata;
import com.questpdf.infrastructure.DocumentSettings;
import com.questpdf.infrastructure.PDFA_Conformance;
import com.questpdf.infrastructure.PDFUA_Conformance;
import org.junit.jupiter.api.Test;

public class AccessibilityExamples extends DocExample {

    @Test
    public void minimalExample() {
        var metadata = new DocumentMetadata();
        metadata.setLanguage("en-US");
        metadata.setTitle("Accessibility Test");
        metadata.setSubject("This document shows how easy it is to create accessible PDF documents with QuestPDF");

        var settings = new DocumentSettings();
        settings.setPdfaConformance(PDFA_Conformance.PDFA_3A);
        settings.setPdfuaConformance(PDFUA_Conformance.PDFUA_1);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5());
                    page.margin(30f);

                    page.header()
                        .paddingBottom(15f)
                        .semanticHeader1()
                        .text("Accessibility Test Document")
                        .fontColor(Colors.Blue.getDarken3())
                        .fontSize(24f)
                        .bold();

                    page.content()
                        .column(column -> {
                            column.spacing(20f);

                            column.item()
                                .semanticSection()
                                .column(sectionColumn -> {
                                    sectionColumn.item()
                                        .paddingBottom(10f)
                                        .semanticHeader2()
                                        .text("Section with text content")
                                        .fontColor(Colors.Blue.getDarken1())
                                        .fontSize(16f);

                                    sectionColumn.item()
                                        .text(Placeholders.paragraphs())
                                        .fontSize(12f)
                                        .paragraphSpacing(8f);
                                });

                            column.item()
                                .preventPageBreak()
                                .semanticSection()
                                .column(sectionColumn -> {
                                    sectionColumn.item()
                                        .paddingBottom(10f)
                                        .semanticHeader2()
                                        .text("Section with image")
                                        .fontColor(Colors.Blue.getDarken1())
                                        .fontSize(16f);

                                    sectionColumn.item()
                                        .width(250f)
                                        .semanticImage("Image showing a laptop")
                                        .image(resource("product.jpg"));
                                });
                        });
                });
            })
            .withMetadata(metadata)
            .withSettings(settings)
            .generatePdf(output("accessibility-minimal-example.pdf"));
    }
}
