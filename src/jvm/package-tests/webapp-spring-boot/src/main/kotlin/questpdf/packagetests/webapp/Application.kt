package questpdf.packagetests.webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import questpdf.packagetests.TestRunner
import java.io.File

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@RestController
class GenerateController {

    @GetMapping("/health")
    fun health(): String = "OK"

    /** Generates all test documents into TestOutput and returns the qpdf-merged PDF. */
    @GetMapping("/generate", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun generate(): ByteArray {
        val outputFolder = TestRunner.run()
        return File(outputFolder, "qpdf.pdf").readBytes()
    }
}
