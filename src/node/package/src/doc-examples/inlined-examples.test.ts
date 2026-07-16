// Port of src/dotnet/library/QuestPDF.DocumentationExamples/InlinedExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, Placeholders } from '../index';
import { imageSettings, output } from './doc-example';

test('InlinedExamples.SimpleExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(450);

                page.content()
                    .background(Colors.Grey.Lighten3)
                    .padding(25)
                    .border(1)
                    .background(Colors.White)
                    .inlined((inlined) => {
                        inlined.spacing(25);
                        inlined.baselineMiddle();
                        inlined.alignCenter();

                        for (let i = 0; i < 15; i++)
                            inlined.item().element(randomBlock);
                    });
            });
        })
        .generateImages(() => output('inlined.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('InlinedExamples.SpacingExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(450);

                page.content()
                    .background(Colors.Grey.Lighten3)
                    .padding(25)
                    .border(1)
                    .background(Colors.White)
                    .inlined((inlined) => {
                        inlined.verticalSpacing(15);
                        inlined.horizontalSpacing(30);

                        for (let i = 0; i < 10; i++)
                            inlined.item().element(randomBlock);
                    });
            });
        })
        .generateImages(() => output('inlined-spacing.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

/** The TypeScript counterpart of C# Random.Shared.Next(minValue, maxValue): a random integer within [minValue, maxValue). */
function randomNext(minValue: number, maxValue: number): number {
    return Math.floor(Math.random() * (maxValue - minValue)) + minValue;
}

function randomBlock(container: IContainer): void {
    container
        .width(randomNext(1, 4) * 25)
        .height(randomNext(1, 4) * 25)
        .border(1)
        .borderColor(Colors.Grey.Darken2)
        .background(Placeholders.backgroundColor());
}
