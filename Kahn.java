package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

/** Kahn's topological sort: in-degrees + queue. Reports a cycle instead of returning a wrong order. */
public final class Kahn {
    private Kahn() {}

    public static final class Result {
        public final int[] order;        // first `size` entries are valid
        public final int size;           // vertices that left the queue
        public final boolean hasCycle;   // size < n
        public final Metrics metrics;

        Result(int[] order, int size, boolean hasCycle, Metrics metrics) {
            this.order = order;
            this.size = size;
            this.hasCycle = hasCycle;
            this.metrics = metrics;
            metrics.result = size;
        }

        /** The complete order; throws if the graph has a cycle. */
        public int[] requireOrder() {
            if (hasCycle) throw new IllegalStateException("graph has a cycle: only " + size + " vertices ordered");
            return order;
        }
    }

    public static Result run(Graph g) {
        if (!g.isDirected()) throw new IllegalArgumentException("Kahn needs a directed graph");
        Metrics mt = new Metrics();
        int n = g.n();
        int[] indeg = g.inDegrees();
        int[] order = new int[n];          // doubles as the queue: head = processed, tail = size
        int tail = 0, head = 0;
        for (int v = 0; v < n; v++) if (indeg[v] == 0) order[tail++] = v;
        while (head < tail) {
            int u = order[head++];
            for (int a = g.head(u); a != -1; a = g.next(a)) {
                mt.edgeScans++;
                int v = g.to(a);
                if (--indeg[v] == 0) order[tail++] = v;
            }
        }
        return new Result(order, tail, tail < n, mt);
    }

    /** True iff order has all n vertices exactly once and pos[u] < pos[v] for every edge u -> v. */
    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.n();
        if (order == null || order.length != n) return false;
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || pos[v] != -1) return false;
            pos[v] = i;
        }
        for (int e = 0; e < g.m(); e++) if (pos[g.edgeU(e)] >= pos[g.edgeV(e)]) return false;
        return true;
    }
}
