package daa.metrics;

/** Operation counters. They are incremented inside the algorithms, never computed from a formula. */
public final class Metrics {
    public long dsuOps;       // find calls + successful union calls
    public long edgeScans;    // edges (arcs) looked at
    public long comparisons;  // weights compared
    public long maxDepth;     // deepest explicit stack
    public long result;       // MST weight / #components / #vertices in order / sum of distances

    public void depth(long d) { if (d > maxDepth) maxDepth = d; }

    @Override public String toString() {
        return "dsuOps=" + dsuOps + " edgeScans=" + edgeScans + " comparisons=" + comparisons
                + " maxDepth=" + maxDepth + " result=" + result;
    }
}
