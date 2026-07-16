// Port of src/dotnet/library/QuestPDF.DocumentationExamples/RotateExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize } from '../index';
import { imageSettings, output, resource } from './doc-example';

test('RotateExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .row((row) => {
                        row.autoItem()
                            .rotateLeft()
                            .alignCenter()
                            .text('Definition')
                            .bold().fontColor(Colors.Blue.Darken2);

                        row.autoItem()
                            .paddingHorizontal(15)
                            .lineVertical(2).lineColor(Colors.Blue.Medium);

                        row.relativeItem()
                            .background(Colors.Blue.Lighten5)
                            .padding(15)
                            .text((text) => {
                                text.span('A variable').bold();
                                text.span(' is a named storage location in memory that holds a value which can be modified during program execution.');
                            });
                    });
            });
        })
        .generateImages(() => output('rotate.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('RotateExamples.FreeExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));

                page.content()
                    .background(Colors.Grey.Lighten2)
                    .padding(25)
                    .row((row) => {
                        row.spacing(25);

                        addIcon(0);
                        addIcon(30);
                        addIcon(45);
                        addIcon(80);

                        function addIcon(angle: number) {
                            const itemSize = 100;

                            row.autoItem()
                                .width(itemSize)
                                .aspectRatio(1)

                                .offsetX(itemSize / 2)
                                .offsetY(itemSize / 2)

                                .rotate(angle)

                                .offsetX(-itemSize / 2)
                                .offsetY(-itemSize / 2)

                                .svg(resource('compass.svg'));
                        }
                    });
            });
        })
        .generateImages(() => output('rotate-free.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
