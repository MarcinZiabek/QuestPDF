import { existsSync } from 'node:fs';
import * as path from 'node:path';
import koffi from 'koffi';
import { declareNativeFunctions, NativeFunctions } from '../generated/interop/native-functions';
import { QuestPdfNativeError } from './errors';

/**
 * The TypeScript half of the interop runtime. Generated bodies marshal their
 * arguments, call an export on [Native.lib], then [Native.check] for errors
 * recorded during the call (either a .NET exception reported through the error
 * callback, or a JavaScript exception thrown inside a user callback and
 * captured by [Native.guard]).
 *
 * Callback lifetime: koffi frees a callback's native trampoline when it is
 * unregistered, but the .NET side may hold the function pointer (a document's
 * content handler is invoked on every generate call), so every adapter is
 * registered through [Native.retain] and kept alive. Call
 * [Native.releaseRetainedCallbacks] between documents to reclaim them in
 * long-running processes.
 */

/** Error channel callback registered at initialization (see InteropRuntime.cs). */
const NativeErrorCallback = koffi.proto('void QuestPdfNativeErrorCallback(str message)');

interface RuntimeFunctions {
    releaseHandle: koffi.KoffiFunction;
    registerString: koffi.KoffiFunction;
    registerBuffer: koffi.KoffiFunction;
    freeString: koffi.KoffiFunction;
    freeBuffer: koffi.KoffiFunction;
    handleCount: koffi.KoffiFunction;
}

interface BridgeState {
    functions: NativeFunctions;
    runtime: RuntimeFunctions;
}

let state: BridgeState | null = null;
let pendingNativeError: string | null = null;
let pendingCallbackError: unknown = null;
const retained: koffi.IKoffiRegisteredCallback[] = [];

// The error channel must outlive releaseRetainedCallbacks(): the native side
// holds its function pointer for the whole process lifetime.
let errorCallbackRegistration: koffi.IKoffiRegisteredCallback | null = null;

/** The platform identifier used in @questpdf/native-* package names. */
function platformPackageId(): string {
    const arch = process.arch === 'arm64' ? 'arm64' : 'x64';

    if (process.platform === 'darwin')
        return `darwin-${arch}`;

    if (process.platform === 'win32')
        return `win32-${arch}`;

    return isMusl() ? `linux-musl-${arch}` : `linux-${arch}`;
}

/** glibc-based distributions report their glibc version in the process report; musl-based ones (Alpine) do not. */
function isMusl(): boolean {
    try {
        const report = process.report?.getReport() as { header?: { glibcVersionRuntime?: string } } | undefined;
        return report?.header?.glibcVersionRuntime === undefined;
    } catch {
        return false;
    }
}

function libraryDirectory(): string {
    const override = process.env.QUESTPDF_NATIVE_DIR;
    if (override !== undefined && override.length > 0)
        return path.resolve(override);

    // The installed platform package (@questpdf/native-*, an optional dependency of questpdf).
    try {
        const manifest = require.resolve(`@questpdf/native-${platformPackageId()}/package.json`);
        return path.join(path.dirname(manifest), 'native');
    } catch {
        // Fall through to the development layout.
    }

    // dist/manual/native-bridge.js → <package root>/native (development layout, npm run publish-native)
    return path.resolve(__dirname, '..', '..', 'native');
}

function libraryFileName(): string {
    switch (process.platform) {
        case 'darwin': return 'QuestPDF.Native.dylib';
        case 'win32': return 'QuestPDF.Native.dll';
        default: return 'QuestPDF.Native.so';
    }
}

function initialize(): BridgeState {
    const directory = libraryDirectory();
    const libraryPath = path.join(directory, libraryFileName());
    if (!existsSync(libraryPath)) {
        throw new Error(
            `Native library not found at ${libraryPath} — install the @questpdf/native-${platformPackageId()} ` +
            `package, point QUESTPDF_NATIVE_DIR at a published runtime directory, or run 'npm run publish-native' ` +
            `when working inside the repository.`);
    }

    const lib = koffi.load(libraryPath);

    // Handwritten lifecycle exports of QuestPDF.Native (see InteropRuntime.cs).
    const initializeExport = lib.func('QuestPdf_Initialize', 'int32_t', ['str', koffi.pointer(NativeErrorCallback)]);
    const runtime: RuntimeFunctions = {
        releaseHandle: lib.func('QuestPdf_ReleaseHandle', 'void', ['int64_t']),
        registerString: lib.func('QuestPdf_RegisterString', 'int64_t', ['str']),
        registerBuffer: lib.func('QuestPdf_RegisterBuffer', 'int64_t', [koffi.pointer('uint8_t'), 'int32_t']),
        freeString: lib.func('QuestPdf_FreeString', 'void', [koffi.pointer('uint8_t')]),
        freeBuffer: lib.func('QuestPdf_FreeBuffer', 'void', [koffi.pointer('uint8_t')]),
        handleCount: lib.func('QuestPdf_HandleCount', 'int64_t', []),
    };

    errorCallbackRegistration = koffi.register((message: string | null) => {
        pendingNativeError = message ?? 'Unknown native error';
    }, koffi.pointer(NativeErrorCallback));

    const rc = initializeExport(directory, errorCallbackRegistration) as number;
    if (rc !== 0)
        throw new Error(`QuestPdf_Initialize failed with code ${rc}`);

    return { functions: declareNativeFunctions(lib), runtime };
}

function ensure(): BridgeState {
    state ??= initialize();
    return state;
}

export const Native = {
    /** The declared native exports; loading and initialization happen on first access. */
    get lib(): NativeFunctions {
        return ensure().functions;
    },

    /** Rethrows any error recorded while the last native call was running. */
    check(): void {
        if (pendingCallbackError !== null) {
            const error = pendingCallbackError;
            pendingCallbackError = null;
            pendingNativeError = null;
            throw error;
        }

        if (pendingNativeError !== null) {
            const message = pendingNativeError;
            pendingNativeError = null;
            throw new QuestPdfNativeError(message);
        }
    },

    /** [check], then pass the value through — lets bridged bodies stay expressions. */
    checked<T>(value: T): T {
        Native.check();
        return value;
    },

    /**
     * Registers a JS function as a native callback of the given prototype and
     * keeps it strongly reachable so its trampoline stays valid: the .NET side
     * may hold the function pointer across calls.
     */
    retain(prototype: koffi.IKoffiCType, fn: (...args: never[]) => unknown): koffi.IKoffiRegisteredCallback {
        const registered = koffi.register(fn, koffi.pointer(prototype));
        retained.push(registered);
        return registered;
    },

    /**
     * Runs a user callback inside a native callback. A thrown exception must
     * not unwind through native frames, so it is recorded and rethrown by the
     * next [check]; the native side sees the fallback value.
     */
    guard<T>(fallback: T, block: () => T): T {
        try {
            return block();
        } catch (error) {
            pendingCallbackError ??= error;
            return fallback;
        }
    },

    /** Reads and frees a native UTF-8 string returned by an export. */
    takeString(pointer: unknown): string | null {
        if (pointer === null || pointer === undefined)
            return null;
        // 'char' with length -1 reads the NUL-terminated UTF-8 string at the
        // pointer ('str' would instead dereference it as a char* variable).
        const value = koffi.decode(pointer, 'char', -1) as string;
        ensure().runtime.freeString(pointer);
        return value;
    },

    /** Copies and frees a native buffer returned through out-parameters. */
    takeBuffer(pointer: unknown, length: number): Uint8Array {
        if (pointer === null || pointer === undefined || length === 0)
            return new Uint8Array(0);
        const bytes = koffi.decode(pointer, koffi.array('uint8_t', length, 'Typed')) as Uint8Array;
        ensure().runtime.freeBuffer(pointer);
        return bytes;
    },

    /** Registers a string as a transfer handle (callback results crossing to .NET). */
    registerString(value: string): number {
        return ensure().runtime.registerString(value) as number;
    },

    /** Registers a byte array as a transfer handle (callback results crossing to .NET). */
    registerBuffer(value: Uint8Array): number {
        return ensure().runtime.registerBuffer(value, value.length) as number;
    },

    /** Releases one handle of the native table (called by the finalization registry). */
    releaseHandle(handle: number): void {
        if (state !== null)
            state.runtime.releaseHandle(handle);
    },

    /** Number of live handles in the native table (diagnostics). */
    nativeHandleCount(): number {
        return ensure().runtime.handleCount() as number;
    },

    /**
     * Drops the registrations keeping callback trampolines alive. Only call
     * this when no document created so far will be generated again.
     */
    releaseRetainedCallbacks(): void {
        for (const callback of retained.splice(0))
            koffi.unregister(callback);
    },
};
