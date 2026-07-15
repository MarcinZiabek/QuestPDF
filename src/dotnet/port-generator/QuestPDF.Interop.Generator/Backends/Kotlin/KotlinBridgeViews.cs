using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// Kotlin-side projections of the language-neutral bridge marshals. The bridge
/// planner records only C#-side identity (full type names); everything Kotlin
/// the emitter needs — the enum's Kotlin type, the wrapper class constructed
/// around a received handle, the java.time type of a temporal value, the
/// method name a proxy forwards to — is derived here, deterministically, from
/// that identity. Must share the classifier's <see cref="ApiIndex"/> and
/// <see cref="TypeMapper"/> instances: type-support marks accumulated during
/// classification affect mapping.
/// </summary>
public sealed class KotlinBridgeViews(ApiIndex index, TypeMapper mapper)
{
    /// <summary>Kotlin enum type of an enum crossing as its underlying int value.</summary>
    public KType EnumType(BridgeMarshal.EnumValue marshal)
    {
        var reference = new TypeRef { Kind = TypeRefKind.Named, FullName = marshal.CSharpEnum, IsApiAssemblyType = true };
        var mapped = mapper.Map(reference);
        return mapped.Success
            ? mapped.Type!
            : throw new InvalidOperationException($"Enum {marshal.CSharpEnum} crossed the bridge but has no Kotlin mapping: {mapped.FailureReason}");
    }

    /// <summary>Kotlin type constructed around a received handle (the Impl class for interfaces).</summary>
    public KType WrapType(BridgeMarshal.Handle marshal)
    {
        var apiType = index.FindType(marshal.CSharpType)
            ?? throw new InvalidOperationException($"Handle type {marshal.CSharpType} crossed the bridge but is not part of the public model.");

        var name = apiType.Kind == ApiTypeKind.Interface
            ? NameMapper.ImplClassName(apiType.Name)
            : index.NestedName(apiType.FullName);

        return KType.Named(NameMapper.Package(apiType.Namespace), name);
    }

    /// <summary>java.time type of a temporal value bridged as round-trip ISO-8601 text.</summary>
    public KType TemporalType(BridgeMarshal.DateTimeText marshal) =>
        KType.Named("java.time", marshal.CSharpType == "System.DateTime" ? "LocalDateTime" : "OffsetDateTime");

    /// <summary>Kotlin member a generated proxy forwards to (IComponent.Compose → compose).</summary>
    public string ProxyMethodName(ProxyPlan proxy) => NameMapper.Member(proxy.CSharpMethodName);
}
