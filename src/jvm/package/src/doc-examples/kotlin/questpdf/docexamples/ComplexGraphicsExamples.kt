package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.helpers.PageSize
import com.questpdf.helpers.Placeholders
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test

class ComplexGraphicsExamples : DocExample() {

    @Test
    fun roundedRectangleWithGradient() {
        Document
            .create {
                page {
                    minSize(PageSize(0f, 0f))
                    maxSize(PageSize(1000f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .layers {
                            layer().svg { size ->
                                """
                                <svg width="${size.width}" height="${size.height}" xmlns="http://www.w3.org/2000/svg">
                                    <defs>
                                      <linearGradient id="backgroundGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                                        <stop stop-color="#00E5FF" offset="0%"/>
                                        <stop stop-color="#2979FF" offset="100%"/>
                                      </linearGradient>
                                    </defs>

                                    <rect x="0" y="0" width="${size.width}" height="${size.height}" rx="${size.height / 2}" ry="${size.height / 2}" fill="url(#backgroundGradient)" />
                                </svg>
                                """.trimIndent()
                            }

                            primaryLayer()
                                .paddingVertical(10f)
                                .paddingHorizontal(20f)
                                .text("QuestPDF")
                                .fontColor(Colors.White)
                                .fontSize(32f)
                                .extraBlack()
                        }
                }
            }
            .generateImages({ output("complex-graphics-rounded-rectangle-with-gradient.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }

    @Test
    fun dottedLine() {
        Document
            .create {
                page {
                    minSize(PageSize(500f, 0f))
                    maxSize(PageSize(500f, 1000f))
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .column {
                            spacing(5f)

                            for (i in 1..5) {
                                val pageNumber = i * 7 + 4

                                item().row {
                                    autoItem().text("$i.")
                                    constantItem(10f)
                                    autoItem().text(Placeholders.label())

                                    relativeItem().paddingHorizontal(3f).offsetY(20f).height(2f).svg { size ->
                                        """
                                        <svg width="${size.width}" height="${size.height}" xmlns="http://www.w3.org/2000/svg">
                                            <line x1="0" y1="0" x2="${size.width}" y2="0" fill="none" stroke="black" stroke-width="2" stroke-dasharray="2 6" />
                                        </svg>
                                        """.trimIndent()
                                    }

                                    autoItem().text("$pageNumber")
                                }
                            }
                        }
                }
            }
            .generateImages({ output("complex-graphics-dotted-line.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
