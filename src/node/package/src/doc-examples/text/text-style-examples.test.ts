// Port of src/dotnet/library/QuestPDF.DocumentationExamples/Text/TextStyleExamples.cs.
import { test } from 'node:test';
import { Colors, Document, FontFeatures, ImageCompressionQuality, ImageFormat, PageSize, Placeholders, Settings } from '../../index';
import { imageSettings, output } from '../doc-example';

test('TextStyleExamples.FontSize', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        column.item()
                            .text('This is small text (16pt)')
                            .fontSize(16);

                        column.item()
                            .text('This is medium text (24pt)')
                            .fontSize(24);

                        column.item()
                            .text('This is large text (36pt)')
                            .fontSize(36);
                    });
            });
        })
        .generateImages(() => output('text-font-size.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextStyleExamples.FontFamily', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(10);

                        column.item().text('This is text with default font (Lato)');

                        column.item().text('This is text with Times New Roman font')
                            .fontFamily('Times New Roman');

                        column.item().text('This is text with Courier New font')
                            .fontFamily('Courier New');
                    });
            });
        })
        .generateImages(() => output('text-font-family.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextStyleExamples.FontColor', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('Each pixel consists of three sub-pixels: ');
                        text.span('red').fontColor(Colors.Red.Medium);
                        text.span(', ');
                        text.span('green').fontColor(Colors.Green.Medium);
                        text.span(' and ');
                        text.span('blue').fontColor(Colors.Blue.Medium);
                        text.span('.');
                    });
            });
        })
        .generateImages(() => output('text-font-color.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.BackgroundColor', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('The term ');
                        text.span('algorithm').backgroundColor(Colors.Yellow.Lighten3).bold();
                        text.span(' refers to a set of rules or steps used to solve a problem.');
                    });
            });
        })
        .generateImages(() => output('text-font-background.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.Italic', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('In this sentence, the word ');
                        text.span('important').italic();
                        text.span(' is emphasized using italics.');
                    });
            });
        })
        .generateImages(() => output('text-font-italic.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.FontWeight', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('This sentence demonstrates ');
                        text.span('bold').bold();
                        text.span(', ');
                        text.span('normal').normalWeight();
                        text.span(', ');
                        text.span('light').light();
                        text.span(' and ');
                        text.span('thin').thin();
                        text.span(' font weights.');
                    });
            });
        })
        .generateImages(() => output('text-font-weight.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.Subscript', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('H');
                        text.span('2').subscript();
                        text.span('O is the chemical formula for water.');
                    });
            });
        })
        .generateImages(() => output('text-subscript.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.Superscript', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('E = mc');
                        text.span('2').superscript();
                        text.span(' is the equation of mass-energy equivalence.');
                    });
            });
        })
        .generateImages(() => output('text-superscript.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.LineHeight', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        const lineHeights = [0.75, 1, 2];
                        const paragraph = Placeholders.paragraph();

                        for (const lineHeight of lineHeights) {
                            column
                                .item()
                                .background(Colors.Grey.Lighten3)
                                .padding(5)
                                .text(paragraph)
                                .fontSize(16)
                                .lineHeight(lineHeight);
                        }
                    });
            });
        })
        .generateImages(() => output('text-line-height.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.LetterSpacing', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        const letterSpacing = [-0.08, 0, 0.2];
                        const paragraph = Placeholders.sentence();

                        for (const spacing of letterSpacing) {
                            column
                                .item()
                                .background(Colors.Grey.Lighten3)
                                .padding(5)
                                .text(paragraph)
                                .fontSize(18)
                                .letterSpacing(spacing);
                        }
                    });
            });
        })
        .generateImages(() => output('text-letter-spacing.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.WordSpacing', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(20);

                        const wordSpacing = [-0.2, 0, 0.4];
                        const paragraph = Placeholders.sentence();

                        for (const spacing of wordSpacing) {
                            column.item()
                                .background(Colors.Grey.Lighten3)
                                .padding(5)
                                .text(paragraph)
                                .fontSize(16)
                                .wordSpacing(spacing);
                        }
                    });
            });
        })
        .generateImages(() => output('text-word-spacing.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.FontFallback', () => {
    Settings.useEnvironmentFonts = false;

    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text('The Arabic word for programming is البرمجة.')
                    .fontFamily('Lato', 'Noto Sans Arabic');
            });
        })
        .generateImages(() => output('text-font-fallback.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.FontFallbackEmoji', () => {
    Settings.useEnvironmentFonts = false;

    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text('Popular emojis include 😊, 😂, ❤️, 👍, and 😎.')
                    .fontFamily('Lato', 'Noto Emoji');
            });
        })
        .generateImages(() => output('text-font-fallback-emoji.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.TextFontFeatures', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .row((row) => {
                        row.spacing(25);

                        row.relativeItem()
                            .background(Colors.Grey.Lighten3)
                            .padding(10)
                            .column((column) => {
                                column.item().text('Without ligatures').fontSize(16);

                                column.item()
                                    .text('fly and fight')
                                    .fontSize(32)
                                    .disableFontFeature(FontFeatures.StandardLigatures);
                            });

                        row.relativeItem()
                            .background(Colors.Grey.Lighten3)
                            .padding(10)
                            .column((column) => {
                                column.item().text('With ligatures').fontSize(16);

                                column.item().text('fly and fight')
                                    .fontSize(32)
                                    .enableFontFeature(FontFeatures.StandardLigatures);
                            });
                    });
            });
        })
        .generateImages(() => output('text-font-features.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.DecorationTypes', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('There are a couple of available text decorations: ');
                        text.span('underline').underline().fontColor(Colors.Red.Medium);
                        text.span(', ');
                        text.span('strikethrough').strikethrough().fontColor(Colors.Green.Medium);
                        text.span(' and ');
                        text.span('overline').overline().fontColor(Colors.Blue.Medium);
                        text.span('. ');
                    });
            });
        })
        .generateImages(() => output('text-decoration-types.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.DecorationStyles', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('Moreover, the decoration can be ');

                        text.span('solid').fontColor(Colors.Indigo.Medium).underline().decorationSolid();
                        text.span(', ');
                        text.span('double').fontColor(Colors.Blue.Medium).underline().decorationDouble();
                        text.span(', ');
                        text.span('wavy').fontColor(Colors.LightBlue.Medium).underline().decorationWavy();
                        text.span(', ');
                        text.span('dotted').fontColor(Colors.Cyan.Medium).underline().decorationDotted();
                        text.span(' or ');
                        text.span('dashed').fontColor(Colors.Green.Medium)
                            .underline().decorationDashed();
                        text.span('.');
                    });
            });
        })
        .generateImages(() => output('text-decoration-styles.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

test('TextStyleExamples.DecorationsAdvanced', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('This text contains a ');

                        text.span('seriuos')
                            .underline()
                            .decorationWavy()
                            .decorationColor(Colors.Red.Medium)
                            .decorationThickness(2);

                        text.span(' typo.');
                    });
            });
        })
        .generateImages(() => output('text-decoration-advanced.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
