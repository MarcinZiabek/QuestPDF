// Port of src/dotnet/library/QuestPDF.DocumentationExamples/RepeatExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('RepeatExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(600, 0));
                page.maxSize(new PageSize(600, 600));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .decoration((decoration) => {
                        const terms = [
                            ['Algorithm', 'A precise set of instructions that defines a process for solving a specific problem or performing a computation. Algorithms are the foundation of programming and are used to optimize tasks efficiently.'],
                            ['Bug', 'An error, flaw, or unintended behavior in a program that causes it to produce incorrect or unexpected results. Debugging is the process of identifying, analyzing, and fixing these issues to improve software reliability.'],
                            ['Variable', 'A named storage location in memory that holds a value, which can be modified during program execution. Variables make code dynamic and flexible by allowing data manipulation and retrieval.'],
                            ['Compilation', 'The process of transforming human-readable source code into machine code (binary instructions) that a computer can execute. This process is performed by a compiler and often includes syntax checks, optimizations, and linking dependencies.'],
                        ];

                        decoration.before().text('Terms and their definitions:').bold();

                        decoration.content().paddingTop(15).column((column) => {
                            for (const term of terms) {
                                column.item().row((row) => {
                                    row.relativeItem(2)
                                        .border(1)
                                        .background(Colors.Grey.Lighten3)
                                        .padding(15)
                                        .repeat()
                                        .text(term[0]);

                                    row.relativeItem(3)
                                        .border(1)
                                        .padding(15)
                                        .text(term[1]);
                                });
                            }
                        });
                    });
            });
        })
        .generateImages((index) => output(`repeat-with-${index}.webp`), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
