using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>
/// The classified TypeScript-level model (Stage 2 output). Every record here is
/// a mapping DECISION — what to emit — while the emitters (Stage 3) only render
/// these decisions as text. No reflection, no emission logic.
/// </summary>
public sealed record TsModel(
    IReadOnlyList<TsDeclaration> Declarations,
    IReadOnlyList<ReportEntry> Report,
    IReadOnlyList<NativeExport> NativeExports,
    IReadOnlyList<CallbackShape> CallbackShapes,
    IReadOnlyList<ProxyPlan> Proxies,
    string SourceAssemblyName,
    string SourceAssemblyVersion);

public abstract record TsDeclaration
{
    /// <summary>Directory module of the generated file, e.g. "questpdf/fluent".</summary>
    public required string ModulePath { get; init; }
    public required string Name { get; init; }
    public string? Doc { get; init; }
    public string? DeprecationMessage { get; init; }
    public required string SourceDocId { get; init; }
}

public enum TsTypeKind
{
    Class,
    Interface,
    /// <summary>Static class: a TypeScript class with a private constructor and static members only.</summary>
    StaticHolder,
    Enum,
}

public sealed record TsTypeDeclaration : TsDeclaration
{
    public required TsTypeKind Kind { get; init; }
    public bool IsAbstract { get; init; }

    /// <summary>Wraps a native handle: classes extend NativeObject (or a generated base), interfaces get an Impl class.</summary>
    public bool IsHandleBacked { get; init; }

    /// <summary>Interface declares <c>readonly nativeHandle: number</c> itself (no API superinterface carries it).</summary>
    public bool DeclaresNativeHandle { get; init; }

    /// <summary>Wrapper class emitted alongside a handle-backed interface.</summary>
    public string? ImplClassName { get; init; }

    /// <summary>
    /// Class the Impl wrapper extends: the Impl of the single handle-backed
    /// superinterface (so inherited members need no re-emission), or NativeObject.
    /// </summary>
    public TsType? ImplBaseClass { get; init; }

    /// <summary>Class this exception type extends (Error) or generated base class; null → NativeObject for handle-backed classes.</summary>
    public TsType? SuperClass { get; init; }
    public IReadOnlyList<TsType> SuperInterfaces { get; init; } = [];

    /// <summary>True for exception types: client-side Error subclass with a message constructor, no bridge.</summary>
    public bool IsException { get; init; }

    /// <summary>The single public constructor bridging to a .NET constructor, when one exists.</summary>
    public TsConstructor? BridgedConstructor { get; init; }

    public IReadOnlyList<TsProperty> Properties { get; init; } = [];
    public IReadOnlyList<TsFunctionGroup> FunctionGroups { get; init; } = [];
    public IReadOnlyList<TsProperty> StaticProperties { get; init; } = [];
    public IReadOnlyList<TsFunctionGroup> StaticFunctionGroups { get; init; } = [];
    public IReadOnlyList<TsEnumEntry> EnumEntries { get; init; } = [];
    public IReadOnlyList<TsTypeDeclaration> NestedTypes { get; init; } = [];
}

public sealed record TsTypeAlias : TsDeclaration
{
    public required TsType AliasedType { get; init; }
}

public sealed record TsConstructor(
    IReadOnlyList<TsParameter> Parameters,
    NativeExport Export,
    string? Doc,
    string? DeprecationMessage,
    string SourceDocId);

/// <summary>
/// All same-name functions of one member set. A single-function group renders
/// as an ordinary method; a multi-function group renders as TypeScript overload
/// signatures plus one implementation dispatching on argument count and runtime
/// type tests.
/// </summary>
public sealed record TsFunctionGroup(string Name, IReadOnlyList<TsFunction> Overloads);

public sealed record TsFunction
{
    public required string Name { get; init; }
    /// <summary>Generic type parameters as rendered constraints, e.g. "T extends IComponent".</summary>
    public IReadOnlyList<TsTypeParameter> TypeParameters { get; init; } = [];
    public IReadOnlyList<TsParameter> Parameters { get; init; } = [];
    public required TsType ReturnType { get; init; }
    public required TsBody Body { get; init; }
    /// <summary>Per-parameter runtime kind, aligned with <see cref="Parameters"/>; drives overload dispatch.</summary>
    public IReadOnlyList<RuntimeKind> RuntimeKinds { get; init; } = [];
    public string? Doc { get; init; }
    public string? DeprecationMessage { get; init; }
    public required string SourceDocId { get; init; }
}

public sealed record TsTypeParameter(string Name, IReadOnlyList<TsType> Constraints);

/// <summary>What a generated function body does.</summary>
public abstract record TsBody
{
    /// <summary>No body: interface signature of a user-implemented interface member.</summary>
    public sealed record None : TsBody;

    /// <summary>Marshal arguments, call the native export, check errors, wrap the result.</summary>
    public sealed record Bridge(NativeExport Export) : TsBody;
}

public sealed record TsParameter(
    string Name,
    TsType Type,
    TsExpr? DefaultValue,
    bool IsRest);

public sealed record TsProperty
{
    public required string Name { get; init; }
    public required TsType Type { get; init; }
    public required bool IsMutable { get; init; }

    /// <summary>Client-side literal initializer (compile-time constants).</summary>
    public TsExpr? Initializer { get; init; }

    /// <summary>Bridged accessors; when set the property renders as get/set accessors.</summary>
    public NativeExport? Getter { get; init; }
    public NativeExport? Setter { get; init; }

    /// <summary>Signature-only (user-implemented interface property).</summary>
    public bool IsAbstract { get; init; }
    public string? Doc { get; init; }
    public string? DeprecationMessage { get; init; }
    public required string SourceDocId { get; init; }
}

public sealed record TsEnumEntry(
    string Name,
    long Value,
    string? Doc,
    string? DeprecationMessage,
    string SourceDocId);

/// <summary>A TypeScript expression the classifier decided on (parameter defaults, constants).</summary>
public abstract record TsExpr
{
    public sealed record Null : TsExpr;
    public sealed record Literal(string Text) : TsExpr;
    public sealed record StringLiteral(string Value) : TsExpr;
    public sealed record EnumEntry(TsType EnumType, string EntryName) : TsExpr;
}

/// <summary>
/// JavaScript-runtime class of a parameter value, used to decide whether two
/// overloads can be told apart by a dispatch test and to render that test.
/// </summary>
public enum RuntimeKind
{
    Number,
    String,
    Boolean,
    Function,
    /// <summary>Uint8Array.</summary>
    Bytes,
    Date,
    /// <summary>A JS array (arrays of handles, strings, scalars).</summary>
    ArrayLike,
    /// <summary>A non-array iterable (IEnumerable parameters).</summary>
    Iterable,
    /// <summary>A handle-backed wrapper: instanceof NativeObject.</summary>
    Handle,
    /// <summary>A user-implemented interface value: a plain object exposing the interface method.</summary>
    UserImpl,
    Unknown,
}

/// <summary>
/// A TypeScript type reference: named (with the generated module that declares
/// it, for import computation), array, function or generic parameter.
/// Rendering and import logic live in Stage 3.
/// </summary>
public sealed record TsType
{
    /// <summary>Generated module declaring this type ("questpdf/fluent"), "" for built-ins/manual runtime types.</summary>
    public string ModulePath { get; init; } = "";
    /// <summary>Simple or dotted-nested name ("IContainer", "DocumentOperation.DocumentAttachment").</summary>
    public string Name { get; init; } = "";
    /// <summary>
    /// File (top-level declaration name) exporting this type when it differs
    /// from the name's first segment — Impl wrapper classes are declared in
    /// their interface's file (ContainerImpl lives in IContainer.ts).
    /// </summary>
    public string? DeclaredInFile { get; init; }
    public bool IsNullable { get; init; }
    public IReadOnlyList<TsType> TypeArguments { get; init; } = [];
    public bool IsGenericParameter { get; init; }

    public bool IsArray { get; init; }
    public bool IsReadonlyArray { get; init; }
    public TsType? ElementType { get; init; }

    public bool IsFunctionType { get; init; }
    public IReadOnlyList<TsParameter> FunctionParameters { get; init; } = [];
    public TsType? FunctionReturn { get; init; }

    public static TsType Named(string modulePath, string name, bool nullable = false, IReadOnlyList<TsType>? args = null) =>
        new() { ModulePath = modulePath, Name = name, IsNullable = nullable, TypeArguments = args ?? [] };

    public static TsType Builtin(string name, bool nullable = false) =>
        new() { Name = name, IsNullable = nullable };

    public static TsType Generic(string name, bool nullable = false) =>
        new() { Name = name, IsGenericParameter = true, IsNullable = nullable };

    public static TsType Array(TsType element, bool nullable = false, bool isReadonly = false) =>
        new() { IsArray = true, ElementType = element, IsNullable = nullable, IsReadonlyArray = isReadonly };

    public static TsType Function(IReadOnlyList<TsParameter> parameters, TsType returnType, bool nullable = false) =>
        new() { IsFunctionType = true, FunctionParameters = parameters, FunctionReturn = returnType, IsNullable = nullable };

    public static readonly TsType Void = Builtin("void");

    public bool IsVoid => !IsFunctionType && !IsArray && ModulePath.Length == 0 && Name == "void";

    /// <summary>All named generated types referenced by this type, recursively (for imports).</summary>
    public IEnumerable<TsType> NamedTypesRecursive()
    {
        if (IsFunctionType)
        {
            foreach (var p in FunctionParameters)
                foreach (var t in p.Type.NamedTypesRecursive()) yield return t;
            if (FunctionReturn is not null)
                foreach (var t in FunctionReturn.NamedTypesRecursive()) yield return t;
            yield break;
        }

        if (IsArray)
        {
            foreach (var t in ElementType!.NamedTypesRecursive()) yield return t;
            yield break;
        }

        if (!IsGenericParameter)
            yield return this;

        foreach (var argument in TypeArguments)
            foreach (var t in argument.NamedTypesRecursive())
                yield return t;
    }
}
