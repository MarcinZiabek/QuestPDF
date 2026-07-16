// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternDocumentStructureExample.cs.
import * as fs from 'node:fs';
import { test } from 'node:test';
import { Colors, Document, IContainer, PageSizes, Placeholders } from '../../index';
import { output } from '../doc-example';

test('CodePatternDocumentStructureExample.Example', () => {
    const content = generateReport();
    fs.writeFileSync(output('code-pattern-document-structure.pdf'), content);
});

function generateReport(): Uint8Array {
    return Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .paddingBottom(15)
                    .column((column) => {
                        column.item().element(reportTitle);
                        column.item().pageBreak();
                        column.item().element(redSection);
                        column.item().pageBreak();
                        column.item().element(greenSection);
                        column.item().pageBreak();
                        column.item().element(blueSection);
                    });

                page.footer().alignCenter().text((text) => text.currentPageNumber());
            });
        })
        .generatePdf();
}

function reportTitle(container: IContainer): void {
    container.extend()
        .alignCenter()
        .alignMiddle()
        .text('Multi-section report')
        .fontSize(48)
        .bold();
}

function redSection(container: IContainer): void {
    container.grid((grid) => {
        grid.columns(3);
        grid.spacing(15);

        grid.item(3).text('Red section')
            .fontColor(Colors.Red.Darken2).fontSize(32).bold();

        grid.item(3).text(Placeholders.paragraph()).light();

        for (let i = 0; i < 6; i++)
            grid.item().aspectRatio(4 / 3).background(Colors.Red.Lighten4);
    });
}

function greenSection(container: IContainer): void {
    container.grid((grid) => {
        grid.columns(3);
        grid.spacing(15);

        grid.item(3).text('Green section')
            .fontColor(Colors.Green.Darken2).fontSize(32).bold();

        grid.item(3).text(Placeholders.paragraph()).light();

        for (let i = 0; i < 12; i++)
            grid.item().aspectRatio(4 / 3).background(Colors.Green.Lighten4);
    });
}

function blueSection(container: IContainer): void {
    container.grid((grid) => {
        grid.columns(3);
        grid.spacing(15);

        grid.item(3).text('Blue section')
            .fontColor(Colors.Blue.Darken2).fontSize(32).bold();

        grid.item(3).text(Placeholders.paragraph()).light();

        for (let i = 0; i < 18; i++)
            grid.item().aspectRatio(4 / 3).background(Colors.Blue.Lighten4);
    });
}
