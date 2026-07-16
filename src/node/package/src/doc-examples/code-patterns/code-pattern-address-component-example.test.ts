// Port of src/dotnet/library/QuestPDF.DocumentationExamples/CodePatterns/CodePatternAddressComponentExample.cs.
import { test } from 'node:test';
import { Document, IComponent, IContainer, ImageCompressionQuality, ImageFormat, PageSize } from '../../index';
import { imageSettings, output } from '../doc-example';

test('CodePatternAddressComponentExample.Example', () => {
    const address: Address = {
        companyName: 'Apple',
        postalCode: '95014',
        country: 'United States',
        city: 'Cupertino',
        street: 'One Apple Park Way',
    };

    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(600, 1200));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .component(new AddressComponent(address));
            });
        })
        .generateImages(() => output('code-pattern-component-address.webp'), imageSettings({ imageFormat: ImageFormat.Webp, imageCompressionQuality: ImageCompressionQuality.Best, rasterDpi: 144 }));
});

interface Address {
    companyName: string;
    postalCode: string;
    country: string;
    city: string;
    street: string;
}

class AddressComponent implements IComponent {
    constructor(private readonly address: Address) {}

    compose(container: IContainer): void {
        container
            .column((column) => {
                column.spacing(10);

                addItem('Company name', this.address.companyName);
                addItem('Postal code', this.address.postalCode);
                addItem('Country', this.address.country);
                addItem('City', this.address.city);
                addItem('Street', this.address.street);

                function addItem(label: string, value: string) {
                    column.item().text((text) => {
                        text.span(`${label}: `).bold();
                        text.span(value);
                    });
                }
            });
    }
}
