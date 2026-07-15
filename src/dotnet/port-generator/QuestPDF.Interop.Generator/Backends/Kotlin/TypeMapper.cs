using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// Maps C# signature types to Kotlin types through an ordered rule pipeline —
/// the first matching rule wins. Types no rule can map yield a failure with a
/// reason, which classifies the containing member as Unsupported.
/// </summary>
public sealed class TypeMapper
{
    private readonly IReadOnlyList<ITypeMappingRule> rules;
    private readonly ApiIndex index;

    public TypeMapper(ApiIndex index)
    {
        this.index = index;
        rules =
        [
            new GenericParameterRule(),
            new PrimitiveRule(),
            new ByteArrayRule(),
            new PrimitiveArrayRule(),
            new ArrayRule(),
            new BclValueTypeRule(),
            new CollectionInterfaceRule(),
            new ActionDelegateRule(),
            new FuncDelegateRule(),
            new PredicateDelegateRule(),
            new ApiDelegateRule(),
            new ApiTypeRule(),
            new UnmappableRule(),
        ];
    }

    public TypeMapping Map(TypeRef type)
    {
        foreach (var rule in rules)
        {
            if (!rule.Matches(type, index))
                continue;

            return rule.Map(type, this, index);
        }

        return TypeMapping.Failed(type, "no mapping rule matched");
    }

    public interface ITypeMappingRule
    {
        bool Matches(TypeRef type, ApiIndex index);
        TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index);
    }

    private sealed class GenericParameterRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) => type.Kind == TypeRefKind.GenericParameter;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index) =>
            TypeMapping.Ok(KType.Generic(type.GenericParameterName, type.IsNullable));
    }

    private sealed class PrimitiveRule : ITypeMappingRule
    {
        private static readonly Dictionary<string, string> Map_ = new()
        {
            ["System.Void"] = "Unit",
            ["System.Boolean"] = "Boolean",
            ["System.Single"] = "Float",
            ["System.Double"] = "Double",
            ["System.Byte"] = "UByte",
            ["System.SByte"] = "Byte",
            ["System.Int16"] = "Short",
            ["System.UInt16"] = "UShort",
            ["System.Int32"] = "Int",
            ["System.UInt32"] = "UInt",
            ["System.Int64"] = "Long",
            ["System.UInt64"] = "ULong",
            ["System.Char"] = "Char",
            ["System.String"] = "String",
            ["System.Object"] = "Any",
        };

        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && Map_.ContainsKey(type.FullName);

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index) =>
            TypeMapping.Ok(KType.Named("kotlin", Map_[type.FullName], type.IsNullable));
    }

    private sealed class ByteArrayRule : ITypeMappingRule
    {
        // A C# byte[] is a binary blob; Kotlin's idiomatic blob type is ByteArray
        // (even though scalar System.Byte maps to UByte).
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Array && type.ElementType!.Is("System.Byte");

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index) =>
            TypeMapping.Ok(KType.Named("kotlin", "ByteArray", type.IsNullable));
    }

    private sealed class PrimitiveArrayRule : ITypeMappingRule
    {
        private static readonly Dictionary<string, string> Map_ = new()
        {
            ["System.Single"] = "FloatArray",
            ["System.Double"] = "DoubleArray",
            ["System.Int32"] = "IntArray",
            ["System.Int64"] = "LongArray",
            ["System.Boolean"] = "BooleanArray",
            ["System.Char"] = "CharArray",
        };

        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Array &&
            type.ElementType!.Kind == TypeRefKind.Named &&
            Map_.ContainsKey(type.ElementType.FullName);

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index) =>
            TypeMapping.Ok(KType.Named("kotlin", Map_[type.ElementType!.FullName], type.IsNullable));
    }

    private sealed class ArrayRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) => type.Kind == TypeRefKind.Array;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index)
        {
            var element = mapper.Map(type.ElementType!);
            if (!element.Success)
                return element;

            return TypeMapping.Ok(KType.Named("kotlin", "Array", type.IsNullable, [element.Type!]));
        }
    }

    private sealed class BclValueTypeRule : ITypeMappingRule
    {
        private static readonly Dictionary<string, (string Package, string Name)> Map_ = new()
        {
            ["System.DateTime"] = ("java.time", "LocalDateTime"),
            ["System.DateTimeOffset"] = ("java.time", "OffsetDateTime"),
            ["System.TimeSpan"] = ("java.time", "Duration"),
            ["System.Guid"] = ("java.util", "UUID"),
            ["System.Random"] = ("java.util", "Random"),
            ["System.Uri"] = ("java.net", "URI"),
            ["System.Exception"] = ("kotlin", "Exception"),
        };

        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && Map_.ContainsKey(type.FullName);

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index)
        {
            var (package, name) = Map_[type.FullName];
            return TypeMapping.Ok(KType.Named(package, name, type.IsNullable));
        }
    }

    private sealed class CollectionInterfaceRule : ITypeMappingRule
    {
        private static readonly Dictionary<string, string> Map_ = new()
        {
            ["System.Collections.Generic.IEnumerable"] = "Iterable",
            ["System.Collections.Generic.ICollection"] = "MutableCollection",
            ["System.Collections.Generic.IReadOnlyCollection"] = "Collection",
            ["System.Collections.Generic.IList"] = "MutableList",
            ["System.Collections.Generic.IReadOnlyList"] = "List",
            ["System.Collections.Generic.List"] = "MutableList",
            ["System.Collections.Generic.ISet"] = "MutableSet",
            ["System.Collections.Generic.IDictionary"] = "MutableMap",
            ["System.Collections.Generic.IReadOnlyDictionary"] = "Map",
            ["System.Collections.Generic.Dictionary"] = "MutableMap",
        };

        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && Map_.ContainsKey(type.FullName) && type.TypeArguments.Count > 0;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index)
        {
            var arguments = new List<KType>();
            foreach (var argument in type.TypeArguments)
            {
                var mapped = mapper.Map(argument);
                if (!mapped.Success)
                    return mapped;
                arguments.Add(mapped.Type!);
            }

            return TypeMapping.Ok(KType.Named("kotlin.collections", Map_[type.FullName], type.IsNullable, arguments));
        }
    }

    private sealed class ActionDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.Action";

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index) =>
            MapFunction(type, type.TypeArguments, returnType: null, mapper, index);
    }

    private sealed class FuncDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.Func" && type.TypeArguments.Count > 0;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index) =>
            MapFunction(type, type.TypeArguments.SkipLast(1).ToList(), type.TypeArguments[^1], mapper, index);
    }

    private sealed class PredicateDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.Predicate" && type.TypeArguments.Count == 1;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index)
        {
            var argument = mapper.Map(type.TypeArguments[0]);
            if (!argument.Success)
                return argument;

            var parameters = new List<KotlinParameter> { new("value", argument.Type!, null, false) };
            return TypeMapping.Ok(KType.Function(null, parameters, KType.Named("kotlin", "Boolean"), type.IsNullable));
        }
    }

    /// <summary>QuestPDF's own delegate types map to references to their generated typealiases.</summary>
    private sealed class ApiDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.IsApiAssemblyType && type.IsDelegateType;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index)
        {
            var apiType = index.FindType(type);
            if (apiType is null)
                return TypeMapping.Failed(type, $"delegate type {type.FullName} not found in model");

            return TypeMapping.Ok(KType.Named(NameMapper.Package(apiType.Namespace), apiType.Name, type.IsNullable));
        }
    }

    private sealed class ApiTypeRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.IsApiAssemblyType;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index)
        {
            var apiType = index.FindType(type);
            if (apiType is null)
                return TypeMapping.Failed(type, $"type {type.FullName} is not part of the public model");

            if (index.IsTypeUnsupported(type, out var reason))
                return TypeMapping.Failed(type, $"references skipped type {apiType.Name} ({reason})");

            var arguments = new List<KType>();
            foreach (var argument in type.TypeArguments)
            {
                var mapped = mapper.Map(argument);
                if (!mapped.Success)
                    return mapped;
                arguments.Add(mapped.Type!);
            }

            // Nested types render as Outer.Inner and import the outer type.
            var kotlinName = index.NestedName(type.FullName);
            var package = NameMapper.Package(apiType.Namespace);

            return TypeMapping.Ok(KType.Named(package, kotlinName, type.IsNullable, arguments) with
            {
                ForceQualified = NameMapper.CollidesWithKotlinDefaultImport(kotlinName.Split('.')[0]),
            });
        }
    }

    private sealed class UnmappableRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) => true;

        public TypeMapping Map(TypeRef type, TypeMapper mapper, ApiIndex index) =>
            TypeMapping.Failed(type, $"references external type {type.FullName} with no Kotlin mapping");
    }

    private static TypeMapping MapFunction(TypeRef type, IReadOnlyList<TypeRef> argumentTypes, TypeRef? returnType, TypeMapper mapper, ApiIndex index)
    {
        var parameters = new List<KotlinParameter>();
        KType? receiver = null;

        for (var i = 0; i < argumentTypes.Count; i++)
        {
            var mapped = mapper.Map(argumentTypes[i]);
            if (!mapped.Success)
                return mapped;

            // Single-argument delegates over DSL scope types become lambdas with
            // receiver: Action<ColumnDescriptor> → ColumnDescriptor.() -> Unit.
            if (i == 0 && argumentTypes.Count == 1 && index.IsDslReceiverType(argumentTypes[0]))
            {
                receiver = mapped.Type;
                continue;
            }

            parameters.Add(new KotlinParameter("arg" + i, mapped.Type!, null, false));
        }

        KType returned;
        if (returnType is null)
        {
            returned = KType.Unit;
        }
        else
        {
            var mapped = mapper.Map(returnType);
            if (!mapped.Success)
                return mapped;
            returned = mapped.Type!;
        }

        return TypeMapping.Ok(KType.Function(receiver, parameters, returned, type.IsNullable));
    }
}

public sealed record TypeMapping(bool Success, KType? Type, string? FailureReason)
{
    public static TypeMapping Ok(KType type) => new(true, type, null);
    public static TypeMapping Failed(TypeRef source, string reason) => new(false, null, reason);
}
