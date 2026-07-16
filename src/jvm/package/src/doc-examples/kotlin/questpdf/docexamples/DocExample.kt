package questpdf.docexamples

import com.questpdf.Settings
import com.questpdf.infrastructure.Color
import com.questpdf.infrastructure.LicenseType
import java.io.File

/**
 * The string representation .NET produces for a Color (Color.ToString()):
 * #RRGGBB, or #AARRGGBB when the color is translucent. Used by examples that
 * pass a color where the .NET original relies on implicit ToString conversion.
 */
fun Color.toHexString(): String {
    val alpha = alpha.toInt()
    val rgb = String.format("%02X%02X%02X", red.toInt(), green.toInt(), blue.toInt())
    return if (alpha == 0xFF) "#$rgb" else "#%02X%s".format(alpha, rgb)
}

/**
 * Base class of every ported documentation example (the Kotlin counterpart of
 * src/dotnet/library/QuestPDF.DocumentationExamples). Output files use exactly
 * the same names as the .NET examples so the port-parity tests can compare the
 * produced images byte-for-byte across runtimes.
 */
abstract class DocExample {

    init {
        configured
    }

    companion object {
        private val configured: Unit by lazy {
            Settings.license = LicenseType.Community
        }

        private val outputDirectory: File by lazy {
            val directory = File(
                System.getenv("QUESTPDF_DOC_EXAMPLES_OUTPUT")
                    ?: error("QUESTPDF_DOC_EXAMPLES_OUTPUT must point at the output directory."),
            )
            directory.mkdirs()
            directory
        }

        private val resourcesDirectory: File by lazy {
            File(
                System.getenv("QUESTPDF_DOC_EXAMPLES_RESOURCES")
                    ?: error("QUESTPDF_DOC_EXAMPLES_RESOURCES must point at the shared Resources directory."),
            )
        }

        /** Absolute path for an output file; the name must match the .NET example. */
        fun output(name: String): String = File(outputDirectory, name).path

        /** Absolute path of a file inside the shared Resources directory (images, fonts, ...). */
        fun resource(name: String): String = File(resourcesDirectory, name).path
    }
}
