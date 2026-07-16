package com.questpdf.interop

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.io.File
import java.lang.ref.Cleaner
import java.nio.file.Files
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The Kotlin half of the interop runtime. Generated bodies marshal their
 * arguments, call an export on [lib], then [check] for errors recorded during
 * the call (either a .NET exception reported through the error callback, or a
 * Kotlin exception thrown inside a user lambda and captured by [guard]).
 *
 * Callback lifetime: JNA frees a callback's native trampoline when the Kotlin
 * object is garbage collected, but the .NET side may hold the function pointer
 * (a document's content handler is invoked on every generate call), so every
 * adapter is [retain]ed. Call [releaseRetainedCallbacks] between documents to
 * reclaim them in long-running processes.
 */
object NativeBridge {

    private val retained = ConcurrentLinkedQueue<Any>()
    private val pendingNativeError = ThreadLocal<String?>()
    private val pendingCallbackError = ThreadLocal<Throwable?>()
    private val cleaner: Cleaner = Cleaner.create()

    internal val lib: QuestPdfNative by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { load() }

    private fun load(): QuestPdfNative {
        System.setProperty("jna.encoding", "UTF-8")

        val directory = resolveNativeDirectory()
        val path = File(directory, nativeLibraryFileName())
        require(path.exists()) { "Native library not found at $path." }

        val loaded = Native.load(
            path.absolutePath,
            QuestPdfNative::class.java,
            mapOf(Library.OPTION_STRING_ENCODING to "UTF-8"),
        )

        val onError = NativeErrorCallback { message ->
            pendingNativeError.set(message ?: "Unknown native error")
        }
        retained.add(onError)

        val rc = loaded.QuestPdf_Initialize(directory.absolutePath, onError)
        check(rc == 0) { "QuestPdf_Initialize failed with code $rc" }

        registerApplicationFonts(loaded)

        return loaded
    }

    /**
     * Registers fonts deployed on the application classpath — the JVM analogue
     * of dropping font files into a .NET publish folder. Any jar may contribute
     * fonts by shipping a questpdf/fonts/index.txt resource (one path per line,
     * relative to questpdf/fonts/) next to the font files themselves; every
     * index on the classpath is honored, like META-INF/services registrations.
     */
    private fun registerApplicationFonts(loaded: QuestPdfNative) {
        val resourceRoot = "questpdf/fonts"
        val loader = Thread.currentThread().contextClassLoader ?: NativeBridge::class.java.classLoader

        val entries = buildList {
            for (index in loader.getResources("$resourceRoot/index.txt"))
                index.openStream().use { stream -> addAll(stream.bufferedReader().readLines()) }
        }
            .map(String::trim)
            .filter { it.isNotEmpty() }
            .distinct()

        if (entries.isEmpty())
            return

        val target = Files.createTempDirectory("questpdf-fonts-").toFile()
        Runtime.getRuntime().addShutdownHook(Thread { target.deleteRecursively() })

        for (entry in entries) {
            require(!entry.contains("..")) { "Invalid font index entry: $entry" }

            val destination = File(target, entry)
            destination.parentFile.mkdirs()

            val stream = loader.getResourceAsStream("$resourceRoot/$entry")
                ?: error("The font index lists $entry, but resource $resourceRoot/$entry is missing.")

            stream.use { input -> destination.outputStream().use { input.copyTo(it) } }
        }

        loaded.QuestPdf_RegisterFontDirectory(target.absolutePath)
        check()
    }

    /**
     * Locates the directory holding the QuestPDF.Native runtime files. An
     * explicit override (the questpdf.native.dir system property or the
     * QUESTPDF_NATIVE_DIR environment variable) wins; otherwise the files are
     * extracted from the natives classifier jar found on the classpath.
     */
    private fun resolveNativeDirectory(): File {
        val override = System.getProperty("questpdf.native.dir") ?: System.getenv("QUESTPDF_NATIVE_DIR")

        if (override != null) {
            val directory = File(override)
            require(directory.isDirectory) {
                "The QuestPDF native directory override points at $directory, which does not exist."
            }
            return directory
        }

        return extractNativesFromClasspath()
    }

    private fun nativeLibraryFileName(): String {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("mac") -> "QuestPDF.Native.dylib"
            osName.contains("win") -> "QuestPDF.Native.dll"
            else -> "QuestPDF.Native.so"
        }
    }

    /** The platform identifier used by both the native build and the natives jar layout. */
    private fun currentRid(): String {
        val osName = System.getProperty("os.name").lowercase()
        val archName = System.getProperty("os.arch").lowercase()
        val arch = if (archName == "aarch64" || archName == "arm64") "arm64" else "x64"

        return when {
            osName.contains("mac") -> "osx-$arch"
            osName.contains("win") -> "win-$arch"
            isMusl() -> "linux-musl-$arch"
            else -> "linux-$arch"
        }
    }

    private fun isMusl(): Boolean =
        File("/etc/alpine-release").exists() ||
            File("/lib").listFiles().orEmpty().any { it.name.startsWith("ld-musl-") }

    /**
     * Extracts the runtime files listed in the natives jar's index into a
     * fresh temporary directory. Streams are read through the classloader, so
     * the mechanism works from plain classpath jars as well as repackaged
     * fat/nested jars (Spring Boot and friends).
     */
    private fun extractNativesFromClasspath(): File {
        val rid = currentRid()
        val resourceRoot = "questpdf/native/$rid"
        val loader = Thread.currentThread().contextClassLoader ?: NativeBridge::class.java.classLoader

        val entries = loader.getResourceAsStream("$resourceRoot/index.txt")?.use { stream ->
            stream.bufferedReader().readLines().map(String::trim).filter { it.isNotEmpty() }
        } ?: error(
            "QuestPDF native libraries for $rid were not found on the classpath. Add the natives " +
                "artifact for your platform (com.questpdf:questpdf with the '$rid' classifier), or point " +
                "the questpdf.native.dir system property / QUESTPDF_NATIVE_DIR environment variable at a " +
                "directory containing the published QuestPDF.Native runtime files."
        )

        val target = Files.createTempDirectory("questpdf-native-").toFile()
        Runtime.getRuntime().addShutdownHook(Thread { target.deleteRecursively() })

        for (entry in entries) {
            require(!entry.contains("..")) { "Invalid natives index entry: $entry" }

            val destination = File(target, entry)
            destination.parentFile.mkdirs()

            val stream = loader.getResourceAsStream("$resourceRoot/$entry")
                ?: error("The natives index lists $entry, but resource $resourceRoot/$entry is missing.")

            stream.use { input -> destination.outputStream().use { input.copyTo(it) } }
        }

        return target
    }

    /** Rethrows any error recorded while the last native call was running. */
    fun check() {
        val callbackError = pendingCallbackError.get()
        if (callbackError != null) {
            pendingCallbackError.remove()
            pendingNativeError.remove()
            throw callbackError
        }

        val nativeError = pendingNativeError.get()
        if (nativeError != null) {
            pendingNativeError.remove()
            throw QuestPdfNativeException(nativeError)
        }
    }

    /** [check], then pass the value through — lets bridged bodies stay expressions. */
    fun <T> checked(value: T): T {
        check()
        return value
    }

    /** Keeps a JNA callback strongly reachable so its native trampoline stays valid. */
    internal fun <T : Any> retain(callback: T): T {
        retained.add(callback)
        return callback
    }

    /**
     * Runs a user lambda inside a native callback. A thrown exception must not
     * unwind through native frames, so it is recorded and rethrown by the next
     * [check] on the Kotlin side; the native side sees the fallback value.
     */
    fun <T> guard(fallback: T, block: () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            if (pendingCallbackError.get() == null)
                pendingCallbackError.set(t)
            fallback
        }

    /** Reads and frees a native UTF-8 string returned by an export. */
    fun takeString(pointer: Pointer?): String? {
        if (pointer == null)
            return null
        val value = pointer.getString(0, "UTF-8")
        lib.QuestPdf_FreeString(pointer)
        return value
    }

    /** Copies and frees a native buffer returned through out-parameters. */
    fun takeBuffer(data: PointerByReference, length: IntByReference): ByteArray {
        val pointer = data.value ?: return ByteArray(0)
        val bytes = pointer.getByteArray(0, length.value)
        lib.QuestPdf_FreeBuffer(pointer)
        return bytes
    }

    /** Registers a string as a transfer handle (callback results crossing to .NET). */
    fun registerString(value: String): Long = lib.QuestPdf_RegisterString(value)

    /** Registers a byte array as a transfer handle (callback results crossing to .NET). */
    fun registerBuffer(value: ByteArray): Long = lib.QuestPdf_RegisterBuffer(value, value.size)

    /** Number of live handles in the native table (diagnostics). */
    fun nativeHandleCount(): Long = lib.QuestPdf_HandleCount()

    /**
     * Drops the strong references keeping callback trampolines alive. Only call
     * this when no document created so far will be generated again.
     */
    fun releaseRetainedCallbacks() {
        retained.clear()
    }

    internal fun registerForCleanup(target: Any, handle: Long) {
        cleaner.register(target) { lib.QuestPdf_ReleaseHandle(handle) }
    }
}
