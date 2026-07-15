using System.Text;

namespace QuestPDF.Interop.Generator.Core.Model;

/// <summary>
/// Renders the extracted model as a stable, diff-friendly text document.
/// Used for the committed snapshot test and as an API-evolution diff between
/// QuestPDF versions. Documentation text is deliberately excluded (only a
/// presence marker) so the snapshot tracks structure, not prose.
/// </summary>
public static class ModelDump
{
    public static string Render(ApiAssembly assembly, IReadOnlyDictionary<string, string>? classifications = null)
    {
        var sb = new StringBuilder();
        sb.Append("assembly ").Append(assembly.Name).Append(' ').AppendLine(assembly.Version);
        sb.AppendLine();

        foreach (var type in assembly.Types.OrderBy(t => t.FullName + "`" + t.TypeParameters.Count, StringComparer.Ordinal))
            RenderType(sb, type, classifications);

        return sb.ToString();
    }

    private static void AppendClassification(StringBuilder sb, IReadOnlyDictionary<string, string>? classifications, string docId)
    {
        if (classifications is not null && classifications.TryGetValue(docId, out var classification))
            sb.Append("  => ").Append(classification);
    }

    private static void RenderType(StringBuilder sb, ApiType type, IReadOnlyDictionary<string, string>? classifications)
    {
        sb.Append(type.Kind switch
        {
            ApiTypeKind.Interface => "interface",
            ApiTypeKind.StaticClass => "static-class",
            ApiTypeKind.Struct => "struct",
            ApiTypeKind.Enum => "enum",
            ApiTypeKind.Delegate => "delegate",
            _ => type.IsAbstract ? "abstract-class" : "class",
        });

        sb.Append(' ').Append(type.FullName);
        if (type.TypeParameters.Count > 0)
            sb.Append('<').Append(string.Join(", ", type.TypeParameters.Select(RenderTypeParameter))).Append('>');
        if (type.BaseType is not null)
            sb.Append(" : ").Append(type.BaseType.Render());
        if (type.Interfaces.Count > 0)
            sb.Append(" implements ").Append(string.Join(", ", type.Interfaces.Select(i => i.Render())));
        if (type.HasInternalConstructorOnly)
            sb.Append(" [internal-ctor]");
        AppendMarkers(sb, type.ObsoleteMessage, type.RawXmlDoc);
        AppendClassification(sb, classifications, type.DocId);
        sb.AppendLine();

        if (type.DelegateInfo is not null)
        {
            sb.Append("  invoke (")
              .Append(string.Join(", ", type.DelegateInfo.Parameters.Select(RenderParameter)))
              .Append(") -> ")
              .AppendLine(type.DelegateInfo.ReturnType.Render());
        }

        foreach (var member in type.EnumMembers)
        {
            sb.Append("  value ").Append(member.Name).Append(" = ").Append(member.Value);
            AppendMarkers(sb, member.ObsoleteMessage, member.RawXmlDoc);
            sb.AppendLine();
        }

        foreach (var field in type.Fields)
        {
            sb.Append("  ").Append(field.IsConst ? "const " : field.IsReadOnly ? "readonly " : "field ")
              .Append(field.Name).Append(": ").Append(field.Type.Render());
            if (field.CapturedValue is not null)
                sb.Append(" = ").Append(Truncate(field.CapturedValue));
            AppendMarkers(sb, field.ObsoleteMessage, field.RawXmlDoc);
            AppendClassification(sb, classifications, field.DocId);
            sb.AppendLine();
        }

        foreach (var property in type.Properties)
        {
            sb.Append("  ").Append(property.IsStatic ? "static-property " : "property ")
              .Append(property.Name).Append(": ").Append(property.Type.Render())
              .Append(" {").Append(property.HasGetter ? " get;" : "").Append(property.HasSetter ? " set;" : "").Append(" }");
            if (property.IsIndexer)
                sb.Append(" [indexer]");
            if (property.CapturedValue is not null)
                sb.Append(" = ").Append(Truncate(property.CapturedValue));
            AppendMarkers(sb, property.ObsoleteMessage, property.RawXmlDoc);
            AppendClassification(sb, classifications, property.DocId);
            sb.AppendLine();
        }

        foreach (var ctor in type.Constructors)
        {
            sb.Append("  ctor (").Append(string.Join(", ", ctor.Parameters.Select(RenderParameter))).Append(')');
            AppendMarkers(sb, ctor.ObsoleteMessage, ctor.RawXmlDoc);
            AppendClassification(sb, classifications, ctor.DocId);
            sb.AppendLine();
        }

        foreach (var method in type.Methods)
        {
            sb.Append("  ");
            sb.Append(method.Kind == ApiMethodKind.Operator ? "operator " : method.IsExtension ? "extension " : method.IsStatic ? "static-method " : "method ");
            sb.Append(method.Name);

            if (method.TypeParameters.Count > 0)
                sb.Append('<').Append(string.Join(", ", method.TypeParameters.Select(RenderTypeParameter))).Append('>');

            sb.Append('(');
            if (method.ExtensionReceiver is not null)
                sb.Append("this ").Append(RenderParameter(method.ExtensionReceiver)).Append(method.Parameters.Count > 0 ? ", " : "");
            sb.Append(string.Join(", ", method.Parameters.Select(RenderParameter)));
            sb.Append(") -> ").Append(method.ReturnType.Render());

            AppendMarkers(sb, method.ObsoleteMessage, method.RawXmlDoc);
            AppendClassification(sb, classifications, method.DocId);
            sb.AppendLine();
        }

        sb.AppendLine();
    }

    private static string RenderTypeParameter(ApiTypeParameter parameter)
    {
        if (parameter.Constraints.Count == 0 &&
            !parameter.HasReferenceTypeConstraint &&
            !parameter.HasValueTypeConstraint &&
            !parameter.HasDefaultConstructorConstraint)
            return parameter.Name;

        var constraints = parameter.Constraints.Select(c => c.Render()).ToList();
        if (parameter.HasReferenceTypeConstraint) constraints.Insert(0, "class");
        if (parameter.HasValueTypeConstraint) constraints.Insert(0, "struct");
        if (parameter.HasDefaultConstructorConstraint) constraints.Add("new()");
        return parameter.Name + " : " + string.Join(" & ", constraints);
    }

    private static string RenderParameter(ApiParameter parameter)
    {
        var sb = new StringBuilder();
        if (parameter.IsParams) sb.Append("params ");
        if (parameter.IsByRef) sb.Append("byref ");
        sb.Append(parameter.Name).Append(": ").Append(parameter.Type.Render());

        if (parameter.Default is { } def)
        {
            sb.Append(" = ").Append(def.Kind switch
            {
                DefaultValueKind.Null => "null",
                DefaultValueKind.DefaultStruct => "default",
                DefaultValueKind.String => "\"" + def.Text + "\"",
                DefaultValueKind.Char => "'" + def.Text + "'",
                _ => def.Text,
            });
        }

        if (parameter.IsCallerInfo) sb.Append(" [caller-info]");
        return sb.ToString();
    }

    private static void AppendMarkers(StringBuilder sb, string? obsoleteMessage, string? rawXmlDoc)
    {
        if (obsoleteMessage is not null)
            sb.Append(" [obsolete]");
        if (rawXmlDoc is not null)
            sb.Append(" [doc]");
    }

    private static string Truncate(string value) =>
        value.Length <= 80 ? value : value[..77] + "...";
}
