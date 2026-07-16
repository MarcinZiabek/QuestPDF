// Port of src/dotnet/library/QuestPDF.DocumentationExamples/MultiColumnExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize, PageSizes, Placeholders } from '../index';
import { imageSettings, output } from './doc-example';

test('MultiColumnExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(650, 0));
                page.maxSize(new PageSize(650, 650));
                page.defaultTextStyle((style) => style.fontSize(12));
                page.margin(25);

                page.content()
                    .multiColumn((multiColumn) => {
                        multiColumn.columns(3);
                        multiColumn.spacing(25);

                        multiColumn
                            .content()
                            .column((column) => {
                                column.spacing(15);

                                for (let sectionId = 0; sectionId < 3; sectionId++) {
                                    for (let textId = 0; textId < 3; textId++)
                                        column.item().text(Placeholders.paragraph()).justify();

                                    column.item().aspectRatio(21 / 9).image((payload) => Placeholders.image(payload.imageSize));
                                }
                            });
                    });
            });
        })
        .generateImages(() => output('multicolumn-example.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});

test('MultiColumnExamples.SpacerExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(450, 0));
                page.maxSize(new PageSize(450, 550));
                page.defaultTextStyle((style) => style.fontSize(12));
                page.margin(25);

                page.content()
                    .multiColumn((multiColumn) => {
                        multiColumn.columns(2);
                        multiColumn.spacing(50);

                        multiColumn
                            .spacer()
                            .alignCenter()
                            .lineVertical(2)
                            .lineColor(Colors.Grey.Medium);

                        multiColumn
                            .content()
                            .column((column) => {
                                column.spacing(15);

                                for (let textId = 0; textId < 5; textId++)
                                    column.item().text(Placeholders.paragraph()).justify();
                            });
                    });
            });
        })
        .generateImages(() => output('multicolumn-spacer.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});

test('MultiColumnExamples.BalanceHeightWithExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A4);
                page.defaultTextStyle((style) => style.fontSize(14));
                page.margin(30);

                page.content()
                    .multiColumn((multiColumn) => {
                        multiColumn.spacing(30);
                        multiColumn.balanceHeight();

                        multiColumn
                            .content()
                            .column((column) => {
                                column.spacing(15);

                                for (let textId = 0; textId < 8; textId++)
                                    column.item().text(Placeholders.paragraph()).justify();
                            });
                    });
            });
        })
        .generateImages(() => output('multicolumn-balance-height-with.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});
