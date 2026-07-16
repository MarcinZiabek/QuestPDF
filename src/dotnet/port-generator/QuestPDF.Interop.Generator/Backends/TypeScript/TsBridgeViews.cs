using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>
/// TypeScript-side projections of the language-neutral bridge marshals. The
/// bridge planner records only C#-side identity (full type names); everything
/// TypeScript the emitter needs — the enum's TS type, the wrapper class
/// constructed around a received handle, the method name a proxy forwards to —
/// is derived here, deterministically, from that identity. Must share the
/// classifier's <see cref="ApiIndex"/> and <see cref="TsTypeMapper"/> instances:
/// type-support marks accumulated during classification affect mapping.
/// </summary>
public sealed class TsBridgeViews(ApiIndex index, TsTypeMapper mapper)
{
    /// <summary>TypeScript enum type of an enum crossing as its underlying int value.</summary>
    public TsType EnumType(BridgeMarshal.EnumValue marshal)
    {
        var reference = new TypeRef { Kind = TypeRefKind.Named, FullName = marshal.CSharpEnum, IsApiAssemblyType = true };
        var mapped = mapper.Map(reference);
        return mapped.Success
            ? mapped.Type!
            : throw new InvalidOperationException($"Enum {marshal.CSharpEnum} crossed the bridge but has no TypeScript mapping: {mapped.FailureReason}");
    }

    /// <summary>TypeScript class constructed around a received handle (the Impl class for interfaces).</summary>
    public TsType WrapType(BridgeMarshal.Handle marshal)
    {
        var apiType = index.FindType(marshal.CSharpType)
            ?? throw new InvalidOperationException($"Handle type {marshal.CSharpType} crossed the bridge but is not part of the public model.");

        if (apiType.Kind == ApiTypeKind.Interface)
        {
            // The Impl wrapper is declared in its interface's file.
            return TsType.Named(TsNameMapper.Module(apiType.Namespace), TsNameMapper.ImplClassName(apiType.Name)) with
            {
                DeclaredInFile = apiType.Name,
            };
        }

        return TsType.Named(TsNameMapper.Module(apiType.Namespace), index.NestedName(apiType.FullName));
    }

    /// <summary>TypeScript member a generated proxy forwards to (IComponent.Compose → compose).</summary>
    public string ProxyMethodName(ProxyPlan proxy) => TsNameMapper.Member(proxy.CSharpMethodName);

    /// <summary>
    /// The concrete TypeScript class an overload-dispatch test can check with
    /// instanceof, or null when the handle is interface-typed (interfaces have
    /// no runtime constructor, and user implementations would not pass it).
    /// </summary>
    public TsType? HandleDispatchType(BridgeMarshal.Handle marshal)
    {
        var apiType = index.FindType(marshal.CSharpType);

        return apiType is { Kind: ApiTypeKind.Class }
            ? TsType.Named(TsNameMapper.Module(apiType.Namespace), index.NestedName(apiType.FullName))
            : null;
    }
}
