package com.questpdf.interop

/**
 * Base class of every handle-backed generated type: wraps the handle of a .NET
 * object living in the native library's handle table. The handle is released
 * when this wrapper is garbage collected.
 */
open class NativeObject internal constructor(val nativeHandle: Long) {
    init {
        if (nativeHandle != 0L)
            NativeBridge.registerForCleanup(this, nativeHandle)
    }
}
