// Port of src/dotnet/library/QuestPDF.DocumentationExamples/Text/TextBasicExamples.cs.
import { test } from 'node:test';
import { Colors, Document, IContainer, ImageCompressionQuality, ImageFormat, PageSize, PageSizes, Placeholders, TextStyle } from '../../index';
import { imageSettings, output } from '../doc-example';

test('TextBasicExamples.Basic', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text('Sample text');
            });
        })
        .generateImages(() => output('text-basic.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextBasicExamples.BasicWithStyle', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        cellStyle(column.item())
                            .text('Text with blue color')
                            .fontColor(Colors.Blue.Darken1);

                        cellStyle(column.item())
                            .text('Bold and underlined text')
                            .bold()
                            .underline();

                        cellStyle(column.item())
                            .text('Centered small text')
                            .fontSize(12)
                            .alignCenter();

                        // The C# original routes CellStyle through the Element(Func<IContainer, IContainer>)
                        // overload, which is not bridged; a free function is the direct equivalent.
                        function cellStyle(container: IContainer): IContainer {
                            return container.background(Colors.Grey.Lighten3).padding(10);
                        }
                    });
            });
        })
        .generateImages(() => output('text-basic-descriptor.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextBasicExamples.Rich', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.alignCenter();

                        text.span('The ');
                        text.span('chemical formula').underline();
                        text.span(' of ');
                        text.span('sulfuric acid').backgroundColor(Colors.Amber.Lighten3);
                        text.span(' is H');
                        text.span('2').subscript();
                        text.span('SO');
                        text.span('4').subscript();
                        text.span('.');
                    });
            });
        })
        .generateImages(() => output('text-rich.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextBasicExamples.StyleInheritance', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .defaultTextStyle((style) => style.fontSize(20))
                    .column((column) => {
                        column.spacing(10);

                        column.item().text('Products').extraBold().underline().decorationThickness(2);

                        column.item().text('Comments: ' + Placeholders.sentence());

                        column.item()
                            .defaultTextStyle((style) => style.fontSize(14))
                            .table((table) => {
                                table.columnsDefinition((columns) => {
                                    columns.constantColumn(30);
                                    columns.relativeColumn(1);
                                    columns.relativeColumn(2);
                                });

                                table.header((header) => {
                                    style(header.cell()).text('ID');
                                    style(header.cell()).text('Name');
                                    style(header.cell()).text('Description');

                                    // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                                    // overload, which is not bridged; a free function is the direct equivalent.
                                    function style(container: IContainer): IContainer {
                                        return container
                                            .background(Colors.Grey.Lighten3)
                                            .borderBottom(1)
                                            .paddingHorizontal(5)
                                            .paddingVertical(10)
                                            .defaultTextStyle((style) => style.bold().fontColor(Colors.Blue.Medium));
                                    }
                                });

                                for (let i = 0; i < 5; i++) {
                                    style(table.cell()).text(i.toString()).bold();
                                    style(table.cell()).text(Placeholders.label());
                                    style(table.cell()).text(Placeholders.sentence());
                                }

                                // The C# original routes Style through the Element(Func<IContainer, IContainer>)
                                // overload, which is not bridged; a free function is the direct equivalent.
                                function style(container: IContainer): IContainer {
                                    return container.padding(5);
                                }
                            });
                    });
            });
        })
        .generateImages(() => output('text-style-inheritance.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextBasicExamples.PageNumber', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .extend()
                    .placeholder();

                page.footer()
                    .paddingTop(25)
                    .alignCenter()
                    .text('3 / 10');
                    // .text((text) => {
                    //     text.currentPageNumber();
                    //     text.span(' / ');
                    //     text.totalPages();
                    // });
            });
        })
        .generateImages(() => output('text-page-number.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextBasicExamples.PageNumberFormat', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A5);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.currentPageNumber().format(formatWithLeadingZeros);
                    });

                function formatWithLeadingZeros(pageNumber: number | null): string {
                    const expectedLength = 3;
                    pageNumber ??= 1;
                    return pageNumber.toString().padStart(expectedLength, '0');
                }
            });
        })
        .generateImages(() => output('text-page-number-format.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextBasicExamples.Hyperlink', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.size(PageSizes.A6.landscape());
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        const hyperlinkStyle = TextStyle.Default
                            .fontColor(Colors.Blue.Medium)
                            .underline();

                        text.span('To learn more about QuestPDF, please visit its ');
                        text.hyperlink('homepage', 'https://www.questpdf.com/').style(hyperlinkStyle);
                        text.span(', ');
                        text.hyperlink('GitHub repository', 'https://github.com/QuestPDF/QuestPDF').style(hyperlinkStyle);
                        text.span(' and ');
                        text.hyperlink('NuGet package page', 'https://www.nuget.org/packages/QuestPDF').style(hyperlinkStyle);
                        text.span('.');
                    });
            });
        })
        .generatePdf(output('text-hyperlink.pdf'));
});
