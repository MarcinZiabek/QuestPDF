// Port of src/dotnet/library/QuestPDF.DocumentationExamples/RoundedCornersExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../index';
import { imageSettings, output, resource } from './doc-example';

test('RoundedCornersExamples.Consistent', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .border(1, Colors.Black)
                    .background(Colors.Grey.Lighten3)
                    .cornerRadius(25)
                    .padding(25)
                    .text('Container with consistently rounded corners');
            });
        })
        .generateImages(() => output('rounded-corners-consistent.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('RoundedCornersExamples.Various', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .border(1, Colors.Black)
                    .background(Colors.Grey.Lighten3)
                    .cornerRadiusTopLeft(5)
                    .cornerRadiusTopRight(10)
                    .cornerRadiusBottomRight(20)
                    .cornerRadiusBottomLeft(40)
                    .padding(25)
                    .text('Container with rounded corners');
            });
        })
        .generateImages(() => output('rounded-corners-various.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('RoundedCornersExamples.Complex', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(550, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .border(1, Colors.Black)
                    .cornerRadius(15)
                    .table((table) => {
                        table.columnsDefinition((columns) => {
                            columns.constantColumn(100);
                            columns.relativeColumn();
                            columns.constantColumn(150);
                        });

                        table.header((header) => {
                            style(header.cell()).text('Index');
                            style(header.cell()).text('Label');
                            style(header.cell()).text('Price');

                            // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                            // overload, which is not bridged; a free function is the direct equivalent.
                            function style(container: IContainer): IContainer {
                                return container
                                    .border(1, Colors.Grey.Darken2)
                                    .background(Colors.Grey.Lighten3)
                                    .paddingVertical(10)
                                    .paddingHorizontal(15)
                                    .defaultTextStyle((textStyle) => textStyle.bold());
                            }
                        });

                        for (let index = 1; index <= 5; index++) {
                            style(table.cell()).text(index.toString());
                            style(table.cell()).text(Placeholders.label());
                            style(table.cell()).text(Placeholders.price());

                            function style(container: IContainer): IContainer {
                                return container
                                    .border(1, Colors.Grey.Darken2)
                                    .paddingVertical(10)
                                    .paddingHorizontal(15);
                            }
                        }
                    });
            });
        })
        .generateImages(() => output('rounded-corners-complex.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('RoundedCornersExamples.Image', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(450, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);
                page.pageColor(Colors.White);

                page.content()
                    .cornerRadius(25)
                    .image(resource('landscape.jpg'));
            });
        })
        .generateImages(() => output('rounded-corners-image.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
