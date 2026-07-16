package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.Colors;
import com.questpdf.infrastructure.ImageCompressionQuality;
import com.questpdf.infrastructure.ImageFormat;
import com.questpdf.infrastructure.ImageGenerationSettings;
import org.junit.jupiter.api.Test;

public class SkipOnceExamples extends DocExample {

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
                        .column(column -> {
                            var terms = new String[][] {
                                { "Repository", "A centralized storage location for source code and related files, typically managed using version control systems like Git. Repositories allow multiple developers to collaborate on projects, track changes, and maintain version history." },
                                { "Version Control", "A system that tracks changes to code over time, enabling developers to collaborate efficiently, revert to previous versions, and maintain a structured development workflow. Popular version control tools include Git, Mercurial, and Subversion." },
                                { "Abstraction", "A programming concept that hides complex implementation details and exposes only the necessary parts. Abstraction helps simplify code and allows developers to focus on high-level design rather than low-level implementation details." },
                                { "Namespace", "A container that groups related identifiers, such as variables, functions, and classes, to prevent naming conflicts in a program. Namespaces are commonly used in large projects to organize code efficiently." },
                            };

                            column.spacing(15f);

                            for (var term : terms) {
                                column.item().decoration(decoration -> {
                                    decoration.before()
                                        .defaultTextStyle(style -> style.fontSize(24f).bold().fontColor(Colors.Blue.getDarken2()))
                                        .column(innerColumn -> {
                                            innerColumn.item().showOnce().text(term[0]);

                                            innerColumn.item().skipOnce().text(text -> {
                                                text.span(term[0]);
                                                text.span(" (continued)").light().italic();
                                            });
                                        });

                                    decoration.content().text(term[1]);
                                });
                            }
                        });
                });
            })
            .generateImages(index -> output("skip-once-" + index + ".webp"), settings);
    }
}
