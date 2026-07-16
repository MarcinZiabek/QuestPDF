namespace QuestPDF.Interop.Generator.Backends.Kotlin;

/// <summary>
/// Stage 2.5: rewrites the classified Kotlin model so the emitted API is
/// equally usable from Java. Every rewrite is additive or annotation-only;
/// Kotlin call sites are unaffected (overload resolution prefers Kotlin
/// function types over SAM conversions, and exact-arity members over calls
/// that would use default values):
///
///  - object and companion members get @JvmStatic, so Java reads
///    <c>Colors.Blue.getDarken4()</c> and <c>Document.create(...)</c> instead
///    of going through INSTANCE / Companion indirections;
///  - functions with default parameter values get @JvmOverloads where the
///    annotation is legal, and explicit delegating overloads inside
///    interfaces (where @JvmOverloads is not applicable);
///  - secondary constructors with default parameter values get @JvmOverloads;
///  - every non-nullable <c>T.() -> Unit</c> handler parameter produces a
///    <c>java.util.function.Consumer&lt;T&gt;</c> twin overload, so Java
///    lambdas don't have to return Unit.INSTANCE.
///
/// Synthesized members whose JVM-erased signature would collide with an
/// existing member are skipped (erasure is approximated conservatively:
/// nullability, boxing and type arguments are ignored; type aliases resolve
/// to their function-type arity).
/// </summary>
public static class JavaFriendliness
{
    public static KotlinModel Apply(KotlinModel model)
    {
        // Alias names must erase like the function types they stand for.
        var aliases = model.Declarations
            .OfType<KotlinTypeAlias>()
            .ToDictionary(a => a.PackageName + "." + a.Name, a => a.AliasedType, StringComparer.Ordinal);

        return model with
        {
            Declarations = model.Declarations
                .Select(d => d is KotlinTypeDeclaration type ? Rewrite(type, aliases) : d)
                .ToList(),
        };
    }

    private static KotlinTypeDeclaration Rewrite(KotlinTypeDeclaration type, IReadOnlyDictionary<string, KType> aliases)
    {
        // Enums only carry the synthesized fromValue companion, which the
        // emitter renders with @JvmStatic directly.
        if (type.Kind == KotlinTypeKind.Enum)
            return type;

        var membersAreStatic = type.Kind == KotlinTypeKind.Object;
        var isInterface = type.Kind == KotlinTypeKind.Interface;

        // @JvmStatic is not applicable inside interface companions.
        var companionIsStatic = !isInterface;

        return type with
        {
            Properties = type.Properties.Select(p => Annotate(p, membersAreStatic)).ToList(),
            Functions = ExpandFunctions(type.Functions, membersAreStatic, isInterface, aliases),
            CompanionProperties = type.CompanionProperties.Select(p => Annotate(p, companionIsStatic)).ToList(),
            CompanionFunctions = ExpandFunctions(type.CompanionFunctions, companionIsStatic, isInterface: false, aliases),
            SecondaryConstructors = type.SecondaryConstructors
                .Select(c => c.Parameters.Any(p => p.DefaultValue is not null) ? c with { IsJvmOverloads = true } : c)
                .ToList(),
            NestedTypes = type.NestedTypes.Select(nested => Rewrite(nested, aliases)).ToList(),
        };
    }

    private static KotlinProperty Annotate(KotlinProperty property, bool markStatic) =>
        markStatic && !property.IsConst && !property.IsAbstract ? property with { IsJvmStatic = true } : property;

    private static IReadOnlyList<KotlinFunction> ExpandFunctions(
        IReadOnlyList<KotlinFunction> functions,
        bool markStatic,
        bool isInterface,
        IReadOnlyDictionary<string, KType> aliases)
    {
        var signatures = functions.Select(f => ErasedSignature(f, aliases)).ToHashSet(StringComparer.Ordinal);
        var result = new List<KotlinFunction>();

        foreach (var function in functions)
        {
            var current = markStatic ? function with { IsJvmStatic = true } : function;

            // Only bridged members are rewritten: abstract members (user-implemented
            // interfaces) define a contract, not a call target to delegate to.
            var synthesizable = current.Body is KotlinBody.Bridge && current.TypeParameters.Count == 0;
            var hasDefaults = current.Parameters.Any(p => p.DefaultValue is not null);

            if (synthesizable && hasDefaults && !isInterface)
                current = current with { IsJvmOverloads = true };

            result.Add(current);

            if (!synthesizable)
                continue;

            if (hasDefaults && isInterface)
                result.AddRange(ReducedOverloads(current, signatures, aliases));

            if (ConsumerTwin(current, signatures, aliases) is { } twin)
                result.Add(twin);

            if (UnsignedTwin(current, signatures, aliases) is { } signedTwin)
                result.Add(signedTwin);
        }

        return result;
    }

    /// <summary>
    /// Mirrors what @JvmOverloads would produce: drops defaulted parameters
    /// from the end, one at a time, each overload delegating to the full form.
    /// </summary>
    private static IEnumerable<KotlinFunction> ReducedOverloads(
        KotlinFunction function,
        ISet<string> signatures,
        IReadOnlyDictionary<string, KType> aliases)
    {
        var parameters = function.Parameters;

        for (var kept = parameters.Count - 1; kept >= 0; kept--)
        {
            if (parameters[kept].DefaultValue is null)
                break;

            var arguments = parameters.Take(kept)
                .Select(p => (KDelegateArg)new KDelegateArg.Ref(p.Name, p.IsVararg))
                .Concat(parameters.Skip(kept).Select(p => (KDelegateArg)new KDelegateArg.Default(p.DefaultValue!)))
                .ToList();

            var reduced = function with
            {
                // Kept parameters lose their defaults so each arity has exactly
                // one applicable overload.
                Parameters = parameters.Take(kept).Select(p => p with { DefaultValue = null }).ToList(),
                Body = new KotlinBody.Delegate(function.Name, arguments),
                IsJvmOverloads = false,
            };

            if (signatures.Add(ErasedSignature(reduced, aliases)))
                yield return reduced;
        }
    }

    /// <summary>
    /// A Consumer&lt;T&gt; twin for functions taking a <c>T.() -> Unit</c>
    /// handler. Kotlin keeps the receiver-lambda form; Java callers get a
    /// void-returning SAM.
    /// </summary>
    private static KotlinFunction? ConsumerTwin(
        KotlinFunction function,
        ISet<string> signatures,
        IReadOnlyDictionary<string, KType> aliases)
    {
        if (!function.Parameters.Any(p => IsReceiverUnitHandler(p.Type)))
            return null;

        var parameters = function.Parameters
            .Select(p => IsReceiverUnitHandler(p.Type)
                ? p with { Type = ConsumerOf(p.Type.LambdaReceiver!), DefaultValue = null }
                : p with { DefaultValue = null })
            .ToList();

        var arguments = function.Parameters
            .Select(p => (KDelegateArg)(IsReceiverUnitHandler(p.Type)
                ? new KDelegateArg.ConsumerAccept(p.Name)
                : new KDelegateArg.Ref(p.Name, p.IsVararg)))
            .ToList();

        var twin = function with
        {
            Parameters = parameters,
            Body = new KotlinBody.Delegate(function.Name, arguments),
            IsJvmOverloads = false,
        };

        return signatures.Add(ErasedSignature(twin, aliases)) ? twin : null;
    }

    /// <summary>
    /// A signed twin for functions taking Kotlin unsigned scalars: their JVM
    /// names are mangled, so Java cannot call them at all. The twin takes
    /// Int/Long and reinterprets (two's complement), matching what an explicit
    /// Kotlin .toUInt()/.toUByte() conversion produces — the java.awt.Color
    /// idiom for packed 0xAARRGGBB values.
    /// </summary>
    private static KotlinFunction? UnsignedTwin(
        KotlinFunction function,
        ISet<string> signatures,
        IReadOnlyDictionary<string, KType> aliases)
    {
        if (!function.Parameters.Any(p => UnsignedConversion(p.Type) is not null))
            return null;

        var parameters = function.Parameters
            .Select(p => UnsignedConversion(p.Type) is not null
                ? p with { Type = SignedCounterpart(p.Type), DefaultValue = null }
                : p with { DefaultValue = null })
            .ToList();

        var arguments = function.Parameters
            .Select(p => (KDelegateArg)(UnsignedConversion(p.Type) is { } conversion
                ? new KDelegateArg.Converted(p.Name, conversion)
                : new KDelegateArg.Ref(p.Name, p.IsVararg)))
            .ToList();

        var twin = function with
        {
            Parameters = parameters,
            Body = new KotlinBody.Delegate(function.Name, arguments),
            IsJvmOverloads = false,
        };

        return signatures.Add(ErasedSignature(twin, aliases)) ? twin : null;
    }

    private static string? UnsignedConversion(KType type) =>
        type is { IsFunctionType: false, IsNullable: false, PackageName: "kotlin", TypeArguments.Count: 0 }
            ? type.Name switch
            {
                "UByte" => "toUByte",
                "UShort" => "toUShort",
                "UInt" => "toUInt",
                "ULong" => "toULong",
                _ => null,
            }
            : null;

    private static KType SignedCounterpart(KType type) =>
        KType.Named("kotlin", type.Name == "ULong" ? "Long" : "Int");

    private static bool IsReceiverUnitHandler(KType type) =>
        type is { IsFunctionType: true, IsNullable: false, LambdaReceiver: not null }
        && type.FunctionParameters.Count == 0
        && (type.FunctionReturn?.IsKotlinUnit ?? true);

    private static KType ConsumerOf(KType receiver) =>
        KType.Named("java.util.function", "Consumer", args: [receiver]);

    private static string ErasedSignature(KotlinFunction function, IReadOnlyDictionary<string, KType> aliases) =>
        function.Name + "(" + string.Join(",", function.Parameters.Select(p => Erased(p, aliases))) + ")";

    private static string Erased(KotlinParameter parameter, IReadOnlyDictionary<string, KType> aliases)
    {
        var type = parameter.Type;
        if (!type.IsFunctionType && aliases.TryGetValue(type.FullName, out var aliased))
            type = aliased;

        string erased;
        if (type.IsFunctionType)
            erased = "kotlin.jvm.functions.Function" + (type.FunctionParameters.Count + (type.LambdaReceiver is null ? 0 : 1));
        else if (type.IsGenericParameter)
            erased = "java.lang.Object";
        else
            erased = type.FullName;

        return parameter.IsVararg ? erased + "[]" : erased;
    }
}
