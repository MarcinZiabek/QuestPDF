package questpdf.packagetests.console

import questpdf.packagetests.TestRunner

fun main() {
    val outputFolder = TestRunner.run()
    println("Documents generated into $outputFolder")
}
