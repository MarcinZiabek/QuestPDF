using QuestPDF.Interop.Generator.Backends.Kotlin;
using QuestPDF.Interop.Generator.Core.Bridge;

namespace QuestPDF.Interop.Generator.Backends.Kotlin.Emission;

/// <summary>
/// Computes the import list for one generated file and resolves type
/// references to simple or fully qualified names. Types whose simple name
/// collides with a kotlin.* default import (QuestPDF's Unit enum) are never
/// imported and always render fully qualified.
/// </summary>
public sealed class ImportResolver
{
    private static readonly KType NativeBridgeType = KType.Named("com.questpdf.interop", "NativeBridge");
    private static readonly KType NativeObjectType = KType.Named("com.questpdf.interop", "NativeObject");
    private static readonly KType PointerType = KType.Named("com.sun.jna", "Pointer");
    private static readonly KType PointerByReferenceType = KType.Named("com.sun.jna.ptr", "PointerByReference");
    private static readonly KType IntByReferenceType = KType.Named("com.sun.jna.ptr", "IntByReference");

    private readonly string currentPackage;
    private readonly Dictionary<string, string> importBySimpleName = new(StringComparer.Ordinal);
    private readonly HashSet<string> reservedSimpleNames = new(StringComparer.Ordinal);

    private ImportResolver(string currentPackage) => this.currentPackage = currentPackage;

    public static ImportResolver For(KotlinDeclaration declaration, KotlinBridgeViews views, IReadOnlySet<string>? packageLocalNames = null)
    {
        var resolver = new ImportResolver(declaration.PackageName);
        foreach (var name in packageLocalNames ?? (IReadOnlySet<string>)new HashSet<string>())
            resolver.reservedSimpleNames.Add(name);
        var references = new List<(string Package, string SimpleName, bool ForceQualified)>();

        void CollectType(KType? type)
        {
            if (type is null)
                return;

            foreach (var named in type.NamedTypesRecursive())
            {
                var simple = named.Name.Split('.')[0];
                references.Add((named.PackageName, simple, named.ForceQualified));
            }
        }

        void CollectExpr(KExpr? expr)
        {
            if (expr is KExpr.EnumEntry entry)
                CollectType(entry.EnumType);
        }

        void CollectMarshal(BridgeMarshal marshal)
        {
            switch (marshal)
            {
                case BridgeMarshal.EnumValue enumValue:
                    CollectType(views.EnumType(enumValue));
                    return;

                case BridgeMarshal.DateTimeText dateTime:
                    CollectType(views.TemporalType(dateTime));
                    return;

                case BridgeMarshal.Handle handle:
                    CollectType(views.WrapType(handle));
                    return;

                case BridgeMarshal.HandleSequence sequence:
                    CollectMarshal(sequence.Element);
                    return;

                case BridgeMarshal.CallbackValue callback:
                    CollectType(KType.Named("com.questpdf.interop", callback.Shape.Name));
                    foreach (var wrap in callback.Adapter.ParameterWraps)
                        CollectMarshal(wrap);
                    CollectMarshal(callback.Adapter.ReturnWrap);
                    return;

                case BridgeMarshal.UserImplValue user:
                    CollectType(KType.Named("com.questpdf.interop", user.Proxy.Method.Shape.Name));
                    foreach (var wrap in user.Proxy.Method.ParameterWraps)
                        CollectMarshal(wrap);
                    CollectMarshal(user.Proxy.Method.ReturnWrap);
                    return;

                default:
                    return;
            }
        }

        void CollectExport(NativeExport? export)
        {
            if (export is null)
                return;

            CollectType(NativeBridgeType);
            foreach (var parameter in export.Parameters)
                CollectMarshal(parameter.Marshal);
            CollectMarshal(export.Return);

            if (export.Return is BridgeMarshal.Blob)
            {
                CollectType(PointerByReferenceType);
                CollectType(IntByReferenceType);
            }

            if (export.Return is BridgeMarshal.NullableScalar or BridgeMarshal.EnumValue { Nullable: true })
                CollectType(KType.Named("com.sun.jna.ptr", "ByteByReference"));
        }

        void CollectFunction(KotlinFunction function)
        {
            foreach (var constraint in function.TypeConstraints)
                CollectType(constraint.Bound);
            foreach (var parameter in function.Parameters)
            {
                CollectType(parameter.Type);
                CollectExpr(parameter.DefaultValue);
            }
            CollectType(function.ReturnType);
            if (function.Body is KotlinBody.Bridge bridge)
                CollectExport(bridge.Export);
        }

        void CollectProperty(KotlinProperty property)
        {
            CollectType(property.Type);
            CollectExpr(property.Initializer);
            CollectExport(property.Getter);
            CollectExport(property.Setter);
        }

        void CollectDeclaration(KotlinDeclaration current)
        {
            switch (current)
            {
                case KotlinTypeAlias alias:
                    CollectType(alias.AliasedType);
                    return;

                case KotlinTypeDeclaration type:
                    CollectType(type.SuperClass);
                    foreach (var iface in type.SuperInterfaces) CollectType(iface);
                    foreach (var parameter in type.PrimaryConstructor?.Parameters ?? [])
                    {
                        CollectType(parameter.Type);
                        CollectExpr(parameter.DefaultValue);
                    }
                    foreach (var ctor in type.SecondaryConstructors)
                    {
                        foreach (var parameter in ctor.Parameters)
                        {
                            CollectType(parameter.Type);
                            CollectExpr(parameter.DefaultValue);
                        }
                        CollectExport(ctor.Export);
                    }
                    if (type.ImplClassName is not null)
                        CollectType(NativeObjectType);
                    foreach (var property in type.Properties) CollectProperty(property);
                    foreach (var function in type.Functions) CollectFunction(function);
                    foreach (var property in type.CompanionProperties) CollectProperty(property);
                    foreach (var function in type.CompanionFunctions) CollectFunction(function);
                    foreach (var nested in type.NestedTypes) CollectDeclaration(nested);
                    return;
            }
        }

        CollectDeclaration(declaration);

        // Names declared by this file itself always win.
        if (declaration is KotlinTypeDeclaration root)
        {
            resolver.reservedSimpleNames.Add(root.Name);
            if (root.ImplClassName is not null)
                resolver.reservedSimpleNames.Add(root.ImplClassName);
        }
        else
        {
            resolver.reservedSimpleNames.Add(declaration.Name);
        }

        var candidates = new SortedDictionary<string, SortedSet<string>>(StringComparer.Ordinal);
        foreach (var (package, simple, forceQualified) in references)
        {
            if (forceQualified || package.Length == 0)
                continue;

            if (package == declaration.PackageName || package == "kotlin" || package == "kotlin.collections")
                continue;

            if (!candidates.TryGetValue(simple, out var set))
                candidates[simple] = set = new SortedSet<string>(StringComparer.Ordinal);
            set.Add(package + "." + simple);
        }

        foreach (var (simple, imports) in candidates)
        {
            if (resolver.reservedSimpleNames.Contains(simple))
                continue; // collides with a local declaration → render qualified

            resolver.importBySimpleName[simple] = imports.First();
        }

        return resolver;
    }

    public IReadOnlyList<string> ImportStatements() =>
        importBySimpleName.Values.OrderBy(v => v, StringComparer.Ordinal).ToList();

    /// <summary>Simple or fully qualified rendering for a named type reference.</summary>
    public string Resolve(KType type)
    {
        if (type.IsGenericParameter)
            return type.Name;

        return ResolveName(type.PackageName, type.Name, type.ForceQualified);
    }

    public string ResolveName(string packageName, string name, bool forceQualified = false)
    {
        if (forceQualified)
            return packageName.Length == 0 ? name : packageName + "." + name;

        if (packageName.Length == 0 || packageName == currentPackage)
            return name;

        var simple = name.Split('.')[0];

        // Default-imported kotlin types are shadowed by same-named declarations
        // in the current package and must then be written fully qualified.
        if (packageName is "kotlin" or "kotlin.collections")
            return reservedSimpleNames.Contains(simple) ? packageName + "." + name : name;

        var expected = packageName + "." + simple;

        return importBySimpleName.TryGetValue(simple, out var chosen) && chosen == expected
            ? name
            : packageName + "." + name;
    }
}
