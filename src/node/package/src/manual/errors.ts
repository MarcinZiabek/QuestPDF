/**
 * A .NET exception reported through the native error channel. The message
 * carries the full .NET exception text including its stack trace; the
 * JavaScript stack of this error shows the call that triggered it.
 */
export class QuestPdfNativeError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'QuestPdfNativeError';
    }
}
