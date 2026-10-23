package daa.algorithms;

import daa.metrics.Metrics;

/** Minimum spanning tree (or forest, if the graph is disconnected). */
public final class MstResult {
    public final long totalWeight;
    public final int treeEdges;     // n - components
    public final int components;
    public final Metrics metrics;

    MstResult(long totalWeight, int treeEdges, int components, Metrics metrics) {
        this.totalWeight = totalWeight;
        this.treeEdges = treeEdges;
        this.components = components;
        this.metrics = metrics;
        metrics.result = totalWeight;
    }
}
