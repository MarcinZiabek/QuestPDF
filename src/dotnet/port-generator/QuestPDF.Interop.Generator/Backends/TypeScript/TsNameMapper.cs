using System.Text;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>
/// Naming conventions: QuestPDF names are preserved, re-cased per TypeScript
/// convention. Types keep PascalCase; methods, parameters and instance
/// properties become lowerCamelCase; constants (const fields, static readonly
/// values, enum entries) keep their original casing.
/// </summary>
public static class TsNameMapper
{
    /// <summary>Words that cannot be used as parameter/variable names in strict-mode TypeScript.</summary>
    private static readonly HashSet<string> ReservedIdentifiers =
    [
        "arguments", "await", "break", "case", "catch", "class", "const", "continue", "debugger",
        "default", "delete", "do", "else", "enum", "eval", "export", "extends", "false", "finally",
        "for", "function", "if", "implements", "import", "in", "instanceof", "interface", "let",
        "new", "null", "package", "private", "protected", "public", "return", "static", "super",
        "switch", "this", "throw", "true", "try", "typeof", "var", "void", "while", "with", "yield",
    ];

    /// <summary>C# namespace → generated module directory: "QuestPDF.Fluent" → "questpdf/fluent".</summary>
    public static string Module(string csharpNamespace)
    {
        if (csharpNamespace.Length == 0 || csharpNamespace == "QuestPDF")
            return "questpdf";

        var rest = csharpNamespace.StartsWith("QuestPDF.", StringComparison.Ordinal)
            ? csharpNamespace["QuestPDF.".Length..]
            : csharpNamespace;

        var segments = rest.Split('.').Select(s => s.ToLowerInvariant());
        return "questpdf/" + string.Join("/", segments);
    }

    /// <summary>Member name → lowerCamelCase, splitting on underscores (PDFA_Conformance → pdfaConformance).</summary>
    public static string Member(string name)
    {
        var segments = name.Split('_', StringSplitOptions.RemoveEmptyEntries);
        if (segments.Length == 0)
            return name;

        var sb = new StringBuilder();
        sb.Append(CamelCase(segments[0]));
        foreach (var segment in segments.Skip(1))
            sb.Append(char.ToUpperInvariant(segment[0])).Append(segment[1..]);

        return sb.ToString();
    }

    /// <summary>Constants keep their original name (A4, Red, Calibri, Default, PointsPerInch).</summary>
    public static string Constant(string name) => name;

    public static string Parameter(string name) =>
        ReservedIdentifiers.Contains(name) ? name + "_" : name;

    /// <summary>Wrapper-class name emitted alongside a handle-backed interface (IContainer → ContainerImpl).</summary>
    public static string ImplClassName(string interfaceSimpleName)
    {
        var trimmed = interfaceSimpleName.Length > 1 && interfaceSimpleName[0] == 'I' && char.IsUpper(interfaceSimpleName[1])
            ? interfaceSimpleName[1..]
            : interfaceSimpleName;
        return trimmed + "Impl";
    }

    /// <summary>Readable parameter name for a DSL scope in a function type: "ColumnDescriptor" → "column".</summary>
    public static string ScopeParameterName(string typeSimpleName)
    {
        var trimmed = typeSimpleName.Length > 1 && typeSimpleName[0] == 'I' && char.IsUpper(typeSimpleName[1])
            ? typeSimpleName[1..]
            : typeSimpleName;

        if (trimmed.EndsWith("Descriptor", StringComparison.Ordinal) && trimmed.Length > "Descriptor".Length)
            trimmed = trimmed[..^"Descriptor".Length];

        return Parameter(CamelCase(trimmed));
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

        if (upperRun > 1)
            return name[..(upperRun - 1)].ToLowerInvariant() + name[(upperRun - 1)..];

        return char.ToLowerInvariant(name[0]) + name[1..];
    }
}
