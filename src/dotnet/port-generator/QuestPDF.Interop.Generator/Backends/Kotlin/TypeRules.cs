using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// The Kotlin backend's ordered type-level rule list; the first matching rule
/// decides how a type maps to Kotlin. New special cases are added as new
/// rules, never as per-type config.
/// </summary>
public static class KotlinTypeRules
{
    public static IReadOnlyList<ITypeRule> All { get; } =
    [
        new ManualOverrideTypeRule(),
        new GenericArityCollisionRule(),
        new DelegateTypeAliasRule(),
        new EnumTypeRule(),
        new ExceptionTypeRule(),
        new ExtensionHolderDissolveRule(),
        new StaticClassObjectRule(),
        new InterfaceTypeRule(),
        new ClassOrStructRule(),
        new UnsupportedTypeFallbackRule(),
    ];
}

public sealed class ManualOverrideTypeRule : ITypeRule
{
    public string Name => "manual-override";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        overrides.Contains(type.DocId);

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.ManualOverride, "excluded via manual-overrides.txt; implemented by hand in the manual/ source set", Name);
}

/// <summary>
/// C# allows IFoo and IFoo&lt;T&gt; side by side; Kotlin forbids same-name
/// declarations differing only in type-parameter arity. The non-generic variant
/// wins (it is the one the plain fluent API consumes).
/// </summary>
public sealed class GenericArityCollisionRule : ITypeRule
{
    public string Name => "generic-arity-collision";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.TypeParameters.Count > 0 &&
        index.Assembly.Types.Any(t => t.FullName == type.FullName && t.TypeParameters.Count == 0);

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Unsupported,
            $"Kotlin forbids declaring {type.Name} and {type.Name}<{string.Join(", ", type.TypeParameters.Select(p => p.Name))}> in one package; the non-generic variant is generated",
            Name);
}

public sealed class DelegateTypeAliasRule : ITypeRule
{
    public string Name => "delegate-to-typealias";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.Delegate;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides)
    {
        if (type.DelegateInfo is null)
            return new TypePlan(type, TypePlanKind.Unsupported, "delegate without an Invoke signature", Name);

        return new TypePlan(type, TypePlanKind.TypeAlias, "delegate mapped to a Kotlin function-type alias", Name);
    }
}

public sealed class EnumTypeRule : ITypeRule
{
    public string Name => "enum";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.Enum;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Enum, "C# enum mapped to a Kotlin enum class", Name);
}

public sealed class ExceptionTypeRule : ITypeRule
{
    public string Name => "exception";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.BaseType is { } baseType &&
        (baseType.Is("System.Exception") || baseType.Is("System.SystemException") || baseType.Is("System.ApplicationException"));

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Class, "exception type extending kotlin.Exception", Name);
}

public sealed class ExtensionHolderDissolveRule : ITypeRule
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

public sealed class StaticClassObjectRule : ITypeRule
{
    public string Name => "static-class-to-object";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.StaticClass;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.StaticHolder, "static class mapped to a Kotlin object", Name);
}

public sealed class InterfaceTypeRule : ITypeRule
{
    public string Name => "interface";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind == ApiTypeKind.Interface;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Interface, "interface mapped to a Kotlin interface (extension methods become default-bodied members)", Name);
}

public sealed class ClassOrStructRule : ITypeRule
{
    public string Name => "class";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        type.Kind is ApiTypeKind.Class or ApiTypeKind.Struct;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Class,
            type.Kind == ApiTypeKind.Struct ? "struct mapped to a Kotlin class" : "class mapped to a Kotlin class",
            Name);
}

public sealed class UnsupportedTypeFallbackRule : ITypeRule
{
    public string Name => "unsupported-fallback";

    public bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides) => true;

    public TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides) =>
        new(type, TypePlanKind.Unsupported, $"no rule can map a {type.Kind} type", Name);
}
