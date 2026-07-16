// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ShowOnceExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat } from '../index';
import { imageSettings, output } from './doc-example';

function formatShortDate(date: Date): string {
    const pad = (value: number) => String(value).padStart(2, '0');
    return `${pad(date.getMonth() + 1)}/${pad(date.getDate())}/${date.getFullYear()}`;
}

test('ShowOnceExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(350, 500);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .decoration((decoration) => {
                        decoration.before().column((column) => {
                            column.item()
                                .showOnce()
                                .row((row) => {
                                    row.constantItem(80).aspectRatio(4 / 3).placeholder();
                                    row.constantItem(10);
                                    row.relativeItem()
                                        .alignMiddle()
                                        .column((innerColumn) => {
                                            innerColumn.item().text('Invoice #1234').fontSize(24).bold();
                                            // C# DateTime.Now:d under invariant culture — MM/dd/yyyy with zero padding
                                            // (toLocaleDateString('en-US') drops the leading zeros).
                                            innerColumn.item().text(`Generated on ${formatShortDate(new Date())}`).fontSize(16).light();
                                        });
                                });

                            column.item()
                                .skipOnce()
                                .text('Invoice #1234').fontSize(24).bold();
                        });

                        // generate dummy content
                        decoration.content()
                            .paddingTop(15)
                            .extendHorizontal()
                            .column((column) => {
                                column.spacing(10);

                                for (let i = 1; i <= 15; i++) {
                                    column.item()
                                        .height(30)
                                        .background(Colors.Grey.Lighten3)
                                        .alignCenter()
                                        .alignMiddle()
                                        .text(`${i}`);
                                }
                            });
                    });
            });
        })
        .generateImages((index) => output(`show-once-${index}.webp`), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
