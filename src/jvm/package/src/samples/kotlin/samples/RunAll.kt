package samples

import java.io.File

/**
 * Runs every sample composition against the real QuestPDF engine (through the
 * JNA → NativeAOT bridge) and writes the resulting PDFs to the output
 * directory. Success means every document generated and starts with a valid
 * PDF header.
 */
fun main() {
    val outputDirectory = File(System.getProperty("questpdf.samples.output") ?: "samples-output")
    outputDirectory.mkdirs()

    val samples = linkedMapOf(
        "invoice" to { InvoiceSample.run() },
        "report" to { ReportSample.run() },
        "text-showcase" to { TextShowcaseSample.run() },
        "layout-showcase" to { LayoutShowcaseSample.run() },
    )

    for ((name, sample) in samples) {
        val bytes = sample()

        require(bytes.size > 1000) { "$name produced only ${bytes.size} bytes — not a real document" }
        require(bytes.decodeToString(0, 5) == "%PDF-") { "$name did not produce a PDF header" }

        val target = File(outputDirectory, "$name.pdf")
        target.writeBytes(bytes)
        println("$name.pdf written (${bytes.size} bytes) -> $target")
    }

    println("All ${samples.size} samples generated real PDFs.")
}
