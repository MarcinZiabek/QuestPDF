using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// The classified Kotlin-level model (Stage 2 output). Every record here is a
/// mapping DECISION — what to emit — while the emitters (Stage 3) only render
/// these decisions as text. No reflection, no emission logic.
/// </summary>
public sealed record KotlinModel(
    IReadOnlyList<KotlinDeclaration> Declarations,
    IReadOnlyList<ReportEntry> Report,
    IReadOnlyList<NativeExport> NativeExports,
    IReadOnlyList<CallbackShape> CallbackShapes,
    IReadOnlyList<ProxyPlan> Proxies,
    string SourceAssemblyName,
    string SourceAssemblyVersion);

public abstract record KotlinDeclaration
{
    public required string PackageName { get; init; }
    public required string Name { get; init; }
    public string? KDoc { get; init; }
    public string? DeprecationMessage { get; init; }
    public required string SourceDocId { get; init; }
}

public enum KotlinTypeKind
{
    Class,
    Interface,
    Object,
    Enum,
}

public sealed record KotlinTypeDeclaration : KotlinDeclaration
{
    public required KotlinTypeKind Kind { get; init; }
    public bool IsAbstract { get; init; }
    public bool IsOpen { get; init; }
    public IReadOnlyList<string> TypeParameters { get; init; } = [];

    /// <summary>
    /// Handle-backed types wrap a native object: classes get an internal
    /// <c>(handle: Long)</c> primary constructor (rooted at NativeObject),
    /// interfaces carry an abstract <c>nativeHandle</c> and an Impl class.
    /// </summary>
    public bool IsHandleBacked { get; init; }

    /// <summary>Interface declares <c>val nativeHandle: Long</c> itself (no API superinterface carries it).</summary>
    public bool DeclaresNativeHandle { get; init; }

    /// <summary>Internal wrapper class emitted alongside a handle-backed interface.</summary>
    public string? ImplClassName { get; init; }

    public KType? SuperClass { get; init; }
    /// <summary>Superclass constructor argument list, e.g. <c>(handle)</c>; null when none.</summary>
    public string? SuperClassCall { get; init; }
    public IReadOnlyList<KType> SuperInterfaces { get; init; } = [];

    /// <summary>Client-side primary constructor (exception types only — handle-backed classes use the handle constructor).</summary>
    public KotlinConstructor? PrimaryConstructor { get; init; }

    /// <summary>Public constructors bridging to .NET constructors.</summary>
    public IReadOnlyList<KotlinSecondaryConstructor> SecondaryConstructors { get; init; } = [];

    public IReadOnlyList<KotlinProperty> Properties { get; init; } = [];
    public IReadOnlyList<KotlinFunction> Functions { get; init; } = [];
    public IReadOnlyList<KotlinProperty> CompanionProperties { get; init; } = [];
    public IReadOnlyList<KotlinFunction> CompanionFunctions { get; init; } = [];
    public IReadOnlyList<KotlinEnumEntry> EnumEntries { get; init; } = [];
    public IReadOnlyList<KotlinTypeDeclaration> NestedTypes { get; init; } = [];
}

public sealed record KotlinTypeAlias : KotlinDeclaration
{
    public required KType AliasedType { get; init; }
}

public sealed record KotlinConstructor(
    IReadOnlyList<KotlinParameter> Parameters,
    bool IsInternal);

public sealed record KotlinSecondaryConstructor(
    IReadOnlyList<KotlinParameter> Parameters,
    NativeExport Export,
    string? KDoc,
    string? DeprecationMessage,
    string SourceDocId,
    bool IsJvmOverloads = false);

public sealed record KotlinFunction
{
    public required string Name { get; init; }
    public IReadOnlyList<string> TypeParameters { get; init; } = [];
    public IReadOnlyList<KotlinTypeConstraint> TypeConstraints { get; init; } = [];
    public IReadOnlyList<KotlinParameter> Parameters { get; init; } = [];
    public required KType ReturnType { get; init; }
    public required KotlinBody Body { get; init; }
    public bool IsAbstract { get; init; }
    public bool IsOpen { get; init; }
    public bool IsOverride { get; init; }
    /// <summary>Render @JvmStatic (object/companion members, for Java callers).</summary>
    public bool IsJvmStatic { get; init; }
    /// <summary>Render @JvmOverloads (defaulted parameters, for Java callers).</summary>
    public bool IsJvmOverloads { get; init; }
    public string? KDoc { get; init; }
    public string? DeprecationMessage { get; init; }
    public required string SourceDocId { get; init; }
}

/// <summary>What a generated function body does.</summary>
public abstract record KotlinBody
{
    /// <summary>No body: abstract member (user-implemented interfaces).</summary>
    public sealed record None : KotlinBody;

    /// <summary>Marshal arguments, call the native export, check errors, wrap the result.</summary>
    public sealed record Bridge(NativeExport Export) : KotlinBody;

    /// <summary>
    /// Calls a sibling overload (Java-friendliness synthesis): supplies the
    /// default values of dropped parameters and adapts SAM handlers.
    /// </summary>
    public sealed record Delegate(string TargetName, IReadOnlyList<KDelegateArg> Arguments) : KotlinBody;
}

/// <summary>One argument a synthesized delegating overload passes to its target.</summary>
public abstract record KDelegateArg
{
    /// <summary>Forwards the overload's own parameter (spread when vararg).</summary>
    public sealed record Ref(string Name, bool Spread) : KDelegateArg;

    /// <summary>Supplies the default value of a parameter the overload dropped.</summary>
    public sealed record Default(KExpr Value) : KDelegateArg;

    /// <summary>Adapts a java.util.function.Consumer parameter to a receiver lambda: <c>{ name.accept(this) }</c>.</summary>
    public sealed record ConsumerAccept(string Name) : KDelegateArg;

    /// <summary>Forwards a parameter through a conversion call: <c>name.toUInt()</c>.</summary>
    public sealed record Converted(string Name, string Conversion) : KDelegateArg;
}

public sealed record KotlinTypeConstraint(string TypeParameter, KType Bound);

public sealed record KotlinParameter(
    string Name,
    KType Type,
    KExpr? DefaultValue,
    bool IsVararg);

public sealed record KotlinProperty
{
    public required string Name { get; init; }
    public required KType Type { get; init; }
    public required bool IsMutable { get; init; }

    /// <summary>Render @JvmStatic (object/companion members, for Java callers).</summary>
    public bool IsJvmStatic { get; init; }

    /// <summary>Client-side literal initializer (compile-time constants).</summary>
    public KExpr? Initializer { get; init; }

    /// <summary>Bridged accessors; when set the property has no backing field.</summary>
    public NativeExport? Getter { get; init; }
    public NativeExport? Setter { get; init; }

    public bool IsAbstract { get; init; }
    public bool IsOverride { get; init; }
    public bool IsConst { get; init; }
    public string? KDoc { get; init; }
    public string? DeprecationMessage { get; init; }
    public required string SourceDocId { get; init; }
}

public sealed record KotlinEnumEntry(
    string Name,
    long Value,
    string? KDoc,
    string? DeprecationMessage,
    string SourceDocId);

/// <summary>A Kotlin expression the classifier decided on (parameter defaults, constants).</summary>
public abstract record KExpr
{
    public sealed record Null : KExpr;
    public sealed record Literal(string Text) : KExpr;
    public sealed record StringLiteral(string Value) : KExpr;
    public sealed record EnumEntry(KType EnumType, string EntryName) : KExpr;
}

/// <summary>
/// A Kotlin type reference: either a named type (with package for import
/// computation) or a function type. Rendering and import logic live in Stage 3.
/// </summary>
public sealed record KType
{
    public string PackageName { get; init; } = "";
    public string Name { get; init; } = "";
    public bool IsNullable { get; init; }
    public IReadOnlyList<KType> TypeArguments { get; init; } = [];
    public bool IsGenericParameter { get; init; }
    /// <summary>Never import this type; always render its fully qualified name (kotlin.Unit clash guard).</summary>
    public bool ForceQualified { get; init; }

    public bool IsFunctionType { get; init; }
    public KType? LambdaReceiver { get; init; }
    public IReadOnlyList<KotlinParameter> FunctionParameters { get; init; } = [];
    public KType? FunctionReturn { get; init; }

    public static KType Named(string packageName, string name, bool nullable = false, IReadOnlyList<KType>? args = null) =>
        new() { PackageName = packageName, Name = name, IsNullable = nullable, TypeArguments = args ?? [] };

    public static KType Generic(string name, bool nullable = false) =>
        new() { Name = name, IsGenericParameter = true, IsNullable = nullable };

    public static KType Function(KType? receiver, IReadOnlyList<KotlinParameter> parameters, KType returnType, bool nullable = false) =>
        new() { IsFunctionType = true, LambdaReceiver = receiver, FunctionParameters = parameters, FunctionReturn = returnType, IsNullable = nullable };

    public static readonly KType Unit = Named("kotlin", "Unit");

    public bool IsKotlinUnit => !IsFunctionType && PackageName == "kotlin" && Name == "Unit";

    public string FullName => PackageName.Length == 0 ? Name : PackageName + "." + Name;

    /// <summary>All named types referenced by this type, recursively (for imports).</summary>
    public IEnumerable<KType> NamedTypesRecursive()
    {
        if (IsFunctionType)
        {
            if (LambdaReceiver is not null)
                foreach (var t in LambdaReceiver.NamedTypesRecursive()) yield return t;
            foreach (var p in FunctionParameters)
                foreach (var t in p.Type.NamedTypesRecursive()) yield return t;
            if (FunctionReturn is not null)
                foreach (var t in FunctionReturn.NamedTypesRecursive()) yield return t;
            yield break;
        }

        if (!IsGenericParameter)
            yield return this;

        foreach (var argument in TypeArguments)
            foreach (var t in argument.NamedTypesRecursive())
                yield return t;
    }
}

