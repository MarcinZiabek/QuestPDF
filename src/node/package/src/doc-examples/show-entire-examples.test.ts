// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ShowEntireExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat } from '../index';
import { imageSettings, output } from './doc-example';

test('ShowEntireExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(500, 500);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .decoration((decoration) => {
                        const terms: [string, string][] = [
                            ['Function', 'A reusable block of code designed to perform a specific task. Functions take input parameters, process them, and return results, making code modular, readable, and maintainable. They are an essential component of all programming languages.'],
                            ['Recursion', 'A programming technique where a function calls itself in order to solve a problem by breaking it down into smaller, similar subproblems. Recursion is often used for complex algorithms, such as searching, sorting, and tree traversal.'],
                            ['Framework', 'A pre-built collection of code, tools, and best practices that provides a structured foundation for developing software. Frameworks simplify development by handling common functionalities, such as database access, user authentication, and UI rendering.'],
                            ['Package', 'A self-contained collection of code, typically consisting of functions, classes, and modules, that provides specific functionality. Packages help organize large projects and allow developers to reuse and distribute their code easily.'],
                        ];

                        decoration.before().text('Terms and their definitions:').fontSize(24).bold().underline();

                        decoration.content().paddingTop(15).column((column) => {
                            column.spacing(15);

                            for (const term of terms) {
                                column.item()
                                    .showEntire()
                                    .text((text) => {
                                        text.span(term[0]).bold().fontColor(Colors.Blue.Darken2);
                                        text.span(` - ${term[1]}`);
                                    });
                            }
                        });
                    });
            });
        })
        .generateImages((index) => output(`show-entire-with-${index}.webp`), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
