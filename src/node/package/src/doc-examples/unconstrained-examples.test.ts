// Port of src/dotnet/library/QuestPDF.DocumentationExamples/UnconstrainedExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('UnconstrainedExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(400)
                    .height(350)
                    .padding(25)
                    .paddingLeft(50)
                    .column((column) => {
                        column.item().width(300).height(150).background(Colors.Blue.Lighten3);

                        column
                            .item()
                            .unconstrained()
                            .offsetX(-50)
                            .offsetY(-50)
                            .width(100)
                            .height(100)
                            .background(Colors.Blue.Darken2);

                        column.item().width(300).height(150).background(Colors.Blue.Lighten2);
                    });
            });
        })
        .generateImages(() => output('unconstrained.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
