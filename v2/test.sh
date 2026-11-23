#!/usr/bin/env bash
# Compiles AlgoScope and runs all its tests (macOS and Linux). Needs JDK 21 or newer.
set -euo pipefail

# Work from the folder of this script, wherever it is called from
cd "$(dirname "$0")"

# Start from a clean output folder so no old class can hide a problem
rm -rf out
mkdir -p out

# Every .java file under src/ and test/
find src test -name '*.java' > out/sources.txt
javac --release 21 -encoding UTF-8 -d out @out/sources.txt

java -Djava.awt.headless=true -cp out algoscope.AllTests
