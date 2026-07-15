using System.Text;

namespace QuestPDF.Interop.Generator.Core;

/// <summary>Simple indentation-aware writer (4-space indents).</summary>
public sealed class CodeWriter
{
    private readonly StringBuilder sb = new();
    private int level;

    public void Indent() => level++;
    public void Outdent() => level = Math.Max(0, level - 1);

    public void Line(string text = "")
    {
        if (text.Length == 0)
        {
            sb.AppendLine();
            return;
        }

        sb.Append(new string(' ', level * 4)).AppendLine(text);
    }

    public override string ToString() => sb.ToString();
}
