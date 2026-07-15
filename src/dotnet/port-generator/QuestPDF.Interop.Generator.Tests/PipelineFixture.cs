using System.Runtime.CompilerServices;
using QuestPDF.Interop.Generator.Backends.Kotlin;
using QuestPDF.Interop.Generator.Core;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Tests;

/// <summary>
/// Runs the full pipeline (extraction → Kotlin backend → shared native
/// emission) once per test session against the real QuestPDF assembly and the
/// repository's manual-overrides.txt.
/// </summary>
public sealed class PipelineFixture
{
    public KotlinBackend Backend { get; } = new();
    public ApiAssembly Assembly { get; }
    public KotlinModel Model { get; }
    public LanguageOutput Output { get; }
    public IReadOnlyDictionary<string, string> Files { get; }
    public IReadOnlyDictionary<string, string> NativeFiles { get; }
    public ApiIndex Index { get; }

    public PipelineFixture()
    {
        Assembly = GeneratorPipeline.Extract();
        var overrides = ManualOverrides.Load(Path.Combine(TestPaths.RepoRoot, Backend.ManualOverridesFile));
        var run = KotlinBackend.Run(Assembly, overrides);
        Model = run.Model;
        Output = run.Output;
        Files = run.Output.Files;
        NativeFiles = GeneratorPipeline.EmitNative(run.Output.Interop);
        Index = new ApiIndex(Assembly);
    }
}

[CollectionDefinition("pipeline")]
public sealed class PipelineCollection : ICollectionFixture<PipelineFixture>;

public static class TestPaths
{
    public static string ProjectDir { get; } = ComputeProjectDir();

    public static string RepoRoot { get; } =
        Path.GetFullPath(Path.Combine(ComputeProjectDir(), "..", "..", "..", ".."));

    public static bool UpdateGoldens =>
        Environment.GetEnvironmentVariable("UPDATE_GOLDENS") == "1";

    private static string ComputeProjectDir([CallerFilePath] string path = "") =>
        Path.GetDirectoryName(path)!;

    /// <summary>
    /// Compares actual content against a committed expectation file. With
    /// UPDATE_GOLDENS=1 the expectation is (re)written instead. On mismatch a
    /// .received file is written next to the expectation for easy diffing.
    /// </summary>
    public static void AssertMatchesFile(string expectationPath, string actual)
    {
        var fullPath = Path.Combine(ProjectDir, expectationPath);
        Directory.CreateDirectory(Path.GetDirectoryName(fullPath)!);

        if (UpdateGoldens)
        {
            File.WriteAllText(fullPath, actual);
            return;
        }

        Assert.True(File.Exists(fullPath),
            $"Expectation file missing: {expectationPath}. Run once with UPDATE_GOLDENS=1 to create it.");

        var expected = File.ReadAllText(fullPath);
        if (expected == actual)
        {
            var received = fullPath + ".received.txt";
            if (File.Exists(received))
                File.Delete(received);
            return;
        }

        File.WriteAllText(fullPath + ".received.txt", actual);
        var firstDiffLine = FirstDifferingLine(expected, actual);
        Assert.Fail(
            $"{expectationPath} differs from the generated output (first difference at line {firstDiffLine}). " +
            $"Inspect {expectationPath}.received.txt and re-run with UPDATE_GOLDENS=1 if the change is intended.");
    }

    private static int FirstDifferingLine(string expected, string actual)
    {
        var expectedLines = expected.Split('\n');
        var actualLines = actual.Split('\n');
        var count = Math.Min(expectedLines.Length, actualLines.Length);

        for (var i = 0; i < count; i++)
        {
            if (expectedLines[i] != actualLines[i])
                return i + 1;
        }

        return count + 1;
    }
}
