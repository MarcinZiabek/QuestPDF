using System.Text;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Core.Classification;

/// <summary>Concise C#-side signatures for report and snapshot lines.</summary>
public static class SignatureRenderer
{
    public static string Render(ApiMethod method)
    {
        var sb = new StringBuilder();
        sb.Append(Short(method.DeclaringTypeFullName)).Append('.');
        sb.Append(method.Kind == ApiMethodKind.Constructor ? "ctor" : method.Name);

        if (method.TypeParameters.Count > 0)
            sb.Append('<').Append(string.Join(", ", method.TypeParameters.Select(p => p.Name))).Append('>');

        sb.Append('(');
        var first = true;
        if (method.ExtensionReceiver is { } receiver)
        {
            sb.Append("this ").Append(receiver.Type.Render());
            first = false;
        }

        foreach (var parameter in method.Parameters)
        {
            if (!first) sb.Append(", ");
            first = false;
            sb.Append(parameter.Type.Render());
        }

        sb.Append(')');

        if (!method.ReturnType.Is("System.Void"))
            sb.Append(" -> ").Append(method.ReturnType.Render());

        return sb.ToString();
    }

    public static string Render(ApiProperty property) =>
        $"{Short(property.DeclaringTypeFullName)}.{property.Name}: {property.Type.Render()}";

    public static string Render(ApiField field) =>
        $"{Short(field.DeclaringTypeFullName)}.{field.Name}: {field.Type.Render()}";

    public static string Render(ApiType type) =>
        type.FullName + (type.TypeParameters.Count > 0 ? "<" + string.Join(", ", type.TypeParameters.Select(p => p.Name)) + ">" : "");

    private static string Short(string fullName)
    {
        const string prefix = "QuestPDF.";
        return fullName.StartsWith(prefix, StringComparison.Ordinal) ? fullName[prefix.Length..] : fullName;
    }
}
