package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.Arrays;

/** Bonus A: Tarjan's SCC, one iterative DFS pass with index[], lowlink[] and a stack of active vertices. */
public final class Tarjan {
    private Tarjan() {}

    public static SccResult run(Graph g) {
        Metrics mt = new Metrics();
        int n = g.n();
        int[] index = new int[n], low = new int[n], it = new int[n], comp = new int[n];
        Arrays.fill(index, -1);
        Arrays.fill(comp, -1);
        boolean[] onStack = new boolean[n];
        int[] active = new int[n];   // Tarjan's stack
        int[] call = new int[n];     // explicit DFS (call) stack
        int ap = 0, idx = 0, count = 0;

        for (int s = 0; s < n; s++) {
            if (index[s] != -1) continue;
            int cp = 0;
            index[s] = low[s] = idx++; it[s] = g.head(s);
            active[ap++] = s; onStack[s] = true;
            call[cp++] = s;
            mt.depth(cp);
            while (cp > 0) {
                int u = call[cp - 1];
                int a = it[u];
                if (a != -1) {
                    it[u] = g.next(a);
                    mt.edgeScans++;
                    int v = g.to(a);
                    if (index[v] == -1) {
                        index[v] = low[v] = idx++; it[v] = g.head(v);
                        active[ap++] = v; onStack[v] = true;
                        call[cp++] = v;
                        mt.depth(cp);
                    } else if (onStack[v]) {
                        low[u] = Math.min(low[u], index[v]);
                    }
                } else {
                    if (low[u] == index[u]) {          // u is the root of an SCC
                        int w;
                        do {
                            w = active[--ap];
                            onStack[w] = false;
                            comp[w] = count;
                        } while (w != u);
                        count++;
                    }
                    cp--;
                    if (cp > 0) {
                        int p = call[cp - 1];
                        low[p] = Math.min(low[p], low[u]);
                    }
                }
            }
        }
        return new SccResult(comp, count, mt);
    }
}
