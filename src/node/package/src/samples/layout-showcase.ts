import {
    BoxShadowStyle,
    Colors,
    Document,
    IContainer,
    PageSizes,
    Placeholders, Unit,
} from '../index';

/**
 * Breadth tour of layout primitives: constraints, alignment, decoration,
 * layers, inlined flow, multi-column, rotation/scaling, styled boxes with
 * gradients/shadows/corner radii, conditional visibility, and images.
 */

const TICK_SVG = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M4 12l5 5L20 6"/></svg>';

function headerBanner(container: IContainer): void {
    container
        .background(Colors.BlueGrey.Darken3)
        .padding(12)
        .row((row) => {
            row.relativeItem().text('Layout Showcase').fontSize(15).semiBold().fontColor(Colors.White);
            row.relativeItem().alignRight().alignMiddle()
                .text(Placeholders.shortDate()).fontColor(Colors.BlueGrey.Lighten4);
        });
}

function labelled(container: IContainer, title: string, block: (content: IContainer) => void): void {
    container.column((column) => {
        column.item().text(title).semiBold().fontSize(10).fontColor(Colors.BlueGrey.Darken2);
        column.item().paddingTop(4).element(block);
    });
}

export function runLayoutShowcaseSample(): Uint8Array {
    const document = Document.create((documentContainer) => {
        documentContainer.page((page) => {
            page.size(PageSizes.Letter);
            page.margin(32);
            page.defaultTextStyle((style) => style.fontSize(9));

            headerBanner(page.header());

            page.content().paddingVertical(12).column((column) => {
                column.spacing(16);

                labelled(column.item(), 'Constraints', (section) => {
                    section.row((row) => {
                        row.spacing(8);
                        row.constantItem(120).height(40).background(Colors.Cyan.Lighten4)
                            .alignCenter().alignMiddle().text('120pt fixed');
                        row.relativeItem().minHeight(40).maxHeight(60).background(Colors.Cyan.Lighten3)
                            .alignCenter().alignMiddle().text('min 40 / max 60');
                        row.autoItem().height(40).padding(4).background(Colors.Cyan.Lighten2)
                            .alignMiddle().text('auto-sized');
                    });
                });

                labelled(column.item(), 'Styled boxes', (section) => {
                    section.row((row) => {
                        row.spacing(10);
                        row.relativeItem()
                            .height(52)
                            .backgroundLinearGradient(45, [Colors.Purple.Lighten3, Colors.Pink.Lighten3])
                            .cornerRadius(8)
                            .alignCenter().alignMiddle()
                            .text('gradient + radius');

                        const shadow = new BoxShadowStyle();
                        shadow.blur = 6;
                        shadow.offsetX = 2;
                        shadow.offsetY = 2;
                        shadow.color = Colors.Grey.Medium;

                        row.relativeItem()
                            .height(52)
                            .background(Colors.White)
                            .border(1)
                            .borderColor(Colors.Grey.Lighten1)
                            .shadow(shadow)
                            .alignCenter().alignMiddle()
                            .text('box shadow');
                        row.relativeItem()
                            .height(52)
                            .borderLinearGradient(90, [Colors.Orange.Medium, Colors.Red.Medium])
                            .border(2)
                            .cornerRadiusTopLeft(12)
                            .cornerRadiusBottomRight(12)
                            .alignCenter().alignMiddle()
                            .text("gradient border");
                    });
                });

                labelled(column.item(), 'Layers', (section) => {
                    section.layers((layers) => {
                        layers.layer().aspectRatio(4).background(Colors.BlueGrey.Lighten5);
                        layers.primaryLayer().padding(14).column((content) => {
                            content.item().text('Primary layer content sits above the tinted background layer.');
                            content.item().text('Watermarks and stamps are additional layers.').fontColor(Colors.Grey.Darken1);
                        });
                        layers.layer().alignRight().alignTop().padding(6)
                            .rotate(8)
                            .text('DRAFT').fontSize(18).extraBold().fontColor(Colors.Red.Lighten2);
                    });
                });

                labelled(column.item(), 'Inlined flow', (section) => {
                    section.inlined((inlined) => {
                        inlined.spacing(6);
                        inlined.alignLeft();
                        inlined.baselineMiddle();
                        for (let index = 1; index <= 14; index++) {
                            const shade = index % 3 === 0 ? Colors.Green.Lighten2 : Colors.Green.Lighten4;
                            inlined.item()
                                .width(30 + (index % 5) * 14)
                                .height(18)
                                .background(shade)
                                .alignCenter().alignMiddle()
                                .text(`${index}`);
                        }
                    });
                });

                labelled(column.item(), 'Multi-column', (section) => {
                    section.multiColumn((multiColumn) => {
                        multiColumn.columns(3);
                        multiColumn.spacing(12);
                        multiColumn.content().column((content) => {
                            content.spacing(6);
                            for (let i = 0; i < 3; i++)
                                content.item().text(Placeholders.sentence());
                        });
                    });
                });

                labelled(column.item(), 'Transforms', (section) => {
                    section.row((row) => {
                        row.spacing(20);
                        row.constantItem(90).aspectRatio(1).scale(0.8).background(Colors.Amber.Lighten3)
                            .alignCenter().alignMiddle().text('scaled 80%');
                        row.constantItem(90).aspectRatio(1).rotateLeft().background(Colors.Lime.Lighten3)
                            .alignCenter().alignMiddle().text('rotated left');
                        row.constantItem(90).aspectRatio(1).flipHorizontal().background(Colors.Teal.Lighten4)
                            .alignCenter().alignMiddle().text('flipped');
                        row.relativeItem().unconstrained()
                            .padding(10).background(Colors.DeepOrange.Lighten4).padding(4);
                    });
                });

                labelled(column.item(), 'Conditional + media', (section) => {
                    section.row((row) => {
                        row.spacing(10);
                        row.relativeItem().showIf((context) => context.pageNumber === 1).background(Colors.Blue.Lighten5)
                            .padding(6).text('Visible on page 1 only (predicate).');
                        row.relativeItem().showIf(true).padding(6).text('Always shown (boolean).');
                        row.constantItem(120).image(Placeholders.image(240, 120)).fitWidth();
                        row.constantItem(60).svg(TICK_SVG).fitArea();
                    });
                });
            });

            page.footer().alignRight().text((text) => {
                text.span('Layout showcase — ');
                text.currentPageNumber();
                text.span('/');
                text.totalPages();
            });
        });
    });

    return document.generatePdf();
}
