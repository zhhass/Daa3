package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shortest paths in a DAG: one pass over a topological order, relaxing the outgoing edges of
 * every vertex, O(V + E). Every vertex with in-degree 0 starts at distance 0 (virtual
 * super-source), so every vertex gets a distance. Negative weights are fine in a DAG.
 */
public final class DagShortestPath {
    public static final long INF = Long.MAX_VALUE / 4;

    public final long[] dist;
    public final int[] parent;      // -1 for sources
    public final long sumOfDistances;
    public final Metrics metrics;

    private DagShortestPath(long[] dist, int[] parent, long sum, Metrics metrics) {
        this.dist = dist;
        this.parent = parent;
        this.sumOfDistances = sum;
        this.metrics = metrics;
        metrics.result = sum;
    }

    public static DagShortestPath run(Graph g, int[] topoOrder) {
        Metrics mt = new Metrics();
        int n = g.n();
        long[] dist = new long[n];
        int[] parent = new int[n];
        java.util.Arrays.fill(dist, INF);
        java.util.Arrays.fill(parent, -1);
        int[] indeg = g.inDegrees();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) dist[v] = 0;   // super-source -> every source

        for (int i = 0; i < n; i++) {
            int u = topoOrder[i];
            if (dist[u] == INF) continue;     // cannot happen in a DAG, kept for safety
            for (int a = g.head(u); a != -1; a = g.next(a)) {
                mt.edgeScans++;
                int v = g.to(a);
                long nd = dist[u] + g.weight(a);
                mt.comparisons++;
                if (nd < dist[v]) { dist[v] = nd; parent[v] = u; }
            }
        }
        long sum = 0;
        for (int v = 0; v < n; v++) sum += dist[v];
        return new DagShortestPath(dist, parent, sum, mt);
    }

    /** Runs Kahn first; throws IllegalArgumentException if the graph has a cycle. */
    public static DagShortestPath run(Graph g) {
        Kahn.Result k = Kahn.run(g);
        if (k.hasCycle) throw new IllegalArgumentException("not a DAG");
        return run(g, k.order);
    }

    /** Vertices of a shortest path from its source to v. */
    public List<Integer> pathTo(int v) {
        List<Integer> p = new ArrayList<>();
        for (int x = v; x != -1; x = parent[x]) p.add(x);
        Collections.reverse(p);
        return p;
    }
}
