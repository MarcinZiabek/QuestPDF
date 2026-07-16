package questpdf.docexamples

import com.questpdf.fluent.Document
import com.questpdf.helpers.Colors
import com.questpdf.infrastructure.ImageCompressionQuality
import com.questpdf.infrastructure.ImageFormat
import com.questpdf.infrastructure.ImageGenerationSettings
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShowOnceExamples : DocExample() {

    @Test
    fun example() {
        Document
            .create {
                page {
                    size(350f, 500f)
                    defaultTextStyle { fontSize(20f) }
                    margin(25f)

                    content()
                        .decoration {
                            before().column {
                                item()
                                    .showOnce()
                                    .row {
                                        constantItem(80f).aspectRatio(4 / 3f).placeholder()
                                        constantItem(10f)
                                        relativeItem()
                                            .alignMiddle()
                                            .column {
                                                item().text("Invoice #1234").fontSize(24f).bold()
                                                item().text("Generated on ${LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))}").fontSize(16f).light()
                                            }
                                    }

                                item()
                                    .skipOnce()
                                    .text("Invoice #1234").fontSize(24f).bold()
                            }

                            // generate dummy content
                            content()
                                .paddingTop(15f)
                                .extendHorizontal()
                                .column {
                                    spacing(10f)

                                    for (i in 1..15) {
                                        item()
                                            .height(30f)
                                            .background(Colors.Grey.Lighten3)
                                            .alignCenter()
                                            .alignMiddle()
                                            .text("$i")
                                    }
                                }
                        }
                }
            }
            .generateImages({ index -> output("show-once-$index.webp") }, ImageGenerationSettings().apply {
                imageFormat = ImageFormat.Webp
                imageCompressionQuality = ImageCompressionQuality.Best
                rasterDpi = 144
            })
    }
}
