// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ScaleExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('ScaleExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(350)
                    .padding(25)
                    .column((column) => {
                        column.spacing(10);

                        const scales = [0.75, 1, 1.25, 1.5];

                        for (const scale of scales) {
                            column
                                .item()
                                .background(Colors.Grey.Lighten3)
                                .scale(scale)
                                .padding(10)
                                .text(`Content scale: ${scale}`)
                                .fontSize(20);
                        }
                    });
            });
        })
        .generateImages(() => output('scale.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
