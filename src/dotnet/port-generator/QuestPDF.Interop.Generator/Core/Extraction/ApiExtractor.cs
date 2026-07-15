using System.Globalization;
using System.Reflection;
using System.Runtime.CompilerServices;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Core.Extraction;

/// <summary>
/// Stage 1: walks the QuestPDF assembly with reflection and produces the plain
/// <see cref="ApiAssembly"/> model. Nothing downstream touches reflection.
/// </summary>
public sealed class ApiExtractor
{
    private readonly Assembly assembly;
    private readonly XmlDocLoader documentation;
    private readonly NullabilityInfoContext nullability = new();

    private const BindingFlags DeclaredPublic =
        BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static | BindingFlags.DeclaredOnly;

    public ApiExtractor(Assembly assembly, XmlDocLoader documentation)
    {
        this.assembly = assembly;
        this.documentation = documentation;
    }

    public ApiAssembly Extract()
    {
        var types = assembly
            .GetExportedTypes()
            .Where(t => !t.IsDefined(typeof(CompilerGeneratedAttribute), inherit: false))
            .OrderBy(t => t.FullName, StringComparer.Ordinal)
            .Select(ExtractType)
            .ToList();

        return new ApiAssembly(
            Name: assembly.GetName().Name ?? "QuestPDF",
            Version: assembly.GetName().Version?.ToString() ?? "unknown",
            Types: types);
    }

    private ApiType ExtractType(Type type)
    {
        var kind = ClassifyTypeKind(type);
        var docId = XmlDocId.ForType(type);

        var constructors = type.GetConstructors(DeclaredPublic)
            .Select(c => ExtractMethod(c, type))
            .OrderBy(m => m.DocId, StringComparer.Ordinal)
            .ToList();

        var methods = kind is ApiTypeKind.Delegate or ApiTypeKind.Enum
            ? []
            : type.GetMethods(DeclaredPublic)
                .Where(m => !IsAccessor(m))
                .Select(m => ExtractMethod(m, type))
                .OrderBy(m => m.DocId, StringComparer.Ordinal)
                .ToList();

        var properties = kind is ApiTypeKind.Delegate or ApiTypeKind.Enum
            ? []
            : type.GetProperties(DeclaredPublic)
                .Select(ExtractProperty)
                .OrderBy(p => p.DocId, StringComparer.Ordinal)
                .ToList();

        var fields = kind is ApiTypeKind.Delegate or ApiTypeKind.Enum
            ? []
            : type.GetFields(DeclaredPublic)
                .Select(ExtractField)
                .OrderBy(f => f.DocId, StringComparer.Ordinal)
                .ToList();

        var enumMembers = kind == ApiTypeKind.Enum ? ExtractEnumMembers(type) : [];

        var delegateInfo = kind == ApiTypeKind.Delegate ? ExtractDelegateInfo(type) : null;

        var hasInternalConstructorOnly =
            kind == ApiTypeKind.Class &&
            type.GetConstructors(DeclaredPublic).Length == 0 &&
            type.GetConstructors(BindingFlags.NonPublic | BindingFlags.Instance).Length > 0;

        return new ApiType(
            FullName: NormalizedFullName(type),
            Namespace: type.Namespace ?? "",
            Name: StripArity(type.Name),
            Kind: kind,
            IsAbstract: type.IsAbstract && !type.IsSealed,
            IsSealed: type.IsSealed && !type.IsAbstract,
            TypeParameters: ExtractTypeParameters(type.IsGenericTypeDefinition ? type.GetGenericArguments() : []),
            BaseType: ExtractBaseType(type),
            Interfaces: type.GetInterfaces()
                .Where(i => i.IsPublic || i.IsNestedPublic)
                .Select(i => FromType(i, null))
                .OrderBy(r => r.Render(), StringComparer.Ordinal)
                .ToList(),
            Constructors: constructors,
            Methods: methods,
            Properties: properties,
            Fields: fields,
            EnumMembers: enumMembers,
            DelegateInfo: delegateInfo,
            DeclaringTypeFullName: type.DeclaringType is null ? null : NormalizedFullName(type.DeclaringType),
            HasInternalConstructorOnly: hasInternalConstructorOnly,
            DocId: docId,
            ObsoleteMessage: ObsoleteMessage(type),
            RawXmlDoc: documentation.Find(docId));
    }

    private static ApiTypeKind ClassifyTypeKind(Type type)
    {
        if (type.IsEnum) return ApiTypeKind.Enum;
        if (type.IsValueType) return ApiTypeKind.Struct;
        if (typeof(Delegate).IsAssignableFrom(type)) return ApiTypeKind.Delegate;
        if (type.IsInterface) return ApiTypeKind.Interface;
        if (type is { IsAbstract: true, IsSealed: true }) return ApiTypeKind.StaticClass;
        return ApiTypeKind.Class;
    }

    private TypeRef? ExtractBaseType(Type type)
    {
        if (type.BaseType is null || type.BaseType == typeof(object) || type.IsValueType || type.IsInterface)
            return null;
        if (typeof(Delegate).IsAssignableFrom(type))
            return null;
        return FromType(type.BaseType, null);
    }

    private ApiMethod ExtractMethod(MethodBase method, Type declaringType)
    {
        var isExtension = method.IsDefined(typeof(ExtensionAttribute), inherit: false);
        var allParameters = method.GetParameters().Select(ExtractParameter).ToList();

        var receiver = isExtension ? allParameters[0] : null;
        var parameters = isExtension ? allParameters.Skip(1).ToList() : allParameters;

        var kind = method switch
        {
            ConstructorInfo => ApiMethodKind.Constructor,
            { IsSpecialName: true } when method.Name.StartsWith("op_", StringComparison.Ordinal) => ApiMethodKind.Operator,
            _ => ApiMethodKind.Ordinary,
        };

        var returnType = method is MethodInfo info
            ? FromType(info.ReturnType, SafeNullability(() => nullability.Create(info.ReturnParameter)))
            : new TypeRef { Kind = TypeRefKind.Named, FullName = "System.Void" };

        return new ApiMethod(
            Name: method.Name,
            Kind: kind,
            IsStatic: method.IsStatic,
            IsAbstract: method.IsAbstract,
            IsVirtual: method is { IsVirtual: true, IsFinal: false } && !method.IsAbstract,
            IsExtension: isExtension,
            ExtensionReceiver: receiver,
            TypeParameters: ExtractTypeParameters(method.IsGenericMethodDefinition ? method.GetGenericArguments() : []),
            Parameters: parameters,
            ReturnType: returnType,
            DeclaringTypeFullName: NormalizedFullName(declaringType),
            DocId: XmlDocId.ForMethod(method),
            ObsoleteMessage: ObsoleteMessage(method),
            RawXmlDoc: documentation.Find(XmlDocId.ForMethod(method)));
    }

    private ApiParameter ExtractParameter(ParameterInfo parameter)
    {
        var isCallerInfo =
            parameter.IsDefined(typeof(CallerFilePathAttribute)) ||
            parameter.IsDefined(typeof(CallerLineNumberAttribute)) ||
            parameter.IsDefined(typeof(CallerMemberNameAttribute)) ||
            parameter.IsDefined(typeof(CallerArgumentExpressionAttribute));

        return new ApiParameter(
            Name: parameter.Name ?? "arg" + parameter.Position,
            Type: FromType(parameter.ParameterType, SafeNullability(() => nullability.Create(parameter))),
            Default: ExtractDefault(parameter),
            IsParams: parameter.IsDefined(typeof(ParamArrayAttribute)),
            IsByRef: parameter.ParameterType.IsByRef,
            IsCallerInfo: isCallerInfo);
    }

    private static DefaultValue? ExtractDefault(ParameterInfo parameter)
    {
        if (!parameter.HasDefaultValue)
            return null;

        var value = parameter.DefaultValue;
        var parameterType = parameter.ParameterType;
        var underlying = Nullable.GetUnderlyingType(parameterType) ?? parameterType;

        if (value is null)
        {
            return parameterType.IsValueType && Nullable.GetUnderlyingType(parameterType) is null
                ? new DefaultValue(DefaultValueKind.DefaultStruct, "")
                : DefaultValue.Null;
        }

        if (underlying.IsEnum)
        {
            var enumValue = Enum.ToObject(underlying, value);
            var name = Enum.GetName(underlying, enumValue);
            return name is not null
                ? new DefaultValue(DefaultValueKind.EnumMember, name)
                : new DefaultValue(DefaultValueKind.Number, Convert.ToInt64(value, CultureInfo.InvariantCulture).ToString(CultureInfo.InvariantCulture));
        }

        return value switch
        {
            bool b => new DefaultValue(DefaultValueKind.Boolean, b ? "true" : "false"),
            string s => new DefaultValue(DefaultValueKind.String, s),
            char c => new DefaultValue(DefaultValueKind.Char, c.ToString()),
            float or double or decimal or byte or sbyte or short or ushort or int or uint or long or ulong =>
                new DefaultValue(DefaultValueKind.Number, FormatNumber(value)),
            _ => new DefaultValue(DefaultValueKind.DefaultStruct, ""),
        };
    }

    private static string FormatNumber(object value) => value switch
    {
        float f => f.ToString("R", CultureInfo.InvariantCulture),
        double d => d.ToString("R", CultureInfo.InvariantCulture),
        IFormattable formattable => formattable.ToString(null, CultureInfo.InvariantCulture),
        _ => value.ToString() ?? "0",
    };

    private ApiProperty ExtractProperty(PropertyInfo property)
    {
        var docId = XmlDocId.ForProperty(property);
        var getter = property.GetGetMethod();

        return new ApiProperty(
            Name: property.Name,
            Type: FromType(property.PropertyType, SafeNullability(() => nullability.Create(property))),
            HasGetter: getter is not null,
            HasSetter: property.GetSetMethod() is not null,
            IsStatic: (getter ?? property.GetSetMethod())?.IsStatic ?? false,
            IsAbstract: (getter ?? property.GetSetMethod())?.IsAbstract ?? false,
            IsIndexer: property.GetIndexParameters().Length > 0,
            DeclaringTypeFullName: NormalizedFullName(property.DeclaringType!),
            DocId: docId,
            ObsoleteMessage: ObsoleteMessage(property),
            RawXmlDoc: documentation.Find(docId),
            CapturedValue: CaptureStaticValue(() => property is { CanRead: true } && (getter?.IsStatic ?? false) && property.GetIndexParameters().Length == 0 ? property.GetValue(null) : null));
    }

    private ApiField ExtractField(FieldInfo field)
    {
        var docId = XmlDocId.ForField(field);

        return new ApiField(
            Name: field.Name,
            Type: FromType(field.FieldType, SafeNullability(() => nullability.Create(field))),
            IsConst: field.IsLiteral,
            IsStatic: field.IsStatic,
            IsReadOnly: field.IsInitOnly,
            DeclaringTypeFullName: NormalizedFullName(field.DeclaringType!),
            DocId: docId,
            ObsoleteMessage: ObsoleteMessage(field),
            RawXmlDoc: documentation.Find(docId),
            CapturedValue: field.IsLiteral
                ? CaptureStaticValue(field.GetRawConstantValue)
                : CaptureStaticValue(() => field.IsStatic ? field.GetValue(null) : null));
    }

    /// <summary>
    /// Best-effort capture of a static constant's runtime value, formatted invariantly.
    /// Used to reproduce real values (font names, page dimensions, color hex codes)
    /// in the generated stubs where doing so is trivial.
    /// </summary>
    private static string? CaptureStaticValue(Func<object?> read)
    {
        object? value;
        try
        {
            value = read();
        }
        catch
        {
            return null;
        }

        return value switch
        {
            null => null,
            string s => "string:" + s,
            bool b => "bool:" + (b ? "true" : "false"),
            float or double or byte or sbyte or short or ushort or int or uint or long or ulong =>
                "number:" + FormatNumber(value),
            _ => CaptureViaToString(value),
        };
    }

    private static string? CaptureViaToString(object value)
    {
        var type = value.GetType();

        // QuestPDF value types with a meaningful textual form (e.g. Color renders as
        // #RRGGBB / #AARRGGBB, PageSize has Width/Height). Capture a structured form
        // the emitter can decide to use.
        if (type.FullName == "QuestPDF.Infrastructure.Color")
            return "color:" + value;

        if (type.FullName == "QuestPDF.Helpers.PageSize")
        {
            var width = type.GetProperty("Width")?.GetValue(value) ?? type.GetField("Width")?.GetValue(value);
            var height = type.GetProperty("Height")?.GetValue(value) ?? type.GetField("Height")?.GetValue(value);
            if (width is float w && height is float h)
                return $"pagesize:{FormatNumber(w)}x{FormatNumber(h)}";
        }

        return null;
    }

    private IReadOnlyList<ApiEnumMember> ExtractEnumMembers(Type type)
    {
        return type.GetFields(BindingFlags.Public | BindingFlags.Static)
            .Where(f => f.IsLiteral)
            .Select(f => new ApiEnumMember(
                Name: f.Name,
                Value: Convert.ToInt64(f.GetRawConstantValue(), CultureInfo.InvariantCulture),
                DocId: XmlDocId.ForField(f),
                ObsoleteMessage: ObsoleteMessage(f),
                RawXmlDoc: documentation.Find(XmlDocId.ForField(f))))
            .OrderBy(m => m.Value)
            .ThenBy(m => m.Name, StringComparer.Ordinal)
            .ToList();
    }

    private ApiDelegateInfo? ExtractDelegateInfo(Type type)
    {
        var invoke = type.GetMethod("Invoke");
        if (invoke is null)
            return null;

        return new ApiDelegateInfo(
            Parameters: invoke.GetParameters().Select(ExtractParameter).ToList(),
            ReturnType: FromType(invoke.ReturnType, SafeNullability(() => nullability.Create(invoke.ReturnParameter))));
    }

    private IReadOnlyList<ApiTypeParameter> ExtractTypeParameters(Type[] genericArguments)
    {
        return genericArguments
            .Select(argument => new ApiTypeParameter(
                Name: argument.Name,
                Constraints: argument.GetGenericParameterConstraints()
                    .Where(c => c != typeof(ValueType))
                    .Select(c => FromType(c, null))
                    .OrderBy(r => r.Render(), StringComparer.Ordinal)
                    .ToList(),
                HasReferenceTypeConstraint: argument.GenericParameterAttributes.HasFlag(GenericParameterAttributes.ReferenceTypeConstraint),
                HasValueTypeConstraint: argument.GenericParameterAttributes.HasFlag(GenericParameterAttributes.NotNullableValueTypeConstraint),
                HasDefaultConstructorConstraint: argument.GenericParameterAttributes.HasFlag(GenericParameterAttributes.DefaultConstructorConstraint)))
            .ToList();
    }

    // ---- TypeRef construction ----

    private TypeRef FromType(Type type, NullabilityInfo? info)
    {
        if (type.IsByRef)
            return FromType(type.GetElementType()!, info);

        if (type.IsArray)
        {
            return new TypeRef
            {
                Kind = TypeRefKind.Array,
                ElementType = FromType(type.GetElementType()!, info?.ElementType),
                IsNullable = info?.ReadState == NullabilityState.Nullable,
            };
        }

        if (type.IsGenericParameter)
        {
            return new TypeRef
            {
                Kind = TypeRefKind.GenericParameter,
                GenericParameterName = type.Name,
                IsNullable = info?.ReadState == NullabilityState.Nullable,
            };
        }

        var nullableUnderlying = Nullable.GetUnderlyingType(type);
        if (nullableUnderlying is not null)
            return FromType(nullableUnderlying, null) with { IsNullable = true };

        if (type.IsGenericType)
        {
            var definition = type.GetGenericTypeDefinition();
            var arguments = type.GetGenericArguments();
            var argumentInfos = info?.GenericTypeArguments;

            return new TypeRef
            {
                Kind = TypeRefKind.Named,
                FullName = NormalizedFullName(definition),
                TypeArguments = arguments
                    .Select((a, i) => FromType(a, argumentInfos is not null && i < argumentInfos.Length ? argumentInfos[i] : null))
                    .ToList(),
                IsNullable = !type.IsValueType && info?.ReadState == NullabilityState.Nullable,
                IsApiAssemblyType = definition.Assembly == assembly,
                IsDelegateType = typeof(Delegate).IsAssignableFrom(definition),
            };
        }

        return new TypeRef
        {
            Kind = TypeRefKind.Named,
            FullName = NormalizedFullName(type),
            IsNullable = !type.IsValueType && info?.ReadState == NullabilityState.Nullable,
            IsApiAssemblyType = type.Assembly == assembly,
            IsDelegateType = typeof(Delegate).IsAssignableFrom(type),
        };
    }

    private static NullabilityInfo? SafeNullability(Func<NullabilityInfo?> create)
    {
        try
        {
            return create();
        }
        catch
        {
            return null;
        }
    }

    private static string StripArity(string name)
    {
        var tick = name.IndexOf('`');
        return tick < 0 ? name : name[..tick];
    }

    private static string NormalizedFullName(Type type)
    {
        var name = type.FullName ?? ((type.Namespace is null ? "" : type.Namespace + ".") + type.Name);
        var withoutArity = System.Text.RegularExpressions.Regex.Replace(name, "`+\\d+", "");
        return withoutArity.Replace('+', '.');
    }

    private static bool IsAccessor(MethodInfo method) =>
        method.IsSpecialName &&
        (method.Name.StartsWith("get_", StringComparison.Ordinal) ||
         method.Name.StartsWith("set_", StringComparison.Ordinal) ||
         method.Name.StartsWith("add_", StringComparison.Ordinal) ||
         method.Name.StartsWith("remove_", StringComparison.Ordinal));

    private static string? ObsoleteMessage(MemberInfo member)
    {
        var attribute = member.GetCustomAttribute<ObsoleteAttribute>(inherit: false);
        return attribute is null ? null : attribute.Message ?? "";
    }
}
