using QuestPDF.Interop.Generator.Core.Model;

namespace QuestPDF.Interop.Generator.Core.Classification;

/// <summary>
/// Lookup structure over the extracted model that classification rules consult:
/// type resolution, inheritance, nesting, and the DSL-receiver predicate that
/// decides which delegate arguments become lambdas with receiver.
/// </summary>
public sealed class ApiIndex
{
    private readonly Dictionary<string, ApiType> typesByFullName = new(StringComparer.Ordinal);
    private readonly Dictionary<string, string> unsupportedTypes = new(StringComparer.Ordinal);
    private readonly Dictionary<string, List<ApiType>> directSubclasses = new(StringComparer.Ordinal);

    public ApiAssembly Assembly { get; }

    /// <summary>
    /// Arity-aware lookup key: the normalized full name drops the CLR backtick
    /// suffix, so IDynamicComponent and IDynamicComponent&lt;TState&gt; would
    /// otherwise collide.
    /// </summary>
    public static string Key(ApiType type) =>
        type.TypeParameters.Count == 0 ? type.FullName : type.FullName + "`" + type.TypeParameters.Count;

    public static string Key(TypeRef type) =>
        type.TypeArguments.Count == 0 ? type.FullName : type.FullName + "`" + type.TypeArguments.Count;

    public ApiIndex(ApiAssembly assembly)
    {
        Assembly = assembly;

        foreach (var type in assembly.Types)
            typesByFullName[Key(type)] = type;

        foreach (var type in assembly.Types)
        {
            if (type.BaseType is { Kind: TypeRefKind.Named, IsApiAssemblyType: true } baseType)
            {
                if (!directSubclasses.TryGetValue(baseType.FullName, out var list))
                    directSubclasses[baseType.FullName] = list = [];
                list.Add(type);
            }
        }
    }

    public ApiType? FindType(string fullName) => typesByFullName.GetValueOrDefault(fullName);

    public ApiType? FindType(TypeRef type) => typesByFullName.GetValueOrDefault(Key(type));

    public void MarkTypeUnsupported(ApiType type, string reason) => unsupportedTypes[Key(type)] = reason;

    public bool IsTypeUnsupported(TypeRef type, out string reason)
    {
        if (unsupportedTypes.TryGetValue(Key(type), out var found))
        {
            reason = found;
            return true;
        }

        reason = "";
        return false;
    }

    public bool IsTypeUnsupported(ApiType type, out string reason)
    {
        if (unsupportedTypes.TryGetValue(Key(type), out var found))
        {
            reason = found;
            return true;
        }

        reason = "";
        return false;
    }

    /// <summary>All types transitively derived from the given class, ordered by name.</summary>
    public IReadOnlyList<ApiType> TransitiveSubclasses(string fullName)
    {
        var result = new List<ApiType>();
        void Visit(string name)
        {
            if (!directSubclasses.TryGetValue(name, out var children))
                return;
            foreach (var child in children.OrderBy(c => c.FullName, StringComparer.Ordinal))
            {
                result.Add(child);
                Visit(child.FullName);
            }
        }

        Visit(fullName);
        return result;
    }

    /// <summary>Base-class chain (nearest first) within the API assembly.</summary>
    public IEnumerable<ApiType> BaseChain(ApiType type)
    {
        var current = type.BaseType;
        while (current is { Kind: TypeRefKind.Named, IsApiAssemblyType: true })
        {
            var resolved = FindType(current.FullName);
            if (resolved is null)
                yield break;
            yield return resolved;
            current = resolved.BaseType;
        }
    }

    /// <summary>All API-assembly interfaces the type implements/extends, transitively.</summary>
    public IReadOnlyList<ApiType> AllApiInterfaces(ApiType type)
    {
        var seen = new HashSet<string>(StringComparer.Ordinal);
        var result = new List<ApiType>();

        void Collect(ApiType current)
        {
            foreach (var iface in current.Interfaces.Where(i => i.IsApiAssemblyType))
            {
                var resolved = FindType(iface.FullName);
                if (resolved is null || !seen.Add(resolved.FullName))
                    continue;
                result.Add(resolved);
                Collect(resolved);
            }

            foreach (var baseType in BaseChain(current))
                Collect(baseType);
        }

        Collect(type);
        return result;
    }

    /// <summary>
    /// DSL scope types: delegate arguments of these types become lambda receivers.
    /// Covers IContainer-family interfaces, all fluent descriptors, and TextStyle.
    /// Data payloads (Size, ImageSize, contexts) stay ordinary lambda parameters.
    /// </summary>
    public bool IsDslReceiverType(TypeRef type)
    {
        if (type.Kind != TypeRefKind.Named || !type.IsApiAssemblyType || type.TypeArguments.Count > 0)
            return false;

        if (type.FullName is "QuestPDF.Infrastructure.IContainer"
            or "QuestPDF.Infrastructure.IDocumentContainer"
            or "QuestPDF.Infrastructure.TextStyle")
            return true;

        if (type.FullName.EndsWith("Descriptor", StringComparison.Ordinal))
            return true;

        // Interfaces extending IContainer (e.g. ITableCellContainer).
        var resolved = FindType(type.FullName);
        return resolved is { Kind: ApiTypeKind.Interface } iface &&
               iface.Interfaces.Any(i => i.Is("QuestPDF.Infrastructure.IContainer"));
    }

    /// <summary>
    /// Client-side name for a possibly-nested type: "QuestPDF.Helpers.Colors.Red"
    /// → "Colors.Red" (nested types stay nested in generated clients).
    /// </summary>
    public string NestedName(string fullName)
    {
        var type = FindType(fullName);
        if (type is null)
            return fullName[(fullName.LastIndexOf('.') + 1)..];

        var segments = new List<string> { type.Name };
        var current = type;
        while (current.DeclaringTypeFullName is not null)
        {
            current = FindType(current.DeclaringTypeFullName);
            if (current is null)
                break;
            segments.Insert(0, current.Name);
        }

        return string.Join(".", segments);
    }
}
