package daa.benchmark;

import daa.algorithms.*;
import daa.graph.Graph;
import daa.metrics.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Runs the 5 algorithms on 10 graphs (5 sizes x 2 densities), checks everything against the
 * reference table and only then writes results/results.csv (50 rows). A wrong number stops
 * the run with exit code 1 and nothing is written.
 *
 * Usage: java daa.benchmark.Benchmark [outDir]   (default: results)
 */
public final class Benchmark {
    static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    static final int[] FACTORS = {4, 20};                 // sparse m = 4n, dense m = 20n
    static final String[] DENSITY = {"sparse", "dense"};
    static final int RUNS = 5;

    // Reference values from the assignment: per size, sparse then dense
    static final long[] REF_MST = {147970, 30363, 468911, 97589, 1486139, 309550, 4715515, 968517, 15012094, 3056681};
    static final long[] REF_SCC = {27, 1, 137, 1, 373, 1, 1188, 1, 3923, 1};
    static final long[] REF_DAG = {396274, 252411, 1271911, 723157, 3941828, 2381342, 12551891, 7376612, 40096539, 23512087};

    static final String HEADER = "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";

    private static void fail(String msg) {
        System.err.println("BENCHMARK CHECK FAILED: " + msg);
        System.exit(1);
    }

    private static void check(boolean ok, String msg) { if (!ok) fail(msg); }

    /** Median wall-clock time of RUNS runs; the metrics of the last run are returned. */
    private static <T> Object[] timed(Supplier<T> run) {
        double[] t = new double[RUNS];
        T res = null;
        for (int i = 0; i < RUNS; i++) {
            long s = System.nanoTime();
            res = run.get();
            t[i] = (System.nanoTime() - s) / 1e6;
        }
        Arrays.sort(t);
        return new Object[]{res, t[RUNS / 2]};
    }

    private static String row(String alg, String graph, int n, int m, double ms, Metrics mt) {
        return String.format(java.util.Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                alg, graph, n, m, ms, mt.dsuOps, mt.edgeScans, mt.comparisons, mt.maxDepth, mt.result);
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results");
        Files.createDirectories(out);
        List<String> rows = new ArrayList<>();
        List<String> bonus = new ArrayList<>();

        for (int si = 0; si < SIZES.length; si++) {
            for (int di = 0; di < 2; di++) {
                int k = si * 2 + di;
                int n = SIZES[si], m = FACTORS[di] * n;
                String name = "n" + n + "_" + DENSITY[di];
                System.out.printf("== %s (n=%d, m=%d)%n", name, n, m);

                Graph und = GraphGenerator.undirected(n, m);
                Graph dir = GraphGenerator.directed(n, m);
                Graph dag = GraphGenerator.dag(n, m);

                // ---- MST
                Object[] kr = timed(() -> Kruskal.run(und));
                Object[] pr = timed(() -> Prim.run(und));
                MstResult K = (MstResult) kr[0], P = (MstResult) pr[0];
                check(K.totalWeight == P.totalWeight, name + ": Kruskal " + K.totalWeight + " != Prim " + P.totalWeight);
                check(K.totalWeight == REF_MST[k], name + ": MST " + K.totalWeight + " != reference " + REF_MST[k]);
                check(K.treeEdges == n - 1 && P.treeEdges == n - 1, name + ": tree must have n-1 edges");
                check(K.components == 1 && P.components == 1, name + ": generated graph must be connected");
                rows.add(row("Kruskal", name, n, m, (double) kr[1], K.metrics));
                rows.add(row("Prim", name, n, m, (double) pr[1], P.metrics));

                // ---- SCC
                Object[] ko = timed(() -> Kosaraju.run(dir));
                SccResult S = (SccResult) ko[0];
                check(S.count == REF_SCC[k], name + ": SCC count " + S.count + " != reference " + REF_SCC[k]);
                check(S.metrics.edgeScans == 2L * m, name + ": Kosaraju must scan exactly 2m edges, got " + S.metrics.edgeScans);
                rows.add(row("Kosaraju", name, n, m, (double) ko[1], S.metrics));

                // ---- Kahn + DAG shortest paths
                Object[] ka = timed(() -> Kahn.run(dag));
                Kahn.Result T = (Kahn.Result) ka[0];
                check(!T.hasCycle && T.size == n, name + ": the DAG must be fully ordered");
                check(Kahn.isValidOrder(dag, T.order), name + ": topological order is invalid");
                check(T.metrics.edgeScans == m, name + ": Kahn must scan exactly m edges, got " + T.metrics.edgeScans);
                rows.add(row("Kahn", name, n, m, (double) ka[1], T.metrics));

                Object[] ds = timed(() -> DagShortestPath.run(dag, T.order));
                DagShortestPath D = (DagShortestPath) ds[0];
                check(D.sumOfDistances == REF_DAG[k], name + ": DAG distance sum " + D.sumOfDistances + " != reference " + REF_DAG[k]);
                check(D.metrics.edgeScans == m, name + ": DAG relaxation must scan exactly m edges, got " + D.metrics.edgeScans);
                rows.add(row("DagShortestPath", name, n, m, (double) ds[1], D.metrics));

                // ---- bonus: Tarjan (must give the same partition as Kosaraju) and condensation critical path
                Object[] ta = timed(() -> Tarjan.run(dir));
                SccResult Tj = (SccResult) ta[0];
                check(Tj.count == S.count && samePartition(S.comp, Tj.comp), name + ": Tarjan != Kosaraju");
                check(Tj.metrics.edgeScans == m, name + ": Tarjan must scan exactly m edges, got " + Tj.metrics.edgeScans);
                bonus.add(row("Tarjan", name, n, m, (double) ta[1], Tj.metrics));

                Object[] cp = timed(() -> CriticalPath.run(dir, S));
                CriticalPath C = (CriticalPath) cp[0];
                Metrics cm = new Metrics();
                cm.edgeScans = C.edgeScans;
                cm.result = C.length;
                bonus.add(row("CriticalPath", name, n, m, (double) cp[1], cm));

                System.out.printf("   MST=%d  SCC=%d  DAGsum=%d  | Kruskal scans/m=%.3f  Kosaraju maxDepth=%d%n",
                        K.totalWeight, S.count, D.sumOfDistances, (double) K.metrics.edgeScans / m, S.metrics.maxDepth);
            }
        }
        check(rows.size() == 50, "expected 50 rows, got " + rows.size());
        write(out.resolve("results.csv"), rows);
        write(out.resolve("results_bonus.csv"), bonus);
        System.out.println("All reference values reproduced. Wrote " + out.resolve("results.csv") + " (50 rows) and results_bonus.csv");
    }

    private static void write(Path p, List<String> rows) throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(p))) {
            w.println(HEADER);
            rows.forEach(w::println);
        }
    }

    /** Same partition of the vertices, ignoring the component numbering. */
    static boolean samePartition(int[] a, int[] b) {
        int[] map = new int[a.length], back = new int[a.length];
        Arrays.fill(map, -1);
        Arrays.fill(back, -1);
        for (int i = 0; i < a.length; i++) {
            if (map[a[i]] == -1 && back[b[i]] == -1) { map[a[i]] = b[i]; back[b[i]] = a[i]; }
            else if (map[a[i]] != b[i] || back[b[i]] != a[i]) return false;
        }
        return true;
    }
}
