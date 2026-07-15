using System.Text;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// Naming conventions: QuestPDF names are preserved, re-cased per Kotlin
/// convention. Types keep PascalCase; methods, parameters and instance
/// properties become lowerCamelCase; constants (const fields, static readonly
/// values, enum entries) keep their original casing, which matches the Kotlin
/// style guide for deeply immutable values.
/// </summary>
public static class NameMapper
{
    private static readonly HashSet<string> HardKeywords =
    [
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if",
        "in", "interface", "is", "null", "object", "package", "return", "super", "this",
        "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
    ];

    /// <summary>
    /// Simple names that kotlin.* auto-imports into every file. Types of ours with
    /// these names (QuestPDF's Unit enum) are always emitted fully qualified.
    /// </summary>
    private static readonly HashSet<string> KotlinDefaultImportNames =
    [
        "Unit", "Any", "Nothing", "String", "Array", "Boolean", "Byte", "Char", "Short",
        "Int", "Long", "Float", "Double", "Number", "Enum", "Throwable", "Comparable",
        "Function", "Pair", "Triple", "Result", "List", "Set", "Map", "Collection",
        "Iterable", "Iterator", "Sequence", "Error", "Exception", "RuntimeException",
        "Lazy", "Annotation", "CharSequence", "Suppress", "Deprecated",
    ];

    public static string Package(string csharpNamespace)
    {
        if (csharpNamespace.Length == 0 || csharpNamespace == "QuestPDF")
            return "com.questpdf";

        var rest = csharpNamespace.StartsWith("QuestPDF.", StringComparison.Ordinal)
            ? csharpNamespace["QuestPDF.".Length..]
            : csharpNamespace;

        var segments = rest.Split('.').Select(s => s.ToLowerInvariant());
        return "com.questpdf." + string.Join(".", segments);
    }

    /// <summary>Member name → lowerCamelCase, splitting on underscores (PDFA_Conformance → pdfaConformance).</summary>
    public static string Member(string name)
    {
        var segments = name.Split('_', StringSplitOptions.RemoveEmptyEntries);
        if (segments.Length == 0)
            return Escape(name);

        var sb = new StringBuilder();
        sb.Append(CamelCase(segments[0]));
        foreach (var segment in segments.Skip(1))
            sb.Append(char.ToUpperInvariant(segment[0])).Append(segment[1..]);

        return Escape(sb.ToString());
    }

    /// <summary>Constants keep their original name (A4, Red, Calibri, Default, PointsPerInch).</summary>
    public static string Constant(string name) => Escape(name);

    public static string Parameter(string name) => Escape(name);

    public static bool CollidesWithKotlinDefaultImport(string simpleName) =>
        KotlinDefaultImportNames.Contains(simpleName);

    /// <summary>Internal wrapper-class name emitted alongside a handle-backed interface (IContainer → ContainerImpl).</summary>
    public static string ImplClassName(string interfaceSimpleName)
    {
        var trimmed = interfaceSimpleName.Length > 1 && interfaceSimpleName[0] == 'I' && char.IsUpper(interfaceSimpleName[1])
            ? interfaceSimpleName[1..]
            : interfaceSimpleName;
        return trimmed + "Impl";
    }

    /// <summary>
    /// Lowercases the leading uppercase run, keeping the last capital when it
    /// starts a new word: "FontSize" → "fontSize", "PDFAConformance" → "pdfaConformance",
    /// "A4" → "a4", "ARCH" → "arch".
    /// </summary>
    private static string CamelCase(string name)
    {
        if (name.Length == 0 || char.IsLower(name[0]))
            return name;

        var upperRun = 0;
        while (upperRun < name.Length && char.IsUpper(name[upperRun]))
            upperRun++;

        if (upperRun == name.Length)
            return name.ToLowerInvariant();

        if (!char.IsLower(name[upperRun]))
            return name[..upperRun].ToLowerInvariant() + name[upperRun..];

        // Leading acronym followed by a lowercase word: keep the acronym's last
        // letter as the start of that word ("PDFAConformance" → "pdfa" + "Conformance").
        if (upperRun > 1)
            return name[..(upperRun - 1)].ToLowerInvariant() + name[(upperRun - 1)..];

        return char.ToLowerInvariant(name[0]) + name[1..];
    }

    private static string Escape(string name) =>
        HardKeywords.Contains(name) ? "`" + name + "`" : name;
}
