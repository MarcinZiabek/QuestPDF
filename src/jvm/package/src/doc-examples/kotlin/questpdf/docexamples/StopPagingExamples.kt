package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.PageSize
import org.junit.jupiter.api.Test

class StopPagingExamples : DocExample() {

    @Test
    fun example() {
        val bookDescription = "\"Master Modern C# Development\" is a comprehensive guide that takes you from the basics to advanced concepts in C# programming. Perfect for beginners and intermediate developers looking to enhance their skills with practical examples and real-world applications. Covering object-oriented programming, LINQ, asynchronous programming, and the latest .NET features, this book provides step-by-step explanations to help you write clean, efficient, and scalable code. Whether you're building desktop, web, or cloud applications, this resource equips you with the knowledge and best practices to become a confident C# developer."

        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .width(400f)
                        .height(300f)
                        .stopPaging()
                        .decoration {
                            before().text("Book description:").bold()
                            content().text(bookDescription)
                        }
                }
            }
            .generatePdf(output("stop-paging-enabled.pdf"))
    }
}
