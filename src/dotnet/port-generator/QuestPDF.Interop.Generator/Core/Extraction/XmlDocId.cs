using System.Reflection;
using System.Text;
using System.Text.RegularExpressions;

namespace QuestPDF.Interop.Generator.Core.Extraction;

/// <summary>
/// Generates XML documentation comment IDs (the <c>name</c> attribute of
/// <c>&lt;member&gt;</c> entries) for reflection members, matching the format the
/// C# compiler writes into QuestPDF.xml. These IDs are the join key between the
/// reflected API, the documentation file, and manual-overrides.txt.
/// </summary>
public static partial class XmlDocId
{
    public static string ForType(Type type) => "T:" + DeclarationName(type);

    public static string ForMethod(MethodBase method)
    {
        var sb = new StringBuilder("M:");
        sb.Append(DeclarationName(method.DeclaringType!));
        sb.Append('.');

        if (method is ConstructorInfo)
            sb.Append(method.IsStatic ? "#cctor" : "#ctor");
        else
            sb.Append(method.Name.Replace('.', '#'));

        if (method.IsGenericMethodDefinition || method.IsGenericMethod)
            sb.Append("``").Append(method.GetGenericArguments().Length);

        AppendParameters(sb, method.GetParameters());

        if (method is MethodInfo { IsSpecialName: true } info &&
            method.Name is "op_Implicit" or "op_Explicit")
        {
            sb.Append('~').Append(ParameterTypeName(info.ReturnType));
        }

        return sb.ToString();
    }

    public static string ForProperty(PropertyInfo property)
    {
        var sb = new StringBuilder("P:");
        sb.Append(DeclarationName(property.DeclaringType!));
        sb.Append('.');
        sb.Append(property.Name);
        AppendParameters(sb, property.GetIndexParameters());
        return sb.ToString();
    }

    public static string ForField(FieldInfo field) =>
        "F:" + DeclarationName(field.DeclaringType!) + "." + field.Name;

    private static void AppendParameters(StringBuilder sb, ParameterInfo[] parameters)
    {
        if (parameters.Length == 0)
            return;

        sb.Append('(');
        for (var i = 0; i < parameters.Length; i++)
        {
            if (i > 0) sb.Append(',');
            sb.Append(ParameterTypeName(parameters[i].ParameterType));
        }
        sb.Append(')');
    }

    /// <summary>Name of a type at its declaration site, e.g. <c>Ns.Outer.Inner`1</c>.</summary>
    private static string DeclarationName(Type type) =>
        (type.FullName ?? type.Name).Replace('+', '.');

    /// <summary>Name of a type in a parameter list position.</summary>
    private static string ParameterTypeName(Type type)
    {
        if (type.IsByRef)
            return ParameterTypeName(type.GetElementType()!) + "@";

        if (type.IsArray)
        {
            var rank = type.GetArrayRank();
            var suffix = rank == 1
                ? "[]"
                : "[" + string.Join(",", Enumerable.Repeat("0:", rank)) + "]";
            return ParameterTypeName(type.GetElementType()!) + suffix;
        }

        if (type.IsPointer)
            return ParameterTypeName(type.GetElementType()!) + "*";

        if (type.IsGenericParameter)
            return (type.DeclaringMethod is not null ? "``" : "`") + type.GenericParameterPosition;

        if (type.IsGenericType && !type.IsGenericTypeDefinition)
        {
            var definitionName = DeclarationName(type.GetGenericTypeDefinition());
            var cleanName = AritySuffix().Replace(definitionName, "");
            var arguments = string.Join(",", type.GetGenericArguments().Select(ParameterTypeName));
            return cleanName + "{" + arguments + "}";
        }

        return DeclarationName(type);
    }

    [GeneratedRegex("`+\\d+")]
    private static partial Regex AritySuffix();
}
