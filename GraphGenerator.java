package daa.benchmark;

import daa.graph.Graph;

import java.util.Random;

/** Exact generator from the assignment: always new Random(42), weights 1 + nextInt(1000). */
public final class GraphGenerator {
    public static final long SEED = 42;
    private GraphGenerator() {}

    private static int[] shuffled(int n, Random rnd) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = p[i]; p[i] = p[j]; p[j] = t;
        }
        return p;
    }

    /** Connected undirected graph: random spanning tree (n-1 edges) + random edges up to m. */
    public static Graph undirected(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, false);
        int[] perm = shuffled(n, rnd);
        for (int i = 1; i < n; i++) {
            int u = perm[rnd.nextInt(i)];
            g.addEdge(u, perm[i], 1 + rnd.nextInt(1000));
        }
        for (int e = n - 1; e < m; e++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            while (b == a) b = rnd.nextInt(n);
            g.addEdge(a, b, 1 + rnd.nextInt(1000));
        }
        return g;
    }

    /** Directed graph: only random edges (b != a), no spanning tree. */
    public static Graph directed(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, true);
        for (int e = 0; e < m; e++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            while (b == a) b = rnd.nextInt(n);
            g.addEdge(a, b, 1 + rnd.nextInt(1000));
        }
        return g;
    }

    /** DAG: random rank shuffle, every random edge is oriented from lower to higher rank. */
    public static Graph dag(int n, int m) {
        Random rnd = new Random(SEED);
        int[] rank = shuffled(n, rnd);
        Graph g = new Graph(n, true);
        for (int e = 0; e < m; e++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            while (b == a) b = rnd.nextInt(n);
            if (rank[a] > rank[b]) { int t = a; a = b; b = t; }
            g.addEdge(a, b, 1 + rnd.nextInt(1000));
        }
        return g;
    }
}
