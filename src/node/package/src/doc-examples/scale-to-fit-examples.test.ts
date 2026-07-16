// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ScaleToFitExamples.cs.
import { test } from 'node:test';
import { Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('ScaleToFitExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        const text = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.';

                        for (let i = 4; i <= 8; i++) {
                            column
                                .item()
                                .shrink()
                                .border(1)
                                .padding(15)
                                .width(i * 50) // sizes from 200x100 to 450x175
                                .height(i * 25)
                                .scaleToFit()
                                .text(text);
                        }
                    });
            });
        })
        .generateImages(() => output('scale-to-fit.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});
