// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ContentDirectionExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('ContentDirectionExamples.LeftToRightExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(250)
                    .contentFromLeftToRight()
                    .row((row) => {
                        row.spacing(5);

                        row.autoItem().height(50).width(50).background(Colors.Red.Lighten1);
                        row.autoItem().height(50).width(50).background(Colors.Green.Lighten1);
                        row.autoItem().height(50).width(75).background(Colors.Blue.Lighten1);
                    });
            });
        })
        .generateImages(() => output('content-direction-ltr.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ContentDirectionExamples.RightToLeftExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));

                page.content()
                    .width(250)
                    .contentFromRightToLeft()
                    .row((row) => {
                        row.spacing(5);

                        row.autoItem().height(50).width(50).background(Colors.Red.Lighten1);
                        row.autoItem().height(50).width(50).background(Colors.Green.Lighten1);
                        row.autoItem().height(50).width(75).background(Colors.Blue.Lighten1);
                    });
            });
        })
        .generateImages(() => output('content-direction-rtl.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
