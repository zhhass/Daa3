package daa;

import daa.algorithms.*;
import daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MstTest {
    @Test void kruskalAndPrimAgreeOn100RandomConnectedGraphs() {
        Random r = new Random(7);
        for (int t = 0; t < 100; t++) {
            int n = 2 + r.nextInt(60);
            Graph g = TestGraphs.randomConnected(n, r.nextInt(150), r);
            MstResult k = Kruskal.run(g), p = Prim.run(g);
            assertEquals(k.totalWeight, p.totalWeight, "graph #" + t);
            assertEquals(n - 1, k.treeEdges);
            assertEquals(n - 1, p.treeEdges);
            assertEquals(1, k.components);
            assertEquals(1, p.components);
        }
    }

    @Test void matchesBruteForceOnTinyGraphs() {
        Random r = new Random(11);
        for (int t = 0; t < 60; t++) {
            int n = 2 + r.nextInt(5);
            Graph g = TestGraphs.randomConnected(n, r.nextInt(6), r);
            assertEquals(bruteForceMst(g), Kruskal.run(g).totalWeight);
        }
    }

    /** Try every subset of n-1 edges, keep the cheapest one that connects everything. */
    static long bruteForceMst(Graph g) {
        int n = g.n(), m = g.m();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != n - 1) continue;
            DSU d = new DSU(n);
            long w = 0;
            for (int e = 0; e < m; e++) if ((mask >> e & 1) == 1) { d.union(g.edgeU(e), g.edgeV(e)); w += g.edgeW(e); }
            if (d.components() == 1) best = Math.min(best, w);
        }
        return best;
    }

    @Test void disconnectedGraphGivesForestNotException() throws Exception {
        Graph g = Graph.read(Path.of("data/city_disconnected.txt"));
        MstResult k = Kruskal.run(g), p = Prim.run(g);
        assertEquals(4, k.components);       // {0,1,2}, {3,4}, {5}, {6}
        assertEquals(4, p.components);
        assertEquals(3, k.treeEdges);        // n - components
        assertEquals(3, p.treeEdges);
        assertEquals(3 + 4 + 2, k.totalWeight);
        assertEquals(k.totalWeight, p.totalWeight);
    }

    @Test void selfLoopsAndParallelEdges() throws Exception {
        Graph g = Graph.read(Path.of("data/city_connected.txt"));
        assertEquals(1 + 2 + 3 + 2 + 6, Kruskal.run(g).totalWeight);
        assertEquals(14, Prim.run(g).totalWeight);
    }

    @Test void kruskalStopsEarlyOnDenseGraphs() {
        Graph g = TestGraphs.randomConnected(200, 5000, new Random(3));
        MstResult k = Kruskal.run(g);
        assertTrue(k.metrics.edgeScans < g.m());
        assertTrue(k.metrics.dsuOps > 0 && k.metrics.comparisons > 0);
    }
}
