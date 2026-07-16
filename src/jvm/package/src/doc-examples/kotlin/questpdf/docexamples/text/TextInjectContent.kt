package questpdf.docexamples.text

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import com.questpdf.infrastructure.TextInjectedElementAlignment
import org.junit.jupiter.api.Test
import questpdf.docexamples.DocExample

class TextInjectContent : DocExample() {

    @Test
    fun injectImage() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("A unit test can either ")
                            element().paddingBottom(-4f).height(24f).image(resource("unit-test-completed-icon.png"))
                            span(" pass").fontColor(Colors.Green.Medium)
                            span(" or ")
                            element().paddingBottom(-4f).height(24f).image(resource("unit-test-failed-icon.png"))
                            span(" fail").fontColor(Colors.Red.Medium)
                            span(".")
                        }
                }
            }
            .generateImages({ output("text-inject-image.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun injectSvg() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(350f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("To synchronize your email inbox, please click the ")
                            element().paddingBottom(-4f).height(24f).svg(resource("mail-synchronize-icon.svg"))
                            span(" icon.")
                        }
                }
            }
            .generateImages({ output("text-inject-svg.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }

    @Test
    fun injectPosition() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(400f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .text {
                            span("This ")

                            element(TextInjectedElementAlignment.AboveBaseline)
                                .width(12f).height(12f)
                                .background(Colors.Green.Medium)

                            span(" element is positioned above the baseline, while this ")

                            element(TextInjectedElementAlignment.BelowBaseline)
                                .width(12f).height(12f)
                                .background(Colors.Blue.Medium)

                            span(" element is positioned below the baseline.")
                        }
                }
            }
            .generateImages({ output("text-inject-position.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.VeryHigh
                rasterDpi = 144
            })
    }
}
