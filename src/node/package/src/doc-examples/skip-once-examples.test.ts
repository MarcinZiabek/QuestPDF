// Port of src/dotnet/library/QuestPDF.DocumentationExamples/SkipOnceExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat } from '../index';
import { imageSettings, output } from './doc-example';

test('SkipOnceExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(500, 500);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        const terms: [string, string][] = [
                            ['Repository', 'A centralized storage location for source code and related files, typically managed using version control systems like Git. Repositories allow multiple developers to collaborate on projects, track changes, and maintain version history.'],
                            ['Version Control', 'A system that tracks changes to code over time, enabling developers to collaborate efficiently, revert to previous versions, and maintain a structured development workflow. Popular version control tools include Git, Mercurial, and Subversion.'],
                            ['Abstraction', 'A programming concept that hides complex implementation details and exposes only the necessary parts. Abstraction helps simplify code and allows developers to focus on high-level design rather than low-level implementation details.'],
                            ['Namespace', 'A container that groups related identifiers, such as variables, functions, and classes, to prevent naming conflicts in a program. Namespaces are commonly used in large projects to organize code efficiently.'],
                        ];

                        column.spacing(15);

                        for (const term of terms) {
                            column.item().decoration((decoration) => {
                                decoration.before()
                                    .defaultTextStyle((style) => style.fontSize(24).bold().fontColor(Colors.Blue.Darken2))
                                    .column((innerColumn) => {
                                        innerColumn.item().showOnce().text(term[0]);

                                        innerColumn.item().skipOnce().text((text) => {
                                            text.span(term[0]);
                                            text.span(' (continued)').light().italic();
                                        });
                                    });

                                decoration.content().text(term[1]);
                            });
                        }
                    });
            });
        })
        .generateImages((index) => output(`skip-once-${index}.webp`), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
