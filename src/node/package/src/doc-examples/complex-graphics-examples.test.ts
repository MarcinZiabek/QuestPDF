// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ComplexGraphicsExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../index';
import { imageSettings, output } from './doc-example';

test('ComplexGraphicsExamples.RoundedRectangleWithGradient', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .layers((layers) => {
                        layers.layer().svg((size) => {
                            return `<svg width="${size.width}" height="${size.height}" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="backgroundGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop stop-color="#00E5FF" offset="0%"/>
        <stop stop-color="#2979FF" offset="100%"/>
      </linearGradient>
    </defs>

    <rect x="0" y="0" width="${size.width}" height="${size.height}" rx="${size.height / 2}" ry="${size.height / 2}" fill="url(#backgroundGradient)" />
</svg>`;
                        });

                        layers.primaryLayer()
                            .paddingVertical(10)
                            .paddingHorizontal(20)
                            .text('QuestPDF')
                            .fontColor(Colors.White)
                            .fontSize(32)
                            .extraBlack();
                    });
            });
        })
        .generateImages(() => output('complex-graphics-rounded-rectangle-with-gradient.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ComplexGraphicsExamples.DottedLine', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(500, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(5);

                        for (let i = 1; i <= 5; i++) {
                            const pageNumber = i * 7 + 4;

                            column.item().row((row) => {
                                row.autoItem().text(`${i}.`);
                                row.constantItem(10);
                                row.autoItem().text(Placeholders.label());

                                row.relativeItem().paddingHorizontal(3).offsetY(20).height(2).svg((size) => {
                                    return `<svg width="${size.width}" height="${size.height}" xmlns="http://www.w3.org/2000/svg">
    <line x1="0" y1="0" x2="${size.width}" y2="0" fill="none" stroke="black" stroke-width="2" stroke-dasharray="2 6" />
</svg>`;
                                });

                                row.autoItem().text(`${pageNumber}`);
                            });
                        }
                    });
            });
        })
        .generateImages(() => output('complex-graphics-dotted-line.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
