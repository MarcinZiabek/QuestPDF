using System.Text;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Core.Bridge;

/// <summary>
/// Assigns entry-point names to native exports independently of any language
/// backend, so every backend binds the same name for the same member and the
/// merged native library carries one export per member.
///
/// A name is <c>QP_{group}_{tag}_{ordinal}</c>: the group is the client-side
/// type the member lands on (extension receiver or declaring type), the tag
/// identifies the member within the group, and the ordinal is the member's
/// position within its raw overload family — computed from the extracted
/// assembly alone, before any backend's classification, so a backend keeping a
/// different subset of an overload family still binds the same names.
/// </summary>
public sealed class ExportNameAllocator
{
    private readonly ApiIndex index;
    private readonly Dictionary<string, List<string>> families = new(StringComparer.Ordinal);

    private ExportNameAllocator(ApiIndex index)
    {
        this.index = index;
        BuildFamilies();
    }

    public static ExportNameAllocator Create(ApiIndex index) => new(index);

    /// <summary>Assigns the export's group and entry-point name from the member it invokes.</summary>
    public void AssignName(NativeExport export, string sourceDocId)
    {
        var (group, tag) = Identity(export.Invocation);
        var key = group + "." + tag;

        if (!families.TryGetValue(key, out var family))
            throw new InvalidOperationException(
                $"Export naming: no overload family '{key}' exists for {sourceDocId}; " +
                "the family enumeration and the invocation-derived identity disagree.");

        var ordinal = family.IndexOf(sourceDocId);
        if (ordinal < 0)
            throw new InvalidOperationException(
                $"Export naming: {sourceDocId} is not part of the overload family '{key}'.");

        export.AssignName(group, $"QP_{group}_{tag}_{ordinal}");
    }

    // ---- identity: where a member lands and what it is called there ----

    private (string Group, string Tag) Identity(CSharpInvocation invocation)
    {
        var group = GroupName(invocation.ReceiverType ?? invocation.DeclaringType);

        var tag = invocation.Kind switch
        {
            InvocationKind.Constructor => "ctor",
            InvocationKind.PropertyGet or InvocationKind.StaticPropertyGet => "get" + invocation.MemberName,
            InvocationKind.PropertySet or InvocationKind.StaticPropertySet => "set" + invocation.MemberName,
            InvocationKind.ConversionToSelf => "from",
            InvocationKind.ConversionFromSelf => ConversionTag(invocation.MemberName),
            _ => CamelCase(invocation.MemberName),
        };

        return (group, tag);
    }

    private string GroupName(string typeFullName) =>
        index.NestedName(typeFullName).Replace('.', '_');

    /// <summary>Tag of a conversion away from the declaring type; the argument is the C# target type name.</summary>
    private static string ConversionTag(string csharpTypeName)
    {
        var withoutArguments = csharpTypeName.Split('<')[0];
        return "to" + withoutArguments[(withoutArguments.LastIndexOf('.') + 1)..];
    }

    // ---- family enumeration over the raw assembly ----

    private void BuildFamilies()
    {
        foreach (var type in index.Assembly.Types.OrderBy(ApiIndex.Key, StringComparer.Ordinal))
        {
            var group = GroupName(type.FullName);

            foreach (var ctor in type.Constructors)
                Add(group, "ctor", ctor.DocId);

            foreach (var property in type.Properties)
            {
                Add(group, "get" + property.Name, property.DocId);
                Add(group, "set" + property.Name, property.DocId);
            }

            foreach (var field in type.Fields)
            {
                Add(group, "get" + field.Name, field.DocId);
                Add(group, "set" + field.Name, field.DocId);
            }

            foreach (var method in type.Methods)
            {
                foreach (var (methodGroup, tag) in MethodPlacements(type, method))
                    Add(methodGroup, tag, method.DocId);
            }
        }

        foreach (var family in families.Values)
            family.Sort(StringComparer.Ordinal);
    }

    /// <summary>
    /// Every (group, tag) a method can produce an export under: its receiver
    /// type for extension methods (the constraint type and every transitive
    /// subclass for self-generic extensions), its declaring type otherwise.
    /// </summary>
    private IEnumerable<(string Group, string Tag)> MethodPlacements(ApiType declaring, ApiMethod method)
    {
        if (method.Name is "op_Implicit" or "op_Explicit" && method.Parameters.Count == 1)
        {
            var group = GroupName(declaring.FullName);
            var sourceIsSelf = ApiIndex.Key(method.Parameters[0].Type) == ApiIndex.Key(declaring);
            yield return sourceIsSelf
                ? (group, ConversionTag(BridgePlanner.CSharpName(method.ReturnType)))
                : (group, "from");
            yield break;
        }

        var tag = CamelCase(method.Name);

        if (method.IsExtension && method.ExtensionReceiver is { Type: { Kind: TypeRefKind.Named, IsApiAssemblyType: true } receiver })
        {
            yield return (GroupName(receiver.FullName), tag);
            yield break;
        }

        if (method.IsExtension && method.ExtensionReceiver is { Type.Kind: TypeRefKind.GenericParameter } genericReceiver)
        {
            var receiverName = genericReceiver.Type.GenericParameterName;
            var constraint = method.TypeParameters.FirstOrDefault(p => p.Name == receiverName)
                ?.Constraints.FirstOrDefault(c => c is { Kind: TypeRefKind.Named, IsApiAssemblyType: true });
            if (constraint is null)
                yield break; // no backend can monomorphize this receiver

            yield return (GroupName(constraint.FullName), tag);
            foreach (var subclass in index.TransitiveSubclasses(constraint.FullName))
                yield return (GroupName(subclass.FullName), tag);
            yield break;
        }

        yield return (GroupName(declaring.FullName), tag);
    }

    private void Add(string group, string tag, string docId)
    {
        var key = group + "." + tag;
        if (!families.TryGetValue(key, out var family))
            families[key] = family = [];
        if (!family.Contains(docId))
            family.Add(docId);
    }

    /// <summary>Member name → lowerCamelCase tag, mirroring client naming conventions (PDFA_Conformance → pdfaConformance).</summary>
    private static string CamelCase(string name)
    {
        var segments = name.Split('_', StringSplitOptions.RemoveEmptyEntries);
        if (segments.Length == 0)
            return name;

        var sb = new StringBuilder();
        sb.Append(CamelCaseSegment(segments[0]));
        foreach (var segment in segments.Skip(1))
            sb.Append(char.ToUpperInvariant(segment[0])).Append(segment[1..]);

        return sb.ToString();
    }

    private static string CamelCaseSegment(string name)
    {
        if (name.Length == 0 || char.IsLower(name[0]))
            return name;

        var upperRun = 0;
        while (upperRun < name.Length && char.IsUpper(name[upperRun]))
            upperRun++;

        if (upperRun == name.Length)
            return name.ToLowerInvariant();

        if (!char.IsLower(name[upperRun]))
            return name[..upperRun].ToLowerInvariant() + name[upperRun..];

        if (upperRun > 1)
            return name[..(upperRun - 1)].ToLowerInvariant() + name[(upperRun - 1)..];

        return char.ToLowerInvariant(name[0]) + name[1..];
    }
}

/// <summary>
/// Canonical content rendering of interop records, used to verify that two
/// backends arriving at the same entry-point name planned the very same export
/// (parameter names excluded — they do not affect the ABI).
/// </summary>
public static class InteropSignature
{
    public static string Of(NativeExport export)
    {
        var sb = new StringBuilder();
        var invocation = export.Invocation;
        sb.Append(invocation.Kind).Append('|')
          .Append(invocation.DeclaringType).Append('|')
          .Append(invocation.MemberName).Append('|')
          .Append(string.Join(",", invocation.GenericArguments)).Append('|')
          .Append(invocation.ReceiverType ?? "");

        sb.Append("|recv=");
        if (export.Receiver is not null)
            Render(export.Receiver, sb);

        foreach (var parameter in export.Parameters)
        {
            sb.Append("|p=");
            Render(parameter.Marshal, sb);
        }

        sb.Append("|ret=");
        Render(export.Return, sb);
        return sb.ToString();
    }

    public static string Of(ProxyPlan proxy)
    {
        var sb = new StringBuilder();
        sb.Append(proxy.ProxyExportEntryPoint).Append('|')
          .Append(proxy.CSharpInterface).Append('|')
          .Append(proxy.CSharpClassName).Append('|')
          .Append(proxy.CSharpMethodName).Append('|')
          .Append(string.Join(",", proxy.CSharpParameterTypes)).Append('|')
          .Append(proxy.CSharpReturnType ?? "").Append('|');
        Render(proxy.Method, sb);
        return sb.ToString();
    }

    private static void Render(CallbackAdapter adapter, StringBuilder sb)
    {
        sb.Append(adapter.Shape.Name).Append('(');
        for (var i = 0; i < adapter.ParameterWraps.Count; i++)
        {
            if (i > 0) sb.Append(',');
            Render(adapter.ParameterWraps[i], sb);
        }
        sb.Append(")->");
        Render(adapter.ReturnWrap, sb);
        if (adapter.FirstParameterIsDslScope)
            sb.Append("|scope");
    }

    private static void Render(BridgeMarshal marshal, StringBuilder sb)
    {
        switch (marshal)
        {
            case BridgeMarshal.Nothing:
                sb.Append("void");
                break;
            case BridgeMarshal.Scalar s:
                sb.Append("scalar:").Append(s.Kind);
                break;
            case BridgeMarshal.NullableScalar n:
                sb.Append("scalar?:").Append(n.Kind);
                break;
            case BridgeMarshal.EnumValue e:
                sb.Append("enum:").Append(e.CSharpEnum).Append(e.Nullable ? "?" : "");
                break;
            case BridgeMarshal.Text t:
                sb.Append("text").Append(t.Nullable ? "?" : "");
                break;
            case BridgeMarshal.DateTimeText d:
                sb.Append("datetime:").Append(d.CSharpType).Append(d.Nullable ? "?" : "");
                break;
            case BridgeMarshal.Blob b:
                sb.Append("blob").Append(b.Nullable ? "?" : "");
                break;
            case BridgeMarshal.Handle h:
                sb.Append("handle:").Append(h.CSharpType).Append(h.Nullable ? "?" : "");
                break;
            case BridgeMarshal.HandleSequence hs:
                sb.Append("seq[").Append(hs.Container).Append("]<");
                Render(hs.Element, sb);
                sb.Append('>');
                break;
            case BridgeMarshal.TextSequence ts:
                sb.Append("textseq[").Append(ts.Container).Append(']');
                break;
            case BridgeMarshal.ScalarArray sa:
                sb.Append("scalararr[").Append(sa.Container).Append("]:").Append(sa.Kind);
                break;
            case BridgeMarshal.CallbackValue c:
                sb.Append("cb:").Append(c.CSharpDelegateType).Append(c.Nullable ? "?" : "").Append(':');
                Render(c.Adapter, sb);
                break;
            case BridgeMarshal.UserImplValue u:
                sb.Append("user:").Append(u.Proxy.CSharpInterface).Append(u.Nullable ? "?" : "");
                break;
            default:
                throw new InvalidOperationException($"Unknown marshal {marshal.GetType().Name}");
        }
    }
}
