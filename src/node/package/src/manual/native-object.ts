import { Native } from './native-bridge';

/**
 * Marker distinguishing the internal handle-wrapping constructor path from the
 * public bridged constructors on generated classes. Never pass it yourself.
 */
export const NATIVE_HANDLE: unique symbol = Symbol('questpdf.native-handle');

const registry = new FinalizationRegistry<number>((handle) => {
    Native.releaseHandle(handle);
});

/**
 * Base class of every handle-backed generated type: wraps the handle of a .NET
 * object living in the native library's handle table. The handle is released
 * when this wrapper is garbage collected.
 */
export class NativeObject {
    readonly nativeHandle: number;

    constructor(handle: number, _marker?: typeof NATIVE_HANDLE) {
        this.nativeHandle = handle;
        if (handle !== 0)
            registry.register(this, handle);
    }
}
