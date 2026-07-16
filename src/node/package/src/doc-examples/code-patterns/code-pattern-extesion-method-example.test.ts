// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternExtesionMethodExample.cs.
import { test } from 'node:test';
import { Color, Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../../index';
import { imageSettings, output } from '../doc-example';

test('CodePatternExtensionMethodExample.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(600, 0));
                page.maxSize(new PageSize(600, 1000));
                page.defaultTextStyle((style) => style.fontSize(14));
                page.margin(25);

                page.content()
                    .border(1)
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            columns.relativeColumn(2);
                            columns.relativeColumn(3);
                            columns.relativeColumn(2);
                            columns.relativeColumn(3);
                        });

                        tableLabelCell(table.cell(), 'Product name');
                        tableValueCell(table.cell()).text(Placeholders.label());

                        tableLabelCell(table.cell(), 'Description');
                        tableValueCell(table.cell()).text(Placeholders.sentence());

                        tableLabelCell(table.cell(), 'Price');
                        tableValueCell(table.cell()).text(Placeholders.price());

                        tableLabelCell(table.cell(), 'Date of production');
                        tableValueCell(table.cell()).text(Placeholders.shortDate());

                        tableLabelCell(table.cell().columnSpan(2), 'Photo of the product');
                        // Image(Func<ImageSize, byte[]>) is not bridged; the payload-based delegate overload is used instead.
                        tableValueCell(table.cell().columnSpan(2)).aspectRatio(16 / 9).image((payload) => Placeholders.image(payload.imageSize));
                    });
            });
        })
        .generateImages(() => output('code-pattern-extension-methods.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

// The C# extension methods on IContainer become free functions taking the container.
function tableCellStyle(container: IContainer, backgroundColor: Color): IContainer {
    return container
        .border(1)
        .borderColor(Colors.Black)
        .background(backgroundColor)
        .padding(10);
}

function tableLabelCell(container: IContainer, text: string): void {
    tableCellStyle(container, Colors.Grey.Lighten3)
        .text(text)
        .bold();
}

function tableValueCell(container: IContainer): IContainer {
    return tableCellStyle(container, Colors.Transparent);
}
