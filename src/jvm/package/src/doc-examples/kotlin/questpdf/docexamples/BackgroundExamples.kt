package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class BackgroundExamples : DocExample() {

    @Test
    fun solidColor() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    pageColor(Colors.White)
                    margin(25f)

                    val colors = arrayOf(
                        Colors.LightBlue.Darken4,
                        Colors.LightBlue.Darken3,
                        Colors.LightBlue.Darken2,
                        Colors.LightBlue.Darken1,

                        Colors.LightBlue.Medium,

                        Colors.LightBlue.Lighten1,
                        Colors.LightBlue.Lighten2,
                        Colors.LightBlue.Lighten3,
                        Colors.LightBlue.Lighten4,
                        Colors.LightBlue.Lighten5,

                        Colors.LightBlue.Accent1,
                        Colors.LightBlue.Accent2,
                        Colors.LightBlue.Accent3,
                        Colors.LightBlue.Accent4,
                    )

                    content()
                        .height(150f)
                        .width(420f)
                        .row {
                            for (color in colors)
                                relativeItem().background(color)
                        }
                }
            }
            .generateImages({ output("background-solid.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun gradient() {
        Document
            .create {
                page {
                    minSize(PageSize(350f, 0f))
                    maxSize(PageSize(350f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    pageColor(Colors.White)
                    margin(25f)

                    content()
                        .column {
                            spacing(25f)

                            item()
                                .backgroundLinearGradient(0f, arrayOf(Colors.Red.Lighten2, Colors.Blue.Lighten2))
                                .aspectRatio(2f)

                            item()
                                .backgroundLinearGradient(45f, arrayOf(Colors.Green.Lighten2, Colors.LightGreen.Lighten2, Colors.Yellow.Lighten2))
                                .aspectRatio(2f)

                            item()
                                .backgroundLinearGradient(90f, arrayOf(Colors.Yellow.Lighten2, Colors.Amber.Lighten2, Colors.Orange.Lighten2))
                                .aspectRatio(2f)
                        }
                }
            }
            .generateImages({ output("background-gradient.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun roundedCorners() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    pageColor(Colors.White)
                    margin(25f)

                    content()
                        .shrink()
                        .background(Colors.Grey.Lighten2)
                        .cornerRadius(25f)
                        .padding(25f)
                        .text("Content with rounded corners")
                }
            }
            .generateImages({ output("background-rounded-corners.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
