using System.Collections.Concurrent;
using System.Globalization;
using System.Reflection;
using System.Runtime.InteropServices;

namespace QuestPDF.Native;

/// <summary>
/// Handwritten interop runtime shared by all generated exports: the handle
/// table, the error channel (a callback registered from Kotlin — exceptions
/// never cross the native boundary), UTF-8 and buffer helpers, and the
/// lifecycle exports (initialize / release / register / free).
/// </summary>
public static unsafe class Interop
{
    private static string? nativeDirectory;
    private static bool resolverInstalled;
    private static delegate* unmanaged[Cdecl]<byte*, void> errorCallback;

    public static class Handles
    {
        private static readonly ConcurrentDictionary<long, object> Objects = new();
        private static long nextHandle;

        public static long Register(object? value)
        {
            if (value is null)
                return 0;

            var handle = Interlocked.Increment(ref nextHandle);
            Objects[handle] = value;
            return handle;
        }

        public static T Get<T>(long handle)
        {
            if (handle == 0)
                throw new InvalidOperationException($"Null handle passed where a {typeof(T).Name} instance was required.");

            if (!Objects.TryGetValue(handle, out var value))
                throw new InvalidOperationException($"Handle {handle} is not registered (already released?).");

            return (T)value;
        }

        public static void Release(long handle)
        {
            if (handle != 0)
                Objects.TryRemove(handle, out _);
        }

        /// <summary>Resolves and releases a transfer handle (callback string/buffer results).</summary>
        public static object? Take(long handle)
        {
            if (handle == 0)
                return null;

            Objects.TryRemove(handle, out var value);
            return value;
        }

        public static string? TakeString(long handle) => (string?)Take(handle);

        public static byte[]? TakeBuffer(long handle) => (byte[]?)Take(handle);

        public static long Count => Objects.Count;
    }

    // ---- error channel ----

    public static void ReportError(Exception exception)
    {
        var message = exception.ToString();
        var callback = errorCallback;
        if (callback is null)
        {
            Console.Error.WriteLine("[QuestPDF.Native] " + message);
            return;
        }

        var utf8 = AllocUtf8(message);
        try
        {
            callback((byte*)utf8);
        }
        finally
        {
            FreeUtf8(utf8);
        }
    }

    // ---- conversion helpers used by generated exports ----

    public static string? ToStringUtf8(byte* utf8) =>
        utf8 is null ? null : Marshal.PtrToStringUTF8((nint)utf8);

    public static nint AllocUtf8(string? value) =>
        value is null ? 0 : Marshal.StringToCoTaskMemUTF8(value);

    public static void FreeUtf8(nint pointer)
    {
        if (pointer != 0)
            Marshal.FreeCoTaskMem(pointer);
    }

    /// <summary>Allocates a UTF-8 return value the Kotlin side frees via QuestPdf_FreeString.</summary>
    public static nint AllocUtf8Return(string? value) => AllocUtf8(value);

    public static byte[]? ToByteArray(byte* data, int length)
    {
        if (data is null)
            return null;

        var result = new byte[length];
        new ReadOnlySpan<byte>(data, length).CopyTo(result);
        return result;
    }

    public static T[] ToObjectArray<T>(long* handles, int count)
    {
        if (handles is null || count == 0)
            return [];

        var result = new T[count];
        for (var i = 0; i < count; i++)
            result[i] = Handles.Get<T>(handles[i]);
        return result;
    }

    public static float[] ToFloatArray(float* items, int count)
    {
        if (items is null || count == 0)
            return [];

        var result = new float[count];
        new ReadOnlySpan<float>(items, count).CopyTo(result);
        return result;
    }

    public static string[] ToStringArray(byte** items, int count)
    {
        if (items is null || count == 0)
            return [];

        var result = new string[count];
        for (var i = 0; i < count; i++)
            result[i] = ToStringUtf8(items[i]) ?? "";
        return result;
    }

    public static DateTime? ParseDateTime(byte* utf8)
    {
        var text = ToStringUtf8(utf8);
        return text is null ? null : DateTime.Parse(text, CultureInfo.InvariantCulture, DateTimeStyles.RoundtripKind);
    }

    public static DateTimeOffset? ParseDateTimeOffset(byte* utf8)
    {
        var text = ToStringUtf8(utf8);
        return text is null ? null : DateTimeOffset.Parse(text, CultureInfo.InvariantCulture, DateTimeStyles.RoundtripKind);
    }

    public static string? FormatRoundTrip(DateTime? value) =>
        value?.ToString("O", CultureInfo.InvariantCulture);

    public static string? FormatRoundTrip(DateTimeOffset? value) =>
        value?.ToString("O", CultureInfo.InvariantCulture);

    public static void WriteBuffer(byte[]? bytes, byte** outData, int* outLength)
    {
        if (bytes is null || bytes.Length == 0)
        {
            *outData = null;
            *outLength = 0;
            return;
        }

        var memory = (byte*)NativeMemory.Alloc((nuint)bytes.Length);
        bytes.AsSpan().CopyTo(new Span<byte>(memory, bytes.Length));
        *outData = memory;
        *outLength = bytes.Length;
    }

    // ---- lifecycle exports ----

    /// <summary>
    /// Must be called before anything touches QuestPDF. The host process (the
    /// JVM) has its own base directory, so QuestPDF's native Skia dependency
    /// and bundled fonts do not resolve on their own: this installs a resolver
    /// rooted at the publish directory and registers the bundled Lato fonts.
    /// </summary>
    [UnmanagedCallersOnly(EntryPoint = "QuestPdf_Initialize")]
    public static int Initialize(byte* nativeDirectoryUtf8, nint onError)
    {
        try
        {
            errorCallback = (delegate* unmanaged[Cdecl]<byte*, void>)onError;
            nativeDirectory = ToStringUtf8(nativeDirectoryUtf8)
                ?? throw new ArgumentException("native directory must not be null");

            if (!resolverInstalled)
            {
                resolverInstalled = true;
                NativeLibrary.SetDllImportResolver(typeof(global::QuestPDF.Settings).Assembly, ResolveNativeDependency);
            }

            var fontDirectory = Path.Combine(nativeDirectory, "LatoFont");
            if (Directory.Exists(fontDirectory))
            {
                foreach (var fontFile in Directory.GetFiles(fontDirectory, "*.ttf").Order(StringComparer.Ordinal))
                {
                    using var stream = File.OpenRead(fontFile);
                    global::QuestPDF.Drawing.FontManager.RegisterFont(stream);
                }
            }

            return 0;
        }
        catch (Exception exception)
        {
            Console.Error.WriteLine("[QuestPDF.Native] initialization failed: " + exception);
            return 1;
        }
    }

    private static nint ResolveNativeDependency(string libraryName, Assembly assembly, DllImportSearchPath? searchPath)
    {
        if (nativeDirectory is null)
            return 0;

        foreach (var candidate in new[]
                 {
                     libraryName,
                     "lib" + libraryName,
                     libraryName + ".dylib",
                     "lib" + libraryName + ".dylib",
                     libraryName + ".so",
                     "lib" + libraryName + ".so",
                     libraryName + ".dll",
                 })
        {
            var path = Path.Combine(nativeDirectory, candidate);
            if (File.Exists(path) && NativeLibrary.TryLoad(path, out var handle))
                return handle;
        }

        return 0;
    }

    [UnmanagedCallersOnly(EntryPoint = "QuestPdf_ReleaseHandle")]
    public static void ReleaseHandle(long handle) => Handles.Release(handle);

    [UnmanagedCallersOnly(EntryPoint = "QuestPdf_RegisterString")]
    public static long RegisterString(byte* utf8) => Handles.Register(ToStringUtf8(utf8));

    [UnmanagedCallersOnly(EntryPoint = "QuestPdf_RegisterBuffer")]
    public static long RegisterBuffer(byte* data, int length) => Handles.Register(ToByteArray(data, length));

    [UnmanagedCallersOnly(EntryPoint = "QuestPdf_FreeString")]
    public static void FreeString(nint pointer) => FreeUtf8(pointer);

    [UnmanagedCallersOnly(EntryPoint = "QuestPdf_FreeBuffer")]
    public static void FreeBuffer(byte* pointer) => NativeMemory.Free(pointer);

    [UnmanagedCallersOnly(EntryPoint = "QuestPdf_HandleCount")]
    public static long HandleCount() => Handles.Count;
}
