namespace QuestPDF.Interop.Generator.Tests;

[Collection("pipeline")]
public sealed class SnapshotTests(PipelineFixture pipeline)
{
    /// <summary>
    /// The committed snapshot of the classified model. Its diff between
    /// QuestPDF versions doubles as an API-evolution report: new members show up
    /// as added lines together with how they were classified.
    /// </summary>
    [Fact]
    public void ClassifiedModelMatchesCommittedSnapshot()
    {
        var snapshot = GeneratorPipeline.RenderSnapshot(pipeline.Assembly, pipeline.Model.Report);
        TestPaths.AssertMatchesFile(Path.Combine("snapshots", "api-model.snapshot.txt"), snapshot);
    }
}
