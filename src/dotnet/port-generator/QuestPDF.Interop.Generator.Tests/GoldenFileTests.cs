namespace QuestPDF.Interop.Generator.Tests;

/// <summary>
/// Golden files for representative generated sources, each exercising a
/// distinct mapping concern — now on both sides of the bridge.
/// </summary>
[Collection("pipeline")]
public sealed class GoldenFileTests(PipelineFixture pipeline)
{
    [Theory]
    // Enum whose name collides with kotlin.Unit (forced qualification at use sites).
    [InlineData("com/questpdf/infrastructure/Unit.kt")]
    // Self-generic extension methods become members; base class is open.
    [InlineData("com/questpdf/fluent/TextSpanDescriptor.kt")]
    // Covariant overrides preserving the fluent self-type, without restating defaults.
    [InlineData("com/questpdf/fluent/TextBlockDescriptor.kt")]
    // Descriptor with lambda-with-receiver members and enum default parameters.
    [InlineData("com/questpdf/fluent/TableDescriptor.kt")]
    // Static class with nullable-enum accessors mapped to an object.
    [InlineData("com/questpdf/Settings.kt")]
    // Delegate mapped to a typealias with named function-type parameters.
    [InlineData("com/questpdf/fluent/PageNumberFormatter.kt")]
    // Struct with bridged constructor, accessors and companion factories.
    [InlineData("com/questpdf/infrastructure/Color.kt")]
    // The JNA library interface declaring every export.
    [InlineData("com/questpdf/interop/QuestPdfNative.kt")]
    // The deduplicated callback fun-interfaces.
    [InlineData("com/questpdf/interop/NativeCallbacks.kt")]
    public void GeneratedFileMatchesGolden(string generatedPath)
    {
        Assert.True(pipeline.Files.TryGetValue(generatedPath, out var actual),
            $"Pipeline did not emit {generatedPath}.");

        var goldenPath = Path.Combine("golden", generatedPath.Replace('/', Path.DirectorySeparatorChar));
        TestPaths.AssertMatchesFile(goldenPath, actual!);
    }

    [Theory]
    // Callback adapters, handle marshaling and extension invocation on the C# side.
    [InlineData("Exports/IContainer.g.cs")]
    // Constructors, accessors and conversion operators on a struct.
    [InlineData("Exports/Color.g.cs")]
    // Buffer out-parameters (GeneratePdf) and string returns.
    [InlineData("Exports/IDocument.g.cs")]
    // User-implemented interface proxies.
    [InlineData("Exports/Proxies.g.cs")]
    public void GeneratedExportFileMatchesGolden(string exportPath)
    {
        Assert.True(pipeline.NativeFiles.TryGetValue(exportPath, out var actual),
            $"Pipeline did not emit {exportPath}.");

        var goldenPath = Path.Combine("golden", "native", exportPath.Replace('/', Path.DirectorySeparatorChar));
        TestPaths.AssertMatchesFile(goldenPath, actual!);
    }
}
