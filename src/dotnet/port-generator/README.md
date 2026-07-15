# QuestPDF Kotlin Port — Generated FFI Bridge

A fully working Kotlin port of [QuestPDF](https://www.questpdf.com): the
generator reflects over the QuestPDF assembly and emits **three coordinated
artifacts** — a Kotlin DSL mirroring the full fluent API, a JNA binding layer,
and a set of C# `UnmanagedCallersOnly` exports compiled into a NativeAOT
shared library. Kotlin compositions run against the real QuestPDF engine and
produce real PDFs.

```
Kotlin DSL  ──JNA──▶  QuestPDF.Native.dylib (NativeAOT)  ──calls──▶  QuestPDF + Skia
   ▲                        │
   └────── callbacks ◀──────┘   (composition lambdas, page-number formatters,
                                 IComponent implementations, dynamic images)
```

The project started as an ergonomics prototype (a no-op stub library used to
judge how the fluent API feels in idiomatic Kotlin — see git history); the
validated API shape was then kept **unchanged** while the generator learned to
emit the live bridge instead of stubs.

The generator is structured as a **language-neutral core plus pluggable
language backends** — Kotlin today, with Python and TypeScript intended next —
all sharing one native library and ABI (see *Architecture* below).

## Layout

| Path | Contents |
|---|---|
| `src/dotnet/port-generator/QuestPDF.Interop.Generator/Core/` | Language-neutral pipeline: extraction, API model/index, classification vocabulary, bridge marshal planning, ABI layout, shared C# export emission, reporting |
| `src/dotnet/port-generator/QuestPDF.Interop.Generator/Backends/Kotlin/` | Everything Kotlin: type/name mapping, classification rules, JVM dedup, export naming, DSL + JNA emitters |
| `src/dotnet/port-generator/QuestPDF.Interop.Generator.Tests/` | Generator tests: model snapshot, golden files (both bridge sides), rule units, ABI invariants |
| `src/dotnet/interop/` | NativeAOT shared-library project (`QuestPDF.Native`): handwritten `InteropRuntime.cs` + generated `Exports/*.g.cs` (wiped each run, not committed); one library serves every language client |
| `src/jvm/package/` | Gradle Kotlin/JVM project: `src/generated/kotlin` (wiped each run, not committed), `src/manual/kotlin` (interop runtime + handwritten overrides), `src/samples/kotlin` |
| `src/node/package/` | npm TypeScript project: `src/generated` (wiped each run, not committed), `src/manual` (interop runtime + handwritten overrides), `src/samples` |
| `src/<client>/package/manual-overrides.txt` | Doc-comment IDs excluded from generation for hand-written implementation (per-backend file) |
| `src/<client>/package/coverage-report.md` | Classification of every public member for that client; regenerated on every run (not committed) |

Generated code is not committed: `Exports/`, both `src/generated` trees, and
the coverage reports are gitignored and recreated by every generator run.

## Commands

All commands run from the repository root.

```bash
# Regenerate all language clients + shared C# exports + coverage reports
dotnet run --project src/dotnet/port-generator/QuestPDF.Interop.Generator

# Regenerate selected backends only
dotnet run --project src/dotnet/port-generator/QuestPDF.Interop.Generator generate --language kotlin

# Generator tests (snapshot, goldens, rules, ABI invariants)
dotnet test src/dotnet/port-generator

# Refresh committed snapshot/golden expectations after an intended change
UPDATE_GOLDENS=1 dotnet test src/dotnet/port-generator

# Publish the native library (dotnet publish, NativeAOT) — automatic
# dependency of runSamples; cached until generator output changes
cd src/jvm/package && ./gradlew publishNative

# Compile everything, generate real PDFs into build/samples-output/
cd src/jvm/package && ./gradlew build runSamples

# Full dev loop (generate + build + samples)
./src/dotnet/port-generator/run.sh
```

Gradle needs JDK 21 (`JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home`).
Toolchain: .NET 10, QuestPDF 2026.7.1 (pinned), Gradle 9.6.1, Kotlin 2.4.0, JNA 5.17.

## Architecture: one core, many language backends

The generator is split into a language-neutral core and pluggable language
backends (`ILanguageBackend`), so Python, TypeScript and further clients can be
added without touching the core:

```
QuestPDF.Interop.Generator/
├── Core/                 extraction · API model/index · TypePlan + rule machinery ·
│                         BridgePlanner (neutral marshals) · BridgeAbi (slot layout) ·
│                         NativeExportsEmitter (shared C# side) · coverage reporting
└── Backends/
    └── Kotlin/           type/name mapping · classification rules + wording ·
                          JVM-erasure dedup · export naming · KotlinEmitter (DSL) ·
                          InteropKotlinEmitter (JNA) · KotlinBridgeViews (projections)
```

The load-bearing decisions:

- **The bridge model is language-neutral.** `BridgeMarshal` records identify
  API types by C# full name only (no client types); each backend projects them
  into its own type system on demand (Kotlin: `KotlinBridgeViews` derives enum
  types, handle wrapper classes, `java.time` types and proxy method names).
- **One native library serves every client.** Each backend returns an
  `InteropModel` (exports, callback shapes, proxies); the pipeline merges them
  with conflict verification (`GeneratorPipeline.MergeInterop`) and emits the
  C# `UnmanagedCallersOnly` sources once. The Kotlin backend currently authors
  the ABI; when a second language lands, export authorship should be lifted
  into a shared naming service so all backends bind one export set.
- **Classification is per-backend.** Which members map, how overloads
  collapse, and the report wording are language decisions (Kotlin needs
  JVM-erasure dedup; Python will need genuine-overload merging). The shared
  vocabulary (`TypePlan`, `ITypeRule`, `ReportEntry`, `OverloadCollapseRule`)
  and the marshal planner live in the core so backends only add their idioms.

### Pipeline per run

1. **Extraction** (`Core/Extraction`) — reflection over the QuestPDF assembly:
   types, members, extension methods, parameter defaults, generics,
   nullability, XML docs, enum underlying values. Runs once for all backends.
2. **Classification** (per backend; `Backends/Kotlin`) — plain records +
   ordered rule pipelines (first match wins). `BridgePlanner` (`Core/Bridge`)
   is the language-neutral first-match marshal chain deciding how every value
   crosses the boundary; the backend attaches a `NativeExport` plan
   (C# invocation + per-value marshals + deterministic entry-point name) to
   each member. Everything unmappable becomes `unsupported` with a reason —
   never a crash, never non-compiling output.
3. **Emission** — per backend for client sources (`KotlinEmitter` for DSL
   bodies, `InteropKotlinEmitter` for the JNA interface + callback
   fun-interfaces), and once for the shared native side
   (`Core/NativeEmission`), all consuming the same plans, so the ABI agrees by
   construction.

### Adding a language backend

1. Implement `ILanguageBackend` under `QuestPDF.Interop.Generator/Backends/<Language>/`:
   declare the repo directory it owns, its manual-overrides file and coverage
   report path, and produce client files + an `InteropModel` + a
   classification report from the extracted `ApiAssembly`.
2. Reuse the core: `ApiIndex`, `TypePlan`/`TypeRulePipeline`,
   `OverloadCollapseRule`, `BridgePlanner` marshals and `BridgeAbi` slot
   layouts; write the language's own type/name mappers and emitters (its FFI
   binding layer maps `AbiSlot`s the way `InteropKotlinEmitter` maps them to
   JNA types).
3. Register it in `Backends/LanguageBackends.All`; select with
   `--language <id>`. Its coverage report and generated tree are wiped and
   rewritten per run, like Kotlin's.

### How values cross the bridge

| C# | ABI | Kotlin |
|---|---|---|
| API object (class/interface/struct) | `long` handle into a .NET-side table | wrapper extending `NativeObject`; released by a `Cleaner` |
| enum | underlying `int` | `enum class E(val value: Int)` + `fromValue` |
| `string` | UTF-8 pointer in; allocated pointer + free-export out | `String` |
| `byte[]` | pointer+length in; out-parameters + free-export out | `ByteArray` |
| `float?`, `LicenseType?` | value + has-value flag (byte) | `Float?`, `LicenseType?` |
| `DateTime(Offset)` | round-trip ISO-8601 text | `java.time.LocalDateTime` / `OffsetDateTime` |
| `Action<T>`/`Func<…>`/delegates | function pointer (JNA callback trampoline) | lambda, wrapped in a generated shape adapter |
| `IComponent` implementations | generated .NET proxy class holding a callback | plain Kotlin `IComponent` implementation |
| `params T[]` / `IEnumerable<T>` of the above | typed array + count | `vararg` / `Array` / `Iterable` |

Callbacks are fully re-entrant: a Kotlin lambda invoked from .NET can call
back into the bridge (the samples nest five levels deep). Exceptions never
cross the native boundary raw — .NET exceptions are reported through an error
callback and rethrown Kotlin-side as `QuestPdfNativeException` (with the full
.NET stack trace); Kotlin exceptions thrown inside composition lambdas are
recorded by `NativeBridge.guard` and rethrown after the native call returns.

Callback trampolines are strongly retained (`NativeBridge.retain`) because
the .NET side may hold a function pointer across calls (a document's content
handler runs on every generate); `NativeBridge.releaseRetainedCallbacks()`
reclaims them between documents in long-running processes.

## Samples — real PDFs

`./gradlew runSamples` publishes the native library and generates four
documents into `src/jvm/package/build/samples-output/`: an invoice (address
components via the `IComponent` proxy, zebra table, computed totals, page
numbering), a table-heavy landscape report (row/column spans), a text
showcase (spans, inline elements, section page numbers) and a layout showcase
(gradients, box shadows, layers with a rotated watermark, multi-column,
transforms, `showIf` predicates, decoded placeholder images, SVG).

## Findings

The stub-first, bridge-second approach was validated hard:

1. **The Kotlin API surface survived the switch untouched.** Samples written
   against stubs needed zero signature changes — only two *semantic* fixes
   the stubs had masked (below).
2. **Stubs hide real engine semantics.** QuestPDF rejects a second fluent
   chain on the same single-child container; a no-op stub happily accepts it.
   One sample had exactly this bug and it only surfaced with the live engine
   ("You should not assign multiple child elements…"). Ergonomics can be
   judged on stubs; *correctness* cannot.
3. **C# nullability leaks into the ABI.** `Settings.License` is a nullable
   enum, page numbers are `int?` — both needed a dedicated flag+value wire
   format. A binding generator has to solve nullable scalars early, not as an
   afterthought.
4. **Callback trampoline lifetime is the sharpest interop edge.** JNA frees a
   trampoline when its Java object is collected, while .NET keeps function
   pointers (content handlers re-run per generate). Without explicit
   retention this is a use-after-free that strikes rarely and confusingly.
5. **Full .NET exception texts crossing the error channel make debugging
   pleasant** — a failed composition prints the QuestPDF exception plus both
   stack traces, native and Kotlin, in one readable chain.
6. **NativeAOT hosting needs explicit probing help.** Inside a JVM host,
   `AppContext.BaseDirectory` points at the JVM, so QuestPDF's native Skia
   dependency and bundled fonts must be resolved via a `DllImportResolver`
   rooted at the publish directory (see `QuestPdf_Initialize`).
7. **A naive NativeAOT dylib is mostly debug info.** Stripping symbols into a
   discardable `.dSYM` plus size-focused trimming (`OptimizationPreference=Size`,
   `InvariantGlobalization`, `UseSystemResourceKeys`, unused-feature switches)
   shrank the library 26 MB → 7.9 MB with byte-identical sample output.
   `StackTraceSupport` stays on deliberately: full .NET stacks through the
   error channel cost only ~0.7 MB more than the absolute minimum.
8. Earlier stub-phase ergonomics findings still stand: trailing lambdas break
   when the lambda is not the last C# parameter (`element(handler = …)`), the
   `Unit` enum collides with `kotlin.Unit` (generated code fully qualifies),
   JVM erasure forbids `Action`/`Func` overload pairs, `UInt` table
   coordinates read as `row(2u)`, and members-instead-of-extensions gives
   clean IDE discoverability.

## Verification

- `dotnet test` — 69 tests: committed Stage 2 model snapshot, golden files on
  **both** sides of the bridge (Kotlin sources, JNA interface, callback
  shapes, C# exports incl. proxies), rule unit tests, and pipeline invariants
  (≥90% coverage, determinism of all three outputs, unique entry points, and
  Kotlin-calls ⊆ JNA-declarations = C#-exports ABI agreement).
- `./gradlew build` — generated + manual + samples compile.
- `./gradlew runSamples` — four compositions generate real, valid PDFs
  through the bridge.
- Coverage: **95.8% generated** (1150 of 1200 public entries; see
  `src/jvm/package/coverage-report.md`, written by each generator run); the
  remainder is reasoned skips (Stream members,
  async surface, collection returns, `Text(object)`, reflective generics)
  plus the deliberate hand-written `Placeholders`.
