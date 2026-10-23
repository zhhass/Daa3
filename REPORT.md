# DAA Assignment 3 — Graphs: DSU, MST, SCC and Shortest Paths

**Student:** Tolegen Zhassulan, group SE-2526  **Course:** Design and Analysis of Algorithms (instructor T. Nurlybek)
**Repository:** `<GitHub link here>` (branch `main`, tag `v1.0`)

All numbers below come from `results/results.csv` (50 rows) and `results/analysis.md`, produced by one command
(`daa.benchmark.Benchmark`, generator seed 42). The benchmark compares its output with the reference table of the
assignment and stops with exit code 1 before writing anything if a single value differs; all ten graphs match, and
`edgeScans` is exactly 2m for Kosaraju and exactly m for Kahn and DAG shortest paths.

## 1. Complexity table

V = vertices, E = edges. "Extra space" is what the algorithm allocates besides the input graph.

| Algorithm | Time | Extra space | Justification |
|---|---|---|---|
| DSU `find` / `union` | O(α(n)) amortized | O(n) | Union by rank keeps trees at depth O(log n); path compression flattens them; together a sequence of m operations costs O(m α(n)), and α(n) ≤ 4 for any realistic n. Arrays `parent[]`, `rank[]`. |
| Kruskal | O(E log E + E α(V)) = O(E log E) | O(E + V) | Merge sort of E edge indices dominates (≈ E log₂ E comparisons); then at most E DSU operations. Index array + merge buffer (2E ints) + DSU. |
| Prim (lazy, binary heap) | O(E log E) = O(E log V) | O(E + V) | Every arc scanned once (2E arcs) and each non-tree neighbour is pushed once, so the heap sees up to 2E pushes of cost O(log E). `inTree[]` is O(V). |
| Kosaraju | O(V + E) | O(V + E) | Two DFS passes, each visits every vertex and arc once. The reversed graph is a full copy (O(V + E)); the stacks and `finish[]` are O(V). |
| Kahn | O(V + E) | O(V) | Each vertex enters the queue once and each arc decrements one in-degree. `indeg[]` and the order/queue array are O(V). |
| DAG shortest paths | O(V + E) given the order (O(V + E) with Kahn) | O(V) | One pass in topological order, one relaxation per arc. `dist[]`, `parent[]`. |
| *Tarjan (bonus)* | O(V + E) | O(V) | One DFS pass, no reversed graph: `index[]`, `low[]`, two stacks. |

## 2. Counters versus formulas

The counters are incremented inside the algorithms (`dsuOps` = `find` calls + successful `union` calls; `edgeScans` = arcs looked at; `comparisons` = weight comparisons; `maxDepth` = deepest explicit stack). If an algorithm has complexity O(f), then counter / f must stay constant as n grows. The columns are n = 1 000, 3 162, 10 000, 31 623, 100 000; sparse is m = 4n, dense is m = 20n.

| Counter / formula | density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 |
|---|---|---|---|---|---|---|
| Kosaraju edgeScans / (V+E) | sparse | 1.600 | 1.600 | 1.600 | 1.600 | 1.600 |
| | dense | 1.905 | 1.905 | 1.905 | 1.905 | 1.905 |
| Kahn edgeScans / (V+E) | sparse | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 |
| | dense | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 |
| DAG SP edgeScans / (V+E) | sparse | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 |
| | dense | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 |
| Prim edgeScans / 2E | both | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 |
| Prim comparisons / (E log₂ E) | sparse | 1.827 | 1.867 | 1.900 | 1.929 | 1.946 |
| | dense | 1.826 | 1.849 | 1.868 | 1.880 | 1.892 |
| Kruskal comparisons / (E log₂ E) | sparse | 0.896 | 0.928 | 0.940 | 0.926 | 0.949 |
| | dense | 0.935 | 0.922 | 0.946 | 0.953 | 0.942 |
| Kruskal dsuOps / edgeScans | sparse | 2.324 | 2.283 | 2.259 | 2.252 | 2.250 |
| | dense | 2.232 | 2.219 | 2.241 | 2.166 | 2.134 |
| Kruskal dsuOps / E | sparse | 1.791 | 2.019 | 2.182 | 2.232 | 2.248 |
| | dense | 0.480 | 0.506 | 0.465 | 0.654 | 0.794 |

The traversals are constant to three digits: the ratio depends on density only because V + E ≠ E (2E/(V+E) = 1.6 for m = 4n and 1.905 for m = 20n), and it does not change with n. Kruskal's sorting comparisons stay at ≈ 0.93 · E log₂ E, which is the expected constant of a merge sort. Prim's comparisons are ≈ 1.9 · E log₂ E: the heap receives up to 2E entries, and Java's `PriorityQueue` spends up to two comparisons per level on a pop; the slow upward drift of this ratio (1.83 → 1.95) is the same log-factor effect, not a different complexity class.

**One ratio that does *not* stay constant, and why.** `dsuOps / E` for Kruskal drifts on dense graphs (0.48 → 0.79). That is not a violation of the bound: Kruskal stops as soon as the tree is complete, so it examines only part of the edge list (section 5), and the DSU does work only for examined edges. The matching formula is `dsuOps / edgeScans`, which is flat at ≈ 2.1–2.3: two `find` calls per examined edge, plus n − 1 successful unions (2.25 = 2 + 1/4 at n = 100 000 sparse, as expected).

## 3. Correctness arguments

**Cut property (Kruskal and Prim).** Let G be a connected weighted graph, let A be a subset of the edges of some minimum spanning tree T, and let (S, V∖S) be a cut that no edge of A crosses. If e is a lightest edge crossing the cut, then A ∪ {e} is still contained in some minimum spanning tree. To see this, suppose e ∉ T. Adding e to T creates a cycle, and that cycle must cross the cut a second time at some edge f ∈ T. Because e is lightest across the cut, w(e) ≤ w(f), so T' = T − f + e is a spanning tree of weight at most w(T), hence also minimum, and it contains A ∪ {e}. Kruskal uses this with S = the component of the DSU that contains one endpoint of the lightest remaining edge: that edge is the lightest edge leaving its component, so it is safe, and edges whose ends are already in one component are skipped because they would close a cycle. Prim uses it with S = the current tree: the heap always yields the lightest edge leaving S, and an entry whose vertex is already in the tree is stale and skipped. Both algorithms therefore add only safe edges, n − 1 times, and end with a minimum spanning tree; ties may produce different trees but never different weights, which the benchmark checks on every graph.

**Induction on the topological order (DAG shortest paths).** Let v₁, …, vₙ be a topological order and d\*(v) the true shortest distance from the virtual super-source. Claim: when the algorithm reaches vᵢ, `dist[vᵢ] = d*(vᵢ)`. For the first vertex there is nothing to prove: it has in-degree 0, so `dist` is 0 by initialisation, and there is no edge into it. Assume the claim for v₁, …, vᵢ₋₁. Every edge (u, vᵢ) has u earlier in the order, so all of them have already been relaxed with the correct `dist[u]`, and `dist[vᵢ]` equals the minimum over these in-edges (or 0 if vᵢ is a source) — which is exactly d\*(vᵢ). Relaxing an edge can never push `dist` below the true value, because every value stored is the length of some real path. So a single pass suffices and each edge is relaxed exactly once.

**Why negative weights are safe in a DAG but not in a general graph.** The proof above never uses the sign of a weight. It uses only that every path to vᵢ goes through earlier vertices, which holds because there are no cycles. In a general graph with a negative cycle there is no shortest path at all (each lap makes the walk shorter), and even without negative cycles Dijkstra's argument ("the closest unsettled vertex cannot be improved later") fails, because a later negative edge can improve it. A DAG has neither problem: no cycles, and the settling order is the topological one, not "closest first". The tests compare against Bellman-Ford on 200 random DAGs with weights in [−10, 10].

## 4. Which algorithm when

| Situation | Choice | Why |
|---|---|---|
| MST, **sparse** graph (E ≈ 4V), edges already in a list | Kruskal | Sort is O(E log E) = O(V log V) here; in our data it examines 77–100 % of the edges, but the DSU is cheap and there is no heap. Counted operations are 0.55× Prim's. |
| MST, **dense** graph (E ≈ 20V) | Kruskal with early exit (or Prim with a better heap) | Kruskal examines only 21–37 % of edges, so most of the edge list is never touched; but it sorts everything first (≈ 0.93 E log₂ E comparisons). On the counters Kruskal is ≈ 2× cheaper than lazy Prim here. For **very** dense graphs (E ≈ V²) an array-based Prim in O(V²) would beat both. |
| Graph stored as an **adjacency list** | Prim, or any traversal-based algorithm | Neighbours are available directly; Kruskal needs an edge list (here we keep both). |
| Graph stored as an **edge list** | Kruskal | No adjacency structure to build; needs only sort + DSU. |
| Edges arrive over time / dynamic connectivity | DSU alone | α(n) per operation, supports "are x and y connected?" online. |
| Graph **may contain cycles**, need ordering of dependent jobs | Kahn | O(V + E) and it *reports* a cycle (fewer than n vertices output) instead of a wrong order. |
| Find deadlocks (groups of mutually dependent jobs) | Kosaraju or Tarjan | Components of size > 1 are the deadlocks; Tarjan does it in half the scans (section 7). |
| Fastest route through a pipeline, graph is **acyclic** | DAG shortest path | One O(V + E) pass, negative weights allowed. |
| Shortest paths, graph **has cycles**, weights ≥ 0 | Dijkstra (not implemented here) | The DAG pass is not valid when a cycle exists. |
| Shortest paths, cycles **and** negative weights | Bellman-Ford (not implemented here) | O(VE), detects negative cycles. |

## 5. Slope check (log-log, n → counter)

Because m = 4n or 20n, n and m grow together, so the slope of a log-log line of a counter against n estimates the exponent of the work in E.

| Algorithm | density | slope of edgeScans | slope of comparisons | slope of total ops |
|---|---|---|---|---|
| Kosaraju | sparse / dense | **1.000** / **1.000** | – | 1.000 / 1.000 |
| Kahn | sparse / dense | **1.000** / **1.000** | – | 1.000 / 1.000 |
| DAG shortest path | sparse / dense | **1.000** / **1.000** | 1.000 / 1.000 | 1.000 / 1.000 |
| Prim | sparse / dense | **1.000** / **1.000** | 1.109 / 1.090 | 1.102 / 1.085 |
| Kruskal | sparse / dense | 1.055 / 1.119 | 1.105 / 1.087 | 1.096 / 1.088 |

All traversals have slope 1.000. The slope of the *comparisons* of Kruskal and Prim is ≈ 1.09–1.11, which is the E log E factor: d ln(E ln E)/d ln E = 1 + 1/ln E, which is about 1.12 at E = 4 000 and 1.07 at E = 2·10⁶, so a measured 1.09–1.11 is what an E log E algorithm should show.

Kruskal's **edgeScans** slope (1.055 sparse, 1.119 dense) has a different source than the sort. Kruskal examines edges only until the graph is connected, and the number of edges needed for that grows faster than n (a random graph needs about ½ n ln n edges to become connected). So the examined *share* of the edge list grows with n (plot 3): 0.77 → 0.999 for sparse, where it saturates at 1, and 0.215 → 0.372 for dense. I report both numbers as asked: 1.00 for the traversals, 1.06–1.12 for Kruskal.

## 6. Plots

1. `results/plots/operations_vs_n.png` — counted operations (dsuOps + edgeScans + comparisons) against n, log-log, one line per algorithm and density. All lines are nearly parallel straight lines; Kruskal and Prim are slightly steeper. Kosaraju lies exactly under DAG shortest path (both 2m).
2. `results/plots/scans_per_edge.png` — edgeScans / m against n. Flat lines: 2.0 for Kosaraju and Prim (two passes; two arcs per undirected edge), 1.0 for Kahn and DAG shortest path. Kruskal's curve is the only one that moves.
3. `results/plots/kruskal_edge_share.png` — share of edges Kruskal examines. On sparse graphs it reaches ≈ 100 % (it needs almost every edge), on dense graphs only 21–37 %. This is where each MST algorithm wins: Kruskal's early exit pays off exactly when there are many more edges than the tree needs; on a sparse graph both algorithms look at everything.

![operations](results/plots/operations_vs_n.png)
![scans per edge](results/plots/scans_per_edge.png)
![Kruskal share](results/plots/kruskal_edge_share.png)

## 7. Bonus tasks

**Tarjan (Task A).** `Tarjan` returns the same partition as Kosaraju on every graph of the benchmark (checked inside the benchmark and by `SccTest` against brute force on 150 random graphs). Edge scans: Kosaraju 2m, Tarjan m — for example 800 000 vs 400 000 at n = 100 000 sparse and 4 000 000 vs 2 000 000 at n = 100 000 dense. The saving comes from doing everything in one DFS: Kosaraju needs a first pass only to compute finish times and then a second pass over the reversed graph (which also has to be built), while Tarjan recognises a component root (`low[u] == index[u]`) during the first pass. The explicit-stack depth is identical (59 397 and 91 705 at n = 100 000), since both do the same DFS. Median times at n = 100 000: Tarjan 28 / 272 ms against Kosaraju 55 / 640 ms (sparse / dense).

**Condensation and critical path (Task B).** `CriticalPath` contracts every SCC into one vertex (the result is always a DAG — if two components could reach each other they would be one component), negates the weights and runs Kahn + the DAG relaxation; the longest path is minus the smallest distance. The longest path is easy in a DAG because the optimal-substructure argument of section 3 works for any weight sign, so maximising is minimising with flipped signs. In a general graph a cycle with positive weight makes walks unbounded, and restricting to *simple* paths makes the problem NP-hard (it contains Hamiltonian path), so the sign flip produces negative cycles and no efficient method is known. Critical paths are the basis of CPM/PERT project scheduling: tasks are vertices, dependencies are edges, the longest path is the minimum project duration, and tasks on it have zero slack. In our data the sparse directed graphs have 27, 137, 373, 1 188 and 3 923 components with critical path lengths 3 053 (n = 1 000) and 4 977 (n = 100 000), and the dense directed graphs are a single SCC, so their condensation has one vertex and the critical path is 0 (`results/results_bonus.csv`).

## 8. Time

Median of 5 runs is in `results.csv`; at n = 100 000 Kruskal took 63 ms (sparse) / 329 ms (dense), Prim 540 / 3 417 ms, Kosaraju 55 / 640 ms, Kahn 11 / 507 ms, DAG shortest path 14 / 346 ms. These numbers depend on the laptop, the JVM version and the background load, and they include JIT warm-up (the two smallest graphs are mostly noise). Two runs of the same benchmark on the same machine already differed: Kosaraju on the sparse n = 100 000 graph took 72 ms in one run and 55 ms in the next, while every counter was identical. They also exaggerate differences that the counters do not show: Prim is about 10 times slower than Kruskal on the dense graph (3 417 against 329 ms), but the counted operations differ by only 2× (83.2 M against 41.8 M); the rest is the cost of boxing in `PriorityQueue<Long>` and heap cache misses, which a counter does not see. That is the reason the conclusions in this report come from the counters and their ratios, not from the clock.

## 9. Notes and limitations

* `dsuOps` is 0 for the algorithms that do not use the DSU, `comparisons` is 0 for the traversals, and `maxDepth` is 0 for Kruskal, Prim, Kahn and DAG shortest path (they have no DFS stack).
* Kruskal calls `union`, which performs its two `find` calls internally; the DSU counts those finds, so Kruskal's `dsuOps` = 2 × examined edges + successful unions.
* Kahn's `edgeScans` counts the arcs scanned in its main loop; the in-degrees are computed from the edge list before the loop and are not counted (that is another m steps, so the real total is 2m).
* The generator does not specify how to redraw when `b == a`; I redraw only `b` until it differs from `a`, which reproduces all ten reference values.
* The explicit DFS stack on the random directed graphs reached 59–92 % of n (`maxDepth` in the CSV), which is why a recursive DFS would not survive n = 100 000.
