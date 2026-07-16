// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternExecutionOrderExample.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../../index';
import { imageSettings, output } from '../doc-example';

test('CodePatternExecutionOrderExample.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(400, 0));
                page.maxSize(new PageSize(400, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(25);

                        column.item()
                            .border(1)
                            .background(Colors.Blue.Lighten4)
                            .padding(15)
                            .text('border → background → padding');

                        column.item()
                            .border(1)
                            .padding(15)
                            .background(Colors.Blue.Lighten4)
                            .text('border → padding → background');

                        column.item()
                            .background(Colors.Blue.Lighten4)
                            .padding(15)
                            .border(1)
                            .text('background → padding → border');

                        column.item()
                            .padding(15)
                            .border(1)
                            .background(Colors.Blue.Lighten4)
                            .text('padding → border → background');
                    });
            });
        })
        .generateImages(() => output('code-pattern-execution-order.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
