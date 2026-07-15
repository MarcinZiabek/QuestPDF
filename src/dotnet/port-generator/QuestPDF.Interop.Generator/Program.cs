using QuestPDF.Interop.Generator.Backends;
using QuestPDF.Interop.Generator.Core;
using QuestPDF.Interop.Generator.Core.Classification;
using QuestPDF.Interop.Generator.Core.Model;
using QuestPDF.Interop.Generator.Core.Reporting;

namespace QuestPDF.Interop.Generator;

internal static class Program
{
    /// <summary>The shared NativeAOT project every language client binds against.</summary>
    private static readonly string NativeProjectRoot = Path.Combine("src", "dotnet", "interop");

    /// <summary>Directories (relative to the repository root) that identify it during discovery.</summary>
    private static readonly string[] RepoRootMarkers =
    [
        Path.Combine("src", "dotnet", "port-generator"),
        Path.Combine("src", "dotnet", "interop"),
    ];

    private static int Main(string[] args)
    {
        var mode = args.Length > 0 && !args[0].StartsWith("--", StringComparison.Ordinal) ? args[0] : "generate";

        switch (mode)
        {
            case "dump":
            {
                var model = GeneratorPipeline.Extract();
                var output = args.Length > 1 ? args[1] : "api-dump.txt";
                File.WriteAllText(output, ModelDump.Render(model));
                Console.WriteLine($"Extracted {model.Types.Count} types from {model.Name} {model.Version}.");
                Console.WriteLine($"Dump written to {Path.GetFullPath(output)}");
                return 0;
            }

            case "generate":
            {
                var positional = new List<string>();
                for (var i = args.Length > 0 && args[0] == "generate" ? 1 : 0; i < args.Length; i++)
                {
                    if (args[i] == "--language")
                        i++; // skip the flag's value
                    else if (!args[i].StartsWith("--", StringComparison.Ordinal))
                        positional.Add(args[i]);
                }

                var repoRoot = positional.Count > 0 ? Path.GetFullPath(positional[0]) : LocateRepoRoot();
                if (repoRoot is null)
                {
                    Console.Error.WriteLine($"Could not locate the repository root (expected '{RepoRootMarkers[0]}' and '{RepoRootMarkers[1]}' directories). Pass it explicitly: generate <repo-root>.");
                    return 2;
                }

                if (!RepoRootMarkers.All(marker => Directory.Exists(Path.Combine(repoRoot, marker))))
                {
                    Console.Error.WriteLine($"'{repoRoot}' does not look like the repository root (expected '{RepoRootMarkers[0]}' and '{RepoRootMarkers[1]}' directories).");
                    return 2;
                }

                var backends = SelectBackends(args);
                if (backends is null)
                    return 2;

                return Generate(repoRoot, backends);
            }

            default:
                Console.Error.WriteLine($"Unknown mode '{mode}'. Expected 'generate [repo-root] [--language id,...]' or 'dump [output-file]'.");
                return 2;
        }
    }

    /// <summary>Backends selected by --language (comma-separated ids); all registered backends by default.</summary>
    private static IReadOnlyList<ILanguageBackend>? SelectBackends(string[] args)
    {
        var ids = new List<string>();
        for (var i = 0; i < args.Length; i++)
        {
            if (args[i] == "--language" && i + 1 < args.Length)
                ids.AddRange(args[i + 1].Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries));
            else if (args[i].StartsWith("--language=", StringComparison.Ordinal))
                ids.AddRange(args[i]["--language=".Length..].Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries));
        }

        if (ids.Count == 0)
            return LanguageBackends.All;

        var backends = new List<ILanguageBackend>();
        foreach (var id in ids)
        {
            var backend = LanguageBackends.Find(id);
            if (backend is null)
            {
                Console.Error.WriteLine($"Unknown language '{id}'. Registered backends: {string.Join(", ", LanguageBackends.All.Select(b => b.Id))}.");
                return null;
            }

            backends.Add(backend);
        }

        return backends;
    }

    private static int Generate(string repoRoot, IReadOnlyList<ILanguageBackend> backends)
    {
        var assembly = GeneratorPipeline.Extract();
        Console.WriteLine($"QuestPDF {assembly.Version}: {assembly.Types.Count} public types extracted.");

        var interopModels = new List<InteropModel>();
        foreach (var backend in backends)
        {
            var overrides = ManualOverrides.Load(Path.Combine(repoRoot, backend.ManualOverridesFile));
            var output = backend.Generate(assembly, overrides);
            interopModels.Add(output.Interop);

            // Generated directories are owned by their backend: wipe and recreate.
            WriteTree(Path.Combine(repoRoot, ToPlatformPath(backend.GeneratedRoot)), output.Files);
            File.WriteAllText(
                Path.Combine(repoRoot, ToPlatformPath(backend.CoverageReportFile)),
                CoverageReport.Render(backend.CoverageLabels, output.Report, output.Interop.SourceAssemblyName, output.Interop.SourceAssemblyVersion));

            var generated = output.Report.DistinctBy(r => r.DocId).Count(r => r.Classification == ApiClassification.Generated);
            var unsupported = output.Report.GroupBy(r => r.DocId)
                .Count(g => g.Any(r => r.Classification == ApiClassification.Unsupported));

            Console.WriteLine($"[{backend.Id}] Emitted {output.Files.Count} files to {backend.GeneratedRoot}.");
            Console.WriteLine($"[{backend.Id}] Classification: {generated} generated, {unsupported} unsupported. Report: {backend.CoverageReportFile}");
        }

        var interop = GeneratorPipeline.MergeInterop(interopModels);
        var nativeFiles = GeneratorPipeline.EmitNative(interop);
        WriteTree(Path.Combine(repoRoot, NativeProjectRoot), nativeFiles, wipeSubdirectory: "Exports");
        Console.WriteLine($"[native] Emitted {nativeFiles.Count} C# export files ({interop.Exports.Count} exports, {interop.CallbackShapes.Count} callback shapes, {interop.Proxies.Count} proxies) to {NativeProjectRoot}/Exports.");

        return 0;
    }

    private static string ToPlatformPath(string repoRelative) =>
        repoRelative.Replace('/', Path.DirectorySeparatorChar);

    private static void WriteTree(string root, IReadOnlyDictionary<string, string> files, string? wipeSubdirectory = null)
    {
        var wipeTarget = wipeSubdirectory is null ? root : Path.Combine(root, wipeSubdirectory);
        if (Directory.Exists(wipeTarget))
            Directory.Delete(wipeTarget, recursive: true);
        Directory.CreateDirectory(wipeTarget);

        foreach (var (relativePath, content) in files)
        {
            var fullPath = Path.Combine(root, ToPlatformPath(relativePath));
            Directory.CreateDirectory(Path.GetDirectoryName(fullPath)!);
            File.WriteAllText(fullPath, content);
        }
    }

    /// <summary>Walks up from the current directory to the repository root.</summary>
    private static string? LocateRepoRoot()
    {
        var current = new DirectoryInfo(Directory.GetCurrentDirectory());
        while (current is not null)
        {
            if (RepoRootMarkers.All(marker => Directory.Exists(Path.Combine(current.FullName, marker))))
                return current.FullName;

            current = current.Parent;
        }

        return null;
    }
}
