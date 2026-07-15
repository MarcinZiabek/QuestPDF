using System.Text;

namespace QuestPDF.Interop.Generator.Core.Model;

/// <summary>
/// The extracted API model (Stage 1 output). Plain records, no reflection types,
/// no emission logic. Everything downstream (classification, emission, reporting,
/// snapshots) consumes this model.
/// </summary>
public sealed record ApiAssembly(
    string Name,
    string Version,
    IReadOnlyList<ApiType> Types);

public enum ApiTypeKind
{
    Class,
    StaticClass,
    Interface,
    Struct,
    Enum,
    Delegate,
}

public sealed record ApiType(
    string FullName,
    string Namespace,
    string Name,
    ApiTypeKind Kind,
    bool IsAbstract,
    bool IsSealed,
    IReadOnlyList<ApiTypeParameter> TypeParameters,
    TypeRef? BaseType,
    IReadOnlyList<TypeRef> Interfaces,
    IReadOnlyList<ApiMethod> Constructors,
    IReadOnlyList<ApiMethod> Methods,
    IReadOnlyList<ApiProperty> Properties,
    IReadOnlyList<ApiField> Fields,
    IReadOnlyList<ApiEnumMember> EnumMembers,
    ApiDelegateInfo? DelegateInfo,
    string? DeclaringTypeFullName,
    bool HasInternalConstructorOnly,
    string DocId,
    string? ObsoleteMessage,
    string? RawXmlDoc);

public sealed record ApiTypeParameter(
    string Name,
    IReadOnlyList<TypeRef> Constraints,
    bool HasReferenceTypeConstraint,
    bool HasValueTypeConstraint,
    bool HasDefaultConstructorConstraint);

public enum ApiMethodKind
{
    Ordinary,
    Constructor,
    Operator,
}

public sealed record ApiMethod(
    string Name,
    ApiMethodKind Kind,
    bool IsStatic,
    bool IsAbstract,
    bool IsVirtual,
    bool IsExtension,
    ApiParameter? ExtensionReceiver,
    IReadOnlyList<ApiTypeParameter> TypeParameters,
    IReadOnlyList<ApiParameter> Parameters,
    TypeRef ReturnType,
    string DeclaringTypeFullName,
    string DocId,
    string? ObsoleteMessage,
    string? RawXmlDoc);

public sealed record ApiParameter(
    string Name,
    TypeRef Type,
    DefaultValue? Default,
    bool IsParams,
    bool IsByRef,
    bool IsCallerInfo);

public enum DefaultValueKind
{
    Null,
    Boolean,
    Number,
    String,
    Char,
    EnumMember,
    DefaultStruct,
}

/// <summary>
/// A parameter default captured at extraction time.
/// <see cref="Text"/> is an invariant-culture payload: the literal for numbers,
/// the member name for enums, the raw value for strings/chars, empty otherwise.
/// </summary>
public sealed record DefaultValue(DefaultValueKind Kind, string Text)
{
    public static readonly DefaultValue Null = new(DefaultValueKind.Null, "");
}

public sealed record ApiProperty(
    string Name,
    TypeRef Type,
    bool HasGetter,
    bool HasSetter,
    bool IsStatic,
    bool IsAbstract,
    bool IsIndexer,
    string DeclaringTypeFullName,
    string DocId,
    string? ObsoleteMessage,
    string? RawXmlDoc,
    string? CapturedValue);

public sealed record ApiField(
    string Name,
    TypeRef Type,
    bool IsConst,
    bool IsStatic,
    bool IsReadOnly,
    string DeclaringTypeFullName,
    string DocId,
    string? ObsoleteMessage,
    string? RawXmlDoc,
    string? CapturedValue);

public sealed record ApiEnumMember(
    string Name,
    long Value,
    string DocId,
    string? ObsoleteMessage,
    string? RawXmlDoc);

/// <summary>Signature of a delegate type's Invoke method.</summary>
public sealed record ApiDelegateInfo(
    IReadOnlyList<ApiParameter> Parameters,
    TypeRef ReturnType);

public enum TypeRefKind
{
    Named,
    GenericParameter,
    Array,
}

/// <summary>
/// A type as it appears in a signature. Named types carry a normalized dotted
/// full name (nested '+' replaced with '.', no generic arity suffix); constructed
/// generics carry their arguments in <see cref="TypeArguments"/>.
/// </summary>
public sealed record TypeRef
{
    public required TypeRefKind Kind { get; init; }
    public string FullName { get; init; } = "";
    public string GenericParameterName { get; init; } = "";
    public bool IsNullable { get; init; }
    public IReadOnlyList<TypeRef> TypeArguments { get; init; } = [];
    public TypeRef? ElementType { get; init; }
    public bool IsApiAssemblyType { get; init; }
    public bool IsDelegateType { get; init; }

    public string SimpleName => Kind switch
    {
        TypeRefKind.GenericParameter => GenericParameterName,
        TypeRefKind.Array => (ElementType?.SimpleName ?? "?") + "[]",
        _ => FullName[(FullName.LastIndexOf('.') + 1)..],
    };

    /// <summary>Stable, human-readable rendering used by dumps and reports.</summary>
    public string Render()
    {
        var sb = new StringBuilder();
        RenderTo(sb);
        return sb.ToString();
    }

    private void RenderTo(StringBuilder sb)
    {
        switch (Kind)
        {
            case TypeRefKind.GenericParameter:
                sb.Append(GenericParameterName);
                break;
            case TypeRefKind.Array:
                ElementType!.RenderTo(sb);
                sb.Append("[]");
                break;
            default:
                sb.Append(FullName);
                if (TypeArguments.Count > 0)
                {
                    sb.Append('<');
                    for (var i = 0; i < TypeArguments.Count; i++)
                    {
                        if (i > 0) sb.Append(", ");
                        TypeArguments[i].RenderTo(sb);
                    }
                    sb.Append('>');
                }
                break;
        }

        if (IsNullable)
            sb.Append('?');
    }

    public bool Is(string fullName) => Kind == TypeRefKind.Named && FullName == fullName;
}
