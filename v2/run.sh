#!/usr/bin/env bash
# Compiles and opens AlgoScope (macOS and Linux). Needs JDK 21 or newer.
set -euo pipefail

# Work from the folder of this script, wherever it is called from
cd "$(dirname "$0")"

rm -rf out/app
mkdir -p out/app

# Only the app, not the tests
find src -name '*.java' > out/app-sources.txt
javac --release 21 -encoding UTF-8 -d out/app @out/app-sources.txt

java -cp out/app algoscope.Main
