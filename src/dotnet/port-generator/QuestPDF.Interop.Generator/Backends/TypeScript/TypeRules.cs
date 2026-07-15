using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>
/// The TypeScript backend's ordered type-level rule list; the first matching
/// rule decides how a type maps to TypeScript. New special cases are added as
/// new rules, never as per-type config.
/// </summary>
public static class TypeScriptTypeRules
{
    public static IReadOnlyList<ITypeRule> All { get; } =
    [
        new TsManualOverrideTypeRule(),
        new TsGenericArityCollisionRule(),
        new TsDelegateTypeAliasRule(),
        new TsEnumTypeRule(),
        new TsExceptionTypeRule(),
        new TsExtensionHolderDissolveRule(),
        new TsStaticClassRule(),
        new TsInterfaceTypeRule(),
        new TsClassOrStructRule(),
        new TsUnsupportedTypeFallbackRule(),
    ];
}

public sealed class TsManualOverrideTypeRule : ITypeRule
{
    public string Name => "manual-override";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        overrides.Contains(type.DocId);

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.ManualOverride, "excluded via the manual-overrides file; implemented by hand in the manual/ source set", Name);
}

/// <summary>
/// C# allows IFoo and IFoo&lt;T&gt; side by side; TypeScript forbids same-name
/// declarations differing only in type-parameter arity. The non-generic variant
/// wins (it is the one the plain fluent API consumes).
/// </summary>
public sealed class TsGenericArityCollisionRule : ITypeRule
{
    public string Name => "generic-arity-collision";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.TypeParameters.Count > 0 &&
        index.Assembly.Types.Any(t => t.FullName == type.FullName && t.TypeParameters.Count == 0);

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Unsupported,
            $"TypeScript forbids declaring {type.Name} and {type.Name}<{string.Join(", ", type.TypeParameters.Select(p => p.Name))}> with the same name; the non-generic variant is generated",
            Name);
}

public sealed class TsDelegateTypeAliasRule : ITypeRule
{
    public string Name => "delegate-to-typealias";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.Delegate;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides)
    {
        if (type.DelegateInfo is null)
            return new TypePlan(type, TypePlanKind.Unsupported, "delegate without an Invoke signature", Name);

        return new TypePlan(type, TypePlanKind.TypeAlias, "delegate mapped to a TypeScript function-type alias", Name);
    }
}

public sealed class TsEnumTypeRule : ITypeRule
{
    public string Name => "enum";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.Enum;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Enum, "C# enum mapped to a TypeScript constant object with a literal union type", Name);
}

public sealed class TsExceptionTypeRule : ITypeRule
{
    public string Name => "exception";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.BaseType is { } baseType &&
        (baseType.Is("System.Exception") || baseType.Is("System.SystemException") || baseType.Is("System.ApplicationException"));

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Class, "exception type extending Error", Name);
}

public sealed class TsExtensionHolderDissolveRule : ITypeRule
{
    public string Name => "extension-holder-dissolve";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.StaticClass &&
        type.Methods.Count > 0 &&
        type.Methods.All(m => m.IsExtension) &&
        type.Properties.Count == 0 &&
        type.Fields.Count == 0;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Dissolve, "static extension holder; methods become members on their receiver types", Name);
}

public sealed class TsStaticClassRule : ITypeRule
{
    public string Name => "static-class-to-holder";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.StaticClass;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.StaticHolder, "static class mapped to a TypeScript class with static members", Name);
}

public sealed class TsInterfaceTypeRule : ITypeRule
{
    public string Name => "interface";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.Interface;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Interface, "interface mapped to a TypeScript interface (members implemented on the generated wrapper class)", Name);
}

public sealed class TsClassOrStructRule : ITypeRule
{
    public string Name => "class";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind is ApiTypeKind.Class or ApiTypeKind.Struct;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Class,
            type.Kind == ApiTypeKind.Struct ? "struct mapped to a TypeScript class" : "class mapped to a TypeScript class",
            Name);
}

public sealed class TsUnsupportedTypeFallbackRule : ITypeRule
{
    public string Name => "unsupported-fallback";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) => true;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Unsupported, $"no rule can map a {type.Kind} type", Name);
}
