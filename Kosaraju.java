package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

/**
 * Kosaraju: pass 1 on G gives the finish order, pass 2 on the reversed graph in decreasing
 * finish order; each tree of pass 2 is one SCC. Both passes use an explicit stack
 * (no recursion), so 100 000 vertices on a path are fine.
 */
public final class Kosaraju {
    private Kosaraju() {}

    public static SccResult run(Graph g) {
        Metrics mt = new Metrics();
        int n = g.n();

        // ---- pass 1: iterative DFS on G, record finish order
        boolean[] seen = new boolean[n];
        int[] finish = new int[n];
        int fc = 0;
        int[] stack = new int[n];
        int[] it = new int[n];               // next unexplored arc of each vertex
        for (int s = 0; s < n; s++) {
            if (seen[s]) continue;
            int sp = 0;
            stack[sp++] = s; seen[s] = true; it[s] = g.head(s);
            mt.depth(sp);
            while (sp > 0) {
                int u = stack[sp - 1];
                int a = it[u];
                if (a != -1) {
                    it[u] = g.next(a);
                    mt.edgeScans++;
                    int v = g.to(a);
                    if (!seen[v]) {
                        seen[v] = true; it[v] = g.head(v);
                        stack[sp++] = v;
                        mt.depth(sp);
                    }
                } else {
                    sp--;
                    finish[fc++] = u;
                }
            }
        }

        // ---- pass 2: DFS on the reversed graph in decreasing finish order
        Graph r = g.reversed();
        int[] comp = new int[n];
        java.util.Arrays.fill(comp, -1);
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = finish[i];
            if (comp[s] != -1) continue;
            int sp = 0;
            stack[sp++] = s; comp[s] = count;
            mt.depth(sp);
            while (sp > 0) {
                int u = stack[--sp];
                for (int a = r.head(u); a != -1; a = r.next(a)) {
                    mt.edgeScans++;
                    int v = r.to(a);
                    if (comp[v] == -1) {
                        comp[v] = count;
                        stack[sp++] = v;
                        mt.depth(sp);
                    }
                }
            }
            count++;
        }
        return new SccResult(comp, count, mt);
    }
}
