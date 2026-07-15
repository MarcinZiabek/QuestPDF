using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Core.Classification;

/// <summary>
/// How a C# type is represented client-side. The kinds are language-neutral
/// shape decisions; each backend renders them in its own idiom (a
/// <see cref="StaticHolder"/> becomes a Kotlin object, a Python class of
/// classmethods, a TypeScript namespace, …).
/// </summary>
public enum TypePlanKind
{
    Class,
    Interface,
    /// <summary>Static member holder that is never instantiated.</summary>
    StaticHolder,
    Enum,
    /// <summary>Delegate type surfaced as a function-type alias.</summary>
    TypeAlias,
    /// <summary>Extension-method holder: its methods are distributed to receiver types, the type itself vanishes.</summary>
    Dissolve,
    ManualOverride,
    Unsupported,
}

public sealed record TypePlan(
    ApiType Type,
    TypePlanKind Kind,
    string Detail,
    string DecidedByRule)
{
    public bool Emits => Kind is TypePlanKind.Class or TypePlanKind.Interface or TypePlanKind.StaticHolder
        or TypePlanKind.Enum or TypePlanKind.TypeAlias;
}

public interface ITypeRule
{
    string Name { get; }
    bool Matches(ApiType type, ApiIndex index, ManualOverrides overrides);
    TypePlan Apply(ApiType type, ApiIndex index, ManualOverrides overrides);
}

/// <summary>
/// Runs an ordered type-level rule list — the first matching rule decides how
/// a type maps. Backends own their rule lists (and the rules' wording); new
/// special cases are added as new rules, never as per-type config.
/// </summary>
public static class TypeRulePipeline
{
    public static TypePlan Classify(ApiType type, ApiIndex index, ManualOverrides overrides, IReadOnlyList<ITypeRule> rules)
    {
        foreach (var rule in rules)
        {
            if (rule.Matches(type, index, overrides))
                return rule.Apply(type, index, overrides);
        }

        throw new InvalidOperationException($"No type rule matched {type.FullName} (a fallback rule must always match).");
    }
}
