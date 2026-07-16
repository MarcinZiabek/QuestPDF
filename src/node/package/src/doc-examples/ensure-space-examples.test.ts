// Port of src/dotnet/library/QuestPDF.DocumentationExamples/EnsureSpaceExamples.cs.
import { test } from 'node:test';
import { Colors, Document, PageSizes, Placeholders } from '../index';
import { output } from './doc-example';

test('EnsureSpaceExamples.EnabledExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(30);

                page.content()
                    .column((column) => {
                        column.item().height(400).background(Colors.Grey.Lighten3);
                        column.item().height(30);

                        column.item()
                            .ensureSpace(100)
                            .table((table) => {
                                table.columnsDefinition((columns) => {
                                    columns.constantColumn(40);
                                    columns.relativeColumn();
                                });

                                for (let i = 1; i <= 12; i++) {
                                    table.cell().text(`${i}.`);
                                    table.cell().showEntire().text(Placeholders.sentence());
                                }
                            });
                    });
            });
        })
        .generatePdf(output('ensure-space-enabled.pdf'));
});

test('EnsureSpaceExamples.DisabledExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(30);

                page.content()
                    .column((column) => {
                        column.item().height(400).background(Colors.Grey.Lighten3);
                        column.item().height(30);

                        column.item()
                            .table((table) => {
                                table.columnsDefinition((columns) => {
                                    columns.constantColumn(40);
                                    columns.relativeColumn();
                                });

                                for (let i = 1; i <= 12; i++) {
                                    table.cell().text(`${i}.`);
                                    table.cell().text(Placeholders.sentence());
                                }
                            });
                    });
            });
        })
        .generatePdf(output('ensure-space-disabled.pdf'));
});
