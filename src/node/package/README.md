# QuestPDF for Node.js

QuestPDF is a modern PDF generation library. Design documents with a fluent, code-first API — a maintainable alternative to HTML-to-PDF conversion — backed by the native QuestPDF engine. Create invoices, reports, and data exports; merge and edit existing documents; produce PDF/A and accessible PDF/UA files.

Learn more at [www.questpdf.com](https://www.questpdf.com).

## Installation

```bash
npm install questpdf
```

The native runtime for your platform is installed automatically as an optional dependency (`@questpdf/native-*`). Supported platforms:

| OS               | Architectures    |
| ---------------- | ---------------- |
| macOS            | arm64, x64       |
| Windows          | x64, arm64       |
| Linux (glibc)    | x64, arm64       |
| Linux (musl)     | x64              |

## Quick start

```ts
import { Document, LicenseType, PageSizes, Settings } from 'questpdf';

Settings.license = LicenseType.Community;

Document
    .create((document) => {
        document.page((page) => {
            page.margin(50);
            page.size(PageSizes.A4);
            page.content().column((column) => {
                column.spacing(10);
                column.item().text('Hello from QuestPDF').fontSize(24);
                column.item().text('A code-first way to create PDF documents.');
            });
        });
    })
    .generatePdf('hello.pdf');
```

## Fonts

The Lato font family ships with the package. Fonts installed in the operating system are available by default (`Settings.useEnvironmentFonts`). To deploy additional fonts with your application, place the font files in a `fonts/` directory next to your entry script — every font found there is registered automatically.

## License

QuestPDF is dual-licensed under the QuestPDF Community license and the QuestPDF Professional / Enterprise licenses. Most users qualify for the free Community license; see the [license selection guide](https://www.questpdf.com/pricing) and [LICENSE.md](https://github.com/QuestPDF/QuestPDF/blob/main/LICENSE.md) for details.
