// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CustomFirstPageExample.cs.
import { test } from 'node:test';
import { Colors, Document, PageSizes } from '../index';
import { output } from './doc-example';

test('CustomFirstPageExample.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.margin(30);
                page.defaultTextStyle((style) => style.fontSize(20));

                page.header().column((column) => {
                    column.item().showOnce().background(Colors.Blue.Lighten2).height(80);
                    column.item().skipOnce().background(Colors.Green.Lighten2).height(60);
                });

                page.content().paddingVertical(20).column((column) => {
                    column.spacing(20);

                    for (let i = 0; i < 20; i++)
                        column.item().background(Colors.Grey.Lighten3).height(40);
                });

                page.footer().alignCenter().text((text) => {
                    text.currentPageNumber();
                    text.span(' / ');
                    text.totalPages();
                });
            });
        })
        .generatePdf(output('example-custom-first-page.pdf'));
});
