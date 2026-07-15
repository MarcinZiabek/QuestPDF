using QuestPDF.Interop.Generator.Backends.Kotlin;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Tests;

[Collection("pipeline")]
public sealed class TypeMapperTests(PipelineFixture pipeline)
{
    private TypeMapper Mapper => new(pipeline.Index);

    private static TypeRef Named(string fullName, bool nullable = false, bool api = false, params TypeRef[] args) =>
        new()
        {
            Kind = TypeRefKind.Named,
            FullName = fullName,
            IsNullable = nullable,
            IsApiAssemblyType = api,
            TypeArguments = args,
        };

    [Fact]
    public void ByteArrayMapsToKotlinByteArray()
    {
        var type = new TypeRef { Kind = TypeRefKind.Array, ElementType = Named("System.Byte") };
        var mapped = Mapper.Map(type);
        Assert.True(mapped.Success);
        Assert.Equal("kotlin.ByteArray", mapped.Type!.FullName);
    }

    [Fact]
    public void ActionOverDescriptorBecomesLambdaWithReceiver()
    {
        var type = Named("System.Action", args: Named("QuestPDF.Fluent.ColumnDescriptor", api: true));
        var mapped = Mapper.Map(type);

        Assert.True(mapped.Success);
        Assert.True(mapped.Type!.IsFunctionType);
        Assert.Equal("ColumnDescriptor", mapped.Type.LambdaReceiver!.Name);
        Assert.Empty(mapped.Type.FunctionParameters);
        Assert.True(mapped.Type.FunctionReturn!.IsKotlinUnit);
    }

    [Fact]
    public void FuncOverTextStyleBecomesReceiverLambdaReturningTextStyle()
    {
        var textStyle = Named("QuestPDF.Infrastructure.TextStyle", api: true);
        var type = Named("System.Func", args: [textStyle, textStyle]);
        var mapped = Mapper.Map(type);

        Assert.True(mapped.Success);
        Assert.True(mapped.Type!.IsFunctionType);
        Assert.Equal("TextStyle", mapped.Type.LambdaReceiver!.Name);
        Assert.Equal("TextStyle", mapped.Type.FunctionReturn!.Name);
    }

    [Fact]
    public void ActionOverDataPayloadStaysOrdinaryLambda()
    {
        var type = Named("System.Action", args: Named("QuestPDF.Infrastructure.Size", api: true));
        var mapped = Mapper.Map(type);

        Assert.True(mapped.Success);
        Assert.Null(mapped.Type!.LambdaReceiver);
        Assert.Single(mapped.Type.FunctionParameters);
    }

    [Fact]
    public void PredicateBecomesBooleanFunction()
    {
        var type = Named("System.Predicate", args: Named("QuestPDF.Elements.ShowIfContext", api: true));
        var mapped = Mapper.Map(type);

        Assert.True(mapped.Success);
        Assert.True(mapped.Type!.IsFunctionType);
        Assert.Equal("Boolean", mapped.Type.FunctionReturn!.Name);
    }

    [Fact]
    public void UnsignedIntMapsToUInt()
    {
        var mapped = Mapper.Map(Named("System.UInt32"));
        Assert.True(mapped.Success);
        Assert.Equal("kotlin.UInt", mapped.Type!.FullName);
    }

    [Fact]
    public void StreamHasNoMapping()
    {
        var mapped = Mapper.Map(Named("System.IO.Stream"));
        Assert.False(mapped.Success);
        Assert.Contains("System.IO.Stream", mapped.FailureReason);
    }

    [Fact]
    public void EnumerableMapsToIterable()
    {
        var type = Named("System.Collections.Generic.IEnumerable", args: Named("QuestPDF.Infrastructure.IDocument", api: true));
        var mapped = Mapper.Map(type);

        Assert.True(mapped.Success);
        Assert.Equal("kotlin.collections.Iterable", mapped.Type!.FullName);
        Assert.Equal("IDocument", mapped.Type.TypeArguments[0].Name);
    }

    [Fact]
    public void QuestPdfUnitEnumIsForceQualified()
    {
        var mapped = Mapper.Map(Named("QuestPDF.Infrastructure.Unit", api: true));
        Assert.True(mapped.Success);
        Assert.True(mapped.Type!.ForceQualified);
    }

    [Fact]
    public void NestedTypeReferenceUsesOuterPrefix()
    {
        var mapped = Mapper.Map(Named("QuestPDF.Fluent.DocumentOperation.DocumentAttachment", api: true));
        Assert.True(mapped.Success);
        Assert.Equal("DocumentOperation.DocumentAttachment", mapped.Type!.Name);
        Assert.Equal("com.questpdf.fluent", mapped.Type.PackageName);
    }
}
