package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

/** Kruskal: sort edges by weight (own merge sort, comparisons counted), DSU for cycle check. */
public final class Kruskal {
    private Kruskal() {}

    public static MstResult run(Graph g) {
        Metrics mt = new Metrics();
        int n = g.n(), m = g.m();
        int[] idx = new int[m];
        for (int i = 0; i < m; i++) idx[i] = i;
        sortByWeight(g, idx, mt);

        DSU dsu = new DSU(n);
        long total = 0;
        for (int k = 0; k < m && dsu.components() > 1; k++) {
            int e = idx[k];
            mt.edgeScans++;
            if (dsu.union(g.edgeU(e), g.edgeV(e))) total += g.edgeW(e);
        }
        mt.dsuOps = dsu.ops();
        return new MstResult(total, n - dsu.components(), dsu.components(), mt);
    }

    /** Stable bottom-up merge sort of edge indices by weight. */
    private static void sortByWeight(Graph g, int[] a, Metrics mt) {
        int len = a.length;
        int[] src = a, dst = new int[len];
        long cmp = 0;
        for (int width = 1; width < len; width *= 2) {
            for (int lo = 0; lo < len; lo += 2 * width) {
                int mid = Math.min(lo + width, len), hi = Math.min(lo + 2 * width, len);
                int i = lo, j = mid, k = lo;
                while (i < mid && j < hi) {
                    cmp++;
                    if (g.edgeW(src[j]) < g.edgeW(src[i])) dst[k++] = src[j++];
                    else dst[k++] = src[i++];
                }
                while (i < mid) dst[k++] = src[i++];
                while (j < hi) dst[k++] = src[j++];
            }
            int[] t = src; src = dst; dst = t;
        }
        if (src != a) System.arraycopy(src, 0, a, 0, len);
        mt.comparisons += cmp;
    }
}
