using QuestPDF.Interop.Generator.Core;
using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Extraction;
using QuestPDF.Interop.Generator.Core.Model;
using QuestPDF.Interop.Generator.Core.NativeEmission;

namespace QuestPDF.Interop.Generator;

/// <summary>
/// The generator as a library, shared by the CLI and the tests. Extraction
/// (reflection over QuestPDF) runs once; each selected language backend
/// classifies the model and emits its client sources; the backends' interop
/// plans merge into the single ABI of the one shared native library, from
/// which the C# export sources are emitted.
/// </summary>
public static class GeneratorPipeline
{
    public static ApiAssembly Extract()
    {
        var assembly = typeof(global::QuestPDF.Infrastructure.IContainer).Assembly;
        var documentation = XmlDocLoader.ForAssembly(assembly);
        return new ApiExtractor(assembly, documentation).Extract();
    }

    /// <summary>Generated C# export sources for the native/QuestPDF.Native project.</summary>
    public static IReadOnlyDictionary<string, string> EmitNative(InteropModel interop) =>
        NativeExportsEmitter.Emit(interop);

    /// <summary>
    /// Merges per-backend interop plans into the one ABI of the shared native
    /// library. Entry-point names come from the shared allocator, so the same
    /// member arrives from every backend under the same name; the merge keeps
    /// one instance per name after verifying the plans are content-identical.
    /// The result is the union — a member only one backend maps still exports.
    /// </summary>
    public static InteropModel MergeInterop(IReadOnlyList<InteropModel> models)
    {
        if (models.Count == 0)
            throw new InvalidOperationException("At least one backend must contribute an interop model.");
        if (models.Count == 1)
            return models[0];

        var first = models[0];
        foreach (var other in models.Skip(1))
        {
            if (other.SourceAssemblyName != first.SourceAssemblyName || other.SourceAssemblyVersion != first.SourceAssemblyVersion)
                throw new InvalidOperationException("Backends disagree on the source assembly identity; they must consume the same extraction.");
        }

        var exports = new SortedDictionary<string, NativeExport>(StringComparer.Ordinal);
        foreach (var export in models.SelectMany(m => m.Exports))
        {
            if (exports.TryGetValue(export.EntryPoint, out var existing))
            {
                if (!ReferenceEquals(existing, export) && InteropSignature.Of(existing) != InteropSignature.Of(export))
                    throw new InvalidOperationException(
                        $"Entry point {export.EntryPoint} is planned differently by two backends:\n" +
                        $"  {InteropSignature.Of(existing)}\n  {InteropSignature.Of(export)}");
                continue;
            }

            exports[export.EntryPoint] = export;
        }

        var shapes = new SortedDictionary<string, CallbackShape>(StringComparer.Ordinal);
        foreach (var shape in models.SelectMany(m => m.CallbackShapes))
        {
            if (shapes.TryGetValue(shape.Name, out var existing))
            {
                if (!existing.ParameterSlots.SequenceEqual(shape.ParameterSlots) || existing.ReturnSlot != shape.ReturnSlot)
                    throw new InvalidOperationException($"Callback shape {shape.Name} differs between backends.");
                continue;
            }

            shapes[shape.Name] = shape;
        }

        var proxies = new SortedDictionary<string, ProxyPlan>(StringComparer.Ordinal);
        foreach (var proxy in models.SelectMany(m => m.Proxies))
        {
            if (proxies.TryGetValue(proxy.ProxyExportEntryPoint, out var existing))
            {
                if (!ReferenceEquals(existing, proxy) && InteropSignature.Of(existing) != InteropSignature.Of(proxy))
                    throw new InvalidOperationException($"Proxy export {proxy.ProxyExportEntryPoint} is planned differently by two backends.");
                continue;
            }

            proxies[proxy.ProxyExportEntryPoint] = proxy;
        }

        return new InteropModel(
            first.SourceAssemblyName,
            first.SourceAssemblyVersion,
            exports.Values.ToList(),
            shapes.Values.ToList(),
            proxies.Values.ToList());
    }

    /// <summary>Stable text dump of the extracted model annotated with a backend's classifications (the committed snapshot).</summary>
    public static string RenderSnapshot(ApiAssembly assembly, IReadOnlyList<ReportEntry> report) =>
        ModelDump.Render(assembly, BuildClassificationLookup(report));

    private static IReadOnlyDictionary<string, string> BuildClassificationLookup(IReadOnlyList<ReportEntry> report)
    {
        return report
            .GroupBy(r => r.DocId)
            .ToDictionary(
                group => group.Key,
                group => string.Join(", ", group
                    .Select(r => r.Classification switch
                    {
                        ApiClassification.Generated => "generated",
                        ApiClassification.ManualOverride => "manual",
                        ApiClassification.Infrastructure => "infrastructure",
                        _ => "unsupported: " + r.Detail,
                    })
                    .Distinct()
                    .OrderBy(s => s, StringComparer.Ordinal)),
                StringComparer.Ordinal);
    }
}
