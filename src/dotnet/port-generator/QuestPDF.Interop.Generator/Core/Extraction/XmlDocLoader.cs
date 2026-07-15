using System.Reflection;
using System.Xml.Linq;

namespace QuestPDF.Interop.Generator.Core.Extraction;

/// <summary>
/// Loads the compiler-generated XML documentation file that ships in the QuestPDF
/// NuGet package and indexes raw <c>&lt;member&gt;</c> elements by doc-comment ID.
/// </summary>
public sealed class XmlDocLoader
{
    private readonly Dictionary<string, string> membersById = new(StringComparer.Ordinal);

    public int Count => membersById.Count;

    public static XmlDocLoader ForAssembly(Assembly assembly)
    {
        var loader = new XmlDocLoader();
        var path = FindDocumentationFile(assembly);
        if (path is not null)
            loader.Load(path);
        return loader;
    }

    public static string? FindDocumentationFile(Assembly assembly)
    {
        // The build copies QuestPDF.xml next to the assembly (CopyDocumentationFilesFromPackages).
        var nextToAssembly = Path.ChangeExtension(assembly.Location, ".xml");
        if (File.Exists(nextToAssembly))
            return nextToAssembly;

        // Fallback: probe the NuGet cache.
        var version = assembly.GetName().Version;
        if (version is null)
            return null;

        var home = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        var packageRoot = Path.Combine(home, ".nuget", "packages", assembly.GetName().Name!.ToLowerInvariant());
        if (!Directory.Exists(packageRoot))
            return null;

        return Directory
            .EnumerateFiles(packageRoot, assembly.GetName().Name + ".xml", SearchOption.AllDirectories)
            .OrderBy(p => p, StringComparer.Ordinal)
            .FirstOrDefault();
    }

    public void Load(string path)
    {
        var document = XDocument.Load(path);
        var members = document.Root?.Element("members")?.Elements("member") ?? [];

        foreach (var member in members)
        {
            var id = member.Attribute("name")?.Value;
            if (id is not null)
                membersById[id] = member.ToString();
        }
    }

    public string? Find(string docId) => membersById.GetValueOrDefault(docId);
}
