# Porting documentation examples across runtimes

The .NET documentation examples (`src/dotnet/library/QuestPDF.DocumentationExamples`)
are ported to Kotlin (`src/jvm/package/src/doc-examples/kotlin/questpdf/docexamples`),
Java (`src/jvm/package/src/doc-examples/java/questpdf/docexamples`) and TypeScript
(`src/node/package/src/doc-examples`). All suites drive the same native QuestPDF
engine, so for a correct port every produced image is **byte-identical** across
runtimes — that identity is what the port-parity tests verify.

Reference ports (read them before porting anything):

- C# original: `src/dotnet/library/QuestPDF.DocumentationExamples/BorderExamples.cs`
- Kotlin port: `src/jvm/package/src/doc-examples/kotlin/questpdf/docexamples/BorderExamples.kt`
- Java port: `src/jvm/package/src/doc-examples/java/questpdf/docexamples/BorderExamples.java`
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

## Java specifics

The Java suite ports the *Kotlin* files one-to-one (same package, same class and
test-method names, JUnit 5, `extends DocExample`); consult the C# original whenever
the Kotlin port looks ambiguous. Java-flavoured mapping on top of the table above:

| Kotlin | Java |
| --- | --- |
| `Document.create { ... }` | `Document.create(document -> { ... })` |
| `page { continuousSize(450f) }` | `document.page(page -> { page.continuousSize(450f); })` |
| `.text { span("a") }` | `.text(text -> { text.span("a"); })` |
| `defaultTextStyle { fontSize(20f) }` | `defaultTextStyle(style -> style.fontSize(20f))` (returns the style) |
| `Colors.White`, `Colors.Blue.Darken4` | `Colors.getWhite()`, `Colors.Blue.getDarken4()` |
| `PageSize(0f, 0f)` | `new PageSize(0f, 0f)` |
| `arrayOf(a, b)` (vararg/array argument) | `new Color[] { a, b }` (or plain varargs) |
| `ImageGenerationSettings().apply { imageFormat = x }` | local `var settings = new ImageGenerationSettings(); settings.setImageFormat(x);` declared *before* the `Document.create(...)` chain |
| `{ output("name.webp") }` | `index -> output("name.webp")` |
| `color.toHexString()` | `color.toString()` (Color/Size/TextStyle bridge .NET `ToString()`) |
| `Placeholders.lorem()` etc. | `Placeholders.lorem()` (object members are `@JvmStatic`) |
| `cell().column(1u).rowSpan(24u)` | `cell().column(1).rowSpan(24)` (unsigned params have `Int` twins) |
| Kotlin extension function workaround | `private static` helper method taking the container |
| `object : IComponent { ... }` / class | (anonymous) class implementing `com.questpdf.infrastructure.IComponent` |

Handler lambdas (`page(...)`, `column(...)`, `row(...)`, `text(...)`, ...) have
`java.util.function.Consumer` overloads — never return `Unit.INSTANCE`. Functions
whose Kotlin form has default parameter values have reduced Java overloads
(`margin(25f)`, `placeholder()`, `relativeItem()`), so defaults are never spelled
out. `com.questpdf.infrastructure.Unit` is only needed for non-point units.

Caveats learned porting the suite:

- On subclassed descriptors (e.g. `TextPageNumberDescriptor`), calling a *reduced*
  overload declared on the base class returns the base type — reorder the chain
  (subclass-specific calls first) instead of restating default arguments.
- An *expression-bodied* lambda whose body is a method call
  (`element(c -> helper(c))`, `text(t -> t.currentPageNumber())`) is ambiguous to
  javac between the `Consumer` overload and Kotlin's `Function1` overload; use a
  *block* body (`element(c -> { helper(c); })`) — it is void-compatible only and
  resolves to the Consumer overload.
- Java lambdas cannot capture mutating `for` counters — hoist a per-iteration
  copy (`var index = i;`).
- A Java file's public class must match its file name, so the preserved C# typo
  `CodePatternExtesionMethodExample` keeps the *class* name but the Java *file*
  is spelled `CodePatternExtensionMethodExample.java`.

## Non-deterministic examples

Examples using `Placeholders` random content or the current date/time cannot be
byte-identical across processes. Port them faithfully anyway (Kotlin/TS `Placeholders`
call the same native implementation; use `java.time` / `Date` for current-date text).
The comparison pipeline detects non-deterministic outputs automatically and only
checks that the files exist, so no special marking is needed.

## Verification

- Kotlin compiles: `cd src/jvm/package && ./gradlew compileDocExamplesKotlin`
  (concurrent invocations wait on Gradle's project lock — retry on lock timeouts).
- Java compiles: `cd src/jvm/package && ./gradlew compileDocExamplesJavaJava`.
- TypeScript compiles: `cd src/node/package && npx tsc --noEmit`.
- Full parity: `zx src/port-parity-tests/run-tests.mjs` (runs every suite and
  compares every produced file).
