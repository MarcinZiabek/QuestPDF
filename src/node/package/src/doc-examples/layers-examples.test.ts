// Port of src/dotnet/library/QuestPDF.DocumentationExamples/LayersExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat } from '../index';
import { imageSettings, output, resource } from './doc-example';

test('LayersExamples.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(450);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.item().paddingBottom(15).text('Proposed Business Card Design:').bold();

                        column.item()
                            .aspectRatio(4 / 3)
                            .layers((layers) => {
                                layers.layer().image(resource('card-background.jpg')).fitUnproportionally();

                                layers.primaryLayer()
                                    .offsetY(75)
                                    .column((innerColumn) => {
                                        innerColumn.item()
                                            .alignCenter()
                                            .text('Horizon Ventures')
                                            .bold().fontSize(32).fontColor(Colors.Blue.Darken2);

                                        innerColumn.item().alignCenter().text('Your journey begins here');
                                    });
                            });
                    });
            });
        })
        .generateImages(() => output('layers.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
