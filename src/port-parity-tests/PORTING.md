# Porting documentation examples across runtimes

The .NET documentation examples (`src/dotnet/library/QuestPDF.DocumentationExamples`)
are ported to Kotlin (`src/jvm/package/src/doc-examples/kotlin/questpdf/docexamples`)
and TypeScript (`src/node/package/src/doc-examples`). All three suites drive the same
native QuestPDF engine, so for a correct port every produced image is **byte-identical**
across runtimes — that identity is what the port-parity tests verify.

Reference ports (read them before porting anything):

- C# original: `src/dotnet/library/QuestPDF.DocumentationExamples/BorderExamples.cs`
- Kotlin port: `src/jvm/package/src/doc-examples/kotlin/questpdf/docexamples/BorderExamples.kt`
- TypeScript port: `src/node/package/src/doc-examples/border-examples.test.ts`

## Ground rules

1. **Fidelity over beauty.** Copy the exact structure, values, call order, and string
   content of the C# example. Do not reword text, reorder chained calls, rename output
   files, or "improve" the layout. A single changed character changes the image bytes.
2. **Identical output names.** The file name passed to `generateImages`/`generatePdf`
   must match the C# original exactly, routed through the `output(...)` helper.
3. **One C# file → one Kotlin file + one TypeScript file.** Keep the C# file name
   (Kotlin: `TableExamples.kt`; TypeScript: kebab-case + `.test.ts`, e.g.
   `table-examples.test.ts`). C# subfolders map to Kotlin subpackages
   (`Text/` → `questpdf.docexamples.text`) and TypeScript subdirectories.
4. **Test naming.** Kotlin: class `XExamples : DocExample()` with `@Test fun camelCase()`
   per C# method. TypeScript: `test('XExamples.MethodName', () => { ... })` using the
   original C# class and method names.
5. **Not portable?** If a test depends on an unbridged API (see the ports'
   `coverage-report.md`) or a .NET-only library, skip just that test and leave a
   `// NOT PORTED: <output-file> — <reason>` comment at the top of the ported file.
   Port the rest of the file normally.

## Shared helpers

Kotlin (inherited from `DocExample`):
- `output("name.webp")` → absolute output path (also configures the license).
- `resource("Photos/photo.jpg")` → file inside the shared .NET `Resources/` directory.
- `Color.toHexString()` → the string .NET produces implicitly for a `Color`.

TypeScript (from `./doc-example`):
- `output(...)`, `resource(...)`, `colorToString(color)` — same responsibilities.
- `imageSettings({ imageFormat, imageCompressionQuality, rasterDpi })` — the counterpart
  of a C# `new ImageGenerationSettings { ... }` object initializer.

## Mechanical mapping

| C# | Kotlin | TypeScript |
| --- | --- | --- |
| `Document.Create(document => ...)` | `Document.create { ... }` (receiver) | `Document.create((document) => ...)` |
| `document.Page(page => ...)` | `page { ... }` | `document.page((page) => ...)` |
| `x => x.FontSize(20)` (text style) | `{ fontSize(20f) }` (receiver returning `TextStyle`) | `(style) => style.fontSize(20)` |
| `.Text(text => { text.Span("a"); })` | `.text { span("a") }` | `.text((text) => { text.span('a'); })` |
| `.Column(column => { column.Item()... })` | `.column { item()... }` | `.column((column) => { column.item()... })` |
| numeric literals `25` | `25f` for float parameters, plain for `Int` | `25` |
| `new PageSize(0, 0)` | `PageSize(0f, 0f)` (import `com.questpdf.helpers.PageSize`) | `new PageSize(0, 0)` |
| `new ImageGenerationSettings { A = x }` | `ImageGenerationSettings().apply { a = x }` | `imageSettings({ a: x })` |
| `x => "name.webp"` (image path) | `{ output("name.webp") }` | `() => output('name.webp')` |
| `x => $"name-{x}.webp"` | `{ index -> output("name-$index.webp") }` | `(index) => output(\`name-${index}.webp\`)` |
| `"Resources/Photos/a.jpg"` | `resource("Photos/a.jpg")` | `resource('Photos/a.jpg')` |
| `[a, b]` collection expression | `arrayOf(a, b)` | `[a, b]` |
| `Placeholders.Lorem...` | `Placeholders...` (`com.questpdf.helpers.Placeholders`) | `Placeholders...` |
| `.Text(someColor)` (implicit ToString) | `.text(color.toHexString())` | `.text(colorToString(color))` |
| custom `IComponent` class | class implementing `com.questpdf.infrastructure.IComponent` | class implementing `IComponent` |
| C# extension method on `IContainer` | Kotlin extension function | free function taking the container |

Type homes (both ports re-export everything from the package root in TypeScript;
Kotlin needs precise imports): `Colors`, `Fonts`, `PageSize`, `PageSizes`,
`Placeholders`, `FontFeatures` live in `com.questpdf.helpers`; `ImageFormat`,
`ImageGenerationSettings`, `ImageCompressionQuality`, `TextStyle`, `LicenseType`
and friends live in `com.questpdf.infrastructure`; descriptors in `com.questpdf.fluent`.

## Non-deterministic examples

Examples using `Placeholders` random content or the current date/time cannot be
byte-identical across processes. Port them faithfully anyway (Kotlin/TS `Placeholders`
call the same native implementation; use `java.time` / `Date` for current-date text).
The comparison pipeline detects non-deterministic outputs automatically and only
checks that the files exist, so no special marking is needed.

## Verification

- Kotlin compiles: `cd src/jvm/package && ./gradlew compileDocExamplesKotlin`
  (concurrent invocations wait on Gradle's project lock — retry on lock timeouts).
- TypeScript compiles: `cd src/node/package && npx tsc --noEmit`.
- Full parity: `zx src/port-parity-tests/run-tests.mjs` (runs all three suites and
  compares every produced file).
