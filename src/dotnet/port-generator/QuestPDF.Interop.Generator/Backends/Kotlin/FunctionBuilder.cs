using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>Kotlin literal formatting shared by defaults and constants.</summary>
public static class KotlinLiterals
{
    /// <summary>Formats an invariant numeric payload as a Kotlin literal of the given type.</summary>
    public static string Number(string payload, KType type) => type.Name switch
    {
        "Float" => payload + "f",
        "Double" => payload.Contains('.') || payload.Contains('E') ? payload : payload + ".0",
        "Long" => payload + "L",
        "UInt" or "UByte" or "UShort" => payload + "u",
        "ULong" => payload + "uL",
        _ => payload,
    };
}

/// <summary>
/// Shared construction of bridged <see cref="KotlinFunction"/>s from classified
/// methods: maps parameter and return types, converts C# defaults to Kotlin
/// default values, and plans the native export (receiver, argument and return
/// marshals plus the C#-side invocation).
/// </summary>
public sealed class FunctionBuilder(ClassifierServices services)
{
    public sealed record BuildResult(KotlinFunction? Function, string? Failure);

    public static TypeRef TypeRefOf(ApiType type) => new()
    {
        Kind = TypeRefKind.Named,
        FullName = type.FullName,
        IsApiAssemblyType = true,
    };

    /// <param name="invocation">C#-side call plan; null produces an abstract member (user-implemented interfaces).</param>
    /// <param name="receiverType">Receiver crossing as the export's first handle parameter; null for statics and constructors.</param>
    /// <param name="forcedCSharpReturn">Overrides the marshalled return type (self-generic monomorphization, constructors).</param>
    public BuildResult Build(
        MethodContext context,
        ApiType targetType,
        string kotlinName,
        IReadOnlyList<ApiParameter> parameters,
        CSharpInvocation? invocation,
        TypeRef? receiverType,
        bool dropTypeParameters = false,
        bool markOpen = false,
        bool markOverride = false,
        TypeRef? forcedCSharpReturn = null,
        KType? forcedKotlinReturn = null)
    {
        var method = context.Method;

        // Type parameters, their mappable constraints, and the bounds the
        // bridge uses to marshal generic parameters.
        var typeParameters = new List<string>();
        var constraints = new List<KotlinTypeConstraint>();
        var genericBounds = new Dictionary<string, TypeRef>(StringComparer.Ordinal);

        foreach (var parameter in method.TypeParameters)
        {
            var apiBound = parameter.Constraints.FirstOrDefault(c => c is { Kind: TypeRefKind.Named, IsApiAssemblyType: true });
            if (apiBound is not null)
                genericBounds[parameter.Name] = apiBound;

            if (dropTypeParameters)
                continue;

            typeParameters.Add(parameter.Name);
            foreach (var constraint in parameter.Constraints)
            {
                var mapped = services.Mapper.Map(constraint);
                if (!mapped.Success)
                    return new BuildResult(null, $"type-parameter constraint unmappable: {mapped.FailureReason}");
                constraints.Add(new KotlinTypeConstraint(parameter.Name, mapped.Type!));
            }
            // class/struct/new() constraints have no Kotlin equivalent and are dropped.
        }

        // Parameters: Kotlin surface type + bridge marshal.
        var kotlinParameters = new List<KotlinParameter>();
        var exportParameters = new List<NativeExportParameter>();

        foreach (var parameter in parameters)
        {
            if (parameter.IsByRef)
                return new BuildResult(null, $"ref/out parameter '{parameter.Name}' has no Kotlin mapping");

            KType type;
            var isVararg = false;

            if (parameter.IsParams && parameter.Type.Kind == TypeRefKind.Array)
            {
                var element = services.Mapper.Map(parameter.Type.ElementType!);
                if (!element.Success)
                    return new BuildResult(null, element.FailureReason);
                type = element.Type!;
                isVararg = true;
            }
            else
            {
                var mapped = services.Mapper.Map(parameter.Type);
                if (!mapped.Success)
                    return new BuildResult(null, mapped.FailureReason);
                type = mapped.Type!;
            }

            KExpr? defaultValue = null;
            if (parameter.Default is { } def)
            {
                defaultValue = DefaultExpr(def, type, out var defaultFailure);
                if (defaultFailure is not null)
                    return new BuildResult(null, $"default value for '{parameter.Name}' cannot be expressed: {defaultFailure}");
            }

            var name = NameMapper.Parameter(parameter.Name);
            kotlinParameters.Add(new KotlinParameter(name, type, defaultValue, isVararg));

            if (invocation is not null)
            {
                BridgeMarshal marshal;
                try
                {
                    marshal = services.Bridge.Plan(parameter.Type, MarshalPosition.Parameter, genericBounds);
                    if (isVararg)
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
            }
        }

        // Return type (Kotlin surface).
        KType returnType;
        if (forcedKotlinReturn is not null)
        {
            returnType = forcedKotlinReturn;
        }
        else
        {
            var mapped = services.Mapper.Map(method.ReturnType);
            if (!mapped.Success)
                return new BuildResult(null, mapped.FailureReason);
            returnType = mapped.Type!;
        }

        // Body plan.
        KotlinBody body;
        if (invocation is null)
        {
            body = new KotlinBody.None();
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

                body = new KotlinBody.Bridge(new NativeExport
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

        var function = new KotlinFunction
        {
            Name = kotlinName,
            TypeParameters = typeParameters,
            TypeConstraints = constraints,
            Parameters = kotlinParameters,
            ReturnType = returnType,
            Body = body,
            IsAbstract = invocation is null,
            IsOpen = markOpen,
            IsOverride = markOverride,
            KDoc = method.RawXmlDoc,
            DeprecationMessage = Deprecation(method.ObsoleteMessage),
            SourceDocId = method.DocId,
        };

        return new BuildResult(function, null);
    }

    private static KExpr? DefaultExpr(DefaultValue value, KType type, out string? failure)
    {
        failure = null;
        switch (value.Kind)
        {
            case DefaultValueKind.Null:
                return new KExpr.Null();
            case DefaultValueKind.Boolean:
                return new KExpr.Literal(value.Text);
            case DefaultValueKind.Number:
                return new KExpr.Literal(KotlinLiterals.Number(value.Text, type));
            case DefaultValueKind.String:
                return new KExpr.StringLiteral(value.Text);
            case DefaultValueKind.Char:
                return new KExpr.Literal("'" + value.Text.Replace("'", "\\'") + "'");
            case DefaultValueKind.EnumMember:
                return new KExpr.EnumEntry(type, value.Text);
            default:
                failure = $"unsupported default kind {value.Kind}";
                return null;
        }
    }

    public static string? Deprecation(string? obsoleteMessage) =>
        obsoleteMessage is null ? null : obsoleteMessage.Length == 0 ? "Deprecated in QuestPDF." : obsoleteMessage;
}
