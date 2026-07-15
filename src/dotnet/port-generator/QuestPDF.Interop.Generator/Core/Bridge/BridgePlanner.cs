using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Core.Bridge;

public enum MarshalPosition
{
    Parameter,
    Return,
    CallbackParameter,
    CallbackReturn,
}

/// <summary>
/// Decides how a C# signature type crosses the native bridge — an ordered
/// first-match rule chain. The decision is language-neutral: marshals carry
/// C#-side identity only, and every backend consumes the same plan. Types that
/// cannot cross yield a <see cref="BridgePlanningException"/>, which classifies
/// the containing member as Unsupported with the exception's reason.
/// </summary>
public sealed class BridgePlanner
{
    private static readonly Dictionary<string, ScalarKind> Scalars = new(StringComparer.Ordinal)
    {
        ["System.Boolean"] = ScalarKind.Boolean,
        ["System.SByte"] = ScalarKind.Byte,
        ["System.Byte"] = ScalarKind.UByte,
        ["System.Int16"] = ScalarKind.Short,
        ["System.UInt16"] = ScalarKind.UShort,
        ["System.Int32"] = ScalarKind.Int,
        ["System.UInt32"] = ScalarKind.UInt,
        ["System.Int64"] = ScalarKind.Long,
        ["System.UInt64"] = ScalarKind.ULong,
        ["System.Single"] = ScalarKind.Float,
        ["System.Double"] = ScalarKind.Double,
    };

    /// <summary>
    /// Interfaces users implement client-side (bridged through generated .NET
    /// proxy classes) rather than receive from the library as handles.
    /// </summary>
    private static readonly HashSet<string> UserImplementedInterfaces = new(StringComparer.Ordinal)
    {
        "QuestPDF.Infrastructure.IComponent",
        "QuestPDF.Infrastructure.IDynamicComponent",
    };

    private readonly ApiIndex index;
    private readonly Func<string, TypePlan?> planFor;
    private readonly Dictionary<string, ProxyPlan?> proxyCache = new(StringComparer.Ordinal);

    public BridgePlanner(ApiIndex index, Func<string, TypePlan?> planFor)
    {
        this.index = index;
        this.planFor = planFor;
    }

    public bool IsUserImplemented(ApiType type) =>
        type.Kind == ApiTypeKind.Interface &&
        type.TypeParameters.Count == 0 &&
        UserImplementedInterfaces.Contains(type.FullName);

    /// <summary>All proxy plans built so far (deterministic once classification finished).</summary>
    public IReadOnlyList<ProxyPlan> Proxies =>
        proxyCache.Values.Where(p => p is not null).Select(p => p!)
            .OrderBy(p => p.ProxyExportEntryPoint, StringComparer.Ordinal).ToList();

    /// <summary>C# source rendering of a signature type (without global:: prefix).</summary>
    public static string CSharpName(TypeRef type) => type.Kind switch
    {
        TypeRefKind.GenericParameter => type.GenericParameterName,
        TypeRefKind.Array => CSharpName(type.ElementType!) + "[]",
        _ => type.FullName + (type.TypeArguments.Count == 0
            ? ""
            : "<" + string.Join(", ", type.TypeArguments.Select(CSharpName)) + ">"),
    };

    public BridgeMarshal Plan(
        TypeRef type,
        MarshalPosition position,
        IReadOnlyDictionary<string, TypeRef>? genericBounds = null)
    {
        // 1. void
        if (type.Is("System.Void"))
        {
            if (position is MarshalPosition.Return or MarshalPosition.CallbackReturn)
                return new BridgeMarshal.Nothing();
            throw new BridgePlanningException("void cannot appear as a parameter");
        }

        // 2. generic parameters marshal as their single API constraint
        if (type.Kind == TypeRefKind.GenericParameter)
        {
            if (position is MarshalPosition.Return or MarshalPosition.CallbackReturn)
                throw new BridgePlanningException($"generic return type {type.GenericParameterName} cannot be wrapped on the Kotlin side");

            var bound = genericBounds?.GetValueOrDefault(type.GenericParameterName);
            if (bound is null)
                throw new BridgePlanningException($"unconstrained type parameter {type.GenericParameterName} cannot cross the bridge");

            return Plan(bound with { IsNullable = type.IsNullable }, position, genericBounds);
        }

        // 3. scalars
        if (type.Kind == TypeRefKind.Named && Scalars.TryGetValue(type.FullName, out var scalar))
        {
            if (!type.IsNullable)
                return new BridgeMarshal.Scalar(scalar);

            if (position is MarshalPosition.Parameter or MarshalPosition.CallbackParameter or MarshalPosition.Return)
                return new BridgeMarshal.NullableScalar(scalar);

            throw new BridgePlanningException($"nullable {type.FullName} cannot be returned from a callback");
        }

        // 4. strings
        if (type.Is("System.String"))
            return new BridgeMarshal.Text(type.IsNullable);

        // 5. date/time as round-trip text
        if (type.Is("System.DateTime"))
            return new BridgeMarshal.DateTimeText("System.DateTime", type.IsNullable);
        if (type.Is("System.DateTimeOffset"))
            return new BridgeMarshal.DateTimeText("System.DateTimeOffset", type.IsNullable);

        // 6. binary blobs
        if (type.Kind == TypeRefKind.Array && type.ElementType!.Is("System.Byte"))
            return new BridgeMarshal.Blob(type.IsNullable);

        // 7. string and handle sequences (parameters only)
        if (SequenceElement(type) is var (element, container) && element is not null)
        {
            if (position is not (MarshalPosition.Parameter))
                throw new BridgePlanningException($"collection type {type.Render()} is only bridged in parameter position");

            if (element.Is("System.String"))
                return new BridgeMarshal.TextSequence(container);

            if (element.Is("System.Single"))
                return new BridgeMarshal.ScalarArray(ScalarKind.Float, container);

            var planned = Plan(element, MarshalPosition.Parameter, genericBounds);
            if (planned is BridgeMarshal.Handle handleElement)
                return new BridgeMarshal.HandleSequence(handleElement, container);

            throw new BridgePlanningException($"collection of {element.Render()} is not bridged");
        }

        // 8. delegates → callbacks
        if (TryPlanCallback(type, position, genericBounds) is { } callback)
            return callback;

        // 9-11. API types: enums, user-implemented interfaces, handles
        if (type.Kind == TypeRefKind.Named && type.IsApiAssemblyType)
            return PlanApiType(type, position);

        throw new BridgePlanningException($"no bridge marshal for {type.Render()}");
    }

    private static (TypeRef? Element, SequenceContainer Container) SequenceElement(TypeRef type)
    {
        if (type.Kind == TypeRefKind.Array)
            return (type.ElementType, SequenceContainer.Array);

        if (type.Kind == TypeRefKind.Named && type.TypeArguments.Count == 1)
        {
            switch (type.FullName)
            {
                case "System.Collections.Generic.IEnumerable":
                    return (type.TypeArguments[0], SequenceContainer.Iterable);
                case "System.Collections.Generic.ICollection":
                case "System.Collections.Generic.IReadOnlyCollection":
                case "System.Collections.Generic.IReadOnlyList":
                case "System.Collections.Generic.IList":
                case "System.Collections.Generic.List":
                    return (type.TypeArguments[0], SequenceContainer.Collection);
            }
        }

        return (null, SequenceContainer.Array);
    }

    // ---- callbacks ----

    private BridgeMarshal.CallbackValue? TryPlanCallback(
        TypeRef type,
        MarshalPosition position,
        IReadOnlyDictionary<string, TypeRef>? genericBounds)
    {
        IReadOnlyList<TypeRef> parameterTypes;
        TypeRef? returnType;
        string delegateTypeName;

        if (type.Is("System.Action"))
        {
            parameterTypes = type.TypeArguments;
            returnType = null;
            delegateTypeName = CSharpName(type);
        }
        else if (type.Is("System.Func") && type.TypeArguments.Count > 0)
        {
            parameterTypes = type.TypeArguments.SkipLast(1).ToList();
            returnType = type.TypeArguments[^1];
            delegateTypeName = CSharpName(type);
        }
        else if (type.Is("System.Predicate") && type.TypeArguments.Count == 1)
        {
            parameterTypes = type.TypeArguments;
            returnType = new TypeRef { Kind = TypeRefKind.Named, FullName = "System.Boolean" };
            delegateTypeName = CSharpName(type);
        }
        else if (type is { Kind: TypeRefKind.Named, IsApiAssemblyType: true, IsDelegateType: true })
        {
            var delegateType = index.FindType(type);
            if (delegateType?.DelegateInfo is null)
                throw new BridgePlanningException($"delegate {type.FullName} has no Invoke signature");
            parameterTypes = delegateType.DelegateInfo.Parameters.Select(p => p.Type).ToList();
            returnType = delegateType.DelegateInfo.ReturnType.Is("System.Void") ? null : delegateType.DelegateInfo.ReturnType;
            delegateTypeName = type.FullName;
        }
        else
        {
            return null;
        }

        if (position is not (MarshalPosition.Parameter))
            throw new BridgePlanningException("delegates are only bridged in parameter position");

        var wraps = parameterTypes
            .Select(p => Plan(p, MarshalPosition.CallbackParameter, genericBounds))
            .ToList();
        var returnWrap = returnType is null
            ? (BridgeMarshal)new BridgeMarshal.Nothing()
            : Plan(returnType, MarshalPosition.CallbackReturn, genericBounds);

        // A single delegate argument over a DSL scope type is recorded as such;
        // receiver-style backends (Kotlin) surface it as the lambda receiver.
        var dslScopeStyle = parameterTypes.Count == 1 &&
                            !type.IsDelegateType &&
                            index.IsDslReceiverType(parameterTypes[0]);

        var shape = BridgeAbi.ShapeFor(wraps, returnWrap);
        var adapter = new CallbackAdapter(shape, wraps, returnWrap, dslScopeStyle);

        return new BridgeMarshal.CallbackValue(
            shape,
            adapter,
            type.IsNullable,
            delegateTypeName,
            parameterTypes.Select(CSharpName).ToList(),
            returnType is null ? null : CSharpName(returnType));
    }

    // ---- API types ----

    private BridgeMarshal PlanApiType(TypeRef type, MarshalPosition position)
    {
        var apiType = index.FindType(type)
            ?? throw new BridgePlanningException($"type {type.FullName} is not part of the public model");

        if (index.IsTypeUnsupported(type, out var reason))
            throw new BridgePlanningException($"references skipped type {apiType.Name} ({reason})");

        var plan = planFor(ApiIndex.Key(apiType))
            ?? throw new BridgePlanningException($"type {type.FullName} has no classification plan");

        if (plan.Kind == TypePlanKind.Enum)
        {
            if (type.IsNullable && position is MarshalPosition.CallbackReturn)
                throw new BridgePlanningException($"nullable enum {apiType.Name} cannot be returned from a callback");

            return new BridgeMarshal.EnumValue(apiType.FullName, type.IsNullable);
        }

        if (IsUserImplemented(apiType))
        {
            if (position is not (MarshalPosition.Parameter))
                throw new BridgePlanningException($"user-implemented interface {apiType.Name} only crosses the bridge as a parameter");

            var proxy = ProxyFor(apiType)
                ?? throw new BridgePlanningException($"user-implemented interface {apiType.Name} has a shape the proxy generator does not support");

            return new BridgeMarshal.UserImplValue(proxy, type.IsNullable);
        }

        if (plan.Kind is not (TypePlanKind.Class or TypePlanKind.Interface))
            throw new BridgePlanningException($"{apiType.Name} ({plan.Kind}) instances cannot cross the bridge");

        if (type.TypeArguments.Count > 0)
            throw new BridgePlanningException($"constructed generic {type.Render()} cannot cross the bridge");

        // A handle received by the client must be wrapped in a constructible
        // client-side class; abstract classes have none.
        var needsWrap = position is MarshalPosition.Return or MarshalPosition.CallbackReturn or MarshalPosition.CallbackParameter;
        if (plan.Kind == TypePlanKind.Class && apiType.IsAbstract && needsWrap)
            throw new BridgePlanningException($"abstract class {apiType.Name} cannot be wrapped on the Kotlin side");

        return new BridgeMarshal.Handle(apiType.FullName, type.IsNullable);
    }

    // ---- user-implemented interface proxies ----

    private ProxyPlan? ProxyFor(ApiType interfaceType)
    {
        if (proxyCache.TryGetValue(interfaceType.FullName, out var cached))
            return cached;

        var plan = BuildProxy(interfaceType);
        proxyCache[interfaceType.FullName] = plan;
        return plan;
    }

    private ProxyPlan? BuildProxy(ApiType interfaceType)
    {
        // v1 proxies support exactly one abstract method and no properties.
        if (interfaceType.Properties.Count > 0 || interfaceType.Methods.Count != 1)
            return null;

        var method = interfaceType.Methods[0];
        if (method.TypeParameters.Count > 0)
            return null;

        try
        {
            var wraps = method.Parameters
                .Select(p => Plan(p.Type, MarshalPosition.CallbackParameter))
                .ToList();
            var returnWrap = method.ReturnType.Is("System.Void")
                ? (BridgeMarshal)new BridgeMarshal.Nothing()
                : Plan(method.ReturnType, MarshalPosition.CallbackReturn);

            var shape = BridgeAbi.ShapeFor(wraps, returnWrap);
            var adapter = new CallbackAdapter(shape, wraps, returnWrap, FirstParameterIsDslScope: false);

            var simpleName = interfaceType.Name.Length > 1 && interfaceType.Name[0] == 'I' && char.IsUpper(interfaceType.Name[1])
                ? interfaceType.Name[1..]
                : interfaceType.Name;

            return new ProxyPlan(
                ProxyExportEntryPoint: "QP_Proxy_" + simpleName + "_new",
                CSharpInterface: interfaceType.FullName,
                CSharpClassName: simpleName + "Proxy",
                CSharpMethodName: method.Name,
                CSharpParameterTypes: method.Parameters.Select(p => CSharpName(p.Type)).ToList(),
                CSharpReturnType: method.ReturnType.Is("System.Void") ? null : CSharpName(method.ReturnType),
                Method: adapter);
        }
        catch (BridgePlanningException)
        {
            return null;
        }
    }
}
