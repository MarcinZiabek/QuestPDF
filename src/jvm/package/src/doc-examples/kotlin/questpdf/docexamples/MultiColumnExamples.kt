package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.PageSizes
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class MultiColumnExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    minSize(PageSize(650f, 0f))
                    maxSize(PageSize(650f, 650f))
                    defaultTextStyle { fontSize(12f) }
                    margin(25f)

                    content()
                        .multiColumn {
                            columns(3)
                            spacing(25f)

                            content()
                                .column {
                                    spacing(15f)

                                    for (sectionId in 0 until 3) {
                                        for (textId in 0 until 3)
                                            item().text(Placeholders.paragraph()).justify()

                                        item().aspectRatio(21 / 9f).image { payload -> Placeholders.image(payload.imageSize) }
                                    }
                                }
                        }
                }
            }
            .generateImages({ output("multicolumn-example.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }

    @Test
    fun spacerExample() {
        Document
            .create {
                page {
                    minSize(PageSize(450f, 0f))
                    maxSize(PageSize(450f, 550f))
                    defaultTextStyle { fontSize(12f) }
                    margin(25f)

                    content()
                        .multiColumn {
                            columns(2)
                            spacing(50f)

                            spacer()
                                .alignCenter()
                                .lineVertical(2f)
                                .lineColor(Colors.Grey.Medium)

                            content()
                                .column {
                                    spacing(15f)

                                    for (textId in 0 until 5)
                                        item().text(Placeholders.paragraph()).justify()
                                }
                        }
                }
            }
            .generateImages({ output("multicolumn-spacer.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }

    @Test
    fun balanceHeightWithExample() {
        Document
            .create {
                page {
                    size(PageSizes.A4)
                    defaultTextStyle { fontSize(14f) }
                    margin(30f)

                    content()
                        .multiColumn {
                            spacing(30f)
                            balanceHeight()

                            content()
                                .column {
                                    spacing(15f)

                                    for (textId in 0 until 8)
                                        item().text(Placeholders.paragraph()).justify()
                                }
                        }
                }
            }
            .generateImages({ output("multicolumn-balance-height-with.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.High
                rasterDpi = 144
            })
    }
}
