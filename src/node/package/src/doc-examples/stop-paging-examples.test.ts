// Port of src/dotnet/library/QuestPDF.DocumentationExamples/StopPagingExamples.cs.
import { test } from 'node:test';
import { Document, PageSize } from '../index';
import { output } from './doc-example';

test('StopPagingExamples.Example', () => {
    const bookDescription = '"Master Modern C# Development" is a comprehensive guide that takes you from the basics to advanced concepts in C# programming. Perfect for beginners and intermediate developers looking to enhance their skills with practical examples and real-world applications. Covering object-oriented programming, LINQ, asynchronous programming, and the latest .NET features, this book provides step-by-step explanations to help you write clean, efficient, and scalable code. Whether you\'re building desktop, web, or cloud applications, this resource equips you with the knowledge and best practices to become a confident C# developer.';

    Document
        .create((document) => {
            document.page((page) => {
                page.minSize(new PageSize(0, 0));
                page.maxSize(new PageSize(1000, 1000));
                page.defaultTextStyle((style) => style.fontSize(20));
                page.margin(25);

                page.content()
                    .width(400)
                    .height(300)
                    .stopPaging()
                    .decoration((decoration) => {
                        decoration.before().text('Book description:').bold();
                        decoration.content().text(bookDescription);
                    });
            });
        })
        .generatePdf(output('stop-paging-enabled.pdf'));
});
