# DAA Assignment 3 — Graphs: DSU, MST, SCC and Shortest Paths

Own graph library in Java 17 (no graph types from the standard library; `java.util` collections are used only as containers). Implements DSU, Kruskal, Prim, Kosaraju, Kahn, DAG shortest paths, plus the bonus tasks Tarjan (A) and condensation + critical path (B). Counters are collected inside the algorithms; the report is in [`REPORT.md`](REPORT.md).

**Student:** Tolegen Zhassulan, group SE-2526

## Requirements

* JDK 17 or newer
* Maven 3.8+ (or only `javac`, see below)
* Python 3 with `matplotlib` (only for the plots)

## Build and test

```bash
mvn -q test
```

JUnit 5 tests: DSU against a reference implementation, Kruskal vs Prim on 100 random graphs, SCC against brute force (n ≤ 200), topological order and cycle detection, DAG shortest paths against Bellman-Ford (with negative weights), SCC on a path of 100 000 vertices (stack safety), the reference values of the assignment for the four smaller sizes, and the bonus tasks.

## Run the benchmark (reproduces every number in the report)

```bash
mvn -q compile exec:java            # writes results/results.csv (50 rows) and results/results_bonus.csv
python3 scripts/plots.py            # writes results/plots/*.png and results/analysis.md
```

The dense graph with n = 100 000 needs about 1–2 GB of heap; if Maven runs out of memory, use `export MAVEN_OPTS=-Xmx3g`.

Without Maven: `scripts/run_benchmark.sh` compiles with `javac`, runs the benchmark with `-Xmx3g` and draws the plots.

The benchmark is **self-checking**: it compares Kruskal with Prim, validates every topological order, checks Tarjan against Kosaraju, checks `edgeScans` (Kosaraju 2m, Kahn m, DAG m) and compares all MST weights, SCC counts and distance sums with the reference table. On any mismatch it prints `BENCHMARK CHECK FAILED` and exits with code 1 **before** writing the CSV.

The graphs come from `daa.benchmark.GraphGenerator` (always `new Random(42)`), so the counters are identical on every machine; only `time_ms` varies.

## Layout

```
src/main/java/daa/graph        Graph (adjacency list, directed or undirected, file reader)
src/main/java/daa/algorithms   DSU, Kruskal, Prim, Kosaraju, Tarjan, Kahn, DagShortestPath, CriticalPath
src/main/java/daa/metrics      Metrics (dsuOps, edgeScans, comparisons, maxDepth)
src/main/java/daa/benchmark    GraphGenerator, Benchmark
src/test/java/daa              JUnit 5 tests
data/                          input graphs (connected, disconnected, cyclic, DAG)
results/                       results.csv, results_bonus.csv, analysis.md, plots/
scripts/plots.py               plots and counter-vs-formula tables
```

Input format: `n m directed|undirected`, then `m` lines `u v w` (lines starting with `#` are ignored).

## Git workflow

`main` holds working code only (tag `v1.0`); work was done on `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths` and `feature/metrics` and merged with `--no-ff`.

## AI-use acknowledgement

This project was written with the help of Claude (Anthropic): Claude produced the first version of the code, tests, benchmark, plotting script, report and this README from the assignment text, and ran the tests and the benchmark. The reference values from the assignment are reproduced by the code (all ten graphs). I am responsible for the submission, and I am prepared to run the tests and the benchmark and explain every algorithm, counter and table at the defense.
