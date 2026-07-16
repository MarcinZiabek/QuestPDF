package questpdf.packagetests

import com.questpdf.Settings
import com.questpdf.fluent.Document
import com.questpdf.fluent.DocumentOperation
import com.questpdf.helpers.PageSizes
import com.questpdf.infrastructure.LicenseType
import java.io.File

/**
 * The document-generation scenario shared by every package-test app. It mirrors
 * the .NET package tests (src/dotnet/package-tests): a Skia-rendered PDF with
 * text and an image, a qpdf document operation (merge + attachment), and an XPS
 * document on Windows. The Arabic line renders with a font the apps deploy
 * themselves (shared/app-resources → the questpdf/fonts/ classpath convention).
 *
 * Test resources (image, attachment) are read from the directory given by the
 * QUESTPDF_TEST_RESOURCES environment variable; documents are written to
 * TestOutput under the current working directory.
 */
object TestRunner {

    const val OUTPUT_FOLDER = "TestOutput"

    fun run(): File {
        val resources = File(
            System.getenv("QUESTPDF_TEST_RESOURCES")
                ?: error("QUESTPDF_TEST_RESOURCES must point at the shared test resources directory.")
        )

        Settings.license = LicenseType.Community
        Settings.useEnvironmentFonts = false
        Settings.checkIfAllTextGlyphsAreAvailable = true

        val outputFolder = File(OUTPUT_FOLDER).absoluteFile
        outputFolder.mkdirs()

        val skiaPdfOutput = File(outputFolder, "skia.pdf")
        val skiaXpsOutput = File(outputFolder, "skia.xps")
        val pdfToMerge = File(outputFolder, "to-merge.pdf")
        val qpdfOutput = File(outputFolder, "qpdf.pdf")

        createMainDocument(resources).generatePdf(skiaPdfOutput.path)
        createDocumentToMerge().generatePdf(pdfToMerge.path)

        val attachment = DocumentOperation.DocumentAttachment()
        attachment.filePath = File(resources, "books.xml").path

        DocumentOperation
            .loadFile(skiaPdfOutput.path)
            .mergeFile(pdfToMerge.path)
            .addAttachment(attachment)
            .save(qpdfOutput.path)

        if (System.getProperty("os.name").lowercase().contains("win"))
            createMainDocument(resources).generateXps(skiaXpsOutput.path)

        return outputFolder
    }

    private fun createMainDocument(resources: File) =
        Document.create {
            page {
                margin(50f)
                size(PageSizes.A5)
                defaultTextStyle { fontSize(24f) }

                content().column {
                    spacing(10f)
                    item().text("Lorem ipsum dolor sit amet")
                    item().text("مرحبا بالعالم").fontFamily("Noto Sans Arabic")
                    item().width(50f).image(File(resources, "questpdf-logo.png").path)
                }
            }
        }

    private fun createDocumentToMerge() =
        Document.create {
            page {
                margin(50f)
                size(PageSizes.A5)
                content().text("Document to merge").fontSize(24f)
            }
        }
}
