using QuestPDF.Interop.Generator.Core.Bridge;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Backends.TypeScript;

/// <summary>
/// TypeScript classification orchestrator: runs the type and member rule
/// pipelines over the extracted model and assembles the TypeScript-level model,
/// the native export plans and the classification report. Deterministic: input
/// is processed in stable order and members are sorted before assembly.
/// </summary>
public sealed class TsClassifier
{
    private readonly ApiIndex index;
    private readonly ManualOverrides overrides;
    private readonly Dictionary<string, TypePlan> plans = new(StringComparer.Ordinal);
    private readonly List<ReportEntry> reports = [];
    private readonly TsTypeMapper mapper;
    private readonly BridgePlanner bridge;
    private readonly TsClassifierServices services;

    private sealed class MemberSet
    {
        public List<TsFunction> Functions = [];
        public List<TsFunction> StaticFunctions = [];
        public List<TsProperty> Properties = [];
        public List<TsProperty> StaticProperties = [];
        public TsConstructor? BridgedConstructor;
    }

    private readonly Dictionary<string, MemberSet> memberSets = new(StringComparer.Ordinal);

    private TsClassifier(ApiIndex index, TsTypeMapper mapper, ManualOverrides overrides)
    {
        this.index = index;
        this.mapper = mapper;
        this.overrides = overrides;
        bridge = new BridgePlanner(index, key => plans.GetValueOrDefault(key));
        services = new TsClassifierServices
        {
            Index = index,
            Mapper = mapper,
            Bridge = bridge,
            Overrides = overrides,
            PlanFor = key => plans.GetValueOrDefault(key),
        };
    }

    /// <summary>Classifies against the given index/mapper — the same instances the emission views must share.</summary>
    public static TsModel Classify(ApiIndex index, TsTypeMapper mapper, ManualOverrides overrides)
    {
        var classifier = new TsClassifier(index, mapper, overrides);
        return classifier.Run();
    }

    private IEnumerable<ApiType> SortedTypes =>
        index.Assembly.Types.OrderBy(ApiIndex.Key, StringComparer.Ordinal);

    /// <summary>Exception types stay client-side; .NET exception details travel over the error channel.</summary>
    private static bool IsExceptionPlan(TypePlan plan) => plan.DecidedByRule == "exception";

    private TsModel Run()
    {
        ClassifyTypes();
        ClassifyConstructors();
        ClassifyMethods();
        ClassifyPropertiesAndFields();
        DeduplicateRuntimeSignatures();
        MergeInheritedMembers();
        var declarations = BuildDeclarations();
        var (exports, shapes) = AssignExportNames(declarations);

        return new TsModel(
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
            var plan = TypeRulePipeline.Classify(type, index, overrides, TypeScriptTypeRules.All);
            plans[ApiIndex.Key(type)] = plan;
            memberSets[ApiIndex.Key(type)] = new MemberSet();

            if (plan.Kind == TypePlanKind.Unsupported)
                index.MarkTypeUnsupported(type, plan.Detail);

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
                        ApiClassification.ManualOverride, "excluded via the manual-overrides file", "manual-override"));
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

                var builder = new TsFunctionBuilder(services);
                var built = builder.Build(
                    new TsMethodContext(type, ctor, services), type, "constructor", ctor.Parameters,
                    invocation, receiverType: null,
                    forcedCSharpReturn: TsFunctionBuilder.TypeRefOf(type),
                    forcedTsReturn: TsType.Void);

                if (built.Failure is not null)
                {
                    reports.Add(new ReportEntry(ctor.DocId, SignatureRenderer.Render(ctor),
                        ApiClassification.Unsupported, built.Failure, "constructor"));
                    continue;
                }

                var export = ((TsBody.Bridge)built.Function!.Body).Export;
                target.BridgedConstructor = new TsConstructor(
                    built.Function.Parameters, export, ctor.RawXmlDoc,
                    TsFunctionBuilder.Deprecation(ctor.ObsoleteMessage), ctor.DocId);

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
                var outcome = TsMethodRulePipeline.Classify(new TsMethodContext(type, method, services));
                reports.Add(outcome.Report);

                foreach (var placement in outcome.Emissions)
                {
                    var target = memberSets.GetValueOrDefault(placement.TargetTypeKey);
                    if (target is null)
                        continue;

                    if (placement.IsStatic && plans[placement.TargetTypeKey].Kind == TypePlanKind.Class)
                        target.StaticFunctions.Add(placement.Function);
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
                ApiClassification.ManualOverride, "excluded via the manual-overrides file", "manual-override"));
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

        var tsType = mapped.Type!;
        var isConstantStyle = property.IsStatic && !property.HasSetter;
        var name = isConstantStyle ? TsNameMapper.Constant(property.Name) : TsNameMapper.Member(property.Name);

        // Members of user-implemented interfaces stay signature-only.
        if (plan.Kind == TypePlanKind.Interface && bridge.IsUserImplemented(type))
        {
            target.Properties.Add(NewProperty(property, name, tsType, isAbstract: true));
            reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
                ApiClassification.Generated, "user-implemented interface property kept abstract", "property"));
            return;
        }

        // Compile-time constants keep their captured literal client-side.
        if (isConstantStyle && LiteralInitializer(tsType, property.CapturedValue) is { } constant)
        {
            var literalProperty = NewProperty(property, name, tsType, isAbstract: false) with
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

        var tsProperty = NewProperty(property, name, tsType, isAbstract: false) with
        {
            Getter = accessors.Getter,
            Setter = accessors.Setter,
        };
        Place(type, plan, property.IsStatic, tsProperty, target);

        reports.Add(new ReportEntry(property.DocId, SignatureRenderer.Render(property),
            ApiClassification.Generated,
            property.IsStatic && plan.Kind == TypePlanKind.Class
                ? "static property mapped to bridged static accessors"
                : "mapped to bridged property accessors",
            "property"));
    }

    private TsProperty NewProperty(ApiProperty property, string name, TsType tsType, bool isAbstract) => new()
    {
        Name = name,
        Type = tsType,
        IsMutable = property.HasSetter,
        IsAbstract = isAbstract,
        Doc = property.RawXmlDoc,
        DeprecationMessage = TsFunctionBuilder.Deprecation(property.ObsoleteMessage),
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
                ApiClassification.ManualOverride, "excluded via the manual-overrides file", "manual-override"));
            return;
        }

        var mapped = mapper.Map(field.Type);
        if (!mapped.Success)
        {
            reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
                ApiClassification.Unsupported, mapped.FailureReason!, "field-type"));
            return;
        }

        var tsType = mapped.Type!;
        var isConstantStyle = field.IsConst || (field.IsStatic && field.IsReadOnly);
        var name = isConstantStyle ? TsNameMapper.Constant(field.Name) : TsNameMapper.Member(field.Name);

        if (isConstantStyle && LiteralInitializer(tsType, field.CapturedValue) is { } constant)
        {
            var literalProperty = new TsProperty
            {
                Name = name,
                Type = tsType,
                IsMutable = false,
                Initializer = constant,
                Doc = field.RawXmlDoc,
                DeprecationMessage = TsFunctionBuilder.Deprecation(field.ObsoleteMessage),
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

        var tsProperty = new TsProperty
        {
            Name = name,
            Type = tsType,
            IsMutable = !field.IsConst && !field.IsReadOnly,
            Getter = accessors.Getter,
            Setter = accessors.Setter,
            Doc = field.RawXmlDoc,
            DeprecationMessage = TsFunctionBuilder.Deprecation(field.ObsoleteMessage),
            SourceDocId = field.DocId,
        };
        Place(type, plan, field.IsStatic, tsProperty, target);

        reports.Add(new ReportEntry(field.DocId, SignatureRenderer.Render(field),
            ApiClassification.Generated, "field mapped to bridged accessors", "field"));
    }

    private void Place(ApiType type, TypePlan plan, bool isStatic, TsProperty property, MemberSet target)
    {
        var toStatic = isStatic && plan.Kind == TypePlanKind.Class;
        (toStatic ? target.StaticProperties : target.Properties).Add(property);
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
                : bridge.Plan(TsFunctionBuilder.TypeRefOf(type), MarshalPosition.Parameter) as BridgeMarshal.Handle
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

    private static TsExpr? LiteralInitializer(TsType type, string? captured)
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
            "string" when type.Name == "string" => new TsExpr.StringLiteral(payload),
            "bool" when type.Name == "boolean" => new TsExpr.Literal(payload),
            "number" when type.Name == "number" => new TsExpr.Literal(payload),
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

    // ---- Pass 5: runtime-dispatch dedup ----

    /// <summary>
    /// JavaScript has no overloading: same-name functions are emitted as
    /// TypeScript overload signatures dispatched at runtime on argument count
    /// and type tests. Overloads no test can tell apart are dropped here, the
    /// way JVM-erasure clashes are dropped by the Kotlin backend.
    /// </summary>
    private void DeduplicateRuntimeSignatures()
    {
        foreach (var (key, set) in memberSets)
        {
            set.Functions = Deduplicate(set.Functions, key);
            set.StaticFunctions = Deduplicate(set.StaticFunctions, key);
        }
    }

    private List<TsFunction> Deduplicate(List<TsFunction> functions, string typeKey)
    {
        var kept = new List<TsFunction>();

        // Prefer non-deprecated members on clashes.
        foreach (var function in functions.OrderBy(f => f.DeprecationMessage is null ? 0 : 1))
        {
            var clash = kept.FirstOrDefault(k =>
                k.Name == function.Name &&
                !Distinguishable(k, function, position => HandleClassesDistinguishable(k, function, position)));
            if (clash is not null)
            {
                if (clash.SourceDocId != function.SourceDocId)
                {
                    reports.Add(new ReportEntry(function.SourceDocId,
                        $"{typeKey}.{function.Name}",
                        ApiClassification.Unsupported,
                        $"runtime dispatch cannot distinguish this overload of '{function.Name}' from the one kept; JavaScript sees the same argument shapes",
                        "runtime-signature-dedup"));
                }
                continue;
            }

            kept.Add(function);
        }

        return kept;
    }

    /// <summary>
    /// True when both positions carry concrete-class handles of nominally
    /// disjoint classes: the dispatcher then routes with instanceof tests
    /// against the concrete wrappers (TypeScript's structural typing would
    /// otherwise let either instance flow into either overload and fail with
    /// an invalid cast on the .NET side).
    /// </summary>
    private bool HandleClassesDistinguishable(TsFunction a, TsFunction b, int position)
    {
        if (HandleDispatchClass(a, position) is not { } classA || HandleDispatchClass(b, position) is not { } classB)
            return false;

        return classA != classB && !DerivesFrom(classA, classB) && !DerivesFrom(classB, classA);
    }

    /// <summary>The concrete generated class behind a handle parameter, null for interfaces and non-handles.</summary>
    private string? HandleDispatchClass(TsFunction function, int position)
    {
        if (function.Body is not TsBody.Bridge bridge || position >= bridge.Export.Parameters.Count)
            return null;

        if (bridge.Export.Parameters[position].Marshal is not BridgeMarshal.Handle handle)
            return null;

        return index.FindType(handle.CSharpType) is { Kind: ApiTypeKind.Class } apiType ? apiType.FullName : null;
    }

    private bool DerivesFrom(string derived, string baseName)
    {
        var current = index.FindType(derived);

        while (current?.BaseType is { } baseType)
        {
            if (baseType.FullName == baseName)
                return true;

            current = index.FindType(baseType.FullName);
        }

        return false;
    }

    /// <summary>
    /// True when the dispatcher can always route between the two overloads:
    /// their arity ranges are disjoint, or every shared argument count has a
    /// position whose runtime kinds cannot both accept one value (or, via
    /// [positionRefinement], a position-specific test can).
    /// </summary>
    internal static bool Distinguishable(TsFunction a, TsFunction b) => Distinguishable(a, b, null);

    internal static bool Distinguishable(TsFunction a, TsFunction b, Func<int, bool>? positionRefinement)
    {
        var (minA, maxA) = ArityRange(a);
        var (minB, maxB) = ArityRange(b);

        var lo = Math.Max(minA, minB);
        var hi = Math.Min(maxA, maxB);
        if (lo > hi)
            return true;

        // Beyond every declared parameter only rest tests repeat, so shared
        // arities stabilize past the longer declared list.
        var cap = Math.Min(hi, Math.Max(a.Parameters.Count, b.Parameters.Count) + 1);
        cap = Math.Max(cap, lo);

        for (var arity = lo; arity <= cap; arity++)
        {
            var distinguished = false;
            for (var position = 0; position < arity && !distinguished; position++)
            {
                var testA = TestAt(a, position);
                var testB = TestAt(b, position);
                if (testA is null || testB is null)
                    continue;

                var bothAcceptNull = testA.Value.AcceptsNull && testB.Value.AcceptsNull;
                if (!Overlaps(testA.Value.Kind, testB.Value.Kind) && !bothAcceptNull)
                    distinguished = true;
                else if (!bothAcceptNull && positionRefinement?.Invoke(position) == true)
                    distinguished = true;
            }

            if (!distinguished)
                return false;
        }

        return true;
    }

    private static (int Min, int Max) ArityRange(TsFunction function)
    {
        var min = 0;
        foreach (var parameter in function.Parameters)
        {
            if (parameter.IsRest || parameter.DefaultValue is not null)
                break;
            min++;
        }

        var hasRest = function.Parameters.Any(p => p.IsRest);
        return (min, hasRest ? int.MaxValue : function.Parameters.Count);
    }

    private static (RuntimeKind Kind, bool AcceptsNull)? TestAt(TsFunction function, int position)
    {
        if (function.Parameters.Count == 0)
            return null;

        var restIndex = function.Parameters.ToList().FindIndex(p => p.IsRest);
        if (restIndex >= 0 && position >= restIndex)
            return (KindAt(function, restIndex), false);

        if (position >= function.Parameters.Count)
            return null;

        var parameter = function.Parameters[position];
        var acceptsNull = parameter.Type.IsNullable || parameter.DefaultValue is not null;
        return (KindAt(function, position), acceptsNull);
    }

    private static RuntimeKind KindAt(TsFunction function, int position) =>
        position < function.RuntimeKinds.Count ? function.RuntimeKinds[position] : RuntimeKind.Unknown;

    /// <summary>Whether one JavaScript value could pass both kinds' runtime tests.</summary>
    internal static bool Overlaps(RuntimeKind a, RuntimeKind b)
    {
        if (a == b)
            return true;
        if (a == RuntimeKind.Unknown || b == RuntimeKind.Unknown)
            return true;

        bool Pair(RuntimeKind x, RuntimeKind y) => (a == x && b == y) || (a == y && b == x);

        // Strings, arrays and byte arrays are iterable; wrappers can satisfy a
        // user-implemented interface structurally.
        return Pair(RuntimeKind.Iterable, RuntimeKind.String) ||
               Pair(RuntimeKind.Iterable, RuntimeKind.ArrayLike) ||
               Pair(RuntimeKind.Iterable, RuntimeKind.Bytes) ||
               Pair(RuntimeKind.Iterable, RuntimeKind.UserImpl) ||
               Pair(RuntimeKind.UserImpl, RuntimeKind.Handle);
    }

    // ---- Pass 6: inherited-member completion ----

    /// <summary>
    /// TypeScript's structural inheritance needs two completions the JVM does
    /// not: an interface member hiding a same-name inherited member must
    /// redeclare the inherited overloads (TypeScript replaces instead of
    /// overloading), and a class implementing an API interface must implement
    /// every interface member itself (there are no default interface bodies).
    /// The inherited functions reuse the supertype's export plans; export
    /// collection deduplicates shared instances.
    /// </summary>
    private void MergeInheritedMembers()
    {
        foreach (var type in SortedTypes)
        {
            var plan = plans[ApiIndex.Key(type)];
            var set = memberSets[ApiIndex.Key(type)];

            if (plan.Kind == TypePlanKind.Interface && !bridge.IsUserImplemented(type))
                MergeCollidingInterfaceOverloads(type, set);

            if (plan.Kind == TypePlanKind.Class && !IsExceptionPlan(plan))
                MergeImplementedInterfaceMembers(type, set);
        }
    }

    /// <summary>Adds inherited overloads of names the interface redeclares with a different shape.</summary>
    private void MergeCollidingInterfaceOverloads(ApiType type, MemberSet set)
    {
        var ownNames = set.Functions.Select(f => f.Name).ToHashSet(StringComparer.Ordinal);
        if (ownNames.Count == 0)
            return;

        foreach (var supertype in index.AllApiInterfaces(type))
        {
            var superset = memberSets.GetValueOrDefault(ApiIndex.Key(supertype));
            if (superset is null || bridge.IsUserImplemented(supertype))
                continue;

            foreach (var inherited in superset.Functions)
            {
                if (!ownNames.Contains(inherited.Name))
                    continue;
                if (set.Functions.Any(own => own.Name == inherited.Name && SameShape(own, inherited)))
                    continue;
                if (!set.Functions.Where(own => own.Name == inherited.Name).All(own => Distinguishable(own, inherited)))
                    continue;

                set.Functions.Add(inherited);
            }
        }
    }

    /// <summary>Copies every member of the class's API interfaces the class does not already provide.</summary>
    private void MergeImplementedInterfaceMembers(ApiType type, MemberSet set)
    {
        var baseSets = index.BaseChain(type)
            .Select(b => memberSets.GetValueOrDefault(ApiIndex.Key(b)))
            .Where(s => s is not null)
            .Select(s => s!)
            .ToList();

        foreach (var supertype in index.AllApiInterfaces(type))
        {
            // Members of interfaces already implemented by a generated base
            // class arrive through the extends chain.
            var viaBase = index.BaseChain(type).Any(b => index.AllApiInterfaces(b).Contains(supertype));
            if (viaBase)
                continue;

            var superset = memberSets.GetValueOrDefault(ApiIndex.Key(supertype));
            if (superset is null || bridge.IsUserImplemented(supertype))
                continue;

            foreach (var inherited in superset.Functions)
            {
                bool Provided(MemberSet candidate) =>
                    candidate.Functions.Any(own => own.Name == inherited.Name && SameShape(own, inherited));

                if (Provided(set) || baseSets.Any(Provided))
                    continue;
                if (!set.Functions.Where(own => own.Name == inherited.Name).All(own => Distinguishable(own, inherited)))
                    continue;

                set.Functions.Add(inherited);
            }

            foreach (var inherited in superset.Properties)
            {
                bool Provided(MemberSet candidate) => candidate.Properties.Any(own => own.Name == inherited.Name);
                if (Provided(set) || baseSets.Any(Provided))
                    continue;

                set.Properties.Add(inherited);
            }
        }
    }

    private static bool SameShape(TsFunction a, TsFunction b)
    {
        if (a.Parameters.Count != b.Parameters.Count)
            return false;

        for (var i = 0; i < a.Parameters.Count; i++)
        {
            if (a.Parameters[i].IsRest != b.Parameters[i].IsRest)
                return false;
            if (RenderForComparison(a.Parameters[i].Type) != RenderForComparison(b.Parameters[i].Type))
                return false;
        }

        return true;
    }

    // ---- Pass 7: declaration assembly ----

    private IReadOnlyList<TsDeclaration> BuildDeclarations()
    {
        var built = new Dictionary<string, TsTypeDeclaration>(StringComparer.Ordinal);
        var aliases = new List<TsTypeAlias>();

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
                    built[ApiIndex.Key(type)] = BuildTypeDeclaration(type, plan, TsTypeKind.StaticHolder);
                    break;

                case TypePlanKind.Class:
                    built[ApiIndex.Key(type)] = BuildTypeDeclaration(type, plan, TsTypeKind.Class);
                    break;

                case TypePlanKind.Interface:
                    built[ApiIndex.Key(type)] = BuildTypeDeclaration(type, plan, TsTypeKind.Interface);
                    break;
            }
        }

        // Nest declarations under their declaring type when that type is emitted.
        var topLevel = new List<TsDeclaration>();
        var nestedByParent = new Dictionary<string, List<TsTypeDeclaration>>(StringComparer.Ordinal);

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

        foreach (var type in SortedTypes)
        {
            if (!built.TryGetValue(ApiIndex.Key(type), out var declaration))
                continue;

            var isNested = type.DeclaringTypeFullName is not null && built.ContainsKey(type.DeclaringTypeFullName);
            if (isNested)
                continue;

            topLevel.Add(AttachNestedRecursively(type.FullName, declaration, nestedByParent));
        }

        topLevel.AddRange(aliases);

        return topLevel
            .OrderBy(d => d.ModulePath, StringComparer.Ordinal)
            .ThenBy(d => d.Name, StringComparer.Ordinal)
            .ToList();
    }

    private TsTypeDeclaration AttachNestedRecursively(
        string fullName,
        TsTypeDeclaration declaration,
        Dictionary<string, List<TsTypeDeclaration>> nestedByParent)
    {
        if (!nestedByParent.TryGetValue(fullName, out var children))
            return declaration;

        var updated = children
            .OrderBy(c => c.Name, StringComparer.Ordinal)
            .Select(nested =>
            {
                var nestedFullName = index.Assembly.Types.FirstOrDefault(t => t.DocId == nested.SourceDocId)?.FullName;
                return nestedFullName is null ? nested : AttachNestedRecursively(nestedFullName, nested, nestedByParent);
            })
            .ToList();

        return declaration with { NestedTypes = updated };
    }

    private TsTypeDeclaration BuildEnum(ApiType type)
    {
        var entries = type.EnumMembers
            .Select(m => new TsEnumEntry(m.Name, m.Value, m.RawXmlDoc, TsFunctionBuilder.Deprecation(m.ObsoleteMessage), m.DocId))
            .ToList();

        return new TsTypeDeclaration
        {
            ModulePath = TsNameMapper.Module(type.Namespace),
            Name = type.Name,
            Kind = TsTypeKind.Enum,
            EnumEntries = entries,
            Doc = type.RawXmlDoc,
            DeprecationMessage = TsFunctionBuilder.Deprecation(type.ObsoleteMessage),
            SourceDocId = type.DocId,
        };
    }

    private TsTypeAlias? BuildAlias(ApiType type)
    {
        var info = type.DelegateInfo!;
        var parameters = new List<TsParameter>();

        foreach (var parameter in info.Parameters)
        {
            var mapped = mapper.Map(parameter.Type);
            if (!mapped.Success)
            {
                index.MarkTypeUnsupported(type, $"delegate parameter unmappable: {mapped.FailureReason}");
                ReplaceTypeReport(type, mapped.FailureReason!);
                return null;
            }

            parameters.Add(new TsParameter(TsNameMapper.Parameter(parameter.Name), mapped.Type!, null, false));
        }

        var returnMapped = mapper.Map(info.ReturnType);
        if (!returnMapped.Success)
        {
            index.MarkTypeUnsupported(type, $"delegate return unmappable: {returnMapped.FailureReason}");
            ReplaceTypeReport(type, returnMapped.FailureReason!);
            return null;
        }

        return new TsTypeAlias
        {
            ModulePath = TsNameMapper.Module(type.Namespace),
            Name = type.Name,
            AliasedType = TsType.Function(parameters, returnMapped.Type!),
            Doc = type.RawXmlDoc,
            DeprecationMessage = TsFunctionBuilder.Deprecation(type.ObsoleteMessage),
            SourceDocId = type.DocId,
        };
    }

    private void ReplaceTypeReport(ApiType type, string reason)
    {
        reports.RemoveAll(r => r.DocId == type.DocId);
        reports.Add(new ReportEntry(type.DocId, SignatureRenderer.Render(type),
            ApiClassification.Unsupported, reason, "delegate-to-typealias"));
    }

    private TsTypeDeclaration BuildTypeDeclaration(ApiType type, TypePlan plan, TsTypeKind kind)
    {
        var set = memberSets[ApiIndex.Key(type)];
        var isException = kind == TsTypeKind.Class && IsExceptionPlan(plan);
        var isUserImplemented = kind == TsTypeKind.Interface && bridge.IsUserImplemented(type);
        var isHandleBacked = (kind == TsTypeKind.Class && !isException) ||
                             (kind == TsTypeKind.Interface && !isUserImplemented);

        // Supertypes.
        TsType? superClass = null;
        if (kind == TsTypeKind.Class && !isException &&
            type.BaseType is { IsApiAssemblyType: true } apiBase && mapper.Map(apiBase) is { Success: true } mappedBase)
        {
            superClass = mappedBase.Type;
        }

        var superInterfaces = DirectApiInterfaces(type)
            .Select(i => mapper.Map(i))
            .Where(m => m.Success)
            .Select(m => m.Type!)
            .OrderBy(t => t.Name, StringComparer.Ordinal)
            .ToList();

        // Handle-backed interfaces inherit nativeHandle from any handle-backed
        // API superinterface; roots declare it themselves.
        var declaresNativeHandle = kind == TsTypeKind.Interface && isHandleBacked &&
            !index.AllApiInterfaces(type).Any(i =>
                plans.GetValueOrDefault(ApiIndex.Key(i))?.Kind == TypePlanKind.Interface &&
                !bridge.IsUserImplemented(i));

        // The Impl wrapper extends the Impl of the single handle-backed direct
        // superinterface so inherited members need no re-emission.
        TsType? implBase = null;
        if (kind == TsTypeKind.Interface && isHandleBacked)
        {
            var handleBackedSupers = DirectApiInterfaces(type)
                .Select(i => index.FindType(i))
                .Where(i => i is not null &&
                            plans.GetValueOrDefault(ApiIndex.Key(i!))?.Kind == TypePlanKind.Interface &&
                            !bridge.IsUserImplemented(i!))
                .Select(i => i!)
                .OrderBy(i => i.FullName, StringComparer.Ordinal)
                .ToList();

            if (handleBackedSupers.Count > 1)
                throw new InvalidOperationException(
                    $"{type.FullName} extends {handleBackedSupers.Count} handle-backed interfaces; " +
                    "the TypeScript Impl class can only extend one Impl base. Emit the remaining members directly to support this.");

            if (handleBackedSupers.Count == 1)
                implBase = TsType.Named(
                    TsNameMapper.Module(handleBackedSupers[0].Namespace),
                    TsNameMapper.ImplClassName(handleBackedSupers[0].Name)) with
                {
                    DeclaredInFile = handleBackedSupers[0].Name,
                };
        }

        return new TsTypeDeclaration
        {
            ModulePath = TsNameMapper.Module(type.Namespace),
            Name = type.Name,
            Kind = kind,
            IsAbstract = kind == TsTypeKind.Class && type.IsAbstract,
            IsHandleBacked = isHandleBacked,
            DeclaresNativeHandle = declaresNativeHandle,
            ImplClassName = kind == TsTypeKind.Interface && isHandleBacked ? TsNameMapper.ImplClassName(type.Name) : null,
            ImplBaseClass = implBase,
            SuperClass = superClass,
            SuperInterfaces = superInterfaces,
            IsException = isException,
            BridgedConstructor = set.BridgedConstructor,
            Properties = SortProperties(set.Properties),
            FunctionGroups = GroupFunctions(set.Functions),
            StaticProperties = SortProperties(set.StaticProperties),
            StaticFunctionGroups = GroupFunctions(set.StaticFunctions),
            Doc = type.RawXmlDoc,
            DeprecationMessage = TsFunctionBuilder.Deprecation(type.ObsoleteMessage),
            SourceDocId = type.DocId,
        };
    }

    private static List<TsProperty> SortProperties(List<TsProperty> properties) =>
        properties
            .OrderBy(p => p.Name, StringComparer.Ordinal)
            .ThenBy(p => p.SourceDocId, StringComparer.Ordinal)
            .ToList();

    private static List<TsFunctionGroup> GroupFunctions(List<TsFunction> functions) =>
        functions
            .OrderBy(f => f.Name, StringComparer.Ordinal)
            .ThenBy(f => f.Parameters.Count)
            .ThenBy(f => string.Join(",", f.Parameters.Select(p => RenderForComparison(p.Type))), StringComparer.Ordinal)
            .ThenBy(f => f.SourceDocId, StringComparer.Ordinal)
            .GroupBy(f => f.Name, StringComparer.Ordinal)
            .Select(g => new TsFunctionGroup(g.Key, g.ToList()))
            .ToList();

    private static string RenderForComparison(TsType type)
    {
        if (type.IsFunctionType)
        {
            var parameters = string.Join(",", type.FunctionParameters.Select(p => RenderForComparison(p.Type)));
            var returns = type.FunctionReturn is null ? "void" : RenderForComparison(type.FunctionReturn);
            return $"({parameters})->{returns}{(type.IsNullable ? "?" : "")}";
        }

        if (type.IsArray)
            return RenderForComparison(type.ElementType!) + "[]" + (type.IsNullable ? "?" : "");

        var arguments = type.TypeArguments.Count == 0
            ? ""
            : "<" + string.Join(",", type.TypeArguments.Select(RenderForComparison)) + ">";

        return type.ModulePath + "/" + type.Name + arguments + (type.IsNullable ? "?" : "");
    }

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
    /// Members merged from supertypes share their export instances, so
    /// collection deduplicates by reference.
    /// </summary>
    private (IReadOnlyList<NativeExport>, IReadOnlyList<CallbackShape>) AssignExportNames(
        IReadOnlyList<TsDeclaration> declarations)
    {
        var allocator = ExportNameAllocator.Create(index);
        var exports = new List<NativeExport>();
        var shapes = new SortedDictionary<string, CallbackShape>(StringComparer.Ordinal);
        var collected = new HashSet<NativeExport>(ReferenceEqualityComparer.Instance);
        var assigned = new HashSet<string>(StringComparer.Ordinal);

        void Collect(NativeExport? export, string sourceDocId)
        {
            if (export is null || !collected.Add(export))
                return;

            allocator.AssignName(export, sourceDocId);
            if (!assigned.Add(export.EntryPoint))
                throw new InvalidOperationException($"Entry point {export.EntryPoint} was assigned twice within the TypeScript backend.");
            exports.Add(export);

            foreach (var parameter in export.Parameters)
                CollectShapes(parameter.Marshal, shapes);
        }

        void Walk(TsTypeDeclaration type)
        {
            if (type.BridgedConstructor is { } ctor)
                Collect(ctor.Export, ctor.SourceDocId);

            foreach (var property in type.Properties.Concat(type.StaticProperties))
            {
                Collect(property.Getter, property.SourceDocId);
                Collect(property.Setter, property.SourceDocId);
            }

            foreach (var group in type.FunctionGroups.Concat(type.StaticFunctionGroups))
            {
                foreach (var function in group.Overloads)
                {
                    if (function.Body is TsBody.Bridge bridged)
                        Collect(bridged.Export, function.SourceDocId);
                }
            }

            foreach (var nested in type.NestedTypes)
                Walk(nested);
        }

        foreach (var declaration in declarations.OfType<TsTypeDeclaration>())
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
}
