package com.questpdf.interop

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.io.File
import java.lang.ref.Cleaner
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

        val directory = System.getProperty("questpdf.native.dir")
            ?: error(
                "System property 'questpdf.native.dir' must point at the directory containing the " +
                    "published QuestPDF.Native shared library. Run './gradlew publishNative' and use " +
                    "the Gradle run tasks, which set the property automatically."
            )

        val osName = System.getProperty("os.name").lowercase()
        val libraryFile = when {
            osName.contains("mac") -> "QuestPDF.Native.dylib"
            osName.contains("win") -> "QuestPDF.Native.dll"
            else -> "QuestPDF.Native.so"
        }

        val path = File(directory, libraryFile)
        require(path.exists()) { "Native library not found at $path — run './gradlew publishNative' first." }

        val loaded = Native.load(
            path.absolutePath,
            QuestPdfNative::class.java,
            mapOf(Library.OPTION_STRING_ENCODING to "UTF-8"),
        )

        val onError = NativeErrorCallback { message ->
            pendingNativeError.set(message ?: "Unknown native error")
        }
        retained.add(onError)

        val rc = loaded.QuestPdf_Initialize(File(directory).absolutePath, onError)
        check(rc == 0) { "QuestPdf_Initialize failed with code $rc" }

        return loaded
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
