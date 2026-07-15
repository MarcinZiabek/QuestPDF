namespace QuestPDF.Interop.Generator.Core.Classification;

/// <summary>Doc-comment IDs excluded from generation for hand-written implementation.</summary>
public sealed class ManualOverrides
{
    private readonly HashSet<string> docIds;

    public ManualOverrides(IEnumerable<string> ids) =>
        docIds = new HashSet<string>(ids, StringComparer.Ordinal);

    public static ManualOverrides Empty { get; } = new([]);

    public static ManualOverrides Load(string path)
    {
        if (!File.Exists(path))
            return Empty;

        var ids = File.ReadAllLines(path)
            .Select(line => line.Trim())
            .Where(line => line.Length > 0 && !line.StartsWith('#'));

        return new ManualOverrides(ids);
    }

    public bool Contains(string docId) => docIds.Contains(docId);
}
