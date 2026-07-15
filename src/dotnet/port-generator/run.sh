#!/usr/bin/env bash
# Regenerates the bindings, builds everything (Kotlin library, samples, and the
# NativeAOT shared library when its inputs changed), then runs all sample
# compositions against the real QuestPDF engine.
#
# Usage:
#   ./run.sh                 full pipeline
#   SKIP_GENERATE=1 ./run.sh skip the generator step (just build + run samples)
set -euo pipefail
cd "$(dirname "$0")/../../.."

# --- dotnet (the Gradle daemon and login shells may not share PATH) ---------
if ! command -v dotnet >/dev/null 2>&1; then
    export PATH="/usr/local/share/dotnet:$PATH"
fi
if ! command -v dotnet >/dev/null 2>&1; then
    echo "error: dotnet SDK not found (expected on PATH or in /usr/local/share/dotnet)" >&2
    exit 1
fi

# --- JDK 21 for Gradle (system default JDK is untested with Gradle 9.6.1) ---
JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null \
    || echo "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home")
if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
    echo "error: no JDK 21 found (looked via /usr/libexec/java_home -v 21)" >&2
    exit 1
fi
export JAVA_HOME

# --- 1. regenerate client sources + C# exports + coverage reports -----------
if [[ "${SKIP_GENERATE:-0}" != "1" ]]; then
    echo "==> Regenerating bindings (dotnet run --project src/dotnet/port-generator/QuestPDF.Interop.Generator)"
    dotnet run --project src/dotnet/port-generator/QuestPDF.Interop.Generator
else
    echo "==> Skipping generator (SKIP_GENERATE=1)"
fi

# --- 2. build + publish native library (cached) + run samples ---------------
echo "==> Building and running samples (Gradle, JDK 21)"
(cd src/jvm/package && ./gradlew build runSamples)

# --- 3. show the results -----------------------------------------------------
echo
echo "==> Generated PDFs:"
ls -lh src/jvm/package/build/samples-output/*.pdf | awk '{print "    " $9 "  (" $5 ")"}'
