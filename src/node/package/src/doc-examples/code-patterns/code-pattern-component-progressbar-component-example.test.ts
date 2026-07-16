// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternComponentProgressbarComponentExample.cs.
import * as fs from 'node:fs';
import { test } from 'node:test';
import { Colors, Document, DynamicComponentComposeResult, DynamicContext, IDynamicComponent, PageSizes } from '../../index';
import { output } from '../doc-example';

test('CodePatternComponentProgressbarComponentExample.Example', () => {
    const content = generateReport();
    fs.writeFileSync(output('code-pattern-dynamic-component-progressbar.pdf'), content);
});

function generateReport(): Uint8Array {
    return Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A4);
                page.margin(50);
                page.defaultTextStyle((style) => style.fontSize(20));

                page.header().column((column) => {
                    column.item()
                        .text('MyBrick Set')
                        .fontSize(48).fontColor(Colors.Blue.Darken2).bold();

                    column.item()
                        .text('Building Instruction')
                        .fontSize(24);

                    column.item().height(15);

                    column.item().dynamic(new PageProgressbarComponent());
                });

                page.content().paddingVertical(25).column((column) => {
                    column.spacing(25);

                    for (let i = 1; i <= 30; i++) {
                        column.item()
                            .background(Colors.Grey.Lighten3)
                            .height((Math.floor(Math.random() * 4) + 4) * 25)
                            .alignCenter()
                            .alignMiddle()
                            .text(`Step ${i}`);
                    }
                });

                page.footer().dynamic(new PageNumberSideComponent());
            });
        })
        .generatePdf();
}

class PageProgressbarComponent implements IDynamicComponent {
    compose(context: DynamicContext): DynamicComponentComposeResult {
        const content = context.createElement((element) => {
            const width = context.availableSize.width * context.pageNumber / context.totalPages;

            element
                .background(Colors.Blue.Lighten3)
                .height(5)
                .width(width)
                .background(Colors.Blue.Darken2);
        });

        const result = new DynamicComponentComposeResult();
        result.content = content;
        result.hasMoreContent = false;
        return result;
    }
}

class PageNumberSideComponent implements IDynamicComponent {
    compose(context: DynamicContext): DynamicComponentComposeResult {
        const content = context.createElement((element) => {
            // The chainable Element(Func<IContainer, IContainer>) overload is not bridged;
            // the conditional alignment is applied inline instead.
            const aligned = context.pageNumber % 2 === 0 ? element.alignRight() : element.alignLeft();

            aligned
                .text((text) => {
                    text.span('Page ');
                    text.currentPageNumber();
                });
        });

        const result = new DynamicComponentComposeResult();
        result.content = content;
        result.hasMoreContent = false;
        return result;
    }
}
