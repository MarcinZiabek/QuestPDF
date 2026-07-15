using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;
using QuestPDF.Interop.Generator.Core.Reporting;

namespace QuestPDF.Interop.Generator.Core;

/// <summary>
/// A language backend turns the extracted QuestPDF API model into one client
/// library: source files for that language plus the interop plan (native
/// exports, callback shapes, proxies) its generated code binds against.
/// Adding a language means implementing this interface and registering the
/// backend — the extraction stage, the shared native-export emission and the
/// reporting mechanics stay untouched.
/// </summary>
public interface ILanguageBackend
{
    /// <summary>Stable identifier used for CLI selection (e.g. "kotlin").</summary>
    string Id { get; }

    string DisplayName { get; }

    /// <summary>Repo-relative file listing doc-ids excluded from generation for hand-written implementation.</summary>
    string ManualOverridesFile { get; }

    /// <summary>Repo-relative directory owned by this backend; wiped and recreated on every run.</summary>
    string GeneratedRoot { get; }

    /// <summary>Repo-relative path of this backend's committed coverage report.</summary>
    string CoverageReportFile { get; }

    /// <summary>Backend-specific wording of the coverage report.</summary>
    CoverageReportLabels CoverageLabels { get; }

    LanguageOutput Generate(ApiAssembly assembly, ManualOverrides overrides);
}

/// <summary>Everything one backend produced in one generator run.</summary>
/// <param name="Files">Generated client sources, paths relative to <see cref="ILanguageBackend.GeneratedRoot"/>.</param>
/// <param name="Interop">The interop plan the generated sources bind against.</param>
/// <param name="Report">Classification of every public API entry, for the coverage report and snapshot.</param>
public sealed record LanguageOutput(
    IReadOnlyDictionary<string, string> Files,
    InteropModel Interop,
    IReadOnlyList<ReportEntry> Report);

/// <summary>
/// The language-neutral interop plan: every native export, deduplicated
/// callback shape and proxy a client binds against. This model alone drives
/// the shared C# UnmanagedCallersOnly emission — the native library knows
/// nothing about the client language. When several backends run, their plans
/// are merged (and cross-checked) into the single ABI of the one shared
/// native library; see <c>GeneratorPipeline.MergeInterop</c>.
/// </summary>
public sealed record InteropModel(
    string SourceAssemblyName,
    string SourceAssemblyVersion,
    IReadOnlyList<NativeExport> Exports,
    IReadOnlyList<CallbackShape> CallbackShapes,
    IReadOnlyList<ProxyPlan> Proxies);
