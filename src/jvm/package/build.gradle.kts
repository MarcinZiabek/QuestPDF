plugins {
    kotlin("jvm") version "2.4.0"
    `maven-publish`
}

group = "com.questpdf"
// Package tests publish unique versions (e.g. 0.1.0-local.20260715120000) so
// stale caches can never satisfy a dependency on a freshly built package.
version = providers.gradleProperty("questpdfVersion").getOrElse("0.1.0-SNAPSHOT")

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
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

// Every platform's publish output is staged under <nativesRoot>/<rid>/ before
// packaging. Locally, publishNative fills in the current platform's slot; on
// CI, prebuilt outputs for the whole platform matrix are downloaded there.
val knownRids = listOf("win-x64", "win-arm64", "linux-x64", "linux-arm64", "linux-musl-x64", "osx-x64", "osx-arm64")

val nativeProjectDir = rootDir.resolve("../../dotnet/interop")
val nativesRoot = providers.gradleProperty("questpdfNativesDir")
    .map { rootDir.resolve(it) }
    .getOrElse(layout.buildDirectory.dir("native-staging").get().asFile)

val currentRid = providers.systemProperty("os.name").map { osName ->
    val arch = System.getProperty("os.arch").lowercase()
    val archPart = if (arch == "aarch64" || arch == "arm64") "arm64" else "x64"
    when {
        osName.lowercase().contains("mac") -> "osx-$archPart"
        osName.lowercase().contains("win") -> "win-$archPart"
        else -> "linux-$archPart"
    }
}.get()

// The Gradle daemon's PATH may not include the dotnet install location.
val dotnetExecutable = sequenceOf(
    System.getenv("DOTNET_ROOT")?.let { "$it/dotnet" },
    "/usr/local/share/dotnet/dotnet",
    "/usr/local/bin/dotnet",
    "/opt/homebrew/bin/dotnet",
).filterNotNull().firstOrNull { File(it).canExecute() } ?: "dotnet"

val publishNative = tasks.register<Exec>("publishNative") {
    group = "build"
    description = "Publishes the QuestPDF.Native shared library for the current platform (dotnet publish, NativeAOT)."

    val outputDir = nativesRoot.resolve(currentRid)

    inputs.dir(nativeProjectDir.resolve("Exports"))
    inputs.file(nativeProjectDir.resolve("InteropRuntime.cs"))
    inputs.file(nativeProjectDir.resolve("QuestPDF.Native.csproj"))
    outputs.dir(outputDir)

    commandLine(
        dotnetExecutable, "publish", nativeProjectDir.absolutePath,
        "-c", "Release", "-r", currentRid,
        "-o", outputDir.absolutePath,
    )

    // StripSymbols=true drops a .dSYM bundle (~32 MB of DWARF) next to the
    // dylib; it is debug-only and must not ship with the runtime directory.
    doLast {
        outputDir.listFiles()?.filter { it.name.endsWith(".dSYM") }?.forEach { it.deleteRecursively() }
    }
}

fun nativeLibraryFileName(rid: String) = when {
    rid.startsWith("osx-") -> "QuestPDF.Native.dylib"
    rid.startsWith("win-") -> "QuestPDF.Native.dll"
    else -> "QuestPDF.Native.so"
}

// ---- natives jars (one classifier jar per staged platform) ----

// With an explicit -PquestpdfNativesDir (CI), the staging directory is filled
// externally (downloaded platform-matrix artifacts) and packaged as found at
// configuration time. Without it (the local flow), the current platform's
// slot is always packaged and produced by publishNative in the same build.
val externallyStaged = providers.gradleProperty("questpdfNativesDir").isPresent
val scannedRids = knownRids.filter { nativesRoot.resolve(it).resolve(nativeLibraryFileName(it)).isFile }
val packagedRids = if (externallyStaged) scannedRids else (scannedRids + currentRid).distinct()

val nativesJarTasks = packagedRids.map { rid ->
    val autoBuild = !externallyStaged && rid == currentRid
    val stagedDir = nativesRoot.resolve(rid)
    val resourceRoot = "questpdf/native/$rid"
    val indexDir = layout.buildDirectory.dir("native-index/$rid")

    // The loader cannot enumerate classpath directories portably (plain jars,
    // Spring Boot nested jars, ...), so each natives jar carries an index of
    // every file to extract.
    val indexTask = tasks.register("generateNativesIndex-$rid") {
        if (autoBuild)
            dependsOn(publishNative)

        inputs.dir(stagedDir)
        outputs.dir(indexDir)

        doLast {
            val indexFile = indexDir.get().asFile.resolve("$resourceRoot/index.txt")
            indexFile.parentFile.mkdirs()

            val entries = stagedDir.walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(stagedDir).invariantSeparatorsPath }
                .sorted()
                .toList()

            check(entries.isNotEmpty()) { "No native files found in $stagedDir." }
            indexFile.writeText(entries.joinToString("\n", postfix = "\n"))
        }
    }

    tasks.register<Jar>("nativesJar-$rid") {
        group = "build"
        description = "Packages the QuestPDF.Native runtime directory for $rid as a classifier jar."
        dependsOn(indexTask)
        if (autoBuild)
            dependsOn(publishNative)

        archiveClassifier.set(rid)
        from(stagedDir) { into(resourceRoot) }
        from(indexDir)
        exclude("**/*.dSYM/**")
    }
}

// ---- publishing ----

val repoRoot = rootDir.resolve("../../..")
val feedDir = providers.gradleProperty("questpdfFeedDir")
    .map { rootDir.resolve(it) }
    .getOrElse(repoRoot.resolve("artifacts/maven-feed"))

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "questpdf"
            from(components["java"])
            nativesJarTasks.forEach { artifact(it) }

            pom {
                name.set("QuestPDF for JVM")
                description.set("QuestPDF fluent document-generation API for the JVM, backed by the native QuestPDF engine.")
                url.set("https://www.questpdf.com")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "localFeed"
            url = uri(feedDir)
        }
    }
}

// Classifier artifacts are not representable in Gradle module metadata; with
// the metadata disabled, Maven and Gradle consumers both resolve through the
// POM and fetch natives jars by the plain Maven classifier convention.
tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

tasks.register("publishToLocalFeed") {
    group = "publishing"
    description = "Publishes the package (main + staged natives jars) to the local Maven feed directory."
    dependsOn("publishMavenPublicationToLocalFeedRepository")
}

// ---- samples ----

tasks.register<JavaExec>("runSamples") {
    group = "verification"
    description = "Runs every sample composition and writes real PDFs to build/samples-output."
    dependsOn(publishNative)
    classpath = sourceSets["samples"].runtimeClasspath
    mainClass.set("samples.RunAllKt")
    systemProperty("questpdf.native.dir", nativesRoot.resolve(currentRid).absolutePath)
    systemProperty("questpdf.samples.output", layout.buildDirectory.dir("samples-output").get().asFile.absolutePath)
}

// Samples must always compile as part of a regular build.
tasks.named("check") {
    dependsOn(tasks.named("compileSamplesKotlin"))
}
