// Port of src/dotnet/library/QuestPDF.DocumentationExamples/SectionExamples.cs.
import { test } from 'node:test';
import { Colors, Document, PageSizes } from '../index';
import { output } from './doc-example';

test('SectionExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5.landscape());
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        const terms: [string, string][] = [
                            ['Bit', 'The smallest unit of data in computing, representing either a 0 or a 1. Multiple bits are combined to form bytes, which are used to store larger data values.'],
                            ['Byte', 'A unit of digital information that consists of 8 bits. A byte is commonly used to store a single character of text, such as a letter or a number, in computer memory.'],
                            ['Binary', 'A number system that uses only two digits, 0 and 1, which are the fundamental building blocks of computer operations. Computers process and store all data in binary format, including text, images, and instructions.'],
                            ['Array', 'A data structure that stores a fixed-size sequence of elements, all of the same type, in a contiguous block of memory. Arrays allow quick access to elements using an index and are commonly used to manage collections of data.'],
                        ];

                        // The C# original interpolates the whole tuple ($"term-{term}"),
                        // which renders as "term-(Item1, Item2)"; replicated here exactly.
                        const sectionName = (term: [string, string]) => `term-(${term[0]}, ${term[1]})`;

                        // title
                        column.item().extend().alignMiddle().alignCenter().text('Programming Glossary').fontSize(32).bold();
                        column.item().pageBreak();

                        // table of contents
                        column.item().paddingBottom(25).text('Table of Contents').fontSize(24).bold().underline();

                        for (const term of terms) {
                            column.item()
                                .paddingBottom(10)
                                .sectionLink(sectionName(term))
                                .text((text) => {
                                    text.span('Term ');
                                    text.span(term[0]).bold();
                                    text.span(' on page ');
                                    text.beginPageNumberOfSection(sectionName(term));
                                });
                        }

                        // content
                        for (const term of terms) {
                            column.item().pageBreak();

                            column.item()
                                .section(sectionName(term))
                                .text((text) => {
                                    text.span(term[0]).bold().fontColor(Colors.Blue.Darken2);
                                    text.span(' - ');
                                    text.span(term[1]);
                                });
                        }
                    });
            });
        })
        .generatePdf(output('sections.pdf'));
});
