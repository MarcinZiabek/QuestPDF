# Port parity tests

The Kotlin (Maven) and TypeScript (npm) QuestPDF ports are thin fluent-API and
FFI layers over the same native engine that powers the .NET library. These
tests prove those layers are correct: the .NET documentation examples
(`src/dotnet/library/QuestPDF.DocumentationExamples`) are ported to both
languages, all three suites run against the **same engine version** (the
QuestPDF package pinned by `src/dotnet/interop`), and every produced image
must be **byte-identical** across runtimes.

## Running locally

```bash
zx src/port-parity-tests/run-tests.mjs
```

Requires the .NET 10 SDK, JDK 21, Node.js ≥ 18 and `zx`. The script:

1. reads the pinned engine version from `src/dotnet/interop/QuestPDF.Native.csproj`,
2. publishes `QuestPDF.Native` once for the current platform,
3. runs the .NET suite twice (`-p:QuestPdfPackageVersion=<pinned>`; the double
   run detects non-deterministic outputs — random placeholder content, current
   date/time, PDF timestamps — which are then only checked for presence),
4. runs the Kotlin suite (`gradlew docExamplesTest`) and the TypeScript suite
   (`node --test dist/doc-examples/**`) against the same native runtime,
5. compares the output sets and hashes, and writes
   `artifacts/port-parity/report.json`.

Failure modes: an output missing from a port, byte differences in a
deterministic output, an output produced by a port but not by .NET, or a stale
entry in `known-not-ported.txt`.

## What is not compared

- Example classes demonstrating .NET-only integrations (ScottPlot charts,
  ZXing barcodes, SkiaSharp canvas drawing, Mapbox network fetch) are excluded
  from the .NET parity run in `run-tests.mjs` — the regular .NET workflows
  still execute them.
- Individual tests that cannot be ported (unbridged APIs) are listed in
  `known-not-ported.txt` and carry a `// NOT PORTED:` comment in the ported
  file.

## Adding a new example

Write the .NET example as usual, then port it to both languages following
[PORTING.md](PORTING.md). The CI workflow
(`.github/workflows/port-parity-tests.yml`) runs the comparison on every
supported platform except linux-musl (no JVM support on musl).
