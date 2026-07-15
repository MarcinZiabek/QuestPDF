using QuestPDF.Interop.Generator.Core.Bridge;

namespace QuestPDF.Interop.Generator.Tests;

/// <summary>
/// The two backends must bind one shared native library: the same member
/// arrives from both under the same allocator-assigned entry-point name and
/// content-identical plan, and the merge is the union of both export sets.
/// </summary>
public sealed class MergedInteropTests : IClassFixture<PipelineFixture>, IClassFixture<TsPipelineFixture>
{
    private readonly PipelineFixture kotlin;
    private readonly TsPipelineFixture typescript;

    public MergedInteropTests(PipelineFixture kotlin, TsPipelineFixture typescript)
    {
        this.kotlin = kotlin;
        this.typescript = typescript;
    }

    [Fact]
    public void BackendsMergeIntoOneUnionAbi()
    {
        var merged = GeneratorPipeline.MergeInterop([kotlin.Output.Interop, typescript.Output.Interop]);

        var kotlinNames = kotlin.Output.Interop.Exports.Select(e => e.EntryPoint).ToHashSet(StringComparer.Ordinal);
        var typescriptNames = typescript.Output.Interop.Exports.Select(e => e.EntryPoint).ToHashSet(StringComparer.Ordinal);
        var mergedNames = merged.Exports.Select(e => e.EntryPoint).ToHashSet(StringComparer.Ordinal);

        Assert.True(kotlinNames.IsSubsetOf(mergedNames));
        Assert.True(typescriptNames.IsSubsetOf(mergedNames));
        Assert.Equal(kotlinNames.Union(typescriptNames).Count(), mergedNames.Count);
    }

    [Fact]
    public void SharedMembersBindTheSameEntryPointWithTheSamePlan()
    {
        var kotlinByName = kotlin.Output.Interop.Exports.ToDictionary(e => e.EntryPoint, StringComparer.Ordinal);
        var shared = 0;

        foreach (var export in typescript.Output.Interop.Exports)
        {
            if (!kotlinByName.TryGetValue(export.EntryPoint, out var kotlinExport))
                continue;

            shared++;
            Assert.Equal(InteropSignature.Of(kotlinExport), InteropSignature.Of(export));
        }

        // The overwhelming majority of the surface is shared; a handful of
        // per-language keeps (JVM-erasure vs runtime-dispatch dedup) differ.
        Assert.True(shared > 1000, $"Only {shared} exports are shared between the backends.");
    }

    [Fact]
    public void CallbackShapesAndProxiesAgreeAcrossBackends()
    {
        var merged = GeneratorPipeline.MergeInterop([kotlin.Output.Interop, typescript.Output.Interop]);

        Assert.Equal(kotlin.Output.Interop.CallbackShapes.Select(s => s.Name).Order(),
            typescript.Output.Interop.CallbackShapes.Select(s => s.Name).Order());
        Assert.Equal(kotlin.Output.Interop.Proxies.Count, merged.Proxies.Count);
    }

    [Fact]
    public void MergedEmissionExportsEveryBackendsEntryPoints()
    {
        var merged = GeneratorPipeline.MergeInterop([kotlin.Output.Interop, typescript.Output.Interop]);
        var files = GeneratorPipeline.EmitNative(merged);
        var content = string.Join("\n", files.Values);

        foreach (var export in kotlin.Output.Interop.Exports.Concat(typescript.Output.Interop.Exports))
            Assert.Contains($"EntryPoint = \"{export.EntryPoint}\"", content);
    }
}
