package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSizes;
import org.junit.jupiter.api.Test;

public class SectionExamples extends DocExample {

    @Test
    public void example() {
        Document
            .create(document -> {
                document.page(page -> {
                    page.size(PageSizes.getA5().landscape());
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .column(column -> {
                            var terms = new String[][] {
                                { "Bit", "The smallest unit of data in computing, representing either a 0 or a 1. Multiple bits are combined to form bytes, which are used to store larger data values." },
                                { "Byte", "A unit of digital information that consists of 8 bits. A byte is commonly used to store a single character of text, such as a letter or a number, in computer memory." },
                                { "Binary", "A number system that uses only two digits, 0 and 1, which are the fundamental building blocks of computer operations. Computers process and store all data in binary format, including text, images, and instructions." },
                                { "Array", "A data structure that stores a fixed-size sequence of elements, all of the same type, in a contiguous block of memory. Arrays allow quick access to elements using an index and are commonly used to manage collections of data." },
                            };

                            // title
                            column.item().extend().alignMiddle().alignCenter().text("Programming Glossary").fontSize(32f).bold();
                            column.item().pageBreak();

                            // table of contents
                            column.item().paddingBottom(25f).text("Table of Contents").fontSize(24f).bold().underline();

                            for (var term : terms) {
                                column.item()
                                    .paddingBottom(10f)
                                    .sectionLink(sectionName(term))
                                    .text(text -> {
                                        text.span("Term ");
                                        text.span(term[0]).bold();
                                        text.span(" on page ");
                                        text.beginPageNumberOfSection(sectionName(term));
                                    });
                            }

                            // content
                            for (var term : terms) {
                                column.item().pageBreak();

                                column.item()
                                    .section(sectionName(term))
                                    .text(text -> {
                                        text.span(term[0]).bold().fontColor(Colors.Blue.getDarken2());
                                        text.span(" - ");
                                        text.span(term[1]);
                                    });
                            }
                        });
                });
            })
            .generatePdf(output("sections.pdf"));
    }

    // The C# original interpolates the whole tuple ($"term-{term}"), which renders
    // as "term-(Item1, Item2)"; replicated here exactly.
    private static String sectionName(String[] term) {
        return "term-(" + term[0] + ", " + term[1] + ")";
    }
}
