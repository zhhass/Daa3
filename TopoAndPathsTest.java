package daa;

import daa.algorithms.*;
import daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TopoAndPathsTest {
    @Test void validOrderOnRandomDags() {
        Random r = new Random(21);
        for (int t = 0; t < 100; t++) {
            int n = 1 + r.nextInt(150);
            Graph g = TestGraphs.randomDag(n, r.nextInt(4 * n), r, 1, 9);
            Kahn.Result k = Kahn.run(g);
            assertFalse(k.hasCycle);
            assertEquals(n, k.size);
            assertTrue(Kahn.isValidOrder(g, k.requireOrder()));
        }
    }

    @Test void cycleIsDetectedNotHidden() throws Exception {
        Graph g = Graph.read(Path.of("data/jobs_cyclic.txt"));
        Kahn.Result k = Kahn.run(g);
        assertTrue(k.hasCycle);
        assertTrue(k.size < g.n());
        assertThrows(IllegalStateException.class, k::requireOrder);
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(g));
        Graph self = new Graph(2, true);
        self.addEdge(1, 1, 1);                       // a self-loop is a cycle too
        assertTrue(Kahn.run(self).hasCycle);
    }

    @Test void validatorRejectsWrongOrders() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1); g.addEdge(1, 2, 1);
        assertTrue(Kahn.isValidOrder(g, new int[]{0, 1, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{2, 1, 0}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1, 1}));
    }

    /** Bellman-Ford with the same virtual super-source: every in-degree-0 vertex starts at 0. */
    static long[] bellmanFord(Graph g) {
        int n = g.n();
        long[] d = new long[n];
        Arrays.fill(d, DagShortestPath.INF);
        int[] indeg = g.inDegrees();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) d[v] = 0;
        for (int round = 0; round < n; round++) {
            boolean changed = false;
            for (int e = 0; e < g.m(); e++) {
                int u = g.edgeU(e), v = g.edgeV(e);
                if (d[u] != DagShortestPath.INF && d[u] + g.edgeW(e) < d[v]) { d[v] = d[u] + g.edgeW(e); changed = true; }
            }
            if (!changed) break;
        }
        return d;
    }

    @Test void matchesBellmanFordIncludingNegativeWeights() {
        Random r = new Random(33);
        for (int t = 0; t < 200; t++) {
            int n = 1 + r.nextInt(40);
            Graph g = TestGraphs.randomDag(n, r.nextInt(5 * n), r, -10, 10);
            DagShortestPath sp = DagShortestPath.run(g);
            assertArrayEquals(bellmanFord(g), sp.dist, "graph #" + t);
            assertEquals(g.m(), sp.metrics.edgeScans);
        }
    }

    @Test void pathRestoringOnJobsDag() throws Exception {
        Graph g = Graph.read(Path.of("data/jobs_dag.txt"));
        DagShortestPath sp = DagShortestPath.run(g);
        assertEquals(0, sp.dist[0]); assertEquals(0, sp.dist[1]);   // both sources start at 0
        assertEquals(1, sp.dist[3]);                                // 1 -> 2 (3) -> 3 (-2)
        assertEquals(List.of(1, 2, 3), sp.pathTo(3));
        assertEquals(1, sp.dist[5]);                                // 1 -> 4 (2) -> 5 (-1) beats 1 -> 2 -> 3 -> 5 = 7
        assertEquals(List.of(1, 4, 5), sp.pathTo(5));
        assertEquals(4, sp.dist[6]);                                // 1 -> 4 -> 5 -> 6
    }

    @Test void pathsAreConsistentWithDistances() {
        Random r = new Random(44);
        Graph g = TestGraphs.randomDag(60, 200, r, -5, 20);
        DagShortestPath sp = DagShortestPath.run(g);
        for (int v = 0; v < g.n(); v++) {
            List<Integer> p = sp.pathTo(v);
            assertEquals(v, p.get(p.size() - 1));
            assertEquals(0, sp.dist[p.get(0)]);                    // path starts at a source
        }
    }
}
