using QuestPDF.Interop.Generator.Backends.TypeScript;
using QuestPDF.Interop.Generator.Core;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Tests;

/// <summary>
/// Runs the full pipeline (extraction → TypeScript backend → shared native
/// emission) once per test session against the real QuestPDF assembly and the
/// repository's TypeScript manual-overrides file.
/// </summary>
public sealed class TsPipelineFixture
{
    public TypeScriptBackend Backend { get; } = new();
    public ApiAssembly Assembly { get; }
    public TsModel Model { get; }
    public LanguageOutput Output { get; }
    public IReadOnlyDictionary<string, string> Files { get; }
    public IReadOnlyDictionary<string, string> NativeFiles { get; }

    public TsPipelineFixture()
    {
        Assembly = GeneratorPipeline.Extract();
        var overrides = ManualOverrides.Load(Path.Combine(TestPaths.RepoRoot, Backend.ManualOverridesFile));
        var run = TypeScriptBackend.Run(Assembly, overrides);
        Model = run.Model;
        Output = run.Output;
        Files = run.Output.Files;
        NativeFiles = GeneratorPipeline.EmitNative(run.Output.Interop);
    }
}

[CollectionDefinition("ts-pipeline")]
public sealed class TsPipelineCollection : ICollectionFixture<TsPipelineFixture>;
