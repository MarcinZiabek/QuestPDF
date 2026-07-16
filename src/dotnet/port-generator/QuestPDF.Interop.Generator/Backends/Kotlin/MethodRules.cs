using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>Services shared by member rules.</summary>
public sealed class ClassifierServices
{
    public required ApiIndex Index { get; init; }
    public required TypeMapper Mapper { get; init; }
    public required BridgePlanner Bridge { get; init; }
    public required ManualOverrides Overrides { get; init; }
    public required Func<string, TypePlan?> PlanFor { get; init; }
}

public sealed record MethodContext(
    ApiType DeclaringType,
    ApiMethod Method,
    ClassifierServices Services);

/// <summary>A function assigned to a Kotlin type (arity-aware key of the target).</summary>
public sealed record PlacedFunction(
    string TargetTypeKey,
    KotlinFunction Function,
    bool InCompanion);

public sealed record MethodOutcome
{
    public required ReportEntry Report { get; init; }
    public IReadOnlyList<PlacedFunction> Emissions { get; init; } = [];
    public ApiMethod? Rewritten { get; init; }

    public static MethodOutcome Rewrite(ApiMethod method) => new()
    {
        Report = null!,
        Rewritten = method,
    };
}

public interface IMethodRule
{
    string Name { get; }
    bool Matches(MethodContext context);
    MethodOutcome Apply(MethodContext context);
}

/// <summary>
/// Ordered member pipeline — the first matching rule classifies the method.
/// A rule may instead rewrite the method (e.g. stripping caller-info
/// parameters), which restarts the pipeline on the rewritten member.
/// </summary>
public static class MethodRulePipeline
{
    public static IReadOnlyList<IMethodRule> Default { get; } =
    [
        new ManualOverrideMethodRule(),
        new CompilerArtifactRule(),
        new EqualityInfrastructureRule(),
        new CallerInfoStripRule(),
        new AsyncSurfaceRule(),
        new StreamParameterRule(),
        new ConversionOperatorRule(),
        new OperatorFallbackRule(),
        new SelfGenericExtensionRule(),
        new ExtensionToMemberRule(),
        new StandardMemberRule(),
    ];

    public static MethodOutcome Classify(MethodContext context)
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
public static class BridgeInvocations
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

public sealed class ManualOverrideMethodRule : IMethodRule
{
    public string Name => "manual-override";

    public bool Matches(MethodContext context) =>
        context.Services.Overrides.Contains(context.Method.DocId);

    public MethodOutcome Apply(MethodContext context) => new()
    {
        Report = Reports.For(context, ApiClassification.ManualOverride,
            "excluded via manual-overrides.txt; implemented by hand in the manual/ source set", Name),
    };
}

public sealed class CompilerArtifactRule : IMethodRule
{
    public string Name => "compiler-artifact";

    public bool Matches(MethodContext context) =>
        context.Method.Name.Contains('<') || context.Method.Name.Contains('$');

    public MethodOutcome Apply(MethodContext context) => new()
    {
        Report = Reports.For(context, ApiClassification.Infrastructure,
            "compiler-generated record member", Name),
    };
}

/// <summary>
/// Equality plumbing (Equals, GetHashCode, ==, !=) is covered by Kotlin's Any
/// and operator conventions; it is not part of the DSL surface. Declared
/// ToString overrides are NOT filtered: they carry real formatting semantics
/// (Color renders as "#RRGGBB") and bridge as kotlin.Any.toString overrides.
/// </summary>
public sealed class EqualityInfrastructureRule : IMethodRule
{
    public string Name => "equality-infrastructure";

    public bool Matches(MethodContext context)
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

    public MethodOutcome Apply(MethodContext context) => new()
    {
        Report = Reports.For(context, ApiClassification.Infrastructure,
            "System.Object / equality infrastructure; Kotlin's Any and == cover this", Name),
    };
}

public sealed class CallerInfoStripRule : IMethodRule
{
    public string Name => "caller-info-strip";

    public bool Matches(MethodContext context) =>
        context.Method.Parameters.Any(p => p.IsCallerInfo);

    public MethodOutcome Apply(MethodContext context) =>
        MethodOutcome.Rewrite(context.Method with
        {
            Parameters = context.Method.Parameters.Where(p => !p.IsCallerInfo).ToList(),
        });
}

public sealed class AsyncSurfaceRule : IMethodRule
{
    public string Name => "async-surface";

    public bool Matches(MethodContext context)
    {
        static bool IsAsyncType(TypeRef type) =>
            type.Kind == TypeRefKind.Named &&
            (type.FullName.StartsWith("System.Threading.Tasks.", StringComparison.Ordinal) ||
             type.FullName == "System.Threading.CancellationToken");

        return IsAsyncType(context.Method.ReturnType) ||
               context.Method.Parameters.Any(p => IsAsyncType(p.Type));
    }

    public MethodOutcome Apply(MethodContext context) => new()
    {
        Report = Reports.For(context, ApiClassification.Unsupported,
            "asynchronous surface (Task/CancellationToken); the bridge is synchronous and the sync overload is generated", Name),
    };
}

public sealed class StreamParameterRule : IMethodRule
{
    public string Name => "stream-parameter";

    public bool Matches(MethodContext context)
    {
        static bool IsStream(TypeRef type) =>
            type.Kind == TypeRefKind.Named && type.FullName == "System.IO.Stream";

        return IsStream(context.Method.ReturnType) ||
               context.Method.Parameters.Any(p => IsStream(p.Type));
    }

    public MethodOutcome Apply(MethodContext context) => new()
    {
        Report = Reports.For(context, ApiClassification.Unsupported,
            "System.IO.Stream has no direction-neutral bridge mapping; byte[] and file-path overloads cover this scenario", Name),
    };
}

/// <summary>
/// Conversion operators: towards the API type they become companion
/// <c>from(...)</c> factories; away from it they become <c>toX()</c> members
/// (skipped when they collide with toString()). The C# side performs a cast.
/// </summary>
public sealed class ConversionOperatorRule : IMethodRule
{
    public string Name => "conversion-operator";

    public bool Matches(MethodContext context) =>
        context.Method.Name is "op_Implicit" or "op_Explicit";

    public MethodOutcome Apply(MethodContext context)
    {
        var method = context.Method;
        var declaring = context.DeclaringType;
        var services = context.Services;

        if (method.Parameters.Count != 1)
            return Unsupported(context, "conversion operator with unexpected arity");

        var sourceIsSelf = ApiIndex.Key(method.Parameters[0].Type) == ApiIndex.Key(declaring);
        var builder = new FunctionBuilder(context.Services);

        if (sourceIsSelf)
        {
            // T → X: instance member toX(); C# side: (X)receiver.
            var mappedReturn = services.Mapper.Map(method.ReturnType);
            if (!mappedReturn.Success)
                return Unsupported(context, mappedReturn.FailureReason!);

            if (mappedReturn.Type!.Name == "String")
                return Unsupported(context, "conversion to String collides with toString(); use toString() instead");

            var functionName = "to" + mappedReturn.Type.Name.Split('.')[^1];
            var invocation = new CSharpInvocation(
                InvocationKind.ConversionFromSelf,
                declaring.FullName,
                BridgePlanner.CSharpName(method.ReturnType),
                [],
                declaring.FullName);

            var result = builder.Build(context, declaring, functionName, [],
                invocation, FunctionBuilder.TypeRefOf(declaring));
            if (result.Failure is not null)
                return Unsupported(context, result.Failure);

            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Generated,
                    $"conversion operator mapped to member {functionName}()", Name),
                Emissions = [new PlacedFunction(ApiIndex.Key(declaring), result.Function!, false)],
            };
        }

        // X → T: companion factory from(x); C# side: (Declaring)arg.
        var fromInvocation = new CSharpInvocation(
            InvocationKind.ConversionToSelf,
            declaring.FullName,
            declaring.FullName,
            [],
            null);

        var build = builder.Build(context, declaring, "from", method.Parameters,
            fromInvocation, receiverType: null,
            forcedCSharpReturn: FunctionBuilder.TypeRefOf(declaring));
        if (build.Failure is not null)
            return Unsupported(context, build.Failure);

        return new MethodOutcome
        {
            Report = Reports.For(context, ApiClassification.Generated,
                "conversion operator mapped to companion factory from(...)", Name),
            Emissions = [new PlacedFunction(ApiIndex.Key(declaring), build.Function!, true)],
        };
    }

    private MethodOutcome Unsupported(MethodContext context, string reason) => new()
    {
        Report = Reports.For(context, ApiClassification.Unsupported, reason, Name),
    };
}

public sealed class OperatorFallbackRule : IMethodRule
{
    public string Name => "operator-fallback";

    public bool Matches(MethodContext context) => context.Method.Kind == ApiMethodKind.Operator;

    public MethodOutcome Apply(MethodContext context) => new()
    {
        Report = Reports.For(context, ApiClassification.Unsupported,
            $"operator {context.Method.Name} has no Kotlin mapping rule", Name),
    };
}

/// <summary>
/// The self-generic fluent pattern <c>T FontSize&lt;T&gt;(this T descriptor, ...)
/// where T : TextSpanDescriptor</c> becomes a member on the constraint class
/// returning the class type, plus covariant overrides in every generated
/// subclass. Each emission is a monomorphic export instantiated at its
/// concrete receiver type.
/// </summary>
public sealed class SelfGenericExtensionRule : IMethodRule
{
    public string Name => "self-generic-extension";

    public bool Matches(MethodContext context)
    {
        var method = context.Method;
        return method.IsExtension &&
               method.ExtensionReceiver is { Type.Kind: TypeRefKind.GenericParameter } &&
               ConstraintOf(context) is not null;
    }

    public MethodOutcome Apply(MethodContext context)
    {
        var services = context.Services;
        var method = context.Method;
        var constraint = ConstraintOf(context)!;

        var constraintType = services.Index.FindType(constraint);
        if (constraintType is null || services.PlanFor(ApiIndex.Key(constraintType))?.Kind != TypePlanKind.Class)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported,
                    $"self-generic receiver constraint {constraint.FullName} is not a generated class", Name),
            };
        }

        var receiverName = method.ExtensionReceiver!.Type.GenericParameterName;

        // Only the receiver-and-return use of T is supported; T anywhere else in
        // the signature has no member-function equivalent.
        var usesTElsewhere = method.Parameters.Any(p => MentionsParameter(p.Type, receiverName));
        if (usesTElsewhere)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported,
                    "self-generic receiver type parameter appears in the parameter list; cannot map to a member function", Name),
            };
        }

        var returnsSelf = method.ReturnType is { Kind: TypeRefKind.GenericParameter } r && r.GenericParameterName == receiverName;
        var builder = new FunctionBuilder(services);
        var subclasses = services.Index.TransitiveSubclasses(constraintType.FullName)
            .Where(s => services.PlanFor(ApiIndex.Key(s))?.Kind == TypePlanKind.Class)
            .ToList();

        var emissions = new List<PlacedFunction>();

        BuildResultFor(context, builder, method, constraintType, returnsSelf,
            markOpen: returnsSelf && subclasses.Count > 0, markOverride: false,
            out var baseResult);

        if (baseResult.Failure is not null)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported, baseResult.Failure, Name),
            };
        }

        emissions.Add(new PlacedFunction(ApiIndex.Key(constraintType), baseResult.Function!, false));

        if (returnsSelf)
        {
            foreach (var subclass in subclasses)
            {
                BuildResultFor(context, builder, method, subclass, returnsSelf,
                    markOpen: false, markOverride: true, out var overrideResult);

                if (overrideResult.Failure is null)
                {
                    // Kotlin overrides inherit default values and may not restate them.
                    var withoutDefaults = overrideResult.Function! with
                    {
                        Parameters = overrideResult.Function!.Parameters
                            .Select(p => p with { DefaultValue = null })
                            .ToList(),
                    };
                    emissions.Add(new PlacedFunction(ApiIndex.Key(subclass), withoutDefaults, false));
                }
            }
        }

        var overrideNote = emissions.Count > 1 ? $" (+{emissions.Count - 1} covariant overrides)" : "";
        return new MethodOutcome
        {
            Report = Reports.For(context, ApiClassification.Generated,
                $"self-generic extension mapped to member on {constraintType.Name}{overrideNote}", Name),
            Emissions = emissions,
        };
    }

    private static void BuildResultFor(
        MethodContext context,
        FunctionBuilder builder,
        ApiMethod method,
        ApiType concreteReceiver,
        bool returnsSelf,
        bool markOpen,
        bool markOverride,
        out FunctionBuilder.BuildResult result)
    {
        var services = context.Services;
        var receiverRef = FunctionBuilder.TypeRefOf(concreteReceiver);

        var invocation = new CSharpInvocation(
            InvocationKind.ExtensionMethod,
            method.DeclaringTypeFullName,
            method.Name,
            [concreteReceiver.FullName],
            concreteReceiver.FullName);

        result = builder.Build(
            context, concreteReceiver, NameMapper.Member(method.Name), method.Parameters,
            invocation, receiverRef,
            dropTypeParameters: true,
            markOpen: markOpen,
            markOverride: markOverride,
            forcedCSharpReturn: returnsSelf ? receiverRef : null,
            forcedKotlinReturn: returnsSelf ? SelfReturn(services, concreteReceiver) : null);
    }

    private static KType SelfReturn(ClassifierServices services, ApiType type) =>
        KType.Named(NameMapper.Package(type.Namespace), services.Index.NestedName(type.FullName));

    private static TypeRef? ConstraintOf(MethodContext context)
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
/// receiver type — members give better discoverability than Kotlin extension
/// functions since the receiver types are ours. The export calls the C#
/// extension holder statically.
/// </summary>
public sealed class ExtensionToMemberRule : IMethodRule
{
    public string Name => "extension-to-member";

    public bool Matches(MethodContext context) =>
        context.Method.IsExtension &&
        context.Method.ExtensionReceiver is { Type: { Kind: TypeRefKind.Named, IsApiAssemblyType: true } };

    public MethodOutcome Apply(MethodContext context)
    {
        var services = context.Services;
        var method = context.Method;
        var receiverType = method.ExtensionReceiver!.Type;
        var receiver = services.Index.FindType(receiverType);
        var plan = receiver is null ? null : services.PlanFor(ApiIndex.Key(receiver));

        if (receiver is null || plan is null || plan.Kind is not (TypePlanKind.Class or TypePlanKind.Interface))
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported,
                    $"extension receiver {receiverType.FullName} is not generated as a class or interface", Name),
            };
        }

        var genericArguments = BridgeInvocations.GenericArgumentsFor(method);
        if (genericArguments is null)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported,
                    "generic method without an API-bound type parameter cannot cross the bridge", Name),
            };
        }

        var invocation = new CSharpInvocation(
            InvocationKind.ExtensionMethod,
            method.DeclaringTypeFullName,
            method.Name,
            genericArguments,
            receiverType.FullName);

        var builder = new FunctionBuilder(services);
        var result = builder.Build(context, receiver, NameMapper.Member(method.Name),
            method.Parameters, invocation, receiverType);

        if (result.Failure is not null)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported, result.Failure, Name),
            };
        }

        return new MethodOutcome
        {
            Report = Reports.For(context, ApiClassification.Generated,
                $"extension method mapped to member function on {receiver.Name}", Name),
            Emissions = [new PlacedFunction(ApiIndex.Key(receiver), result.Function!, false)],
        };
    }
}

/// <summary>Fallback: ordinary instance and static members mapped in place.</summary>
public sealed class StandardMemberRule : IMethodRule
{
    public string Name => "standard-member";

    public bool Matches(MethodContext context) => true;

    public MethodOutcome Apply(MethodContext context)
    {
        var services = context.Services;
        var method = context.Method;
        var declaring = context.DeclaringType;
        var plan = services.PlanFor(ApiIndex.Key(declaring));

        if (plan is null || !plan.Emits)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported,
                    $"declaring type is not generated ({plan?.Detail ?? "unknown type"})", Name),
            };
        }

        if (method.IsExtension)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported,
                    "extension method with an unsupported receiver shape", Name),
            };
        }

        var builder = new FunctionBuilder(services);

        // Members of user-implemented interfaces stay abstract: Kotlin classes
        // implement them and the bridge calls back through a generated proxy.
        if (plan.Kind == TypePlanKind.Interface && services.Bridge.IsUserImplemented(declaring))
        {
            var abstractResult = builder.Build(context, declaring, NameMapper.Member(method.Name),
                method.Parameters, invocation: null, receiverType: null);

            if (abstractResult.Failure is not null)
            {
                return new MethodOutcome
                {
                    Report = Reports.For(context, ApiClassification.Unsupported, abstractResult.Failure, Name),
                };
            }

            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Generated,
                    "user-implemented interface member kept abstract; bridged through a generated proxy", Name),
                Emissions = [new PlacedFunction(ApiIndex.Key(declaring), abstractResult.Function!, false)],
            };
        }

        var genericArguments = BridgeInvocations.GenericArgumentsFor(method);
        if (genericArguments is null)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported,
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

        var inCompanion = isStatic && plan.Kind == TypePlanKind.Class;
        var result = builder.Build(context, declaring, NameMapper.Member(method.Name),
            method.Parameters, invocation,
            receiverType: isStatic ? null : FunctionBuilder.TypeRefOf(declaring));

        if (result.Failure is not null)
        {
            return new MethodOutcome
            {
                Report = Reports.For(context, ApiClassification.Unsupported, result.Failure, Name),
            };
        }

        return new MethodOutcome
        {
            Report = Reports.For(context, ApiClassification.Generated,
                inCompanion ? "static method mapped to companion function" : "mapped to member function", Name),
            Emissions = [new PlacedFunction(ApiIndex.Key(declaring), result.Function!, inCompanion)],
        };
    }
}

public static class Reports
{
    public static ReportEntry For(MethodContext context, ApiClassification classification, string detail, string rule) =>
        new(context.Method.DocId, SignatureRenderer.Render(context.Method), classification, detail, rule);
}
