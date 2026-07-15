// Console app consuming the QuestPDF package from the local Maven feed with
// Gradle. The feed location, package version and natives classifier are passed
// by run-tests.mjs.
plugins {
    kotlin("jvm") version "2.4.0"
    application
}

val questpdfVersion: String = providers.gradleProperty("questpdfVersion").get()
val questpdfFeedDir: String = providers.gradleProperty("questpdfFeedDir").get()
val questpdfRid: String = providers.gradleProperty("questpdfRid").get()

repositories {
    maven { url = uri(questpdfFeedDir) }
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.questpdf:questpdf:$questpdfVersion")
    runtimeOnly("com.questpdf:questpdf:$questpdfVersion:$questpdfRid")
}

sourceSets {
    main {
        kotlin.srcDir("../shared/kotlin")
    }
}

application {
    mainClass.set("questpdf.packagetests.console.MainKt")
}
