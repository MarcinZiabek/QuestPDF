using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Core.Classification;

/// <summary>
/// Set-level rule: overload sets that exist only to simulate optional parameters
/// (same name, shorter parameter list is an exact prefix of the longer, same
/// return type, neither obsolete) collapse into the longest overload, whose
/// extra parameters receive Kotlin default values. Genuine overloads —
/// different parameter types or return types — are kept as overloads.
/// </summary>
public static class OverloadCollapseRule
{
    public const string RuleName = "overload-collapse";

    public static IReadOnlyList<ApiMethod> Apply(
        IReadOnlyList<ApiMethod> methods,
        List<ReportEntry> reports)
    {
        var result = methods.ToList();

        var groups = result
            .Where(m => m.Kind == ApiMethodKind.Ordinary && m.ObsoleteMessage is null)
            .GroupBy(GroupKey)
            .Where(g => g.Count() > 1);

        foreach (var group in groups.ToList())
        {
            var ordered = group.OrderBy(m => m.Parameters.Count).ThenBy(m => m.DocId, StringComparer.Ordinal).ToList();

            for (var shorter = 0; shorter < ordered.Count; shorter++)
            {
                for (var longer = ordered.Count - 1; longer > shorter; longer--)
                {
                    if (!IsPrefixOf(ordered[shorter], ordered[longer]))
                        continue;

                    var absorbed = ordered[shorter];
                    var target = ordered[longer];

                    // Give the longer overload defaults for its tail so callers of
                    // the shorter form keep compiling.
                    var index = result.IndexOf(target);
                    if (index >= 0)
                    {
                        result[index] = target with
                        {
                            Parameters = target.Parameters
                                .Select((p, i) => i >= absorbed.Parameters.Count && p.Default is null
                                    ? p with { Default = new DefaultValue(DefaultValueKind.DefaultStruct, "") }
                                    : p)
                                .ToList(),
                        };
                    }

                    result.Remove(absorbed);
                    reports.Add(new ReportEntry(
                        absorbed.DocId,
                        SignatureRenderer.Render(absorbed),
                        ApiClassification.Generated,
                        $"optional-parameter simulation collapsed into {SignatureRenderer.Render(target)}",
                        RuleName));
                    break;
                }
            }
        }

        return result;
    }

    private static (string, bool, string, bool) GroupKey(ApiMethod method) => (
        method.Name,
        method.IsExtension,
        method.ExtensionReceiver?.Type.Render() ?? "",
        method.IsStatic);

    private static bool IsPrefixOf(ApiMethod shorter, ApiMethod longer)
    {
        if (shorter.Parameters.Count >= longer.Parameters.Count)
            return false;

        if (shorter.ReturnType.Render() != longer.ReturnType.Render())
            return false;

        if (shorter.TypeParameters.Count != longer.TypeParameters.Count)
            return false;

        // Same generic shape: differing constraints (e.g. an extra new()) signal
        // genuinely different behavior, not an optional-parameter simulation.
        for (var i = 0; i < shorter.TypeParameters.Count; i++)
        {
            if (RenderTypeParameter(shorter.TypeParameters[i]) != RenderTypeParameter(longer.TypeParameters[i]))
                return false;
        }

        for (var i = 0; i < shorter.Parameters.Count; i++)
        {
            if (shorter.Parameters[i].Type.Render() != longer.Parameters[i].Type.Render())
                return false;
        }

        // The absorbed callers rely on synthesized defaults for the tail, which
        // is impossible for generic-typed parameters.
        for (var i = shorter.Parameters.Count; i < longer.Parameters.Count; i++)
        {
            if (MentionsGenericParameter(longer.Parameters[i].Type))
                return false;
        }

        return true;
    }

    private static string RenderTypeParameter(ApiTypeParameter parameter) =>
        parameter.Name + ":" +
        string.Join("&", parameter.Constraints.Select(c => c.Render())) +
        (parameter.HasReferenceTypeConstraint ? "&class" : "") +
        (parameter.HasValueTypeConstraint ? "&struct" : "") +
        (parameter.HasDefaultConstructorConstraint ? "&new" : "");

    private static bool MentionsGenericParameter(TypeRef type) => type.Kind switch
    {
        TypeRefKind.GenericParameter => true,
        TypeRefKind.Array => MentionsGenericParameter(type.ElementType!),
        _ => type.TypeArguments.Any(MentionsGenericParameter),
    };
}
