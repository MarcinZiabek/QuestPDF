// Port of src/dotnet/library/QuestPDF.DocumentationExamples/HyperlinkExamples.cs.
import { test } from 'node:test';
import { Colors, Document } from '../index';
import { output, resource } from './doc-example';

test('HyperlinkExamples.ElementExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(400);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .column((column) => {
                        column.spacing(25);

                        column.item()
                            .text('Clicking the NuGet logo will redirect you to the NuGet website.');

                        column.item()
                            .width(150)
                            .hyperlink('https://www.nuget.org/')
                            .svg(resource('nuget-logo.svg'));
                    });
            });
        })
        .generatePdf(output('hyperlink-element.pdf'));
});

test('HyperlinkExamples.InsideTextExample', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.continuousSize(300);
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .text((text) => {
                        text.span('Click ');
                        text.hyperlink('here', 'https://www.nuget.org/').underline().fontColor(Colors.Blue.Darken2);
                        text.span(' to visit the official NuGet website.');
                    });
            });
        })
        .generatePdf(output('hyperlink-text.pdf'));
});
