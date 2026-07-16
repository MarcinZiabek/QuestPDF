using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>A function assigned to a TypeScript type (arity-aware key of the target).</summary>
public sealed record TsPlacedFunction(
    string TargetTypeKey,
    TsFunction Function,
    bool IsStatic);

public sealed record TsMethodOutcome
{
    public required ReportEntry Report { get; init; }
    public IReadOnlyList<TsPlacedFunction> Emissions { get; init; } = [];
    public ApiMethod? Rewritten { get; init; }

    public static TsMethodOutcome Rewrite(ApiMethod method) => new()
    {
        Report = null!,
        Rewritten = method,
    };
}

public interface ITsMethodRule
{
    string Name { get; }
    bool Matches(TsMethodContext context);
    TsMethodOutcome Apply(TsMethodContext context);
}

/// <summary>
/// Ordered member pipeline — the first matching rule classifies the method.
/// A rule may instead rewrite the method (e.g. stripping caller-info
/// parameters), which restarts the pipeline on the rewritten member.
/// </summary>
public static class TsMethodRulePipeline
{
    public static IReadOnlyList<ITsMethodRule> Default { get; } =
    [
        new TsManualOverrideMethodRule(),
        new TsCompilerArtifactRule(),
        new TsEqualityInfrastructureRule(),
        new TsCallerInfoStripRule(),
        new TsAsyncSurfaceRule(),
        new TsStreamParameterRule(),
        new TsConversionOperatorRule(),
        new TsOperatorFallbackRule(),
        new TsSelfGenericExtensionRule(),
        new TsExtensionToMemberRule(),
        new TsStandardMemberRule(),
    ];

    public static TsMethodOutcome Classify(TsMethodContext context)
    {
        for (var iterations = 0; iterations < 10; iterations++)
        {
            var matched = Default.FirstOrDefault(rule => rule.Matches(context))
                ?? throw new InvalidOperationException($"No method rule matched {context.Method.DocId}.");

            var outcome = matched.Apply(context);
            if (outcome.Rewritten is null)
                return outcome;

            context = context with { Method = outcome.Rewritten };
        }

        throw new InvalidOperationException($"Method rule pipeline did not converge for {context.Method.DocId}.");
    }
}

/// <summary>Shared invocation-plan helpers for method rules.</summary>
public static class TsBridgeInvocations
{
    /// <summary>
    /// Explicit C# generic arguments for a generic method's monomorphic export:
    /// every type parameter is instantiated at its single API constraint.
    /// Returns null when a type parameter has no such bound.
    /// </summary>
    public static IReadOnlyList<string>? GenericArgumentsFor(ApiMethod method)
    {
        var arguments = new List<string>();
        foreach (var parameter in method.TypeParameters)
        {
            // new()/struct constraints imply reflective instantiation of T,
            // which instantiating at the interface bound cannot satisfy.
            if (parameter.HasDefaultConstructorConstraint || parameter.HasValueTypeConstraint)
                return null;

            var bound = parameter.Constraints.FirstOrDefault(c => c is { Kind: TypeRefKind.Named, IsApiAssemblyType: true });
            if (bound is null)
                return null;
            arguments.Add(bound.FullName);
        }

        return arguments;
    }
}

public sealed class TsManualOverrideMethodRule : ITsMethodRule
{
    public string Name => "manual-override";

    public bool Matches(TsMethodContext context) =>
        context.Services.Overrides.Contains(context.Method.DocId);

    public TsMethodOutcome Apply(TsMethodContext context) => new()
    {
        Report = TsReports.For(context, ApiClassification.ManualOverride,
            "excluded via the manual-overrides file; implemented by hand in the manual/ source set", Name),
    };
}

public sealed class TsCompilerArtifactRule : ITsMethodRule
{
    public string Name => "compiler-artifact";

    public bool Matches(TsMethodContext context) =>
        context.Method.Name.Contains('<') || context.Method.Name.Contains('$');

    public TsMethodOutcome Apply(TsMethodContext context) => new()
    {
        Report = TsReports.For(context, ApiClassification.Infrastructure,
            "compiler-generated record member", Name),
    };
}

/// <summary>
/// Equality plumbing (Equals, GetHashCode, ==, !=) is not part of the DSL
/// surface; JavaScript object identity covers it. Declared ToString overrides
/// are NOT filtered: they carry real formatting semantics (Color renders as
/// "#RRGGBB") and bridge as Object.prototype.toString overrides, so template
/// literals produce the same text as C# string interpolation.
/// </summary>
public sealed class TsEqualityInfrastructureRule : ITsMethodRule
{
    public string Name => "equality-infrastructure";

    public bool Matches(TsMethodContext context)
    {
        var m = context.Method;
        return m.Name switch
        {
            "GetHashCode" when m.Parameters.Count == 0 => true,
            "Equals" when m.Parameters.Count == 1 => true,
            "GetType" when m.Parameters.Count == 0 => true,
            "op_Equality" or "op_Inequality" => true,
            _ => false,
        };
    }

    public TsMethodOutcome Apply(TsMethodContext context) => new()
    {
        Report = TsReports.For(context, ApiClassification.Infrastructure,
            "System.Object / equality infrastructure; not part of the DSL surface", Name),
    };
}

public sealed class TsCallerInfoStripRule : ITsMethodRule
{
    public string Name => "caller-info-strip";

    public bool Matches(TsMethodContext context) =>
        context.Method.Parameters.Any(p => p.IsCallerInfo);

    public TsMethodOutcome Apply(TsMethodContext context) =>
        TsMethodOutcome.Rewrite(context.Method with
        {
            Parameters = context.Method.Parameters.Where(p => !p.IsCallerInfo).ToList(),
        });
}

public sealed class TsAsyncSurfaceRule : ITsMethodRule
{
    public string Name => "async-surface";

    public bool Matches(TsMethodContext context)
    {
        static bool IsAsyncType(TypeRef type) =>
            type.Kind == TypeRefKind.Named &&
            (type.FullName.StartsWith("System.Threading.Tasks.", StringComparison.Ordinal) ||
             type.FullName == "System.Threading.CancellationToken");

        return IsAsyncType(context.Method.ReturnType) ||
               context.Method.Parameters.Any(p => IsAsyncType(p.Type));
    }

    public TsMethodOutcome Apply(TsMethodContext context) => new()
    {
        Report = TsReports.For(context, ApiClassification.Unsupported,
            "asynchronous surface (Task/CancellationToken); the bridge is synchronous and the sync overload is generated", Name),
    };
}

public sealed class TsStreamParameterRule : ITsMethodRule
{
    public string Name => "stream-parameter";

    public bool Matches(TsMethodContext context)
    {
        static bool IsStream(TypeRef type) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.IO.Stream";

        return IsStream(context.Method.ReturnType) ||
               context.Method.Parameters.Any(p => IsStream(p.Type));
    }

    public TsMethodOutcome Apply(TsMethodContext context) => new()
    {
        Report = TsReports.For(context, ApiClassification.Unsupported,
            "System.IO.Stream has no direction-neutral bridge mapping; Uint8Array and file-path overloads cover this scenario", Name),
    };
}

/// <summary>
/// Conversion operators: towards the API type they become static
/// <c>from(...)</c> factories; away from it they become <c>toX()</c> members
/// (skipped when they collide with toString()). The C# side performs a cast.
/// </summary>
public sealed class TsConversionOperatorRule : ITsMethodRule
{
    public string Name => "conversion-operator";

    public bool Matches(TsMethodContext context) =>
        context.Method.Name is "op_Implicit" or "op_Explicit";

    public TsMethodOutcome Apply(TsMethodContext context)
    {
        var method = context.Method;
        var declaring = context.DeclaringType;
        var services = context.Services;

        if (method.Parameters.Count != 1)
            return Unsupported(context, "conversion operator with unexpected arity");

        var sourceIsSelf = ApiIndex.Key(method.Parameters[0].Type) == ApiIndex.Key(declaring);
        var builder = new TsFunctionBuilder(context.Services);

        if (sourceIsSelf)
        {
            // T → X: instance member toX(); C# side: (X)receiver.
            var mappedReturn = services.Mapper.Map(method.ReturnType);
            if (!mappedReturn.Success)
                return Unsupported(context, mappedReturn.FailureReason!);

            if (mappedReturn.Type!.Name == "string")
                return Unsupported(context, "conversion to String collides with toString(); use toString() instead");

            var targetName = BridgePlanner.CSharpName(method.ReturnType);
            var functionName = "to" + targetName.Split('<')[0][(targetName.Split('<')[0].LastIndexOf('.') + 1)..];
            var invocation = new CSharpInvocation(
                InvocationKind.ConversionFromSelf,
                declaring.FullName,
                targetName,
                [],
                declaring.FullName);

            var result = builder.Build(context, declaring, functionName, [],
                invocation, TsFunctionBuilder.TypeRefOf(declaring));
            if (result.Failure is not null)
                return Unsupported(context, result.Failure);

            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Generated,
                    $"conversion operator mapped to member {functionName}()", Name),
                Emissions = [new TsPlacedFunction(ApiIndex.Key(declaring), result.Function!, false)],
            };
        }

        // X → T: static factory from(x); C# side: (Declaring)arg.
        var fromInvocation = new CSharpInvocation(
            InvocationKind.ConversionToSelf,
            declaring.FullName,
            declaring.FullName,
            [],
            null);

        var build = builder.Build(context, declaring, "from", method.Parameters,
            fromInvocation, receiverType: null,
            forcedCSharpReturn: TsFunctionBuilder.TypeRefOf(declaring));
        if (build.Failure is not null)
            return Unsupported(context, build.Failure);

        return new TsMethodOutcome
        {
            Report = TsReports.For(context, ApiClassification.Generated,
                "conversion operator mapped to static factory from(...)", Name),
            Emissions = [new TsPlacedFunction(ApiIndex.Key(declaring), build.Function!, true)],
        };
    }

    private TsMethodOutcome Unsupported(TsMethodContext context, string reason) => new()
    {
        Report = TsReports.For(context, ApiClassification.Unsupported, reason, Name),
    };
}

public sealed class TsOperatorFallbackRule : ITsMethodRule
{
    public string Name => "operator-fallback";

    public bool Matches(TsMethodContext context) => context.Method.Kind == ApiMethodKind.Operator;

    public TsMethodOutcome Apply(TsMethodContext context) => new()
    {
        Report = TsReports.For(context, ApiClassification.Unsupported,
            $"operator {context.Method.Name} has no TypeScript mapping rule", Name),
    };
}

/// <summary>
/// The self-generic fluent pattern <c>T FontSize&lt;T&gt;(this T descriptor, ...)
/// where T : TextSpanDescriptor</c> becomes a member on the constraint class
/// returning the class type, plus covariant redeclarations in every generated
/// subclass. Each emission is a monomorphic export instantiated at its
/// concrete receiver type.
/// </summary>
public sealed class TsSelfGenericExtensionRule : ITsMethodRule
{
    public string Name => "self-generic-extension";

    public bool Matches(TsMethodContext context)
    {
        var method = context.Method;
        return method.IsExtension &&
               method.ExtensionReceiver is { Type.Kind: TypeRefKind.GenericParameter } &&
               ConstraintOf(context) is not null;
    }

    public TsMethodOutcome Apply(TsMethodContext context)
    {
        var services = context.Services;
        var method = context.Method;
        var constraint = ConstraintOf(context)!;

        var constraintType = services.Index.FindType(constraint);
        if (constraintType is null || services.PlanFor(ApiIndex.Key(constraintType))?.Kind != TypePlanKind.Class)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported,
                    $"self-generic receiver constraint {constraint.FullName} is not a generated class", Name),
            };
        }

        var receiverName = method.ExtensionReceiver!.Type.GenericParameterName;

        // Only the receiver-and-return use of T is supported; T anywhere else in
        // the signature has no member-function equivalent.
        var usesTElsewhere = method.Parameters.Any(p => MentionsParameter(p.Type, receiverName));
        if (usesTElsewhere)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported,
                    "self-generic receiver type parameter appears in the parameter list; cannot map to a member function", Name),
            };
        }

        var returnsSelf = method.ReturnType is { Kind: TypeRefKind.GenericParameter } r && r.GenericParameterName == receiverName;
        var builder = new TsFunctionBuilder(services);
        var subclasses = services.Index.TransitiveSubclasses(constraintType.FullName)
            .Where(s => services.PlanFor(ApiIndex.Key(s))?.Kind == TypePlanKind.Class)
            .ToList();

        var emissions = new List<TsPlacedFunction>();

        var baseResult = BuildResultFor(context, builder, method, constraintType, returnsSelf);
        if (baseResult.Failure is not null)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported, baseResult.Failure, Name),
            };
        }

        emissions.Add(new TsPlacedFunction(ApiIndex.Key(constraintType), baseResult.Function!, false));

        if (returnsSelf)
        {
            foreach (var subclass in subclasses)
            {
                // TypeScript redeclarations are independent methods, so unlike
                // Kotlin overrides they keep their default values.
                var overrideResult = BuildResultFor(context, builder, method, subclass, returnsSelf);
                if (overrideResult.Failure is null)
                    emissions.Add(new TsPlacedFunction(ApiIndex.Key(subclass), overrideResult.Function!, false));
            }
        }

        var overrideNote = emissions.Count > 1 ? $" (+{emissions.Count - 1} covariant overrides)" : "";
        return new TsMethodOutcome
        {
            Report = TsReports.For(context, ApiClassification.Generated,
                $"self-generic extension mapped to member on {constraintType.Name}{overrideNote}", Name),
            Emissions = emissions,
        };
    }

    private static TsFunctionBuilder.BuildResult BuildResultFor(
        TsMethodContext context,
        TsFunctionBuilder builder,
        ApiMethod method,
        ApiType concreteReceiver,
        bool returnsSelf)
    {
        var services = context.Services;
        var receiverRef = TsFunctionBuilder.TypeRefOf(concreteReceiver);

        var invocation = new CSharpInvocation(
            InvocationKind.ExtensionMethod,
            method.DeclaringTypeFullName,
            method.Name,
            [concreteReceiver.FullName],
            concreteReceiver.FullName);

        return builder.Build(
            context, concreteReceiver, TsNameMapper.Member(method.Name), method.Parameters,
            invocation, receiverRef,
            dropTypeParameters: true,
            forcedCSharpReturn: returnsSelf ? receiverRef : null,
            forcedTsReturn: returnsSelf ? SelfReturn(services, concreteReceiver) : null);
    }

    private static TsType SelfReturn(TsClassifierServices services, ApiType type) =>
        TsType.Named(TsNameMapper.Module(type.Namespace), services.Index.NestedName(type.FullName));

    private static TypeRef? ConstraintOf(TsMethodContext context)
    {
        var receiverName = context.Method.ExtensionReceiver!.Type.GenericParameterName;
        var parameter = context.Method.TypeParameters.FirstOrDefault(p => p.Name == receiverName);
        var constraint = parameter?.Constraints.FirstOrDefault(c => c is { Kind: TypeRefKind.Named, IsApiAssemblyType: true });
        return constraint;
    }

    private static bool MentionsParameter(TypeRef type, string name) => type.Kind switch
    {
        TypeRefKind.GenericParameter => type.GenericParameterName == name,
        TypeRefKind.Array => MentionsParameter(type.ElementType!, name),
        _ => type.TypeArguments.Any(a => MentionsParameter(a, name)),
    };
}

/// <summary>
/// Extension methods on QuestPDF types become member functions on the generated
/// receiver type — members give better discoverability than free functions
/// since the receiver types are ours. The export calls the C# extension holder
/// statically.
/// </summary>
public sealed class TsExtensionToMemberRule : ITsMethodRule
{
    public string Name => "extension-to-member";

    public bool Matches(TsMethodContext context) =>
        context.Method.IsExtension &&
        context.Method.ExtensionReceiver is { Type: { Kind: TypeRefKind.Named, IsApiAssemblyType: true } };

    public TsMethodOutcome Apply(TsMethodContext context)
    {
        var services = context.Services;
        var method = context.Method;
        var receiverType = method.ExtensionReceiver!.Type;
        var receiver = services.Index.FindType(receiverType);
        var plan = receiver is null ? null : services.PlanFor(ApiIndex.Key(receiver));

        if (receiver is null || plan is null || plan.Kind is not (TypePlanKind.Class or TypePlanKind.Interface))
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported,
                    $"extension receiver {receiverType.FullName} is not generated as a class or interface", Name),
            };
        }

        var genericArguments = TsBridgeInvocations.GenericArgumentsFor(method);
        if (genericArguments is null)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported,
                    "generic method without an API-bound type parameter cannot cross the bridge", Name),
            };
        }

        var invocation = new CSharpInvocation(
            InvocationKind.ExtensionMethod,
            method.DeclaringTypeFullName,
            method.Name,
            genericArguments,
            receiverType.FullName);

        var builder = new TsFunctionBuilder(services);
        var result = builder.Build(context, receiver, TsNameMapper.Member(method.Name),
            method.Parameters, invocation, receiverType);

        if (result.Failure is not null)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported, result.Failure, Name),
            };
        }

        return new TsMethodOutcome
        {
            Report = TsReports.For(context, ApiClassification.Generated,
                $"extension method mapped to member function on {receiver.Name}", Name),
            Emissions = [new TsPlacedFunction(ApiIndex.Key(receiver), result.Function!, false)],
        };
    }
}

/// <summary>Fallback: ordinary instance and static members mapped in place.</summary>
public sealed class TsStandardMemberRule : ITsMethodRule
{
    public string Name => "standard-member";

    public bool Matches(TsMethodContext context) => true;

    public TsMethodOutcome Apply(TsMethodContext context)
    {
        var services = context.Services;
        var method = context.Method;
        var declaring = context.DeclaringType;
        var plan = services.PlanFor(ApiIndex.Key(declaring));

        if (plan is null || !plan.Emits)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported,
                    $"declaring type is not generated ({plan?.Detail ?? "unknown type"})", Name),
            };
        }

        if (method.IsExtension)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported,
                    "extension method with an unsupported receiver shape", Name),
            };
        }

        var builder = new TsFunctionBuilder(services);

        // Members of user-implemented interfaces stay signature-only: user
        // classes implement them and the bridge calls back through a generated proxy.
        if (plan.Kind == TypePlanKind.Interface && services.Bridge.IsUserImplemented(declaring))
        {
            var abstractResult = builder.Build(context, declaring, TsNameMapper.Member(method.Name),
                method.Parameters, invocation: null, receiverType: null);

            if (abstractResult.Failure is not null)
            {
                return new TsMethodOutcome
                {
                    Report = TsReports.For(context, ApiClassification.Unsupported, abstractResult.Failure, Name),
                };
            }

            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Generated,
                    "user-implemented interface member kept abstract; bridged through a generated proxy", Name),
                Emissions = [new TsPlacedFunction(ApiIndex.Key(declaring), abstractResult.Function!, false)],
            };
        }

        var genericArguments = TsBridgeInvocations.GenericArgumentsFor(method);
        if (genericArguments is null)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported,
                    "generic method without an API-bound type parameter cannot cross the bridge", Name),
            };
        }

        var isStatic = method.IsStatic;
        var invocation = new CSharpInvocation(
            isStatic ? InvocationKind.StaticMethod : InvocationKind.InstanceMethod,
            declaring.FullName,
            method.Name,
            genericArguments,
            isStatic ? null : declaring.FullName);

        var result = builder.Build(context, declaring, TsNameMapper.Member(method.Name),
            method.Parameters, invocation,
            receiverType: isStatic ? null : TsFunctionBuilder.TypeRefOf(declaring));

        if (result.Failure is not null)
        {
            return new TsMethodOutcome
            {
                Report = TsReports.For(context, ApiClassification.Unsupported, result.Failure, Name),
            };
        }

        return new TsMethodOutcome
        {
            Report = TsReports.For(context, ApiClassification.Generated,
                isStatic ? "static method mapped to a static member function" : "mapped to member function", Name),
            Emissions = [new TsPlacedFunction(ApiIndex.Key(declaring), result.Function!, isStatic)],
        };
    }
}

public static class TsReports
{
    public static ReportEntry For(TsMethodContext context, ApiClassification classification, string detail, string rule) =>
        new(context.Method.DocId, SignatureRenderer.Render(context.Method), classification, detail, rule);
}
