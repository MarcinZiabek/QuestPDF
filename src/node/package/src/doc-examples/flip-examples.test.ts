// Port of src/dotnet/library/QuestPDF.DocumentationExamples/FlipExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('FlipExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(350, 0));
                page.maxSize(new PageSize(350, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(15);

                        column.item()
                            .text('Read the message below by putting a mirror on the right side of the screen.');

                        column.item()
                            .alignLeft()
                            .background(Colors.Red.Lighten5)
                            .padding(10)
                            .flipHorizontal()
                            .text('This is a secret message.')
                            .fontColor(Colors.Red.Darken2);
                    });
            });
        })
        .generateImages(() => output('flip.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
