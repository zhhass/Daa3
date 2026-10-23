package daa.algorithms;

import daa.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Bonus B: condensation graph (every SCC becomes one vertex, always a DAG) and the longest
 * path in it, found by shortest-path relaxation in topological order with the sign of the
 * weights flipped.
 */
public final class CriticalPath {
    public final Graph condensation;      // directed DAG on scc.count vertices
    public final long length;             // length of the longest path (sum of edge weights)
    public final List<Integer> path;      // component ids along the critical path
    public final long edgeScans;

    private CriticalPath(Graph c, long length, List<Integer> path, long edgeScans) {
        this.condensation = c; this.length = length; this.path = path; this.edgeScans = edgeScans;
    }

    public static Graph condense(Graph g, SccResult scc, boolean negateWeights) {
        Graph c = new Graph(scc.count, true);
        for (int e = 0; e < g.m(); e++) {
            int a = scc.comp[g.edgeU(e)], b = scc.comp[g.edgeV(e)];
            if (a != b) c.addEdge(a, b, negateWeights ? -g.edgeW(e) : g.edgeW(e));
        }
        return c;
    }

    public static CriticalPath run(Graph g, SccResult scc) {
        Graph neg = condense(g, scc, true);               // flipped sign: longest path = shortest path of -w
        Kahn.Result k = Kahn.run(neg);
        if (k.hasCycle) throw new IllegalStateException("condensation must be acyclic");
        DagShortestPath sp = DagShortestPath.run(neg, k.order);
        int best = 0;
        for (int v = 1; v < neg.n(); v++) if (sp.dist[v] < sp.dist[best]) best = v;
        if (neg.n() == 0) return new CriticalPath(neg, 0, new ArrayList<>(), 0);
        return new CriticalPath(condense(g, scc, false), -sp.dist[best], sp.pathTo(best),
                k.metrics.edgeScans + sp.metrics.edgeScans);
    }
}
