// Port of src/dotnet/library/QuestPDF.DocumentationExamples/Text/TextInjectContent.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize, TextInjectedElementAlignment } from '../../index';
import { imageSettings, output, resource } from '../doc-example';

test('TextInjectContent.InjectImage', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(500, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('A unit test can either ');
                        text.element().paddingBottom(-4).height(24).image(resource('unit-test-completed-icon.png'));
                        text.span(' pass').fontColor(Colors.Green.Medium);
                        text.span(' or ');
                        text.element().paddingBottom(-4).height(24).image(resource('unit-test-failed-icon.png'));
                        text.span(' fail').fontColor(Colors.Red.Medium);
                        text.span('.');
                    });
            });
        })
        .generateImages(() => output('text-inject-image.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextInjectContent.InjectSvg', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(350, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('To synchronize your email inbox, please click the ');
                        text.element().paddingBottom(-4).height(24).svg(resource('mail-synchronize-icon.svg'));
                        text.span(' icon.');
                    });
            });
        })
        .generateImages(() => output('text-inject-svg.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});

test('TextInjectContent.InjectPosition', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(400, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('This ');

                        text.element(TextInjectedElementAlignment.AboveBaseline)
                            .width(12).height(12)
                            .background(Colors.Green.Medium);

                        text.span(' element is positioned above the baseline, while this ');

                        text.element(TextInjectedElementAlignment.BelowBaseline)
                            .width(12).height(12)
                            .background(Colors.Blue.Medium);

                        text.span(' element is positioned below the baseline.');
                    });
            });
        })
        .generateImages(() => output('text-inject-position.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.VeryHigh, rasterDpi: 144 }));
});
