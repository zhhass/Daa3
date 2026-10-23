#!/usr/bin/env bash
# Maven-free fallback: compile with javac, run the benchmark, draw the plots.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p out/main results/plots
javac --release 17 -d out/main $(find src/main -name '*.java')
java -Xmx3g -cp out/main daa.benchmark.Benchmark results
python3 scripts/plots.py results/results.csv results
