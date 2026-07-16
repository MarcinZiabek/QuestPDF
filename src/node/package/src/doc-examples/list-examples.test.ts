// Port of src/dotnet/library/QuestPDF.DocumentationExamples/ListExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../index';
import { imageSettings, output, resource } from './doc-example';

test('ListExamples.BulletpointExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(350, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        for (let i = 1; i <= 7; i++) {
                            column.item().row((row) => {
                                row.constantItem(26).image(resource('bulletpoint.png'));
                                row.constantItem(5);
                                row.relativeItem().text(Placeholders.label());
                            });
                        }
                    });
            });
        })
        .generateImages(() => output('list-unordered.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ListExamples.OrderedExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        for (let i = 1; i <= 11; i++) {
                            column.item().row((row) => {
                                row.constantItem(35).text(`${i}.`);
                                row.relativeItem().text(Placeholders.sentence());
                            });
                        }
                    });
            });
        })
        .generateImages(() => output('list-ordered.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ListExamples.Nested', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        const nestingSize = 25;

                        column.spacing(10);

                        column.item()
                            .text('Algorithm: Checking if a Number is Prime')
                            .fontSize(24).fontColor(Colors.Blue.Darken2);

                        addListItem(0, '1.', 'Handle special cases');
                        addListItem(1, 'a)', 'If n is less than 2, return false (not prime).');
                        addListItem(1, 'b)', 'If n is 2, return true (prime).');

                        addListItem(0, '2.', 'Check divisibility');
                        addListItem(1, '-', 'Iterate through numbers from 2 to n - 1:');
                        addListItem(2, '-', 'If n is divisible by any of these numbers, return false.');

                        addListItem(0, '3.', 'Return true (if no divisors were found, n is prime).');

                        function addListItem(nestingLevel: number, bulletText: string, text: string): void {
                            column.item().row((row) => {
                                row.constantItem(nestingSize * nestingLevel);
                                row.constantItem(nestingSize).text(bulletText);
                                row.relativeItem().text(text);
                            });
                        }
                    });
            });
        })
        .generateImages(() => output('list-nested.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
