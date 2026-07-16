// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternLocalHelpersExample.cs.
import { test } from 'node:test';
import { Colors, Document, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../../index';
import { imageSettings, output, resource } from '../doc-example';

test('CodePatternLocalHelpersExample.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(15);

                        column.item().text('Business details:').fontSize(24).bold().fontColor(Colors.Blue.Darken2);

                        addContactItem(resource('Icons/phone.svg'), Placeholders.phoneNumber());
                        addContactItem(resource('Icons/email.svg'), Placeholders.email());
                        addContactItem(resource('Icons/web.svg'), Placeholders.webpageUrl());

                        function addContactItem(iconPath: string, label: string) {
                            column.item().row((row) => {
                                row.constantItem(32).aspectRatio(1).svg(iconPath);
                                row.constantItem(15);
                                row.autoItem().alignMiddle().text(label);
                            });
                        }
                    });
            });
        })
        .generateImages(() => output('code-pattern-local-helpers.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});
