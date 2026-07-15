using QuestPDF.Interop.Generator.Backends.Kotlin;
using QuestPDF.Interop.Generator.Core.Classification;

namespace QuestPDF.Interop.Generator.Tests;

public sealed class NameMapperTests
{
    [Theory]
    [InlineData("FontSize", "fontSize")]
    [InlineData("Text", "text")]
    [InlineData("AlignCenter", "alignCenter")]
    [InlineData("PDFA_Conformance", "pdfaConformance")]
    [InlineData("PDFUA_Conformance", "pdfuaConformance")]
    [InlineData("A4", "a4")]
    [InlineData("ZIndex", "zIndex")]
    [InlineData("GeneratePdf", "generatePdf")]
    [InlineData("UseOriginalImage", "useOriginalImage")]
    public void MemberNamesBecomeLowerCamelCase(string csharp, string kotlin) =>
        Assert.Equal(kotlin, NameMapper.Member(csharp));

    [Theory]
    [InlineData("A4", "A4")]
    [InlineData("ARCH_A", "ARCH_A")]
    [InlineData("Calibri", "Calibri")]
    [InlineData("Default", "Default")]
    [InlineData("PointsPerInch", "PointsPerInch")]
    public void ConstantsKeepOriginalCasing(string csharp, string kotlin) =>
        Assert.Equal(kotlin, NameMapper.Constant(csharp));

    [Theory]
    [InlineData("object", "`object`")]
    [InlineData("fun", "`fun`")]
    [InlineData("in", "`in`")]
    [InlineData("value", "value")]
    public void HardKeywordsAreEscaped(string name, string expected) =>
        Assert.Equal(expected, NameMapper.Parameter(name));

    [Theory]
    [InlineData("QuestPDF", "com.questpdf")]
    [InlineData("QuestPDF.Fluent", "com.questpdf.fluent")]
    [InlineData("QuestPDF.Elements.Table", "com.questpdf.elements.table")]
    public void NamespacesMapToPackages(string csharpNamespace, string package) =>
        Assert.Equal(package, NameMapper.Package(csharpNamespace));

    [Fact]
    public void KotlinDefaultImportCollisionsAreDetected()
    {
        Assert.True(NameMapper.CollidesWithKotlinDefaultImport("Unit"));
        Assert.False(NameMapper.CollidesWithKotlinDefaultImport("Color"));
    }
}
