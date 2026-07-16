package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.AspectRatioOption
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ConstrainedExamples : DocExample() {

    @Test
    fun widthExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(300f)
                        .padding(25f)
                        .column {
                            spacing(25f)

                            item()
                                .minWidth(200f)
                                .background(Colors.Grey.Lighten3)
                                .text("Lorem ipsum")

                            item()
                                .maxWidth(100f)
                                .background(Colors.Grey.Lighten3)
                                .text("dolor sit amet")
                        }
                }
            }
            .generateImages({ output("width.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun heightExample() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }

                    content()
                        .width(300f)
                        .padding(25f)
                        .height(100f)
                        .aspectRatio(2f, AspectRatioOption.FitHeight)
                        .background(Colors.Grey.Lighten1)
                }
            }
            .generateImages({ output("height.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
