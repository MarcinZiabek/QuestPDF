plugins {
    kotlin("jvm") version "2.4.0"
    `maven-publish`
    signing
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

// The Java documentation examples contain non-ASCII literals (Arabic, emoji);
// javac must not fall back to the platform encoding (cp1252 on Windows CI).
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    // Maven Central requires -sources and -javadoc artifacts for the main jar.
    // The javadoc jar is empty (Kotlin sources produce no javadoc), which the
    // Central validation explicitly accepts.
    withSourcesJar()
    withJavadocJar()
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

    // Ports of the .NET documentation examples (src/dotnet/library/
    // QuestPDF.DocumentationExamples); the images they produce are compared
    // byte-for-byte against the other runtimes by the port-parity tests.
    create("docExamples") {
        kotlin.setSrcDirs(listOf("src/doc-examples/kotlin"))
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
        runtimeClasspath += output + compileClasspath
    }

    // The same documentation examples ported to Java: the package targets both
    // JVM languages, so every example exists in Kotlin and in Java form and
    // both suites must produce identical images.
    create("docExamplesJava") {
        java.setSrcDirs(listOf("src/doc-examples/java"))
        kotlin.setSrcDirs(emptyList<String>())
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
        runtimeClasspath += output + compileClasspath
    }
}

dependencies {
    "docExamplesImplementation"("org.junit.jupiter:junit-jupiter:5.13.4")
    "docExamplesRuntimeOnly"("org.junit.platform:junit-platform-launcher:1.13.4")
    "docExamplesJavaImplementation"("org.junit.jupiter:junit-jupiter:5.13.4")
    "docExamplesJavaRuntimeOnly"("org.junit.platform:junit-platform-launcher:1.13.4")
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

            // The runtime registers every font deployed next to the natives;
            // the bundled Lato family (QuestPDF's default typeface) must ship
            // with every platform, like the LatoFont folder in the NuGet package.
            check(entries.any { it.startsWith("LatoFont/") && it.endsWith(".ttf") }) {
                "The staged native directory $stagedDir does not contain the bundled LatoFont fonts."
            }

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

// Every published jar carries the license text, like the LICENSE.md packed
// into the NuGet package.
tasks.withType<Jar>().configureEach {
    from(repoRoot.resolve("LICENSE.md")) { into("META-INF") }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "questpdf"
            from(components["java"])
            nativesJarTasks.forEach { artifact(it) }

            // The complete metadata set required by the Maven Central
            // validation: name, description, url, license, developers and scm.
            pom {
                name.set("QuestPDF for JVM")
                description.set("QuestPDF fluent document-generation API for the JVM, backed by the native QuestPDF engine.")
                url.set("https://www.questpdf.com")
                licenses {
                    license {
                        // QuestPDF is dual-licensed (Community / Professional /
                        // Enterprise); the full text ships as META-INF/LICENSE.md.
                        name.set("QuestPDF License Agreement")
                        url.set("https://github.com/QuestPDF/QuestPDF/blob/main/LICENSE.md")
                    }
                }
                developers {
                    developer {
                        id.set("MarcinZiabek")
                        name.set("Marcin Ziąbek")
                        organization.set("CodeFlint")
                        organizationUrl.set("https://www.questpdf.com")
                    }
                }
                scm {
                    url.set("https://github.com/QuestPDF/QuestPDF")
                    connection.set("scm:git:https://github.com/QuestPDF/QuestPDF.git")
                    developerConnection.set("scm:git:git@github.com:QuestPDF/QuestPDF.git")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/QuestPDF/QuestPDF/issues")
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

// Maven Central requires every artifact to be GPG-signed. Signing activates
// only when a key is supplied (the signingKey / signingPassword properties, or
// the ORG_GRADLE_PROJECT_signingKey / ORG_GRADLE_PROJECT_signingPassword
// environment variables of a release pipeline); without one, nothing is signed
// and the local feed flow is unaffected.
signing {
    val signingKey = providers.gradleProperty("signingKey").orNull
    val signingPassword = providers.gradleProperty("signingPassword").orNull

    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
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

// ---- documentation examples (port-parity tests) ----

// One suite per JVM language, both over the same example set; each writes the
// full set of images so the port-parity tests can compare the two suites (and
// the other runtimes) byte-for-byte.
fun registerDocExamplesTest(taskName: String, sourceSetName: String, defaultOutputDir: String) =
    tasks.register<Test>(taskName) {
        group = "verification"
        description = "Runs the ported documentation examples ($sourceSetName); images land in the directory given by QUESTPDF_DOC_EXAMPLES_OUTPUT."
        testClassesDirs = sourceSets[sourceSetName].output.classesDirs
        classpath = sourceSets[sourceSetName].runtimeClasspath
        useJUnitPlatform()

        // An externally published native runtime (the parity script publishes one
        // for all suites) wins; otherwise the local publishNative output is used.
        if (System.getenv("QUESTPDF_NATIVE_DIR") == null) {
            dependsOn(publishNative)
            systemProperty("questpdf.native.dir", nativesRoot.resolve(currentRid).absolutePath)
        }

        environment(
            "QUESTPDF_DOC_EXAMPLES_OUTPUT",
            System.getenv("QUESTPDF_DOC_EXAMPLES_OUTPUT")
                ?: layout.buildDirectory.dir(defaultOutputDir).get().asFile.absolutePath,
        )
        environment(
            "QUESTPDF_DOC_EXAMPLES_RESOURCES",
            System.getenv("QUESTPDF_DOC_EXAMPLES_RESOURCES")
                ?: repoRoot.resolve("src/dotnet/library/QuestPDF.DocumentationExamples/Resources").absolutePath,
        )

        // The suite writes files consumed by an external comparison; never skip it.
        outputs.upToDateWhen { false }
    }

registerDocExamplesTest("docExamplesTest", "docExamples", "doc-examples-output/kotlin")
registerDocExamplesTest("docExamplesJavaTest", "docExamplesJava", "doc-examples-output/java")

// Samples and documentation examples must always compile as part of a regular build.
tasks.named("check") {
    dependsOn(tasks.named("compileSamplesKotlin"))
    dependsOn(tasks.named("compileDocExamplesKotlin"))
    dependsOn(tasks.named("compileDocExamplesJavaJava"))
}
