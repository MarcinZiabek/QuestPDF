// Port of src/dotnet/library/QuestPDF.DocumentationExamples/RowExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('RowExamples.SimpleExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.margin(25);

                page.content()
                    .padding(25)
                    .width(325)
                    .row((row) => {
                        row.constantItem(100)
                            .background(Colors.Grey.Medium)
                            .padding(10)
                            .text('100pt');

                        row.relativeItem()
                            .background(Colors.Grey.Lighten1)
                            .padding(10)
                            .text('75pt');

                        row.relativeItem(2)
                            .background(Colors.Grey.Lighten2)
                            .padding(10)
                            .text('150pt');
                    });
            });
        })
        .generateImages(() => output('row-simple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('RowExamples.SpacingExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.margin(25);

                page.content()
                    .padding(25)
                    .width(220)
                    .height(50)
                    .row((row) => {
                        row.spacing(10);

                        row.relativeItem(2).background(Colors.Grey.Medium);
                        row.relativeItem(3).background(Colors.Grey.Lighten1);
                        row.relativeItem(5).background(Colors.Grey.Lighten2);
                    });
            });
        })
        .generateImages(() => output('row-spacing.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('RowExamples.CustomSpacingExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(250, 0));
                page.maxSize(new PageSize(250, 1000));
                page.margin(25);

                page.content()
                    .height(50)
                    .row((row) => {
                        row.relativeItem().background(Colors.Grey.Darken1);
                        row.constantItem(10);
                        row.relativeItem().background(Colors.Grey.Medium);
                        row.constantItem(20);
                        row.relativeItem().background(Colors.Grey.Lighten1);
                        row.constantItem(30);
                        row.relativeItem().background(Colors.Grey.Lighten2);
                    });
            });
        })
        .generateImages(() => output('row-spacing-custom.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('RowExamples.DisableUniformItemsHeightExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(700, 0));
                page.maxSize(new PageSize(700, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .row((row) => {
                        row.spacing(15);

                        labelStyle(row.relativeItem())
                            .text('Programming is both a science and an art — it demands precision, creativity, and patience. At its core, it’s about understanding how to break down complex problems into small, logical steps that a computer can execute.');

                        labelStyle(row.relativeItem())
                            .text('Programming is the art of turning ideas into logic, logic into code, and code into something that solves real problems.');

                        // The C# original routes LabelStyle through the Element(Func<IContainer, IContainer>)
                        // overload, which is not bridged; a free function is the direct equivalent.
                        function labelStyle(container: IContainer): IContainer {
                            return container
                                .shrinkVertical()
                                .background(Colors.Grey.Lighten3)
                                .cornerRadius(15)
                                .padding(15);
                        }
                    });
            });
        })
        .generateImages(() => output('row-uniform-height-enabled.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});
