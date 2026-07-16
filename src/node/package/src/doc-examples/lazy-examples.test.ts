// Port of src/dotnet/library/QuestPDF.DocumentationExamples/LazyExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IComponent, IContainer } from '../index';
import { output } from './doc-example';

class SimpleComponent implements IComponent {
    constructor(
        private readonly start: number,
        private readonly end: number) {
    }

    compose(container: IContainer): void {
        container.decoration((decoration) => {
            decoration.before()
                .text(`Numbers from ${this.start} to ${this.end}`)
                .fontSize(20).bold().fontColor(Colors.Blue.Darken2);

            decoration.content().column((column) => {
                for (let i = this.start; i <= this.end; i++)
                    column.item().text(`Number ${i}`).fontSize(10);
            });
        });
    }
}

test('LazyExamples.Disabled', { skip: 'This test is for manual testing only.' }, () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.margin(10);

                page.content().column((column) => {
                    const sectionSize = 1000;

                    for (let i = 0; i < 1000; i++) {
                        column.item().component(new SimpleComponent(
                            i * sectionSize,
                            i * sectionSize + sectionSize - 1));
                    }
                });
            });
        })
        .generatePdf(output('lazy-disabled.pdf'));
});

test('LazyExamples.Enabled', { skip: 'This test is for manual testing only.' }, () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.margin(10);
                page.content().column((column) => {
                    const sectionSize = 1000;

                    for (let i = 0; i < 1000; i++) {
                        const start = i * sectionSize;
                        const end = start + sectionSize - 1;

                        column.item().lazy((c) => {
                            c.component(new SimpleComponent(start, end));
                        });
                    }
                });
            });
        })
        .generatePdf(output('lazy-enabled.pdf'));
});

test('LazyExamples.EnabledWithCache', { skip: 'This test is for manual testing only.' }, () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.margin(10);

                page.content().column((column) => {
                    const sectionSize = 1000;

                    for (let i = 0; i < 1000; i++) {
                        const start = i * sectionSize;
                        const end = start + sectionSize - 1;

                        column.item().lazyWithCache((c) => {
                            c.component(new SimpleComponent(start, end));
                        });
                    }
                });
            });
        })
        .generatePdf(output('lazy-enabled-with-cache.pdf'));
});
