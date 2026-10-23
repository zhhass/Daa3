package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.PriorityQueue;

/**
 * Lazy Prim with a PriorityQueue. Starts at vertex 0; if the graph is disconnected it
 * restarts from the next unvisited vertex, so the result is a minimum spanning forest.
 * Queue entries are packed as (weight << 32 | vertex).
 */
public final class Prim {
    private Prim() {}

    public static MstResult run(Graph g) {
        Metrics mt = new Metrics();
        int n = g.n();
        boolean[] inTree = new boolean[n];
        long[] cmp = new long[1];
        PriorityQueue<Long> pq = new PriorityQueue<>((x, y) -> { cmp[0]++; return Long.compare(x, y); });
        long total = 0;
        int components = 0, added = 0;

        for (int s = 0; s < n; s++) {
            if (inTree[s]) continue;
            components++;
            pq.add((long) s);                  // weight 0
            while (!pq.isEmpty()) {
                long top = pq.poll();
                int u = (int) (top & 0xFFFFFFFFL);
                if (inTree[u]) continue;       // skip a vertex that is already in the tree
                inTree[u] = true;
                total += top >> 32;
                added++;
                for (int a = g.head(u); a != -1; a = g.next(a)) {
                    mt.edgeScans++;
                    int v = g.to(a);
                    if (!inTree[v]) pq.add(((long) g.weight(a) << 32) | v);
                }
            }
        }
        mt.comparisons = cmp[0];
        return new MstResult(total, added - components, components, mt);
    }
}
