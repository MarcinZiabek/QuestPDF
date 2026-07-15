using QuestPDF.Interop.Generator.Backends.Kotlin.Emission;
using QuestPDF.Interop.Generator.Core;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;
using QuestPDF.Interop.Generator.Core.Reporting;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// The Kotlin backend: a Kotlin DSL mirroring the fluent QuestPDF API plus a
/// JNA binding layer (library interface and callback fun-interfaces), bridged
/// through the shared native library. This backend currently authors the
/// native ABI: its classification decides the export set and the deterministic
/// entry-point names.
/// </summary>
public sealed class KotlinBackend : ILanguageBackend
{
    public string Id => "kotlin";

    public string DisplayName => "Kotlin";

    public string ManualOverridesFile => "src/jvm/package/manual-overrides.txt";

    public string GeneratedRoot => "src/jvm/package/src/generated/kotlin";

    public string CoverageReportFile => "src/jvm/package/coverage-report.md";

    public CoverageReportLabels CoverageLabels { get; } = new(
        Title: "QuestPDF → Kotlin DSL coverage report",
        GeneratedMeaning: "emitted into `src/jvm/package/src/generated/kotlin`",
        ManualOverrideMeaning: "listed in `manual-overrides.txt`, implemented by hand in `manual/`");

    public LanguageOutput Generate(ApiAssembly assembly, ManualOverrides overrides) =>
        Run(assembly, overrides).Output;

    /// <summary>Typed entry point exposing the Kotlin model alongside the neutral output (tests introspect it).</summary>
    public static KotlinGeneration Run(ApiAssembly assembly, ManualOverrides overrides)
    {
        var index = new ApiIndex(assembly);
        var mapper = new TypeMapper(index);
        var model = Classifier.Classify(index, mapper, overrides);

        // Views must share the classifier's index/mapper: type-support marks
        // accumulated during classification affect type mapping.
        var views = new KotlinBridgeViews(index, mapper);

        var files = new SortedDictionary<string, string>(
            new KotlinEmitter(views).Emit(model).ToDictionary(f => f.Key, f => f.Value),
            StringComparer.Ordinal);
        InteropKotlinEmitter.Emit(model, files);

        var interop = new InteropModel(
            model.SourceAssemblyName,
            model.SourceAssemblyVersion,
            model.NativeExports,
            model.CallbackShapes,
            model.Proxies);

        return new KotlinGeneration(model, new LanguageOutput(files, interop, model.Report));
    }
}

/// <summary>One Kotlin generation run: the neutral output plus the typed Kotlin model.</summary>
public sealed record KotlinGeneration(KotlinModel Model, LanguageOutput Output);
