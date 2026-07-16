// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternContentStylingExample.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize, Placeholders, TextStyle } from '../../index';
import { imageSettings, output } from '../doc-example';

test('CodePatternContentStylingExample.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(650, 0));
                page.maxSize(new PageSize(650, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            columns.constantColumn(50);
                            columns.relativeColumn(1);
                            columns.relativeColumn(2);
                        });

                        table.header((header) => {
                            // The chainable Element(Func<IContainer, IContainer>) overload is not bridged;
                            // the style helper is applied as a free function instead.
                            style(header.cell()).text('#');
                            style(header.cell()).text('Product Name');
                            style(header.cell()).text('Description');

                            function style(container: IContainer): IContainer {
                                return container
                                    .background(Colors.Blue.Lighten5)
                                    .padding(10)
                                    .defaultTextStyle(TextStyle.Default.fontColor(Colors.Blue.Darken4).bold());
                            }
                        });

                        for (let i = 1; i <= 5; i++) {
                            style(table.cell()).text(i.toString());
                            style(table.cell()).text(Placeholders.label());
                            style(table.cell()).text(Placeholders.sentence());
                        }

                        function style(container: IContainer): IContainer {
                            return container
                                .borderTop(2)
                                .borderColor(Colors.Blue.Lighten3)
                                .padding(10);
                        }
                    });
            });
        })
        .generateImages(() => output('code-pattern-content-styling.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
