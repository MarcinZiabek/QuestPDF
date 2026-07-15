using QuestPDF.Interop.Generator.Backends.TypeScript.Emission;
using QuestPDF.Interop.Generator.Core;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;
using QuestPDF.Interop.Generator.Core.Reporting;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>
/// The TypeScript backend: a fluent Node.js API mirroring the QuestPDF surface
/// plus a koffi binding layer (native function declarations and callback
/// prototypes), bridged through the shared native library. Entry-point names
/// come from the shared allocator, so this backend binds the same exports as
/// every other language client.
/// </summary>
public sealed class TypeScriptBackend : ILanguageBackend
{
    public string Id => "typescript";

    public string DisplayName => "TypeScript";

    public string ManualOverridesFile => "src/node/package/manual-overrides.txt";

    public string GeneratedRoot => "src/node/package/src/generated";

    public string CoverageReportFile => "src/node/package/coverage-report.md";

    public CoverageReportLabels CoverageLabels { get; } = new(
        Title: "QuestPDF → TypeScript fluent API coverage report",
        GeneratedMeaning: "emitted into `src/node/package/src/generated`",
        ManualOverrideMeaning: "listed in `manual-overrides.txt`, implemented by hand in `src/manual/`");

    public LanguageOutput Generate(ApiAssembly assembly, ManualOverrides overrides) =>
        Run(assembly, overrides).Output;

    /// <summary>Typed entry point exposing the TypeScript model alongside the neutral output (tests introspect it).</summary>
    public static TypeScriptGeneration Run(ApiAssembly assembly, ManualOverrides overrides)
    {
        var index = new ApiIndex(assembly);
        var mapper = new TsTypeMapper(index);
        var model = TsClassifier.Classify(index, mapper, overrides);

        // Views must share the classifier's index/mapper: type-support marks
        // accumulated during classification affect type mapping.
        var views = new TsBridgeViews(index, mapper);

        var files = new SortedDictionary<string, string>(
            new TypeScriptEmitter(views).Emit(model).ToDictionary(f => f.Key, f => f.Value),
            StringComparer.Ordinal);
        InteropTypeScriptEmitter.Emit(model, files);

        var interop = new InteropModel(
            model.SourceAssemblyName,
            model.SourceAssemblyVersion,
            model.NativeExports,
            model.CallbackShapes,
            model.Proxies);

        return new TypeScriptGeneration(model, new LanguageOutput(files, interop, model.Report));
    }
}

/// <summary>One TypeScript generation run: the neutral output plus the typed TypeScript model.</summary>
public sealed record TypeScriptGeneration(TsModel Model, LanguageOutput Output);
