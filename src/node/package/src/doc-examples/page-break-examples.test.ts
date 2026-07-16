// Port of src/dotnet/library/QuestPDF.DocumentationExamples/PageBreakExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer } from '../index';
import { output } from './doc-example';

test('PageBreakExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(300, 450);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .paddingTop(15)
                    .column((column) => {
                        const terms: [string, string][] = [
                            ['Garbage Collection', 'An automatic memory management feature in many programming languages that identifies and removes unused objects to free up memory, preventing memory leaks.'],
                            ['Constructor', 'A special method in object-oriented programming that is automatically called when an object is created. It initializes the object\'s properties and sets up any necessary resources.'],
                            ['Dependency', 'A software component or external library that a program relies on to function correctly. Dependencies can include third-party modules, frameworks, or system-level packages that provide additional functionality without requiring developers to write everything from scratch.'],
                        ];

                        column.item()
                            .extend()
                            .alignCenter().alignMiddle()
                            .text('Programming dictionary').fontSize(24).bold();

                        for (const term of terms) {
                            column.item().pageBreak();
                            column.item().element((c) => generatePage(c, term[0], term[1]));
                        }

                        function generatePage(container: IContainer, term: string, definition: string) {
                            container.text((text) => {
                                text.span(term).bold().fontColor(Colors.Blue.Darken2);
                                text.span(` - ${definition}`);
                            });
                        }
                    });
            });
        })
        .generatePdf(output('page-break.pdf'));
});
