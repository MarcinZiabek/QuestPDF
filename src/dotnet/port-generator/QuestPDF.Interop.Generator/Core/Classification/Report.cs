namespace QuestPDF.Interop.Generator.Core.Classification;

public enum ApiClassification
{
    Generated,
    ManualOverride,
    Unsupported,
    Infrastructure,
}

/// <summary>One classification decision about one public API entry (keyed by doc-comment id).</summary>
public sealed record ReportEntry(
    string DocId,
    string DisplaySignature,
    ApiClassification Classification,
    string Detail,
    string DecidedByRule);
