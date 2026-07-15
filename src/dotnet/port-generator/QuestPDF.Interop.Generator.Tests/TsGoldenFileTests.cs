namespace QuestPDF.Interop.Generator.Tests;

/// <summary>
/// Golden files for representative generated TypeScript sources, each
/// exercising a distinct mapping concern — both the fluent API and the koffi
/// binding side of the bridge.
/// </summary>
[Collection("ts-pipeline")]
public sealed class TsGoldenFileTests(TsPipelineFixture pipeline)
{
    [Theory]
    // Enum as a constant object with a literal union type.
    [InlineData("questpdf/infrastructure/Unit.ts")]
    // Self-generic extension methods become members; overload dispatch on fallback().
    [InlineData("questpdf/fluent/TextSpanDescriptor.ts")]
    // Covariant redeclarations preserving the fluent self-type, restating defaults.
    [InlineData("questpdf/fluent/TextBlockDescriptor.ts")]
    // Descriptor with function-typed members and enum default parameters.
    [InlineData("questpdf/fluent/TableDescriptor.ts")]
    // Static class with nullable-enum accessors mapped to static get/set.
    [InlineData("questpdf/Settings.ts")]
    // Delegate mapped to a function-type alias with named parameters.
    [InlineData("questpdf/fluent/PageNumberFormatter.ts")]
    // Struct with marker-dispatched constructor, accessors and static factories.
    [InlineData("questpdf/infrastructure/Color.ts")]
    // Interface + Impl wrapper pair with overload dispatch and inherited-member completion.
    [InlineData("questpdf/elements/table/ITableCellContainer.ts")]
    // Nested classes and enums merged through a namespace, in base-first order.
    [InlineData("questpdf/fluent/DocumentOperation.ts")]
    // The koffi declarations for every export.
    [InlineData("interop/native-functions.ts")]
    // The deduplicated callback prototypes.
    [InlineData("interop/callback-shapes.ts")]
    // The internal barrel encoding module evaluation order (base classes first).
    [InlineData("internal.ts")]
    public void GeneratedFileMatchesGolden(string generatedPath)
    {
        Assert.True(pipeline.Files.TryGetValue(generatedPath, out var actual),
            $"Pipeline did not emit {generatedPath}.");

        var goldenPath = Path.Combine("golden", "typescript", generatedPath.Replace('/', Path.DirectorySeparatorChar));
        TestPaths.AssertMatchesFile(goldenPath, actual!);
    }
}
