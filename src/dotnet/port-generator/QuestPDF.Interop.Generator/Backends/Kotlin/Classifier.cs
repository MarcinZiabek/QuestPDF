using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// Kotlin classification orchestrator: runs the type and member rule pipelines
/// over the extracted model and assembles the Kotlin-level model, the native
/// export plans and the classification report. Deterministic: input is
/// processed in stable order and members are sorted before assembly.
/// </summary>
public sealed class Classifier
{
    private readonly ApiIndex index;
    private readonly ManualOverrides overrides;
    private readonly Dictionary<string, TypePlan> plans = new(StringComparer.Ordinal);
    private readonly List<ReportEntry> reports = [];
    private readonly TypeMapper mapper;
    private readonly BridgePlanner bridge;
    private readonly ClassifierServices services;

    private sealed class MemberSet
    {
        public List<KotlinFunction> Functions = [];
        public List<KotlinFunction> CompanionFunctions = [];
        public List<KotlinProperty> Properties = [];
        public List<KotlinProperty> CompanionProperties = [];
        public List<KotlinSecondaryConstructor> SecondaryConstructors = [];
    }

    private readonly Dictionary<string, MemberSet> memberSets = new(StringComparer.Ordinal);

    /// <summary>Function-type arity of each generated delegate alias, for JVM-erasure comparison.</summary>
    private readonly Dictionary<string, int> delegateAliasArity = new(StringComparer.Ordinal);

    private Classifier(ApiIndex index, TypeMapper mapper, ManualOverrides overrides)
    {
        this.index = index;
        this.mapper = mapper;
        this.overrides = overrides;
        bridge = new BridgePlanner(index, key => plans.GetValueOrDefault(key));
        services = new ClassifierServices
        {
            Index = index,
            Mapper = mapper,
            Bridge = bridge,
            Overrides = overrides,
            PlanFor = key => plans.GetValueOrDefault(key),
        };
    }

    /// <summary>Classifies against the given index/mapper — the same instances the emission views must share.</summary>
    public static KotlinModel Classify(ApiIndex index, TypeMapper mapper, ManualOverrides overrides)
    {
        var classifier = new Classifier(index, mapper, overrides);
        return classifier.Run();
    }

    public static KotlinModel Classify(ApiAssembly assembly, ManualOverrides overrides)
    {
        var index = new ApiIndex(assembly);
        return Classify(index, new TypeMapper(index), overrides);
    }

    private IEnumerable<ApiType> SortedTypes =>
        index.Assembly.Types.OrderBy(ApiIndex.Key, StringComparer.Ordinal);

    /// <summary>Exception types stay client-side; .NET exception details travel over the error channel.</summary>
    private static bool IsExceptionPlan(TypePlan plan) => plan.DecidedByRule == "exception";

    private KotlinModel Run()
    {
        ClassifyTypes();
        ClassifyConstructors();
        ClassifyMethods();
        ClassifyPropertiesAndFields();
        DeduplicateJvmSignatures();
        MarkOverrides();
        var declarations = BuildDeclarations();
        var (exports, shapes) = AssignExportNames(declarations);

        return new KotlinModel(
            declarations,
            reports
                .OrderBy(r => r.DocId, StringComparer.Ordinal)
                .ThenBy(r => r.Detail, StringComparer.Ordinal)
                .ToList(),
            exports,
            shapes,
            bridge.Proxies,
            index.Assembly.Name,
            index.Assembly.Version);
    }

    // ---- Pass 1: types ----

    private void ClassifyTypes()
    {
        foreach (var type in SortedTypes)
        {
            var plan = TypeRulePipeline.Classify(type, index, overrides, KotlinTypeRules.All);
            plans[ApiIndex.Key(type)] = plan;
            memberSets[ApiIndex.Key(type)] = new MemberSet();

            if (plan.Kind == TypePlanKind.Unsupported)
                index.MarkTypeUnsupported(type, plan.Detail);

            if (plan.Kind == TypePlanKind.TypeAlias && type.DelegateInfo is not null)
                delegateAliasArity[NameMapper.Package(type.Namespace) + "." + type.Name] = type.DelegateInfo.Parameters.Count;

            var classification = plan.Kind switch
            {
                TypePlanKind.Unsupported => ApiClassification.Unsupported,
                TypePlanKind.ManualOverride => ApiClassification.ManualOverride,
                _ => ApiClassification.Generated,
            };

            reports.Add(new ReportEntry(type.DocId, SignatureRenderer.Render(type), classification, plan.Detail, plan.DecidedByRule));
        }
    }

    // ---- Pass 2: constructors ----

    private void ClassifyConstructors()
    {
        foreach (var type in SortedTypes)
        {
            var plan = plans[ApiIndex.Key(type)];
            var target = memberSets[ApiIndex.Key(type)];

            foreach (var (ctor, position) in type.Constructors.Select((c, i) => (c, i)))
            {
                if (SkipMemberForPlan(plan, ctor.DocId, SignatureRenderer.Render(ctor)))
                    continue;

                if (plan.Kind != TypePlanKind.Class)
                    continue;

                if (IsExceptionPlan(plan))
                {
                    reports.Add(new ReportEntry(ctor.DocId, SignatureRenderer.Render(ctor),
                        ApiClassification.Infrastructure, "exception types are client-side; .NET details travel over the error channel", "exception"));
                    continue;
                }

                if (overrides.Contains(ctor.DocId))
                {
                    reports.Add(new ReportEntry(ctor.DocId, SignatureRenderer.Render(ctor),
                        ApiClassification.ManualOverride, "excluded via manual-overrides.txt", "manual-override"));
                    continue;
                }

                if (position > 0)
                {
                    reports.Add(new ReportEntry(ctor.DocId, SignatureRenderer.Render(ctor),
                        ApiClassification.Unsupported, "additional constructor overloads are not emitted", "constructor"));
                    continue;
                }

                if (type.IsAbstract)
                {
                    reports.Add(new ReportEntry(ctor.DocId, SignatureRenderer.Render(ctor),
                        ApiClassification.Unsupported, "abstract classes cannot be constructed over the bridge", "constructor"));
                    continue;
                }

                var invocation = new CSharpInvocation(
                    InvocationKind.Constructor, type.FullName, ".ctor", [], null);

                var builder = new FunctionBuilder(services);
                var built = builder.Build(
                    new MethodContext(type, ctor, services), type, "constructor", ctor.Parameters,
                    invocation, receiverType: null,
                    forcedCSharpReturn: FunctionBuilder.TypeRefOf(type),
                    forcedKotlinReturn: KType.Unit);

                if (built.Failure is not null)
                {
                    reports.Add(new ReportEntry(ctor.DocId, SignatureRenderer.Render(ctor),
                        ApiClassification.Unsupported, built.Failure, "constructor"));
                    continue;
                }

                var export = ((KotlinBody.Bridge)built.Function!.Body).Export;
                target.SecondaryConstructors.Add(new KotlinSecondaryConstructor(
                    built.Function.Parameters, export, ctor.RawXmlDoc,
                    FunctionBuilder.Deprecation(ctor.ObsoleteMessage), ctor.DocId));

                reports.Add(new ReportEntry(ctor.DocId, SignatureRenderer.Render(ctor),
                    ApiClassification.Generated, "mapped to a bridged constructor", "constructor"));
            }
        }
    }

    // ---- Pass 3: methods through the rule pipeline ----

    private void ClassifyMethods()
    {
        foreach (var type in SortedTypes)
        {
            var plan = plans[ApiIndex.Key(type)];

            if (plan.Kind == TypePlanKind.ManualOverride)
            {
                foreach (var method in type.Methods)
                    reports.Add(new ReportEntry(method.DocId, SignatureRenderer.Render(method),
                        ApiClassification.ManualOverride, "declared on a manually-overridden type", "manual-override"));
                continue;
            }

            if (plan.Kind == TypePlanKind.Unsupported)
            {
                foreach (var method in type.Methods)
                    reports.Add(new ReportEntry(method.DocId, SignatureRenderer.Render(method),
                        ApiClassification.Unsupported, $"declared on skipped type ({plan.Detail})", plan.DecidedByRule));
                continue;
            }

            if (plan.Kind is TypePlanKind.Enum or TypePlanKind.TypeAlias)
                continue;

            if (plan.Kind == TypePlanKind.Class && IsExceptionPlan(plan))
            {
                foreach (var method in type.Methods)
                    reports.Add(new ReportEntry(method.DocId, SignatureRenderer.Render(method),
                        ApiClassification.Infrastructure, "exception types are client-side; .NET details travel over the error channel", "exception"));
                continue;
            }

            var methods = OverloadCollapseRule.Apply(type.Methods, reports);

            foreach (var method in methods.OrderBy(m => m.DocId, StringComparer.Ordinal))
            {
                var outcome = MethodRulePipeline.Classify(new MethodContext(type, method, services));
                reports.Add(outcome.Report);

                foreach (var placement in outcome.Emissions)
                {
                    var target = memberSets.GetValueOrDefault(placement.TargetTypeKey);
                    if (target is null)
                        continue;

                    if (placement.InCompanion)
                        target.CompanionFunctions.Add(placement.Function);
                    else
                        target.Functions.Add(placement.Function);
                }
            }
        }
    }

    // ---- Pass 4: properties and fields ----

    private void ClassifyPropertiesAndFields()
    {
        foreach (var type in SortedTypes)
        {
            var plan = plans[ApiIndex.Key(type)];
            var target = memberSets[ApiIndex.Key(type)];

            foreach (var property in type.Properties.OrderBy(p => p.DocId, StringComparer.Ordinal))
                ClassifyProperty(type, plan, property, target);

            foreach (var field in type.Fields.OrderBy(f => f.DocId, StringComparer.Ordinal))
                ClassifyField(type, plan, field, target);
        }
    }

    private void ClassifyProperty(ApiType type, TypePlan plan, ApiProperty property, MemberSet target)
    {
        if (SkipMemberForPlan(plan, property.DocId, SignatureRenderer.Render(property)))
            return;

        if (IsExceptionPlan(plan))
        {
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.Infrastructure, "exception types are client-side; .NET details travel over the error channel", "exception"));
            return;
        }

        if (overrides.Contains(property.DocId))
        {
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.ManualOverride, "excluded via manual-overrides.txt", "manual-override"));
            return;
        }

        if (property.IsIndexer)
        {
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.Unsupported, "indexers are not mapped", "indexer"));
            return;
        }

        var mapped = mapper.Map(property.Type);
        if (!mapped.Success)
        {
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.Unsupported, mapped.FailureReason!, "property-type"));
            return;
        }

        var kType = mapped.Type!;
        var isConstantStyle = property.IsStatic && !property.HasSetter;
        var name = isConstantStyle ? NameMapper.Constant(property.Name) : NameMapper.Member(property.Name);

        // Members of user-implemented interfaces stay abstract.
        if (plan.Kind == TypePlanKind.Interface && bridge.IsUserImplemented(type))
        {
            target.Properties.Add(NewProperty(property, name, kType, isAbstract: true));
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.Generated, "user-implemented interface property kept abstract", "property"));
            return;
        }

        // Compile-time constants keep their captured literal client-side.
        if (isConstantStyle && LiteralInitializer(kType, property.CapturedValue) is { } constant)
        {
            var literalProperty = NewProperty(property, name, kType, isAbstract: false) with
            {
                Initializer = constant,
            };
            Place(type, plan, property.IsStatic, literalProperty, target);
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.Generated, "constant mapped with its real value", "property"));
            return;
        }

        // Everything else becomes bridged accessors.
        var accessors = BuildAccessors(type, property.DeclaringTypeFullName, property.Name, property.Type,
            property.IsStatic, property.HasSetter);
        if (accessors.Failure is not null)
        {
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.Unsupported, accessors.Failure, "property-bridge"));
            return;
        }

        var kotlinProperty = NewProperty(property, name, kType, isAbstract: false) with
        {
            Getter = accessors.Getter,
            Setter = accessors.Setter,
        };
        Place(type, plan, property.IsStatic, kotlinProperty, target);

        reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
            ApiClassification.Generated,
            property.IsStatic && plan.Kind == TypePlanKind.Class
                ? "static property mapped to bridged companion accessors"
                : "mapped to bridged property accessors",
            "property"));
    }

    private KotlinProperty NewProperty(ApiProperty property, string name, KType kType, bool isAbstract) => new()
    {
        Name = name,
        Type = kType,
        IsMutable = property.HasSetter,
        IsAbstract = isAbstract,
        KDoc = property.RawXmlDoc,
        DeprecationMessage = FunctionBuilder.Deprecation(property.ObsoleteMessage),
        SourceDocId = property.DocId,
    };

    private void ClassifyField(ApiType type, TypePlan plan, ApiField field, MemberSet target)
    {
        if (SkipMemberForPlan(plan, field.DocId, SignatureRenderer.Render(field)))
            return;

        if (IsExceptionPlan(plan))
        {
            reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
                ApiClassification.Infrastructure, "exception types are client-side; .NET details travel over the error channel", "exception"));
            return;
        }

        if (overrides.Contains(field.DocId))
        {
            reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
                ApiClassification.ManualOverride, "excluded via manual-overrides.txt", "manual-override"));
            return;
        }

        var mapped = mapper.Map(field.Type);
        if (!mapped.Success)
        {
            reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
                ApiClassification.Unsupported, mapped.FailureReason!, "field-type"));
            return;
        }

        var kType = mapped.Type!;
        var isConstantStyle = field.IsConst || (field.IsStatic && field.IsReadOnly);
        var name = isConstantStyle ? NameMapper.Constant(field.Name) : NameMapper.Member(field.Name);

        if (isConstantStyle && LiteralInitializer(kType, field.CapturedValue) is { } constant)
        {
            var literalProperty = new KotlinProperty
            {
                Name = name,
                Type = kType,
                IsMutable = false,
                Initializer = constant,
                IsConst = field.IsConst && IsConstable(kType),
                KDoc = field.RawXmlDoc,
                DeprecationMessage = FunctionBuilder.Deprecation(field.ObsoleteMessage),
                SourceDocId = field.DocId,
            };
            Place(type, plan, field.IsStatic, literalProperty, target);
            reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
                ApiClassification.Generated, "constant mapped with its real value", "field"));
            return;
        }

        var accessors = BuildAccessors(type, field.DeclaringTypeFullName, field.Name, field.Type,
            field.IsStatic, setter: !field.IsConst && !field.IsReadOnly);
        if (accessors.Failure is not null)
        {
            reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
                ApiClassification.Unsupported, accessors.Failure, "field-bridge"));
            return;
        }

        var kotlinProperty = new KotlinProperty
        {
            Name = name,
            Type = kType,
            IsMutable = !field.IsConst && !field.IsReadOnly,
            Getter = accessors.Getter,
            Setter = accessors.Setter,
            KDoc = field.RawXmlDoc,
            DeprecationMessage = FunctionBuilder.Deprecation(field.ObsoleteMessage),
            SourceDocId = field.DocId,
        };
        Place(type, plan, field.IsStatic, kotlinProperty, target);

        reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
            ApiClassification.Generated, "field mapped to bridged accessors", "field"));
    }

    private void Place(ApiType type, TypePlan plan, bool isStatic, KotlinProperty property, MemberSet target)
    {
        var toCompanion = isStatic && plan.Kind == TypePlanKind.Class;
        (toCompanion ? target.CompanionProperties : target.Properties).Add(property);
    }

    private sealed record AccessorResult(NativeExport? Getter, NativeExport? Setter, string? Failure);

    private AccessorResult BuildAccessors(
        ApiType type, string declaringTypeFullName, string memberName, TypeRef memberType,
        bool isStatic, bool setter)
    {
        try
        {
            var receiver = isStatic
                ? null
                : bridge.Plan(FunctionBuilder.TypeRefOf(type), MarshalPosition.Parameter) as BridgeMarshal.Handle
                    ?? throw new BridgePlanningException($"receiver {type.FullName} does not cross as a handle");

            var getter = new NativeExport
            {
                Invocation = new CSharpInvocation(
                    isStatic ? InvocationKind.StaticPropertyGet : InvocationKind.PropertyGet,
                    declaringTypeFullName, memberName, [], isStatic ? null : type.FullName),
                Receiver = receiver,
                Parameters = [],
                Return = bridge.Plan(memberType, MarshalPosition.Return),
            };

            NativeExport? setterExport = null;
            if (setter)
            {
                setterExport = new NativeExport
                {
                    Invocation = new CSharpInvocation(
                        isStatic ? InvocationKind.StaticPropertySet : InvocationKind.PropertySet,
                        declaringTypeFullName, memberName, [], isStatic ? null : type.FullName),
                    Receiver = receiver,
                    Parameters = [new NativeExportParameter("value", bridge.Plan(memberType, MarshalPosition.Parameter))],
                    Return = new BridgeMarshal.Nothing(),
                };
            }

            return new AccessorResult(getter, setterExport, null);
        }
        catch (BridgePlanningException e)
        {
            return new AccessorResult(null, null, e.Message);
        }
    }

    private static KExpr? LiteralInitializer(KType type, string? captured)
    {
        if (captured is null)
            return null;

        var separator = captured.IndexOf(':');
        if (separator < 0)
            return null;

        var kind = captured[..separator];
        var payload = captured[(separator + 1)..];

        return kind switch
        {
            "string" when type.Name == "String" => new KExpr.StringLiteral(payload),
            "bool" when type.Name == "Boolean" => new KExpr.Literal(payload),
            "number" when IsConstable(type) || type.Name is "UInt" or "UByte" or "UShort" or "ULong"
                => new KExpr.Literal(KotlinLiterals.Number(payload, type)),
            _ => null,
        };
    }

    private bool SkipMemberForPlan(TypePlan plan, string docId, string signature)
    {
        switch (plan.Kind)
        {
            case TypePlanKind.ManualOverride:
                reports.Add(new ReportEntry(docId, signature, ApiClassification.ManualOverride,
                    "declared on a manually-overridden type", "manual-override"));
                return true;

            case TypePlanKind.Unsupported:
                reports.Add(new ReportEntry(docId, signature, ApiClassification.Unsupported,
                    $"declared on skipped type ({plan.Detail})", plan.DecidedByRule));
                return true;

            case TypePlanKind.Enum or TypePlanKind.TypeAlias:
                return true;

            default:
                return false;
        }
    }

    private static bool IsConstable(KType type) =>
        type.PackageName == "kotlin" &&
        type.Name is "String" or "Int" or "Long" or "Short" or "Byte" or "Float" or "Double" or "Boolean" or "Char";

    // ---- Pass 5: JVM-erasure dedup ----

    private void DeduplicateJvmSignatures()
    {
        foreach (var (key, set) in memberSets)
        {
            set.Functions = Deduplicate(set.Functions, key);
            set.CompanionFunctions = Deduplicate(set.CompanionFunctions, key);
        }
    }

    private List<KotlinFunction> Deduplicate(List<KotlinFunction> functions, string typeKey)
    {
        var kept = new Dictionary<string, KotlinFunction>(StringComparer.Ordinal);
        var order = new List<string>();

        // Prefer non-deprecated members on clashes.
        foreach (var function in functions.OrderBy(f => f.DeprecationMessage is null ? 0 : 1))
        {
            var signature = ErasedSignature(function);
            if (kept.TryGetValue(signature, out var existing))
            {
                if (existing.SourceDocId != function.SourceDocId)
                {
                    reports.Add(new ReportEntry(function.SourceDocId,
                        $"{typeKey}.{function.Name}",
                        ApiClassification.Unsupported,
                        $"JVM signature clash with the overload kept as '{existing.Name}' (erasure: {signature}); Kotlin forbids both",
                        "jvm-signature-dedup"));
                }
                continue;
            }

            kept[signature] = function;
            order.Add(signature);
        }

        return order.Select(s => kept[s]).ToList();
    }

    private string ErasedSignature(KotlinFunction function)
    {
        string Erase(KType type)
        {
            if (type.IsFunctionType)
                return "Function" + (type.FunctionParameters.Count + (type.LambdaReceiver is null ? 0 : 1));

            if (type.IsGenericParameter)
                return "T";

            // Delegate typealiases erase to their underlying function type.
            if (delegateAliasArity.TryGetValue(type.FullName, out var arity))
                return "Function" + arity;

            return type.FullName;
        }

        var parameters = function.Parameters
            .Select(p => (p.IsVararg ? "vararg " : "") + Erase(p.Type));

        return function.Name + "(" + string.Join(",", parameters) + ")";
    }

    // ---- Pass 6: override detection ----

    private void MarkOverrides()
    {
        foreach (var type in SortedTypes)
        {
            var plan = plans[ApiIndex.Key(type)];
            if (plan.Kind != TypePlanKind.Class)
                continue;

            var set = memberSets[ApiIndex.Key(type)];
            var supertypeSets = index.AllApiInterfaces(type)
                .Select(i => (Type: i, Set: memberSets.GetValueOrDefault(ApiIndex.Key(i))))
                .Concat(index.BaseChain(type).Select(b => (Type: b, Set: memberSets.GetValueOrDefault(ApiIndex.Key(b)))))
                .Where(pair => pair.Set is not null)
                .ToList();

            for (var i = 0; i < set.Functions.Count; i++)
            {
                var function = set.Functions[i];
                if (function.IsOverride)
                    continue;

                foreach (var (supertype, superset) in supertypeSets)
                {
                    var match = superset!.Functions.FirstOrDefault(f =>
                        f.Name == function.Name && SameParameters(f, function));

                    if (match is null)
                        continue;

                    set.Functions[i] = function with { IsOverride = true };

                    // A match on a base *class* must be open to be overridden.
                    var superPlan = plans[ApiIndex.Key(supertype)];
                    if (superPlan.Kind == TypePlanKind.Class && !match.IsOpen)
                    {
                        var superSetIndex = superset.Functions.IndexOf(match);
                        superset.Functions[superSetIndex] = match with { IsOpen = true };
                    }

                    break;
                }
            }

            for (var i = 0; i < set.Properties.Count; i++)
            {
                var property = set.Properties[i];
                foreach (var (_, superset) in supertypeSets)
                {
                    if (superset!.Properties.Any(p => p.Name == property.Name && !p.IsConst))
                    {
                        set.Properties[i] = property with { IsOverride = true };
                        break;
                    }
                }
            }
        }
    }

    private static bool SameParameters(KotlinFunction a, KotlinFunction b)
    {
        if (a.Parameters.Count != b.Parameters.Count)
            return false;

        for (var i = 0; i < a.Parameters.Count; i++)
        {
            if (RenderForComparison(a.Parameters[i].Type) != RenderForComparison(b.Parameters[i].Type))
                return false;
        }

        return true;
    }

    private static string RenderForComparison(KType type)
    {
        if (type.IsFunctionType)
        {
            var receiver = type.LambdaReceiver is null ? "" : RenderForComparison(type.LambdaReceiver) + ".";
            var parameters = string.Join(",", type.FunctionParameters.Select(p => RenderForComparison(p.Type)));
            var returns = type.FunctionReturn is null ? "Unit" : RenderForComparison(type.FunctionReturn);
            return $"{receiver}({parameters})->{returns}{(type.IsNullable ? "?" : "")}";
        }

        var arguments = type.TypeArguments.Count == 0
            ? ""
            : "<" + string.Join(",", type.TypeArguments.Select(RenderForComparison)) + ">";

        return type.FullName + arguments + (type.IsNullable ? "?" : "");
    }

    // ---- Pass 7: declaration assembly ----

    private IReadOnlyList<KotlinDeclaration> BuildDeclarations()
    {
        var built = new Dictionary<string, KotlinTypeDeclaration>(StringComparer.Ordinal);
        var aliases = new List<KotlinTypeAlias>();

        foreach (var type in SortedTypes)
        {
            var plan = plans[ApiIndex.Key(type)];
            switch (plan.Kind)
            {
                case TypePlanKind.Enum:
                    built[ApiIndex.Key(type)] = BuildEnum(type);
                    break;

                case TypePlanKind.TypeAlias:
                    if (BuildAlias(type) is { } alias)
                        aliases.Add(alias);
                    break;

                case TypePlanKind.StaticHolder:
                    built[ApiIndex.Key(type)] = BuildObjectOrClass(type, plan, KotlinTypeKind.Object);
                    break;

                case TypePlanKind.Class:
                    built[ApiIndex.Key(type)] = BuildObjectOrClass(type, plan, KotlinTypeKind.Class);
                    break;

                case TypePlanKind.Interface:
                    built[ApiIndex.Key(type)] = BuildObjectOrClass(type, plan, KotlinTypeKind.Interface);
                    break;
            }
        }

        // Nest declarations under their declaring type when that type is emitted.
        var topLevel = new List<KotlinDeclaration>();
        var nestedByParent = new Dictionary<string, List<KotlinTypeDeclaration>>(StringComparer.Ordinal);

        foreach (var type in SortedTypes)
        {
            if (!built.TryGetValue(ApiIndex.Key(type), out var declaration))
                continue;

            var parentKey = type.DeclaringTypeFullName;
            if (parentKey is not null && built.ContainsKey(parentKey))
            {
                if (!nestedByParent.TryGetValue(parentKey, out var list))
                    nestedByParent[parentKey] = list = [];
                list.Add(declaration);
            }
        }

        KotlinTypeDeclaration WithNested(ApiType type, KotlinTypeDeclaration declaration)
        {
            if (!nestedByParent.TryGetValue(type.FullName, out var children))
                return declaration;

            return declaration with
            {
                NestedTypes = children.OrderBy(c => c.Name, StringComparer.Ordinal).ToList(),
            };
        }

        foreach (var type in SortedTypes)
        {
            if (!built.TryGetValue(ApiIndex.Key(type), out var declaration))
                continue;

            var isNested = type.DeclaringTypeFullName is not null && built.ContainsKey(type.DeclaringTypeFullName);
            if (isNested)
                continue;

            topLevel.Add(WithNested(type, declaration));
        }

        // Nested declarations attached above must themselves carry their nested children.
        for (var i = 0; i < topLevel.Count; i++)
        {
            if (topLevel[i] is KotlinTypeDeclaration typeDeclaration)
                topLevel[i] = AttachNestedRecursively(typeDeclaration, nestedByParent);
        }

        topLevel.AddRange(aliases);

        return topLevel
            .OrderBy(d => d.PackageName, StringComparer.Ordinal)
            .ThenBy(d => d.Name, StringComparer.Ordinal)
            .ToList();
    }

    private KotlinTypeDeclaration AttachNestedRecursively(
        KotlinTypeDeclaration declaration,
        Dictionary<string, List<KotlinTypeDeclaration>> nestedByParent)
    {
        if (declaration.NestedTypes.Count == 0)
            return declaration;

        var updated = declaration.NestedTypes
            .Select(nested =>
            {
                var nestedKey = FindFullNameFor(nested);
                var withChildren = nestedKey is not null && nestedByParent.TryGetValue(nestedKey, out var children)
                    ? nested with { NestedTypes = children.OrderBy(c => c.Name, StringComparer.Ordinal).ToList() }
                    : nested;
                return AttachNestedRecursively(withChildren, nestedByParent);
            })
            .ToList();

        return declaration with { NestedTypes = updated };
    }

    private string? FindFullNameFor(KotlinTypeDeclaration declaration)
    {
        var type = index.Assembly.Types.FirstOrDefault(t => t.DocId == declaration.SourceDocId);
        return type?.FullName;
    }

    private KotlinTypeDeclaration BuildEnum(ApiType type)
    {
        var entries = type.EnumMembers
            .Select(m => new KotlinEnumEntry(m.Name, m.Value, m.RawXmlDoc, FunctionBuilder.Deprecation(m.ObsoleteMessage), m.DocId))
            .ToList();

        return new KotlinTypeDeclaration
        {
            PackageName = NameMapper.Package(type.Namespace),
            Name = type.Name,
            Kind = KotlinTypeKind.Enum,
            EnumEntries = entries,
            KDoc = type.RawXmlDoc,
            DeprecationMessage = FunctionBuilder.Deprecation(type.ObsoleteMessage),
            SourceDocId = type.DocId,
        };
    }

    private KotlinTypeAlias? BuildAlias(ApiType type)
    {
        var info = type.DelegateInfo!;
        var parameters = new List<KotlinParameter>();

        foreach (var parameter in info.Parameters)
        {
            var mapped = mapper.Map(parameter.Type);
            if (!mapped.Success)
            {
                index.MarkTypeUnsupported(type, $"delegate parameter unmappable: {mapped.FailureReason}");
                ReplaceTypeReport(type, mapped.FailureReason!);
                return null;
            }

            parameters.Add(new KotlinParameter(NameMapper.Parameter(parameter.Name), mapped.Type!, null, false));
        }

        var returnMapped = mapper.Map(info.ReturnType);
        if (!returnMapped.Success)
        {
            index.MarkTypeUnsupported(type, $"delegate return unmappable: {returnMapped.FailureReason}");
            ReplaceTypeReport(type, returnMapped.FailureReason!);
            return null;
        }

        return new KotlinTypeAlias
        {
            PackageName = NameMapper.Package(type.Namespace),
            Name = type.Name,
            AliasedType = KType.Function(null, parameters, returnMapped.Type!),
            KDoc = type.RawXmlDoc,
            DeprecationMessage = FunctionBuilder.Deprecation(type.ObsoleteMessage),
            SourceDocId = type.DocId,
        };
    }

    private void ReplaceTypeReport(ApiType type, string reason)
    {
        reports.RemoveAll(r => r.DocId == type.DocId);
        reports.Add(new ReportEntry(type.DocId, SignatureRenderer.Render(type),
            ApiClassification.Unsupported, reason, "delegate-to-typealias"));
    }

    private KotlinTypeDeclaration BuildObjectOrClass(ApiType type, TypePlan plan, KotlinTypeKind kind)
    {
        var set = memberSets[ApiIndex.Key(type)];
        var isException = kind == KotlinTypeKind.Class && IsExceptionPlan(plan);
        var isUserImplemented = kind == KotlinTypeKind.Interface && bridge.IsUserImplemented(type);
        var isHandleBacked = (kind == KotlinTypeKind.Class && !isException) ||
                             (kind == KotlinTypeKind.Interface && !isUserImplemented);

        // Supertypes.
        KType? superClass = null;
        string? superClassCall = null;
        if (kind == KotlinTypeKind.Class)
        {
            if (isException)
            {
                superClass = KType.Named("kotlin", "Exception");
                superClassCall = "(message)";
            }
            else if (type.BaseType is { IsApiAssemblyType: true } apiBase && mapper.Map(apiBase) is { Success: true } mappedBase)
            {
                superClass = mappedBase.Type;
                superClassCall = "(handle)";
            }
            else
            {
                superClass = KType.Named("com.questpdf.interop", "NativeObject");
                superClassCall = "(handle)";
            }
        }

        var superInterfaces = DirectApiInterfaces(type)
            .Select(i => mapper.Map(i))
            .Where(m => m.Success)
            .Select(m => m.Type!)
            .OrderBy(t => t.FullName, StringComparer.Ordinal)
            .ToList();

        // Handle-backed interfaces inherit nativeHandle from any handle-backed
        // API superinterface; roots declare it themselves.
        var declaresNativeHandle = kind == KotlinTypeKind.Interface && isHandleBacked &&
            !index.AllApiInterfaces(type).Any(i =>
                plans.GetValueOrDefault(ApiIndex.Key(i))?.Kind == TypePlanKind.Interface &&
                !bridge.IsUserImplemented(i));

        KotlinConstructor? primaryConstructor = null;
        if (isException)
        {
            primaryConstructor = new KotlinConstructor(
                [new KotlinParameter("message", KType.Named("kotlin", "String", nullable: true), new KExpr.Null(), false)],
                IsInternal: false);
        }

        return new KotlinTypeDeclaration
        {
            PackageName = NameMapper.Package(type.Namespace),
            Name = type.Name,
            Kind = kind,
            IsAbstract = kind == KotlinTypeKind.Class && type.IsAbstract,
            IsOpen = kind == KotlinTypeKind.Class && !type.IsAbstract && index.TransitiveSubclasses(type.FullName).Count > 0,
            IsHandleBacked = isHandleBacked,
            DeclaresNativeHandle = declaresNativeHandle,
            ImplClassName = kind == KotlinTypeKind.Interface && isHandleBacked ? NameMapper.ImplClassName(type.Name) : null,
            TypeParameters = type.TypeParameters.Select(p => p.Name).ToList(),
            SuperClass = superClass,
            SuperClassCall = superClassCall,
            SuperInterfaces = superInterfaces,
            PrimaryConstructor = primaryConstructor,
            SecondaryConstructors = set.SecondaryConstructors,
            Properties = set.Properties
                .OrderBy(p => p.Name, StringComparer.Ordinal)
                .ThenBy(p => p.SourceDocId, StringComparer.Ordinal)
                .ToList(),
            Functions = SortFunctions(set.Functions),
            CompanionProperties = set.CompanionProperties
                .OrderBy(p => p.Name, StringComparer.Ordinal)
                .ThenBy(p => p.SourceDocId, StringComparer.Ordinal)
                .ToList(),
            CompanionFunctions = SortFunctions(set.CompanionFunctions),
            KDoc = type.RawXmlDoc,
            DeprecationMessage = FunctionBuilder.Deprecation(type.ObsoleteMessage),
            SourceDocId = type.DocId,
        };
    }

    private static List<KotlinFunction> SortFunctions(List<KotlinFunction> functions) =>
        functions
            .OrderBy(f => f.Name, StringComparer.Ordinal)
            .ThenBy(f => f.Parameters.Count)
            .ThenBy(f => string.Join(",", f.Parameters.Select(p => RenderForComparison(p.Type))), StringComparer.Ordinal)
            .ThenBy(f => f.SourceDocId, StringComparer.Ordinal)
            .ToList();

    /// <summary>Direct superinterfaces: API interfaces not already implied by another listed interface.</summary>
    private IEnumerable<TypeRef> DirectApiInterfaces(ApiType type)
    {
        var apiInterfaces = type.Interfaces.Where(i => i.IsApiAssemblyType).ToList();
        var inheritedViaOthers = new HashSet<string>(StringComparer.Ordinal);
        var inheritedViaBase = new HashSet<string>(StringComparer.Ordinal);

        foreach (var candidate in apiInterfaces)
        {
            var resolved = index.FindType(candidate);
            if (resolved is null)
                continue;
            foreach (var parent in index.AllApiInterfaces(resolved))
                inheritedViaOthers.Add(ApiIndex.Key(parent));
        }

        foreach (var baseType in index.BaseChain(type))
        {
            foreach (var parent in index.AllApiInterfaces(baseType))
                inheritedViaBase.Add(ApiIndex.Key(parent));
            foreach (var direct in baseType.Interfaces.Where(i => i.IsApiAssemblyType))
                inheritedViaBase.Add(ApiIndex.Key(direct));
        }

        return apiInterfaces.Where(i =>
            !inheritedViaOthers.Contains(ApiIndex.Key(i)) &&
            !inheritedViaBase.Contains(ApiIndex.Key(i)));
    }

    // ---- Pass 8: export naming and collection ----

    /// <summary>
    /// Names every surviving export through the shared, backend-independent
    /// allocator (so all language backends bind the same entry points) and
    /// collects the flat export/shape lists the interop emitters consume.
    /// Runs after JVM dedup so dropped members never produce orphan exports.
    /// </summary>
    private (IReadOnlyList<NativeExport>, IReadOnlyList<CallbackShape>) AssignExportNames(
        IReadOnlyList<KotlinDeclaration> declarations)
    {
        var allocator = ExportNameAllocator.Create(index);
        var exports = new List<NativeExport>();
        var shapes = new SortedDictionary<string, CallbackShape>(StringComparer.Ordinal);
        var assigned = new HashSet<string>(StringComparer.Ordinal);

        void Collect(NativeExport? export, string sourceDocId)
        {
            if (export is null)
                return;

            allocator.AssignName(export, sourceDocId);
            if (!assigned.Add(export.EntryPoint))
                throw new InvalidOperationException($"Entry point {export.EntryPoint} was assigned twice within the Kotlin backend.");
            exports.Add(export);

            foreach (var parameter in export.Parameters)
                CollectShapes(parameter.Marshal, shapes);
        }

        void Walk(KotlinTypeDeclaration type)
        {
            foreach (var ctor in type.SecondaryConstructors)
                Collect(ctor.Export, ctor.SourceDocId);

            foreach (var property in type.Properties.Concat(type.CompanionProperties))
            {
                Collect(property.Getter, property.SourceDocId);
                Collect(property.Setter, property.SourceDocId);
            }

            foreach (var function in type.Functions.Concat(type.CompanionFunctions))
            {
                if (function.Body is KotlinBody.Bridge bridged)
                    Collect(bridged.Export, function.SourceDocId);
            }

            foreach (var nested in type.NestedTypes)
                Walk(nested);
        }

        foreach (var declaration in declarations.OfType<KotlinTypeDeclaration>())
            Walk(declaration);

        foreach (var proxy in bridge.Proxies)
            CollectShapes(proxy.Method, shapes);

        return (exports, shapes.Values.ToList());
    }

    private static void CollectShapes(BridgeMarshal marshal, SortedDictionary<string, CallbackShape> shapes)
    {
        switch (marshal)
        {
            case BridgeMarshal.CallbackValue callback:
                shapes[callback.Shape.Name] = callback.Shape;
                break;
            case BridgeMarshal.UserImplValue user:
                CollectShapes(user.Proxy.Method, shapes);
                break;
        }
    }

    private static void CollectShapes(CallbackAdapter adapter, SortedDictionary<string, CallbackShape> shapes) =>
        shapes[adapter.Shape.Name] = adapter.Shape;

    internal static string RenderForComparisonPublic(KType type) => RenderForComparison(type);
}
