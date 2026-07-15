using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>
/// Maps C# signature types to TypeScript types through an ordered rule pipeline —
/// the first matching rule wins. Types no rule can map yield a failure with a
/// reason, which classifies the containing member as Unsupported.
/// </summary>
public sealed class TsTypeMapper
{
    private readonly IReadOnlyList<ITypeMappingRule> rules;
    private readonly ApiIndex index;

    public TsTypeMapper(ApiIndex index)
    {
        this.index = index;
        rules =
        [
            new GenericParameterRule(),
            new PrimitiveRule(),
            new ByteArrayRule(),
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

    public TsTypeMapping Map(TypeRef type)
    {
        foreach (var rule in rules)
        {
            if (!rule.Matches(type, index))
                continue;

            return rule.Map(type, this, index);
        }

        return TsTypeMapping.Failed(type, "no mapping rule matched");
    }

    public interface ITypeMappingRule
    {
        bool Matches(TypeRef type, ApiIndex index);
        TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index);
    }

    private sealed class GenericParameterRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) => type.Kind == TypeRefKind.GenericParameter;

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index) =>
            TsTypeMapping.Ok(TsType.Generic(type.GenericParameterName, type.IsNullable));
    }

    private sealed class PrimitiveRule : ITypeMappingRule
    {
        private static readonly Dictionary<string, string> Map_ = new()
        {
            ["System.Void"] = "void",
            ["System.Boolean"] = "boolean",
            ["System.Single"] = "number",
            ["System.Double"] = "number",
            ["System.Byte"] = "number",
            ["System.SByte"] = "number",
            ["System.Int16"] = "number",
            ["System.UInt16"] = "number",
            ["System.Int32"] = "number",
            ["System.UInt32"] = "number",
            ["System.Int64"] = "number",
            ["System.UInt64"] = "number",
            ["System.Char"] = "string",
            ["System.String"] = "string",
            ["System.Object"] = "unknown",
        };

        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && Map_.ContainsKey(type.FullName);

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index) =>
            TsTypeMapping.Ok(TsType.Builtin(Map_[type.FullName], type.IsNullable));
    }

    private sealed class ByteArrayRule : ITypeMappingRule
    {
        // A C# byte[] is a binary blob; its idiomatic Node type is Uint8Array.
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Array && type.ElementType!.Is("System.Byte");

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index) =>
            TsTypeMapping.Ok(TsType.Builtin("Uint8Array", type.IsNullable));
    }

    private sealed class ArrayRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) => type.Kind == TypeRefKind.Array;

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index)
        {
            var element = mapper.Map(type.ElementType!);
            if (!element.Success)
                return element;

            return TsTypeMapping.Ok(TsType.Array(element.Type!, type.IsNullable));
        }
    }

    private sealed class BclValueTypeRule : ITypeMappingRule
    {
        private static readonly Dictionary<string, string> Map_ = new()
        {
            ["System.DateTime"] = "Date",
            ["System.DateTimeOffset"] = "Date",
            ["System.TimeSpan"] = "number",
            ["System.Guid"] = "string",
            ["System.Uri"] = "URL",
            ["System.Exception"] = "Error",
        };

        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && Map_.ContainsKey(type.FullName);

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index) =>
            TsTypeMapping.Ok(TsType.Builtin(Map_[type.FullName], type.IsNullable));
    }

    private sealed class CollectionInterfaceRule : ITypeMappingRule
    {
        private static readonly HashSet<string> IterableTypes =
        [
            "System.Collections.Generic.IEnumerable",
        ];

        private static readonly HashSet<string> ListTypes =
        [
            "System.Collections.Generic.ICollection",
            "System.Collections.Generic.IReadOnlyCollection",
            "System.Collections.Generic.IList",
            "System.Collections.Generic.IReadOnlyList",
            "System.Collections.Generic.List",
        ];

        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.TypeArguments.Count == 1 &&
            (IterableTypes.Contains(type.FullName) || ListTypes.Contains(type.FullName));

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index)
        {
            var element = mapper.Map(type.TypeArguments[0]);
            if (!element.Success)
                return element;

            return TsTypeMapping.Ok(IterableTypes.Contains(type.FullName)
                ? TsType.Builtin("Iterable", type.IsNullable) with { TypeArguments = [element.Type!] }
                : TsType.Array(element.Type!, type.IsNullable, isReadonly: true));
        }
    }

    private sealed class ActionDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.Action";

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index) =>
            MapFunction(type, type.TypeArguments, returnType: null, mapper, index);
    }

    private sealed class FuncDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.Func" && type.TypeArguments.Count > 0;

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index) =>
            MapFunction(type, type.TypeArguments.SkipLast(1).ToList(), type.TypeArguments[^1], mapper, index);
    }

    private sealed class PredicateDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.Predicate" && type.TypeArguments.Count == 1;

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index)
        {
            var argument = mapper.Map(type.TypeArguments[0]);
            if (!argument.Success)
                return argument;

            var parameters = new List<TsParameter> { new("value", argument.Type!, null, false) };
            return TsTypeMapping.Ok(TsType.Function(parameters, TsType.Builtin("boolean"), type.IsNullable));
        }
    }

    /// <summary>QuestPDF's own delegate types map to references to their generated type aliases.</summary>
    private sealed class ApiDelegateRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.IsApiAssemblyType && type.IsDelegateType;

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index)
        {
            var apiType = index.FindType(type);
            if (apiType is null)
                return TsTypeMapping.Failed(type, $"delegate type {type.FullName} not found in model");

            return TsTypeMapping.Ok(TsType.Named(TsNameMapper.Module(apiType.Namespace), apiType.Name, type.IsNullable));
        }
    }

    private sealed class ApiTypeRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) =>
            type.Kind == TypeRefKind.Named && type.IsApiAssemblyType;

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index)
        {
            var apiType = index.FindType(type);
            if (apiType is null)
                return TsTypeMapping.Failed(type, $"type {type.FullName} is not part of the public model");

            if (index.IsTypeUnsupported(type, out var reason))
                return TsTypeMapping.Failed(type, $"references skipped type {apiType.Name} ({reason})");

            var arguments = new List<TsType>();
            foreach (var argument in type.TypeArguments)
            {
                var mapped = mapper.Map(argument);
                if (!mapped.Success)
                    return mapped;
                arguments.Add(mapped.Type!);
            }

            // Nested types render as Outer.Inner and import the outer type.
            var name = index.NestedName(type.FullName);
            var module = TsNameMapper.Module(apiType.Namespace);

            return TsTypeMapping.Ok(TsType.Named(module, name, type.IsNullable, arguments));
        }
    }

    private sealed class UnmappableRule : ITypeMappingRule
    {
        public bool Matches(TypeRef type, ApiIndex index) => true;

        public TsTypeMapping Map(TypeRef type, TsTypeMapper mapper, ApiIndex index) =>
            TsTypeMapping.Failed(type, $"references external type {type.FullName} with no TypeScript mapping");
    }

    private static TsTypeMapping MapFunction(TypeRef type, IReadOnlyList<TypeRef> argumentTypes, TypeRef? returnType, TsTypeMapper mapper, ApiIndex index)
    {
        var parameters = new List<TsParameter>();

        for (var i = 0; i < argumentTypes.Count; i++)
        {
            var mapped = mapper.Map(argumentTypes[i]);
            if (!mapped.Success)
                return mapped;

            // Single-argument delegates over DSL scope types keep the scope as
            // the first parameter (no receivers in TypeScript), readably named:
            // Action<ColumnDescriptor> → (column: ColumnDescriptor) => void.
            var name = i == 0 && argumentTypes.Count == 1 && index.IsDslReceiverType(argumentTypes[0])
                ? TsNameMapper.ScopeParameterName(argumentTypes[0].SimpleName)
                : "arg" + i;

            parameters.Add(new TsParameter(name, mapped.Type!, null, false));
        }

        TsType returned;
        if (returnType is null)
        {
            returned = TsType.Void;
        }
        else
        {
            var mapped = mapper.Map(returnType);
            if (!mapped.Success)
                return mapped;
            returned = mapped.Type!;
        }

        return TsTypeMapping.Ok(TsType.Function(parameters, returned, type.IsNullable));
    }
}

public sealed record TsTypeMapping(bool Success, TsType? Type, string? FailureReason)
{
    public static TsTypeMapping Ok(TsType type) => new(true, type, null);
    public static TsTypeMapping Failed(TypeRef source, string reason) => new(false, null, reason);
}
