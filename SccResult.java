package daa.algorithms;

import daa.metrics.Metrics;

/** Strongly connected components: comp[v] is the component id of v (0..count-1). */
public final class SccResult {
    public final int[] comp;
    public final int count;
    public final Metrics metrics;

    SccResult(int[] comp, int count, Metrics metrics) {
        this.comp = comp;
        this.count = count;
        this.metrics = metrics;
        metrics.result = count;
    }
}
