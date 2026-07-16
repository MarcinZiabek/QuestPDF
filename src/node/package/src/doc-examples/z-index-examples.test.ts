// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ZIndexExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../index';
import { imageSettings, output } from './doc-example';

test('ZIndexExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(650, 0));
                page.maxSize(new PageSize(650, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .paddingVertical(15)
                    .border(2)
                    .row((row) => {
                        row.relativeItem()
                            .background(Colors.Grey.Lighten3)
                            .element((c) => addPricingItem(c, 'Community', 'Free'));

                        row.relativeItem()
                            .zIndex(1) // -1 or 0 or 1
                            .padding(-15)
                            .border(1)
                            .background(Colors.Grey.Lighten1)
                            .paddingTop(15)
                            .element((c) => addPricingItem(c, 'Professional', '$699'));

                        row.relativeItem()
                            .background(Colors.Grey.Lighten3)
                            .element((c) => addPricingItem(c, 'Enterprise', '$1999'));

                        function addPricingItem(container: IContainer, name: string, formattedPrice: string): void {
                            container
                                .padding(25)
                                .column((column) => {
                                    column.item().alignCenter().text(name).fontSize(24).black();
                                    column.item().alignCenter().text(formattedPrice).fontSize(20).semiBold();

                                    column.item().paddingHorizontal(-25).paddingVertical(10).lineHorizontal(1);

                                    for (let i = 1; i <= 4; i++) {
                                        column.item()
                                            .paddingTop(10)
                                            .alignCenter()
                                            .text(Placeholders.label())
                                            .fontSize(16)
                                            .light();
                                    }
                                });
                        }
                    });
            });
        })
        .generateImages(() => output('zindex-positive.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
