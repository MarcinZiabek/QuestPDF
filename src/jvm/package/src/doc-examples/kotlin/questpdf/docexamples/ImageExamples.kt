package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.DocumentSettings
import com.questpdf.infrastructure.Image
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import java.io.File

// NOT PORTED: image-dynamic.webp — depends on .NET SkiaSharp (SKBitmap/SKCanvas synthesize the dynamic image).
class ImageExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .grid {
                            columns(2)
                            spacing(10f)

                            item(2).text("My photo gallery:").bold()

                            item().image(resource("Photos/photo-gallery-1.jpg"))
                            item().image(resource("Photos/photo-gallery-2.jpg"))
                            item().image(resource("Photos/photo-gallery-3.jpg"))
                            item().image(resource("Photos/photo-gallery-4.jpg"))
                        }
                }
            }
            .generateImages({ output("image-example.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun imageScaling() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1500f))
                    margin(25f)

                    content()
                        .column {
                            item().paddingBottom(5f).text("FitWidth").bold()
                            item()
                                .width(200f)
                                .height(150f)
                                .border(4f)
                                .borderColor(Colors.Red.Medium)
                                .image(resource("Photos/photo.jpg"))
                                .fitWidth()

                            item().height(15f)

                            item().paddingBottom(5f).text("FitHeight").bold()
                            item()
                                .width(200f)
                                .height(100f)
                                .border(4f)
                                .borderColor(Colors.Red.Medium)
                                .image(resource("Photos/photo.jpg"))
                                .fitHeight()

                            item().height(15f)

                            item().paddingBottom(5f).text("FitArea 1").bold()
                            item()
                                .width(200f)
                                .height(100f)
                                .border(4f)
                                .borderColor(Colors.Red.Medium)
                                .image(resource("Photos/photo.jpg"))
                                .fitArea()

                            item().height(15f)

                            item().paddingBottom(5f).text("FitArea 2").bold()
                            item()
                                .width(200f)
                                .height(150f)
                                .border(4f)
                                .borderColor(Colors.Red.Medium)
                                .image(resource("Photos/photo.jpg"))
                                .fitArea()

                            item().height(15f)

                            item().paddingBottom(5f).text("FitUnproportionally").bold()
                            item()
                                .width(200f)
                                .height(50f)
                                .border(4f)
                                .borderColor(Colors.Red.Medium)
                                .image(resource("Photos/photo.jpg"))
                                .fitUnproportionally()
                        }
                }
            }
            .generateImages({ output("image-scaling.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun dpiSetting() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            // lower raster dpi = lower resolution, pixelation
                            item()
                                .image(resource("Photos/photo.jpg"))
                                .withRasterDpi(16)

                            // higher raster dpi = higher resolution
                            item()
                                .image(resource("Photos/photo.jpg"))
                                .withRasterDpi(288)
                        }
                }
            }
            .generateImages({ output("image-dpi.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun compressionSetting() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    margin(25f)

                    content()
                        .column {
                            spacing(10f)

                            // low quality = smaller output file
                            item()
                                .image(resource("Photos/photo.jpg"))
                                .withCompressionQuality(ImageCompressionQuality.VeryLow)

                            // high quality / fidelity = larger output file
                            item()
                                .image(resource("Photos/photo.jpg"))
                                .withCompressionQuality(ImageCompressionQuality.VeryHigh)
                        }
                }
            }
            .generateImages({ output("image-compression.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun globalSettings() {
        Document
            .create {
                page {
                    content().image(resource("Photos/photo.jpg"))
                }
            }
            .withSettings(DocumentSettings().apply {
                // default: ImageCompressionQuality.High;
                imageCompressionQuality = ImageCompressionQuality.Medium

                // default: 288
                imageRasterDpi = 14
            })
            .generatePdf(output("image-global-settings.pdf"))
    }

    @Test
    fun sharedImages() {
        val image = Image.fromFile(resource("checkbox.png"))

        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(350f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(15f)

                            repeat(5) {
                                item().row {
                                    autoItem().width(28f).image(image)
                                    relativeItem().paddingLeft(8f).alignMiddle().text(Placeholders.label())
                                }
                            }
                        }
                }
            }
            .generateImages({ output("image-shared.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun svgSupport() {
        Document
            .create {
                page {
                    continuousSize(250f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    val svgContent = File(resource("pdf-icon.svg")).readText()

                    content()
                        .column {
                            item().text("The classic PDF icon looks like this:").bold()
                            item().height(15f)
                            item().svg(svgContent)
                        }
                }
            }
            .generateImages({ output("image-svg.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
