package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class ShowEntireExamples extends DocExample {

    @Test
    public void example() {
        var settings = new ImageGenerationSettings();
        settings.setImageFormat(ImageFormat.Webp);
        settings.setImageCompressionQuality(ImageCompressionQuality.Best);
        settings.setRasterDpi(144);

        Document
            .create(document -> {
                document.page(page -> {
                    page.size(500f, 500f);
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .decoration(decoration -> {
                            var terms = new String[][] {
                                { "Function", "A reusable block of code designed to perform a specific task. Functions take input parameters, process them, and return results, making code modular, readable, and maintainable. They are an essential component of all programming languages." },
                                { "Recursion", "A programming technique where a function calls itself in order to solve a problem by breaking it down into smaller, similar subproblems. Recursion is often used for complex algorithms, such as searching, sorting, and tree traversal." },
                                { "Framework", "A pre-built collection of code, tools, and best practices that provides a structured foundation for developing software. Frameworks simplify development by handling common functionalities, such as database access, user authentication, and UI rendering." },
                                { "Package", "A self-contained collection of code, typically consisting of functions, classes, and modules, that provides specific functionality. Packages help organize large projects and allow developers to reuse and distribute their code easily." },
                            };

                            decoration.before().text("Terms and their definitions:").fontSize(24f).bold().underline();

                            decoration.content().paddingTop(15f).column(column -> {
                                column.spacing(15f);

                                for (var term : terms) {
                                    column.item()
                                        .showEntire()
                                        .text(text -> {
                                            text.span(term[0]).bold().fontColor(Colors.Blue.getDarken2());
                                            text.span(" - " + term[1]);
                                        });
                                }
                            });
                        });
                });
            })
            .generateImages(index -> output("show-entire-with-" + index + ".webp"), settings);
    }
}
