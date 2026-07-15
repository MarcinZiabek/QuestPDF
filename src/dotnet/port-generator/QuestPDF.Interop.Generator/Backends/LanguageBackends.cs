using QuestPDF.Interop.Generator.Backends.Kotlin;
using QuestPDF.Interop.Generator.Backends.TypeScript;
using QuestPDF.Interop.Generator.Core;

namespace QuestPDF.Interop.Generator.Backends;

/// <summary>
/// Registry of every language backend the generator can produce. New client
/// languages (Python, …) are added here once they implement
/// <see cref="ILanguageBackend"/>.
/// </summary>
public static class LanguageBackends
{
    public static IReadOnlyList<ILanguageBackend> All { get; } = [new KotlinBackend(), new TypeScriptBackend()];

    public static ILanguageBackend? Find(string id) =>
        All.FirstOrDefault(b => string.Equals(b.Id, id, StringComparison.OrdinalIgnoreCase));
}
