// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternConfigurableComponentExample.cs.
// The C# SectionComponent.Custom() returns a detached container created with EmptyContainer.Create()
// that Compose() later attaches via Element(IContainer). That Element overload is not bridged in
// TypeScript (runtime dispatch cannot distinguish it from Element(IDynamicElement)), so custom()
// accepts a content callback and compose() attaches it through the bridged Element(Action<IContainer>).
// The extra pass-through wrapper does not draw, so the rendered output stays identical.
import { test } from 'node:test';
import { Colors, Document, IComponent, IContainer, ImageCompressionQuality, ImageFormat, PageSize, Placeholders } from '../../index';
import { imageSettings, output, resource } from '../doc-example';

test('CodePatternConfigurableComponentExample.Example', () => {
    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1200));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .component(buildSampleSection());
            });
        })
        .generateImages(() => output('code-pattern-component-configurable.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));

    function buildSampleSection(): IComponent {
        const section = new SectionComponent();

        section.text('Product name', Placeholders.label());
        section.text('Description', Placeholders.sentence());
        section.text('Price', Placeholders.price());
        section.text('Date of production', Placeholders.shortDate());
        section.image('Photo of the product', resource('product.jpg'));
        section.custom('Status', (container) => { container.text('Accepted').fontColor(Colors.Green.Darken2).bold(); });

        return section;
    }
});

class SectionComponent implements IComponent {
    private fields: Array<{ label: string; content: (container: IContainer) => void }> = [];

    compose(container: IContainer): void {
        container
            .border(1)
            .column((column) => {
                for (const field of this.fields) {
                    column.item().row((row) => {
                        row.relativeItem()
                            .border(1)
                            .borderColor(Colors.Grey.Medium)
                            .background(Colors.Grey.Lighten3)
                            .padding(10)
                            .text(field.label);

                        row.relativeItem(2)
                            .border(1)
                            .borderColor(Colors.Grey.Medium)
                            .padding(10)
                            .element(field.content);
                    });
                }
            });
    }

    text(label: string, text: string): void {
        this.custom(label, (container) => { container.text(text); });
    }

    image(label: string, imagePath: string): void {
        this.custom(label, (container) => { container.image(imagePath); });
    }

    custom(label: string, content: (container: IContainer) => void): void {
        this.fields.push({ label, content });
    }
}
