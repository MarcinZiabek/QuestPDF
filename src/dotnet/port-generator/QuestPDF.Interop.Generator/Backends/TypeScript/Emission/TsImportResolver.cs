namespace QuestPDF.Interop.Generator.Backends.TypeScript.Emission;

/// <summary>
/// Computes the import list for one generated file. Generated types import from
/// the internal barrel (which evaluates all modules in inheritance order, so
/// CommonJS cycles never observe an uninitialized base class); runtime pieces
/// import from the handwritten manual/ source set; callback prototypes import
/// from the generated interop module. All specifiers are relative,
/// extensionless (CommonJS resolution).
/// </summary>
public sealed class TsImportResolver
{
    private readonly string currentModule;
    private readonly ISet<string> localNames;

    private readonly SortedSet<string> generatedImports = new(StringComparer.Ordinal);
    private readonly SortedDictionary<string, SortedSet<string>> manualImports = new(StringComparer.Ordinal);
    private readonly SortedSet<string> callbackShapeImports = new(StringComparer.Ordinal);

    public TsImportResolver(string currentModule, ISet<string> localNames)
    {
        this.currentModule = currentModule;
        this.localNames = localNames;
    }

    /// <summary>Registers a generated type reference and returns its rendered name.</summary>
    public string Generated(TsType type)
    {
        var topLevel = type.Name.Split('.')[0];

        if (!localNames.Contains(topLevel))
            generatedImports.Add(topLevel);

        return type.Name;
    }

    /// <summary>Registers an import from a handwritten manual/ file ("native-bridge", "native-object").</summary>
    public string Manual(string file, string name)
    {
        if (!manualImports.TryGetValue(file, out var names))
            manualImports[file] = names = new SortedSet<string>(StringComparer.Ordinal);
        names.Add(name);
        return name;
    }

    /// <summary>Registers a callback prototype import from the generated interop module.</summary>
    public string CallbackShape(string shapeName)
    {
        callbackShapeImports.Add(shapeName);
        return shapeName;
    }

    public IReadOnlyList<string> ImportStatements()
    {
        var statements = new List<string>();

        foreach (var (file, names) in manualImports)
            statements.Add($"import {{ {string.Join(", ", names)} }} from '{ManualSpecifier(file)}';");

        if (callbackShapeImports.Count > 0)
            statements.Add($"import {{ {string.Join(", ", callbackShapeImports)} }} from '{RelativeSpecifier("interop/callback-shapes")}';");

        if (generatedImports.Count > 0)
            statements.Add($"import {{ {string.Join(", ", generatedImports)} }} from '{RelativeSpecifier("internal")}';");

        return statements;
    }

    /// <summary>Relative specifier from the current module directory to a file under the generated root.</summary>
    private string RelativeSpecifier(string targetFile)
    {
        var from = currentModule.Length == 0 ? [] : currentModule.Split('/');
        var target = targetFile.Split('/');
        var targetDirectory = target[..^1];

        var common = 0;
        while (common < from.Length && common < targetDirectory.Length && from[common] == targetDirectory[common])
            common++;

        var ups = from.Length - common;
        var segments = new List<string>();
        segments.AddRange(Enumerable.Repeat("..", ups));
        segments.AddRange(target[common..]);

        var path = string.Join("/", segments);
        return ups == 0 ? "./" + path : path;
    }

    /// <summary>Relative specifier from the current module (under src/generated) to src/manual/&lt;file&gt;.</summary>
    private string ManualSpecifier(string file)
    {
        var depth = currentModule.Length == 0 ? 0 : currentModule.Split('/').Length;
        return string.Concat(Enumerable.Repeat("../", depth + 1)) + "manual/" + file;
    }
}
