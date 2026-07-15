using System.Text;
using System.Xml.Linq;

namespace QuestPDF.Interop.Generator.Backends.TypeScript.Emission;

/// <summary>
/// Converts the raw <c>&lt;member&gt;</c> XML documentation captured in Stage 1
/// into TSDoc: summary and remarks become body text, <c>&lt;param&gt;</c> becomes
/// <c>@param</c>, <c>&lt;returns&gt;</c> becomes <c>@returns</c>, and common
/// inline tags become Markdown.
/// </summary>
public static class TsDocConverter
{
    /// <summary>Returns TSDoc lines (without comment markers), or empty when there is no usable content.</summary>
    public static IReadOnlyList<string> ToDocLines(string? rawMemberXml)
    {
        if (string.IsNullOrWhiteSpace(rawMemberXml))
            return [];

        XElement member;
        try
        {
            member = XElement.Parse(rawMemberXml);
        }
        catch
        {
            return [];
        }

        var sections = new List<string>();

        void AddSection(string? text)
        {
            var cleaned = Normalize(text);
            if (cleaned.Length > 0)
                sections.Add(cleaned);
        }

        AddSection(InlineText(member.Element("summary")));
        AddSection(InlineText(member.Element("remarks")));

        foreach (var example in member.Elements("example"))
            AddSection(InlineText(example));

        var tags = new List<string>();

        foreach (var param in member.Elements("param"))
        {
            var name = param.Attribute("name")?.Value;
            var text = Normalize(InlineText(param));
            if (name is not null && text.Length > 0)
                tags.Add($"@param {name} {CollapseForTag(text)}");
        }

        foreach (var typeParam in member.Elements("typeparam"))
        {
            var name = typeParam.Attribute("name")?.Value;
            var text = Normalize(InlineText(typeParam));
            if (name is not null && text.Length > 0)
                tags.Add($"@param {name} {CollapseForTag(text)}");
        }

        var returns = Normalize(InlineText(member.Element("returns")));
        if (returns.Length > 0)
            tags.Add($"@returns {CollapseForTag(returns)}");

        if (sections.Count == 0 && tags.Count == 0)
            return [];

        var lines = new List<string>();
        for (var i = 0; i < sections.Count; i++)
        {
            if (i > 0)
                lines.Add("");
            lines.AddRange(sections[i].Split('\n'));
        }

        if (tags.Count > 0)
        {
            if (lines.Count > 0)
                lines.Add("");
            lines.AddRange(tags);
        }

        return lines;
    }

    /// <summary>Recursive inline conversion of doc XML content to Markdown-ish text.</summary>
    private static string InlineText(XElement? element)
    {
        if (element is null)
            return "";

        var sb = new StringBuilder();
        Append(element, sb);
        return sb.ToString();
    }

    private static void Append(XElement element, StringBuilder sb)
    {
        foreach (var node in element.Nodes())
        {
            switch (node)
            {
                case XText text:
                    sb.Append(text.Value);
                    break;

                case XElement child:
                    AppendElement(child, sb);
                    break;
            }
        }
    }

    private static void AppendElement(XElement element, StringBuilder sb)
    {
        switch (element.Name.LocalName)
        {
            case "c":
                sb.Append('`').Append(element.Value.Trim()).Append('`');
                break;

            case "code":
                sb.Append("\n```\n").Append(element.Value.Trim('\n')).Append("\n```\n");
                break;

            case "see" or "seealso":
            {
                var href = element.Attribute("href")?.Value;
                if (href is not null)
                {
                    var text = element.Value.Trim();
                    sb.Append('[').Append(text.Length > 0 ? text : href).Append("](").Append(href).Append(')');
                }
                else
                {
                    var cref = element.Attribute("cref")?.Value ?? "";
                    sb.Append('{').Append("@link ").Append(CrefDisplayName(cref)).Append('}');
                }
                break;
            }

            case "a":
            {
                var href = element.Attribute("href")?.Value ?? "";
                sb.Append('[').Append(element.Value.Trim()).Append("](").Append(href).Append(')');
                break;
            }

            case "paramref" or "typeparamref":
                sb.Append('`').Append(element.Attribute("name")?.Value ?? "").Append('`');
                break;

            case "br":
                sb.Append('\n');
                break;

            case "para":
                sb.Append('\n');
                Append(element, sb);
                sb.Append('\n');
                break;

            case "list":
                foreach (var item in element.Elements("item"))
                {
                    sb.Append("\n- ");
                    var term = item.Element("term");
                    var description = item.Element("description");
                    if (term is not null || description is not null)
                    {
                        if (term is not null)
                            sb.Append("**").Append(term.Value.Trim()).Append("** ");
                        if (description is not null)
                            Append(description, sb);
                    }
                    else
                    {
                        Append(item, sb);
                    }
                }
                sb.Append('\n');
                break;

            case "b" or "strong":
                sb.Append("**").Append(element.Value.Trim()).Append("**");
                break;

            case "i" or "em":
                sb.Append('*').Append(element.Value.Trim()).Append('*');
                break;

            default:
                Append(element, sb);
                break;
        }
    }

    /// <summary>Readable member name from a doc-comment ID: last identifier of the member path.</summary>
    private static string CrefDisplayName(string cref)
    {
        var withoutPrefix = cref.Length > 2 && cref[1] == ':' ? cref[2..] : cref;
        var withoutParams = withoutPrefix.Split('(')[0];
        var segments = withoutParams.Split('.');

        if (withoutPrefix.StartsWith("QuestPDF", StringComparison.Ordinal) && cref.StartsWith("M:", StringComparison.Ordinal) && segments.Length >= 2)
            return segments[^2] + "." + segments[^1];

        return segments[^1].Replace("`", "");
    }

    private static string Normalize(string? text)
    {
        if (text is null)
            return "";

        var lines = text.Replace("\r\n", "\n").Split('\n').Select(l => l.Trim()).ToList();

        while (lines.Count > 0 && lines[0].Length == 0) lines.RemoveAt(0);
        while (lines.Count > 0 && lines[^1].Length == 0) lines.RemoveAt(lines.Count - 1);

        var collapsed = new List<string>();
        var previousBlank = false;
        foreach (var line in lines)
        {
            var blank = line.Length == 0;
            if (blank && previousBlank)
                continue;
            collapsed.Add(line);
            previousBlank = blank;
        }

        // A comment can never contain the sequence */
        return string.Join("\n", collapsed).Replace("*/", "* /");
    }

    private static string CollapseForTag(string text) =>
        text.Replace('\n', ' ').Trim();
}
