// Port of src/dotnet/library/QuestPDF.DocumentationExamples/Text/ParagraphStyleExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../../index';
import { imageSettings, output } from '../doc-example';

test('ParagraphStyleExamples.DefaultTextStyle', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(400, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.defaultTextStyle((style) => style.light().letterSpacing(-0.1).wordSpacing(0.1));

                        text.span('Changing typography settings helps creating ');
                        text.span('significant').letterSpacing(0.2).black().backgroundColor(Colors.Grey.Lighten2);
                        text.span(' visual contrast.');
                    });
            });
        })
        .generateImages(() => output('text-paragraph-default-style.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ParagraphStyleExamples.TextAlignment', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(400, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        cellStyle(column.item())
                            .text('This is an example of left-aligned text, showcasing how the text starts from the left margin and continues naturally across the container.')
                            .alignLeft();

                        cellStyle(column.item())
                            .text('This text is centered within its container, creating a balanced look, especially for titles or headers.')
                            .alignCenter();

                        cellStyle(column.item())
                            .text('This example demonstrates right-aligned text, often used for dates, numbers, or aligning text to the right margin.')
                            .alignRight();

                        cellStyle(column.item())
                            .text('Justified text adjusts the spacing between words so that both the left and right edges of the text block are aligned, creating a clean, newspaper-like look.')
                            .justify();

                        function cellStyle(container: IContainer): IContainer {
                            return container.background(Colors.Grey.Lighten3).padding(10);
                        }
                    });
            });
        })
        .generateImages(() => output('text-paragraph-alignment.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('ParagraphStyleExamples.FirstLineIndentation', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1200));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text(Placeholders.paragraphs())
                    .paragraphFirstLineIndentation(40);
            });
        })
        .generateImages(() => output('text-paragraph-first-line-indentation.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});

test('ParagraphStyleExamples.Spacing', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1200));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text(Placeholders.paragraphs())
                    .paragraphSpacing(10);
            });
        })
        .generateImages(() => output('text-paragraph-spacing.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.High, rasterDpi: 144 }));
});

test('ParagraphStyleExamples.ClampLines', () => {
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

                        const paragraph = Placeholders.paragraph();

                        column.item()
                            .background(Colors.Grey.Lighten3)
                            .padding(5)
                            .text(paragraph);

                        column.item()
                            .background(Colors.Grey.Lighten3)
                            .padding(5)
                            .text(paragraph)
                            .clampLines(3);
                    });
            });
        })
        .generateImages(() => output('text-paragraph-clamp-lines.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('ParagraphStyleExamples.ClampLinesWithCustomEllipsis', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text(Placeholders.paragraph())
                    .clampLines(3, ' [...]');
            });
        })
        .generateImages(() => output('text-paragraph-clamp-lines-custom-ellipsis.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
