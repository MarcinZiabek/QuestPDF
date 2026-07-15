using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Tests;

public sealed class OverloadCollapseTests
{
    private static TypeRef Void => new() { Kind = TypeRefKind.Named, FullName = "System.Void" };

    private static TypeRef String_ => new() { Kind = TypeRefKind.Named, FullName = "System.String" };

    private static TypeRef Single_ => new() { Kind = TypeRefKind.Named, FullName = "System.Single" };

    private static ApiMethod Method(string name, string docSuffix, TypeRef returnType, string? obsolete = null, params ApiParameter[] parameters) =>
        new(
            Name: name,
            Kind: ApiMethodKind.Ordinary,
            IsStatic: false,
            IsAbstract: false,
            IsVirtual: false,
            IsExtension: false,
            ExtensionReceiver: null,
            TypeParameters: [],
            Parameters: parameters,
            ReturnType: returnType,
            DeclaringTypeFullName: "QuestPDF.Test.Widget",
            DocId: $"M:QuestPDF.Test.Widget.{name}{docSuffix}",
            ObsoleteMessage: obsolete,
            RawXmlDoc: null);

    private static ApiParameter Parameter(string name, TypeRef type) =>
        new(name, type, Default: null, IsParams: false, IsByRef: false, IsCallerInfo: false);

    [Fact]
    public void PrefixOverloadCollapsesIntoLongestWithSynthesizedDefaults()
    {
        var reports = new List<ReportEntry>();
        var methods = new[]
        {
            Method("Draw", "(System.String)", Void, null, Parameter("text", String_)),
            Method("Draw", "(System.String,System.Single)", Void, null, Parameter("text", String_), Parameter("size", Single_)),
        };

        var collapsed = OverloadCollapseRule.Apply(methods, reports);

        var survivor = Assert.Single(collapsed);
        Assert.Equal(2, survivor.Parameters.Count);
        Assert.NotNull(survivor.Parameters[1].Default);
        Assert.Single(reports);
        Assert.Equal(ApiClassification.Generated, reports[0].Classification);
        Assert.Contains("collapsed", reports[0].Detail);
    }

    [Fact]
    public void ObsoleteOverloadsAreNotCollapsed()
    {
        var reports = new List<ReportEntry>();
        var methods = new[]
        {
            Method("Draw", "(System.String)", Void, obsolete: "old", Parameter("text", String_)),
            Method("Draw", "(System.String,System.Single)", Void, null, Parameter("text", String_), Parameter("size", Single_)),
        };

        var collapsed = OverloadCollapseRule.Apply(methods, reports);

        Assert.Equal(2, collapsed.Count);
        Assert.Empty(reports);
    }

    [Fact]
    public void DifferentReturnTypesAreGenuineOverloads()
    {
        var reports = new List<ReportEntry>();
        var container = new TypeRef { Kind = TypeRefKind.Named, FullName = "QuestPDF.Infrastructure.IContainer", IsApiAssemblyType = true };
        var methods = new[]
        {
            Method("After", "", container),
            Method("After", "(System.String)", Void, null, Parameter("text", String_)),
        };

        var collapsed = OverloadCollapseRule.Apply(methods, reports);

        Assert.Equal(2, collapsed.Count);
        Assert.Empty(reports);
    }

    [Fact]
    public void GenericTailParameterPreventsCollapse()
    {
        var reports = new List<ReportEntry>();
        var generic = new TypeRef { Kind = TypeRefKind.GenericParameter, GenericParameterName = "T" };
        var methods = new[]
        {
            Method("Component", "``1", Void) with
            {
                TypeParameters = [new ApiTypeParameter("T", [], false, false, HasDefaultConstructorConstraint: true)],
            },
            Method("Component", "``1(``0)", Void, null, Parameter("component", generic)) with
            {
                TypeParameters = [new ApiTypeParameter("T", [], false, false, false)],
            },
        };

        var collapsed = OverloadCollapseRule.Apply(methods, reports);

        Assert.Equal(2, collapsed.Count);
        Assert.Empty(reports);
    }
}
