package daa;

import daa.graph.Graph;

import java.util.Random;

/** Helpers shared by the tests. */
final class TestGraphs {
    private TestGraphs() {}

    static Graph randomDirected(int n, int m, Random r, int minW, int maxW) {
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) g.addEdge(r.nextInt(n), r.nextInt(n), minW + r.nextInt(maxW - minW + 1));
        return g;
    }

    /** Random DAG: edges go from lower to higher position of a random permutation. */
    static Graph randomDag(int n, int m, Random r, int minW, int maxW) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = i;
        for (int i = n - 1; i > 0; i--) { int j = r.nextInt(i + 1); int t = rank[i]; rank[i] = rank[j]; rank[j] = t; }
        Graph g = new Graph(n, true);
        for (int i = 0; i < m && n > 1; i++) {
            int a = r.nextInt(n), b = r.nextInt(n);
            if (a == b) continue;
            if (rank[a] > rank[b]) { int t = a; a = b; b = t; }
            g.addEdge(a, b, minW + r.nextInt(maxW - minW + 1));
        }
        return g;
    }

    /** Connected undirected graph with random weights (random tree + extra edges, self-loops and parallels allowed). */
    static Graph randomConnected(int n, int extra, Random r) {
        Graph g = new Graph(n, false);
        for (int i = 1; i < n; i++) g.addEdge(r.nextInt(i), i, 1 + r.nextInt(20));
        for (int i = 0; i < extra; i++) g.addEdge(r.nextInt(n), r.nextInt(n), 1 + r.nextInt(20));
        return g;
    }

    static Graph path(int n) {
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) g.addEdge(i, i + 1, 1);
        return g;
    }
}
