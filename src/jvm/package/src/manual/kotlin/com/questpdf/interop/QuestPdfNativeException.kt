package com.questpdf.interop

/**
 * A .NET exception reported by the QuestPDF native library. The message
 * carries the full .NET exception text including its stack trace.
 */
class QuestPdfNativeException(message: String) : RuntimeException(message)
