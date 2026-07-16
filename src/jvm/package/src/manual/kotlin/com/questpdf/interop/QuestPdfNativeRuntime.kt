package com.questpdf.interop

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Pointer

/**
 * Error channel callback registered at initialization. The native side invokes
 * it with the full .NET exception text instead of letting exceptions cross the
 * native boundary; [NativeBridge.check] rethrows it as [QuestPdfNativeException]
 * right after the offending call returns.
 */
internal fun interface NativeErrorCallback : Callback {
    fun invoke(message: String?)
}

/**
 * JNA declarations for the handwritten lifecycle exports of QuestPDF.Native
 * (see InteropRuntime.cs). The generated [QuestPdfNative] interface extends
 * this with every bridged API member.
 */
@Suppress("FunctionName")
internal interface QuestPdfNativeRuntime : Library {
    fun QuestPdf_Initialize(nativeDirectory: String, onError: NativeErrorCallback): Int
    fun QuestPdf_RegisterFontDirectory(directory: String)
    fun QuestPdf_ReleaseHandle(handle: Long)
    fun QuestPdf_RegisterString(value: String): Long
    fun QuestPdf_RegisterBuffer(data: ByteArray, length: Int): Long
    fun QuestPdf_FreeString(pointer: Pointer)
    fun QuestPdf_FreeBuffer(pointer: Pointer)
    fun QuestPdf_HandleCount(): Long
}
