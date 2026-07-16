package questpdf.docexamples;

import com.questpdf.fluent.Document;
import com.questpdf.helpers.PageSize;
import org.junit.jupiter.api.Test;

public class StopPagingExamples extends DocExample {

    @Test
    public void example() {
        var bookDescription = "\"Master Modern C# Development\" is a comprehensive guide that takes you from the basics to advanced concepts in C# programming. Perfect for beginners and intermediate developers looking to enhance their skills with practical examples and real-world applications. Covering object-oriented programming, LINQ, asynchronous programming, and the latest .NET features, this book provides step-by-step explanations to help you write clean, efficient, and scalable code. Whether you're building desktop, web, or cloud applications, this resource equips you with the knowledge and best practices to become a confident C# developer.";

        Document
            .create(document -> {
                document.page(page -> {
                    page.minSize(new PageSize(0f, 0f));
                    page.maxSize(new PageSize(1000f, 1000f));
                    page.defaultTextStyle(style -> style.fontSize(20f));
                    page.margin(25f);

                    page.content()
                        .width(400f)
                        .height(300f)
                        .stopPaging()
                        .decoration(decoration -> {
                            decoration.before().text("Book description:").bold();
                            decoration.content().text(bookDescription);
                        });
                });
            })
            .generatePdf(output("stop-paging-enabled.pdf"));
    }
}
