// Port of src/dotnet/library/QuestPDF.DocumentationExamples/DebugAreaExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('DebugAreaExamples.LeftToRightExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(250)
                    .height(250)
                    .padding(25)
                    .debugArea('Grid example', Colors.Blue.Medium)
                    .grid((grid) => {
                        grid.columns(3);
                        grid.spacing(5);

                        for (let i = 0; i < 8; i++)
                            grid.item().height(50).placeholder();
                    });
            });
        })
        .generateImages(() => output('debug-area.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 216 }));
});
