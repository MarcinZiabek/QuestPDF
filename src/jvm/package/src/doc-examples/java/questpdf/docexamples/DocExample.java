package questpdf.docexamples;

import com.questpdf.Settings;
import com.questpdf.infrastructure.LicenseType;

import java.io.File;

/**
 * Base class of every ported documentation example (the Java counterpart of
 * src/dotnet/library/QuestPDF.DocumentationExamples). Output files use exactly
 * the same names as the .NET examples so the port-parity tests can compare the
 * produced images byte-for-byte across runtimes.
 */
public abstract class DocExample {

    static {
        Settings.setLicense(LicenseType.Community);
    }

    private static File outputDirectory;
    private static File resourcesDirectory;

    /** Absolute path for an output file; the name must match the .NET example. */
    protected static String output(String name) {
        if (outputDirectory == null) {
            outputDirectory = new File(requireEnv("QUESTPDF_DOC_EXAMPLES_OUTPUT", "the output directory"));
            outputDirectory.mkdirs();
        }

        return new File(outputDirectory, name).getPath();
    }

    /** Absolute path of a file inside the shared Resources directory (images, fonts, ...). */
    protected static String resource(String name) {
        if (resourcesDirectory == null)
            resourcesDirectory = new File(requireEnv("QUESTPDF_DOC_EXAMPLES_RESOURCES", "the shared Resources directory"));

        return new File(resourcesDirectory, name).getPath();
    }

    private static String requireEnv(String variable, String meaning) {
        var value = System.getenv(variable);
        if (value == null)
            throw new IllegalStateException(variable + " must point at " + meaning + ".");

        return value;
    }
}
