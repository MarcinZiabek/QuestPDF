// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ColumnExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output } from './doc-example';

test('ColumnExamples.SimpleExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(250, 0));
                page.maxSize(new PageSize(250, 1000));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.item().background(Colors.Grey.Medium).height(50);
                        column.item().background(Colors.Grey.Lighten1).height(75);
                        column.item().background(Colors.Grey.Lighten2).height(100);
                    });
            });
        })
        .generateImages(() => output('column-simple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ColumnExamples.SpacingExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(250, 0));
                page.maxSize(new PageSize(250, 1000));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(25);

                        column.item().background(Colors.Grey.Medium).height(50);
                        column.item().background(Colors.Grey.Lighten1).height(75);
                        column.item().background(Colors.Grey.Lighten2).height(100);
                    });
            });
        })
        .generateImages(() => output('column-spacing.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ColumnExamples.CustomSpacingExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(250, 0));
                page.maxSize(new PageSize(250, 1000));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.item().background(Colors.Grey.Darken1).height(50);
                        column.item().height(10);
                        column.item().background(Colors.Grey.Medium).height(50);
                        column.item().height(20);
                        column.item().background(Colors.Grey.Lighten1).height(50);
                        column.item().height(30);
                        column.item().background(Colors.Grey.Lighten2).height(50);
                    });
            });
        })
        .generateImages(() => output('column-spacing-custom.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ColumnExamples.DisableUniformItemsWidthExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(400, 0));
                page.maxSize(new PageSize(400, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .column((column) => {
                        column.spacing(15);

                        labelStyle(column.item())
                            .text('REST API');

                        labelStyle(column.item())
                            .text('Garbage Collection');

                        labelStyle(column.item())
                            .text('Object-Oriented Programming');

                        // The C# original routes LabelStyle through the Element(Func<IContainer, IContainer>)
                        // overload, which is not bridged; a free function is the direct equivalent.
                        function labelStyle(container: IContainer): IContainer {
                            return container
                                .shrinkHorizontal()
                                .background(Colors.Grey.Lighten3)
                                .cornerRadius(15)
                                .padding(15);
                        }
                    });
            });
        })
        .generateImages(() => output('column-uniform-width-disabled.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
