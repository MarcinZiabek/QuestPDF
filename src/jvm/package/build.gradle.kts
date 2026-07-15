plugins {
    kotlin("jvm") version "2.4.0"
}

group = "com.questpdf"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // The generated bindings call the QuestPDF.Native shared library via JNA.
    api("net.java.dev.jna:jna:5.17.0")
}

sourceSets {
    // The generated/ directory is wiped and rewritten by the generator on every
    // run; handwritten code lives in manual/ and is never touched by it.
    main {
        kotlin.setSrcDirs(listOf("src/generated/kotlin", "src/manual/kotlin"))
    }

    create("samples") {
        kotlin.setSrcDirs(listOf("src/samples/kotlin"))
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
        runtimeClasspath += output + compileClasspath
    }
}

// ---- native library (dotnet publish, NativeAOT) ----

val nativeProjectDir = rootDir.resolve("../../dotnet/interop")
val nativePublishDir = layout.buildDirectory.dir("native")

val dotnetRid = providers.systemProperty("os.name").map { osName ->
    val arch = System.getProperty("os.arch").lowercase()
    val archPart = if (arch == "aarch64" || arch == "arm64") "arm64" else "x64"
    when {
        osName.lowercase().contains("mac") -> "osx-$archPart"
        osName.lowercase().contains("win") -> "win-$archPart"
        else -> "linux-$archPart"
    }
}

// The Gradle daemon's PATH may not include the dotnet install location.
val dotnetExecutable = sequenceOf(
    System.getenv("DOTNET_ROOT")?.let { "$it/dotnet" },
    "/usr/local/share/dotnet/dotnet",
    "/usr/local/bin/dotnet",
    "/opt/homebrew/bin/dotnet",
).filterNotNull().firstOrNull { File(it).canExecute() } ?: "dotnet"

val publishNative = tasks.register<Exec>("publishNative") {
    group = "build"
    description = "Publishes the QuestPDF.Native shared library (dotnet publish, NativeAOT)."

    val rid = dotnetRid.get()
    val outputDir = nativePublishDir.get().asFile

    inputs.dir(nativeProjectDir.resolve("Exports"))
    inputs.file(nativeProjectDir.resolve("InteropRuntime.cs"))
    inputs.file(nativeProjectDir.resolve("QuestPDF.Native.csproj"))
    outputs.dir(outputDir)

    commandLine(
        dotnetExecutable, "publish", nativeProjectDir.absolutePath,
        "-c", "Release", "-r", rid,
        "-o", outputDir.absolutePath,
    )

    // StripSymbols=true drops a .dSYM bundle (~32 MB of DWARF) next to the
    // dylib; it is debug-only and must not ship with the runtime directory.
    doLast {
        outputDir.listFiles()?.filter { it.name.endsWith(".dSYM") }?.forEach { it.deleteRecursively() }
    }
}

val nativeDirProperty = nativePublishDir.map { it.asFile.absolutePath }

tasks.register<JavaExec>("runSamples") {
    group = "verification"
    description = "Runs every sample composition and writes real PDFs to build/samples-output."
    dependsOn(publishNative)
    classpath = sourceSets["samples"].runtimeClasspath
    mainClass.set("samples.RunAllKt")
    systemProperty("questpdf.native.dir", nativeDirProperty.get())
    systemProperty("questpdf.samples.output", layout.buildDirectory.dir("samples-output").get().asFile.absolutePath)
}

// Samples must always compile as part of a regular build.
tasks.named("check") {
    dependsOn(tasks.named("compileSamplesKotlin"))
}
