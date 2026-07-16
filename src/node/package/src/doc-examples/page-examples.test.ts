// Port of src/dotnet/library/QuestPDF.DocumentationExamples/PageExamples.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSizes, Placeholders, Unit } from '../index';
import { imageSettings, output, resource } from './doc-example';

test('PageExamples.Simple', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.margin(2, Unit.Centimetre);
                page.defaultTextStyle((style) => style.fontSize(24));

                page.header()
                    .text('Hello, World!')
                    .fontSize(48).bold();

                page.content()
                    .paddingVertical(25)
                    .text(Placeholders.loremIpsum())
                    .justify();

                page.footer()
                    .alignCenter()
                    .text((text) => {
                        text.currentPageNumber();
                        text.span(' / ');
                        text.totalPages();
                    });
            });
        })
        .generateImages(() => output('page-simple.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('PageExamples.MainSlots', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A4);
                page.margin(2, Unit.Centimetre);
                page.defaultTextStyle((style) => style.fontSize(24));

                page.header()
                    .background(Colors.Grey.Lighten1)
                    .height(125)
                    .alignCenter()
                    .alignMiddle()
                    .text('Header');

                page.content()
                    .background(Colors.Grey.Lighten2)
                    .alignCenter()
                    .alignMiddle()
                    .text('Content');

                page.footer()
                    .background(Colors.Grey.Lighten1)
                    .height(75)
                    .alignCenter()
                    .alignMiddle()
                    .text('Footer');
            });
        })
        .generateImages(() => output('page-main-slots.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('PageExamples.Foreground', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A4);
                page.margin(2, Unit.Centimetre);
                page.defaultTextStyle((style) => style.fontSize(20));

                page.header()
                    .paddingBottom(1, Unit.Centimetre)
                    .text('Report')
                    .fontSize(30)
                    .bold();

                page.content()
                    .text(Placeholders.paragraphs())
                    .paragraphSpacing(1, Unit.Centimetre)
                    .justify();

                page.foreground().svg(resource('draft-foreground.svg')).fitArea();
            });
        })
        .generateImages(() => output('page-foreground.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});

test('PageExamples.Background', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A4.landscape());

                page.background().svg(resource('certificate-background.svg')).fitArea();

                page.content()
                    .paddingLeft(10, Unit.Centimetre)
                    .paddingRight(5, Unit.Centimetre)
                    .alignMiddle()
                    .column((column) => {
                        column.item().height(50).svg(resource('questpdf-logo.svg'));

                        column.item().height(50);

                        column.item().text('CERTIFICATE').fontSize(64).extraBlack();

                        column.item().height(25);

                        column.item()
                            .shrink().borderBottom(1).padding(10)
                            .text('Marcin Ziąbek').fontSize(32).italic();

                        column.item().height(10);

                        column.item()
                            .text(`has successfully completed the course "QuestPDF Basics" on ${new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}.`)
                            .fontSize(20).light();
                    });
            });
        })
        .generateImages(() => output('page-background.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
