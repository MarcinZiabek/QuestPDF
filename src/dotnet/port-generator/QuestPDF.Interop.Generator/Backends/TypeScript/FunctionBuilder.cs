using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>Services shared by TypeScript member rules.</summary>
public sealed class TsClassifierServices
{
    public required ApiIndex Index { get; init; }
    public required TsTypeMapper Mapper { get; init; }
    public required BridgePlanner Bridge { get; init; }
    public required ManualOverrides Overrides { get; init; }
    public required Func<string, TypePlan?> PlanFor { get; init; }
}

public sealed record TsMethodContext(
    ApiType DeclaringType,
    ApiMethod Method,
    TsClassifierServices Services);

/// <summary>
/// Shared construction of bridged <see cref="TsFunction"/>s from classified
/// methods: maps parameter and return types, converts C# defaults to TypeScript
/// default values, plans the native export, and computes each parameter's
/// runtime kind for overload dispatch.
/// </summary>
public sealed class TsFunctionBuilder(TsClassifierServices services)
{
    public sealed record BuildResult(TsFunction? Function, string? Failure);

    public static TypeRef TypeRefOf(ApiType type) => new()
    {
        Kind = TypeRefKind.Named,
        FullName = type.FullName,
        IsApiAssemblyType = true,
    };

    /// <param name="invocation">C#-side call plan; null produces a signature-only member (user-implemented interfaces).</param>
    /// <param name="receiverType">Receiver crossing as the export's first handle parameter; null for statics and constructors.</param>
    /// <param name="forcedCSharpReturn">Overrides the marshalled return type (self-generic monomorphization, constructors).</param>
    public BuildResult Build(
        TsMethodContext context,
        ApiType targetType,
        string tsName,
        IReadOnlyList<ApiParameter> parameters,
        CSharpInvocation? invocation,
        TypeRef? receiverType,
        bool dropTypeParameters = false,
        TypeRef? forcedCSharpReturn = null,
        TsType? forcedTsReturn = null)
    {
        var method = context.Method;

        // Type parameters, their mappable constraints, and the bounds the
        // bridge uses to marshal generic parameters.
        var typeParameters = new List<TsTypeParameter>();
        var genericBounds = new Dictionary<string, TypeRef>(StringComparer.Ordinal);

        foreach (var parameter in method.TypeParameters)
        {
            var apiBound = parameter.Constraints.FirstOrDefault(c => c is { Kind: TypeRefKind.Named, IsApiAssemblyType: true });
            if (apiBound is not null)
                genericBounds[parameter.Name] = apiBound;

            if (dropTypeParameters)
                continue;

            var constraints = new List<TsType>();
            foreach (var constraint in parameter.Constraints)
            {
                var mapped = services.Mapper.Map(constraint);
                if (!mapped.Success)
                    return new BuildResult(null, $"type-parameter constraint unmappable: {mapped.FailureReason}");
                constraints.Add(mapped.Type!);
            }
            // class/struct/new() constraints have no TypeScript equivalent and are dropped.

            typeParameters.Add(new TsTypeParameter(parameter.Name, constraints));
        }

        // Parameters: TypeScript surface type + bridge marshal + runtime kind.
        var tsParameters = new List<TsParameter>();
        var runtimeTests = new List<RuntimeKind>();
        var exportParameters = new List<NativeExportParameter>();

        foreach (var parameter in parameters)
        {
            if (parameter.IsByRef)
                return new BuildResult(null, $"ref/out parameter '{parameter.Name}' has no TypeScript mapping");

            TsType type;
            var isRest = false;

            if (parameter.IsParams && parameter.Type.Kind == TypeRefKind.Array)
            {
                var element = services.Mapper.Map(parameter.Type.ElementType!);
                if (!element.Success)
                    return new BuildResult(null, element.FailureReason);
                type = element.Type!;
                isRest = true;
            }
            else
            {
                var mapped = services.Mapper.Map(parameter.Type);
                if (!mapped.Success)
                    return new BuildResult(null, mapped.FailureReason);
                type = mapped.Type!;
            }

            TsExpr? defaultValue = null;
            if (parameter.Default is { } def)
            {
                defaultValue = DefaultExpr(def, type, out var defaultFailure);
                if (defaultFailure is not null)
                    return new BuildResult(null, $"default value for '{parameter.Name}' cannot be expressed: {defaultFailure}");
            }

            var name = TsNameMapper.Parameter(parameter.Name);
            tsParameters.Add(new TsParameter(name, type, defaultValue, isRest));

            if (invocation is not null)
            {
                BridgeMarshal marshal;
                try
                {
                    marshal = services.Bridge.Plan(parameter.Type, MarshalPosition.Parameter, genericBounds);
                    if (isRest)
                        marshal = marshal switch
                        {
                            BridgeMarshal.HandleSequence h => h with { Container = SequenceContainer.Vararg },
                            BridgeMarshal.TextSequence t => t with { Container = SequenceContainer.Vararg },
                            _ => throw new BridgePlanningException($"params array of {parameter.Type.ElementType!.Render()} is not bridged"),
                        };
                }
                catch (BridgePlanningException e)
                {
                    return new BuildResult(null, e.Message);
                }

                exportParameters.Add(new NativeExportParameter(name, marshal));
                runtimeTests.Add(RuntimeKindOf(marshal));
            }
            else
            {
                runtimeTests.Add(RuntimeKind.Unknown);
            }
        }

        // Return type (TypeScript surface).
        TsType returnType;
        if (forcedTsReturn is not null)
        {
            returnType = forcedTsReturn;
        }
        else
        {
            var mapped = services.Mapper.Map(method.ReturnType);
            if (!mapped.Success)
                return new BuildResult(null, mapped.FailureReason);
            returnType = mapped.Type!;
        }

        // Body plan.
        TsBody body;
        if (invocation is null)
        {
            body = new TsBody.None();
        }
        else
        {
            try
            {
                BridgeMarshal.Handle? receiverMarshal = null;
                if (receiverType is not null)
                {
                    receiverMarshal = services.Bridge.Plan(receiverType, MarshalPosition.Parameter, genericBounds) as BridgeMarshal.Handle
                        ?? throw new BridgePlanningException($"receiver {receiverType.Render()} does not cross as a handle");
                }

                var returnMarshal = services.Bridge.Plan(
                    forcedCSharpReturn ?? method.ReturnType,
                    MarshalPosition.Return,
                    genericBounds);

                body = new TsBody.Bridge(new NativeExport
                {
                    Invocation = invocation,
                    Receiver = receiverMarshal,
                    Parameters = exportParameters,
                    Return = returnMarshal,
                });
            }
            catch (BridgePlanningException e)
            {
                return new BuildResult(null, e.Message);
            }
        }

        var function = new TsFunction
        {
            Name = tsName,
            TypeParameters = typeParameters,
            Parameters = tsParameters,
            ReturnType = returnType,
            Body = body,
            RuntimeKinds = runtimeTests,
            Doc = method.RawXmlDoc,
            DeprecationMessage = Deprecation(method.ObsoleteMessage),
            SourceDocId = method.DocId,
        };

        return new BuildResult(function, null);
    }

    /// <summary>JavaScript-runtime class of a bridged parameter, for overload dispatch.</summary>
    public static RuntimeKind RuntimeKindOf(BridgeMarshal marshal) => marshal switch
    {
        BridgeMarshal.Scalar { Kind: ScalarKind.Boolean } => RuntimeKind.Boolean,
        BridgeMarshal.Scalar => RuntimeKind.Number,
        BridgeMarshal.NullableScalar { Kind: ScalarKind.Boolean } => RuntimeKind.Boolean,
        BridgeMarshal.NullableScalar => RuntimeKind.Number,
        BridgeMarshal.EnumValue => RuntimeKind.Number,
        BridgeMarshal.Text => RuntimeKind.String,
        BridgeMarshal.DateTimeText => RuntimeKind.Date,
        BridgeMarshal.Blob => RuntimeKind.Bytes,
        BridgeMarshal.Handle => RuntimeKind.Handle,
        BridgeMarshal.HandleSequence { Container: SequenceContainer.Vararg } => RuntimeKind.Handle,
        BridgeMarshal.HandleSequence { Container: SequenceContainer.Array } => RuntimeKind.ArrayLike,
        BridgeMarshal.HandleSequence => RuntimeKind.Iterable,
        BridgeMarshal.TextSequence { Container: SequenceContainer.Vararg } => RuntimeKind.String,
        BridgeMarshal.TextSequence { Container: SequenceContainer.Array } => RuntimeKind.ArrayLike,
        BridgeMarshal.TextSequence => RuntimeKind.Iterable,
        BridgeMarshal.ScalarArray { Container: SequenceContainer.Vararg } => RuntimeKind.Number,
        BridgeMarshal.ScalarArray { Container: SequenceContainer.Array } => RuntimeKind.ArrayLike,
        BridgeMarshal.ScalarArray => RuntimeKind.Iterable,
        BridgeMarshal.CallbackValue => RuntimeKind.Function,
        BridgeMarshal.UserImplValue => RuntimeKind.UserImpl,
        _ => RuntimeKind.Unknown,
    };

    private static TsExpr? DefaultExpr(DefaultValue value, TsType type, out string? failure)
    {
        failure = null;
        switch (value.Kind)
        {
            case DefaultValueKind.Null:
                return new TsExpr.Null();
            case DefaultValueKind.Boolean:
                return new TsExpr.Literal(value.Text);
            case DefaultValueKind.Number:
                return new TsExpr.Literal(value.Text);
            case DefaultValueKind.String:
                return new TsExpr.StringLiteral(value.Text);
            case DefaultValueKind.Char:
                return new TsExpr.StringLiteral(value.Text);
            case DefaultValueKind.EnumMember:
                return new TsExpr.EnumEntry(type, value.Text);
            default:
                failure = $"unsupported default kind {value.Kind}";
                return null;
        }
    }

    public static string? Deprecation(string? obsoleteMessage) =>
        obsoleteMessage is null ? null : obsoleteMessage.Length == 0 ? "Deprecated in QuestPDF." : obsoleteMessage;
}
