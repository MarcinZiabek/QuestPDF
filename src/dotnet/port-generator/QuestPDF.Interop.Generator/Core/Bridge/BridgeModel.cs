namespace QuestPDF.Interop.Generator.Core.Bridge;

/// <summary>
/// The interop plan attached to every bridged member. One
/// <see cref="NativeExport"/> describes a single UnmanagedCallersOnly export:
/// how the C# side invokes QuestPDF, and how every value crosses the
/// client ⇄ native boundary. The model is language-neutral — values are
/// identified by their C#-side types only — so the same record drives the
/// shared C# export emission and every language backend's binding emission,
/// which keeps the ABI consistent by construction.
/// </summary>
public sealed class NativeExport
{
    /// <summary>Exported symbol name; assigned by the authoring backend's naming pass.</summary>
    public string EntryPoint { get; private set; } = "";

    /// <summary>Client-side top-level type this export belongs to (grouping for generated C# files).</summary>
    public string Group { get; private set; } = "";

    public required CSharpInvocation Invocation { get; init; }

    /// <summary>Receiver marshal for instance/extension members; null for statics and constructors.</summary>
    public BridgeMarshal.Handle? Receiver { get; init; }

    public required IReadOnlyList<NativeExportParameter> Parameters { get; init; }

    public required BridgeMarshal Return { get; init; }

    public void AssignName(string group, string entryPoint)
    {
        Group = group;
        EntryPoint = entryPoint;
    }
}

/// <summary>One logical export parameter; <see cref="Name"/> names its ABI slot(s) on both sides of the bridge.</summary>
public sealed record NativeExportParameter(string Name, BridgeMarshal Marshal);

public enum InvocationKind
{
    InstanceMethod,
    ExtensionMethod,
    StaticMethod,
    Constructor,
    PropertyGet,
    PropertySet,
    StaticPropertyGet,
    StaticPropertySet,
    /// <summary>Conversion operator towards the declaring type: <c>(Declaring)arg0</c>.</summary>
    ConversionToSelf,
    /// <summary>Conversion operator away from the declaring type: <c>(MemberName)receiver</c> — MemberName carries the C# target type.</summary>
    ConversionFromSelf,
}

/// <summary>
/// How the generated C# export calls into QuestPDF. Type names are C# full
/// names (nested types dotted) rendered with a <c>global::</c> prefix.
/// </summary>
public sealed record CSharpInvocation(
    InvocationKind Kind,
    string DeclaringType,
    string MemberName,
    IReadOnlyList<string> GenericArguments,
    string? ReceiverType);

/// <summary>Scalar kinds that cross the boundary by value.</summary>
public enum ScalarKind
{
    Boolean,
    Byte,
    UByte,
    Short,
    UShort,
    Int,
    UInt,
    Long,
    ULong,
    Float,
    Double,
}

/// <summary>
/// How one value crosses the bridge. A marshal is a decision record: the C#
/// export emitter and each backend's emitters translate it into their side's
/// conversions. API types are referenced by C# full name; how a backend
/// projects them into its own type system is the backend's decision (see the
/// Kotlin backend's KotlinBridgeViews).
/// </summary>
public abstract record BridgeMarshal
{
    /// <summary>Void return.</summary>
    public sealed record Nothing : BridgeMarshal;

    public sealed record Scalar(ScalarKind Kind) : BridgeMarshal;

    /// <summary>Nullable scalar parameter: crosses as (flag byte, value).</summary>
    public sealed record NullableScalar(ScalarKind Kind) : BridgeMarshal;

    /// <summary>API enum crossing as its underlying int value; nullable adds a has-value flag.</summary>
    public sealed record EnumValue(string CSharpEnum, bool Nullable = false) : BridgeMarshal;

    /// <summary>String: UTF-8 pointer in, allocated UTF-8 pointer (freed by the client) out.</summary>
    public sealed record Text(bool Nullable) : BridgeMarshal;

    /// <summary>DateTime/DateTimeOffset bridged as round-trip ("O") formatted text.</summary>
    public sealed record DateTimeText(string CSharpType, bool Nullable) : BridgeMarshal;

    /// <summary>byte[]: pointer+length in; buffer out-parameters (copied then freed by the client) out.</summary>
    public sealed record Blob(bool Nullable) : BridgeMarshal;

    /// <summary>An API object crossing as a <c>long</c> handle into the .NET-side handle table.</summary>
    public sealed record Handle(string CSharpType, bool Nullable) : BridgeMarshal;

    /// <summary>Sequence of handles (params-array/array/enumerable of API objects): long[] + count.</summary>
    public sealed record HandleSequence(Handle Element, SequenceContainer Container) : BridgeMarshal;

    /// <summary>Sequence of strings: string[] + count.</summary>
    public sealed record TextSequence(SequenceContainer Container) : BridgeMarshal;

    /// <summary>Array of scalars (float[] dash patterns): typed array + count.</summary>
    public sealed record ScalarArray(ScalarKind Kind, SequenceContainer Container) : BridgeMarshal;

    /// <summary>Function-typed parameter: a client function pointer the C# side wraps in a delegate.</summary>
    public sealed record CallbackValue(
        CallbackShape Shape,
        CallbackAdapter Adapter,
        bool Nullable,
        string CSharpDelegateType,
        IReadOnlyList<string> CSharpParameterTypes,
        string? CSharpReturnType) : BridgeMarshal;

    /// <summary>
    /// A user-implemented API interface (IComponent). The client creates a
    /// native proxy from the user object's methods and passes the proxy handle.
    /// </summary>
    public sealed record UserImplValue(
        ProxyPlan Proxy,
        bool Nullable) : BridgeMarshal;
}

/// <summary>Client-side container shape a sequence parameter was declared with.</summary>
public enum SequenceContainer
{
    Vararg,
    Array,
    Iterable,
    Collection,
}

/// <summary>ABI slot of one native parameter or return value.</summary>
public enum AbiSlot
{
    Byte,
    Short,
    Int,
    Long,
    Float,
    Double,
    /// <summary>const char* (UTF-8), owned by the caller for the duration of the call.</summary>
    Text,
    /// <summary>byte* — always followed by an Int length slot.</summary>
    Bytes,
    /// <summary>float* — always followed by an Int length slot.</summary>
    FloatArray,
    /// <summary>long* — always followed by an Int length slot.</summary>
    LongArray,
    /// <summary>char** (UTF-8 elements) — always followed by an Int length slot.</summary>
    TextArray,
    /// <summary>Function pointer (client callback trampoline; C# IntPtr).</summary>
    Callback,
    /// <summary>Allocated UTF-8 return the client copies and then frees through the free-export.</summary>
    TextReturn,
    /// <summary>Buffer out-parameters: byte** + int* appended after the declared parameters.</summary>
    BufferOut,
    Void,
}

public sealed record AbiParameter(string Name, AbiSlot Slot, CallbackShape? Shape = null);

/// <summary>
/// The deduplicated ABI shape of a callback (each backend generates one
/// callback type per shape). Parameter slots are restricted to scalars,
/// handles and incoming text.
/// </summary>
public sealed record CallbackShape(string Name, IReadOnlyList<AbiSlot> ParameterSlots, AbiSlot ReturnSlot);

/// <summary>
/// How a client function value (or user object method) adapts to a callback
/// shape, and symmetrically how the C# delegate invokes the function pointer.
/// Wraps are aligned with the shape's logical parameters (before slot
/// expansion). <see cref="FirstParameterIsDslScope"/> records that the single
/// logical parameter is a DSL scope object — backends with receiver-style
/// lambdas (Kotlin) surface it as the lambda receiver; other backends may
/// ignore the flag.
/// </summary>
public sealed record CallbackAdapter(
    CallbackShape Shape,
    IReadOnlyList<BridgeMarshal> ParameterWraps,
    BridgeMarshal ReturnWrap,
    bool FirstParameterIsDslScope);

/// <summary>
/// A generated .NET proxy class implementing a user-implementable QuestPDF
/// interface by forwarding to a callback supplied by the client.
/// </summary>
public sealed record ProxyPlan(
    string ProxyExportEntryPoint,
    string CSharpInterface,
    string CSharpClassName,
    string CSharpMethodName,
    IReadOnlyList<string> CSharpParameterTypes,
    string? CSharpReturnType,
    CallbackAdapter Method);

public sealed class BridgePlanningException(string message) : Exception(message);
