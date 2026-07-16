package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.helpers.PageSize;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class RepeatExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(600f, 0f));
                    page.maxSize(new PageSize(600f, 600f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .decoration(decoration -> {
                            var terms = new String[][] {
                                { "Algorithm", "A precise set of instructions that defines a process for solving a specific problem or performing a computation. Algorithms are the foundation of programming and are used to optimize tasks efficiently." },
                                { "Bug", "An error, flaw, or unintended behavior in a program that causes it to produce incorrect or unexpected results. Debugging is the process of identifying, analyzing, and fixing these issues to improve software reliability." },
                                { "Variable", "A named storage location in memory that holds a value, which can be modified during program execution. Variables make code dynamic and flexible by allowing data manipulation and retrieval." },
                                { "Compilation", "The process of transforming human-readable source code into machine code (binary instructions) that a computer can execute. This process is performed by a compiler and often includes syntax checks, optimizations, and linking dependencies." },
                            };

                            decoration.before().text("Terms and their definitions:").bold();

                            decoration.content().paddingTop(15f).column(column -> {
                                for (var term : terms) {
                                    column.item().row(row -> {
                                        row.relativeItem(2f)
                                            .border(1f)
                                            .background(Colors.Grey.getLighten3())
                                            .padding(15f)
                                            .repeat()
                                            .text(term[0]);

                                        row.relativeItem(3f)
                                            .border(1f)
                                            .padding(15f)
                                            .text(term[1]);
                                    });
                                }
                            });
                        });
                });
            })
            .generateImages(index -> output("repeat-with-" + index + ".webp"), settings);
    }
}
