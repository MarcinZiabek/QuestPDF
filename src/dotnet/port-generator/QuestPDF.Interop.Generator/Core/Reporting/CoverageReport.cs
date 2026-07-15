using System.Text;
using QuestPDF.Interop.Generator.Core.Classification;

namespace QuestPDF.Interop.Generator.Core.Reporting;

/// <summary>Backend-specific wording of the coverage report.</summary>
/// <param name="Title">Report heading (without the leading "# ").</param>
/// <param name="GeneratedMeaning">Meaning column for generated entries, e.g. where they are emitted.</param>
/// <param name="ManualOverrideMeaning">Meaning column for manual overrides, e.g. where hand-written code lives.</param>
public sealed record CoverageReportLabels(
    string Title,
    string GeneratedMeaning,
    string ManualOverrideMeaning);

/// <summary>
/// Renders a backend's committed coverage report: every public type and member
/// of QuestPDF classified as generated / manual-override / unsupported (with
/// reason), with a summary percentage at the top. Each backend's report is the
/// source of truth for its remaining manual work. Deliberately timestamp-free
/// so re-runs only diff when the classification really changes.
/// </summary>
public static class CoverageReport
{
    public static string Render(
        CoverageReportLabels labels,
        IReadOnlyList<ReportEntry> report,
        string sourceAssemblyName,
        string sourceAssemblyVersion)
    {
        // A member may have several entries (e.g. an emission dropped later by
        // a backend's signature dedup); the effective classification is the
        // most severe one.
        var effective = report
            .GroupBy(r => r.DocId)
            .Select(group => group
                .OrderByDescending(r => Severity(r.Classification))
                .First())
            .OrderBy(r => r.DocId, StringComparer.Ordinal)
            .ToList();

        var generated = effective.Count(r => r.Classification == ApiClassification.Generated);
        var manual = effective.Count(r => r.Classification == ApiClassification.ManualOverride);
        var unsupported = effective.Count(r => r.Classification == ApiClassification.Unsupported);
        var infrastructure = effective.Count(r => r.Classification == ApiClassification.Infrastructure);
        var denominator = generated + manual + unsupported;
        var percentage = denominator == 0 ? 0 : 100.0 * generated / denominator;

        var sb = new StringBuilder();
        sb.AppendLine($"# {labels.Title}");
        sb.AppendLine();
        sb.AppendLine($"Source: **{sourceAssemblyName} {sourceAssemblyVersion}**. Regenerate with `dotnet run --project generator`.");
        sb.AppendLine();
        sb.AppendLine("## Summary");
        sb.AppendLine();
        sb.AppendLine($"**Coverage: {percentage:0.0}% generated** ({generated} of {denominator} public API entries; percentage excludes the {infrastructure} infrastructure entries).");
        sb.AppendLine();
        sb.AppendLine("| Classification | Count | Meaning |");
        sb.AppendLine("|---|---:|---|");
        sb.AppendLine($"| generated | {generated} | {labels.GeneratedMeaning} |");
        sb.AppendLine($"| manual-override | {manual} | {labels.ManualOverrideMeaning} |");
        sb.AppendLine($"| unsupported | {unsupported} | skipped with a reason (see below) |");
        sb.AppendLine($"| infrastructure | {infrastructure} | equality/formatting plumbing outside the DSL surface (excluded from the percentage) |");
        sb.AppendLine();

        var unsupportedGroups = effective
            .Where(r => r.Classification == ApiClassification.Unsupported)
            .GroupBy(r => r.Detail)
            .OrderByDescending(g => g.Count())
            .ThenBy(g => g.Key, StringComparer.Ordinal)
            .ToList();

        if (unsupportedGroups.Count > 0)
        {
            sb.AppendLine("## Unsupported, by reason");
            sb.AppendLine();
            foreach (var group in unsupportedGroups)
            {
                sb.AppendLine($"- **{group.Count()}× — {group.Key}**");
                foreach (var entry in group.OrderBy(e => e.DocId, StringComparer.Ordinal))
                    sb.AppendLine($"  - `{entry.DisplaySignature}`");
            }
            sb.AppendLine();
        }

        var manualEntries = effective.Where(r => r.Classification == ApiClassification.ManualOverride).ToList();
        if (manualEntries.Count > 0)
        {
            sb.AppendLine("## Manual overrides");
            sb.AppendLine();
            foreach (var entry in manualEntries)
                sb.AppendLine($"- `{entry.DisplaySignature}`");
            sb.AppendLine();
        }

        sb.AppendLine("## Full classification");
        sb.AppendLine();
        sb.AppendLine("| Signature | Classification | Rule | Detail |");
        sb.AppendLine("|---|---|---|---|");
        foreach (var entry in effective)
        {
            sb.Append("| `").Append(entry.DisplaySignature.Replace("|", "\\|"))
              .Append("` | ").Append(Label(entry.Classification))
              .Append(" | ").Append(entry.DecidedByRule)
              .Append(" | ").Append(entry.Detail.Replace("|", "\\|"))
              .AppendLine(" |");
        }

        return sb.ToString();
    }

    private static int Severity(ApiClassification classification) => classification switch
    {
        ApiClassification.Unsupported => 3,
        ApiClassification.ManualOverride => 2,
        ApiClassification.Infrastructure => 1,
        _ => 0,
    };

    private static string Label(ApiClassification classification) => classification switch
    {
        ApiClassification.Generated => "generated",
        ApiClassification.ManualOverride => "manual-override",
        ApiClassification.Unsupported => "unsupported",
        _ => "infrastructure",
    };
}
