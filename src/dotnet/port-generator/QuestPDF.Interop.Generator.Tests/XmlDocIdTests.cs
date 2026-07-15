using System.Reflection;
using QuestPDF.Interop.Generator.Core.Extraction;

namespace QuestPDF.Interop.Generator.Tests;

/// <summary>
/// Doc-comment IDs are the join key between reflection, QuestPDF.xml and
/// manual-overrides.txt — they must match the compiler's format exactly.
/// </summary>
public sealed class XmlDocIdTests
{
    private static MethodInfo Method(Type type, string name, int parameterCount) =>
        type.GetMethods(BindingFlags.Public | BindingFlags.Static | BindingFlags.Instance | BindingFlags.DeclaredOnly)
            .Single(m => m.Name == name && m.GetParameters().Length == parameterCount);

    [Fact]
    public void SimpleExtensionMethod()
    {
        var method = Method(typeof(global::QuestPDF.Fluent.PaddingExtensions), "Padding", 3);
        Assert.Equal(
            "M:QuestPDF.Fluent.PaddingExtensions.Padding(QuestPDF.Infrastructure.IContainer,System.Single,QuestPDF.Infrastructure.Unit)",
            XmlDocId.ForMethod(method));
    }

    [Fact]
    public void ConstructedGenericParameter()
    {
        var method = Method(typeof(global::QuestPDF.Fluent.ColumnExtensions), "Column", 2);
        Assert.Equal(
            "M:QuestPDF.Fluent.ColumnExtensions.Column(QuestPDF.Infrastructure.IContainer,System.Action{QuestPDF.Fluent.ColumnDescriptor})",
            XmlDocId.ForMethod(method));
    }

    [Fact]
    public void GenericMethodUsesDoubleBacktickAndPositionalParameters()
    {
        var method = typeof(global::QuestPDF.Fluent.TextSpanDescriptorExtensions)
            .GetMethods()
            .Single(m => m.Name == "FontSize");

        Assert.Equal(
            "M:QuestPDF.Fluent.TextSpanDescriptorExtensions.FontSize``1(``0,System.Single)",
            XmlDocId.ForMethod(method));
    }

    [Fact]
    public void ConversionOperatorEncodesReturnType()
    {
        var conversions = typeof(global::QuestPDF.Infrastructure.Color)
            .GetMethods(BindingFlags.Public | BindingFlags.Static)
            .Where(m => m.Name == "op_Implicit")
            .Select(XmlDocId.ForMethod)
            .ToList();

        Assert.Contains(
            "M:QuestPDF.Infrastructure.Color.op_Implicit(System.String)~QuestPDF.Infrastructure.Color",
            conversions);
    }

    [Fact]
    public void NestedTypeUsesDotSeparator()
    {
        Assert.Equal(
            "T:QuestPDF.Helpers.Colors.Red",
            XmlDocId.ForType(typeof(global::QuestPDF.Helpers.Colors.Red)));
    }

    [Fact]
    public void GeneratedIdsResolveQuestPdfDocumentation()
    {
        // Empirical validation: the IDs must actually match entries written by
        // the C# compiler into QuestPDF.xml.
        var assembly = typeof(global::QuestPDF.Infrastructure.IContainer).Assembly;
        var docs = XmlDocLoader.ForAssembly(assembly);
        Assert.True(docs.Count > 0, "QuestPDF.xml was not found next to the assembly or in the NuGet cache.");

        var documented = typeof(global::QuestPDF.Fluent.TextExtensions)
            .GetMethods(BindingFlags.Public | BindingFlags.Static)
            .Count(m => docs.Find(XmlDocId.ForMethod(m)) is not null);

        Assert.True(documented >= 2,
            $"Expected at least two documented TextExtensions methods to resolve, found {documented}.");
    }
}
