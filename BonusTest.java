package daa;

import daa.algorithms.*;
import daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BonusTest {
    @Test void condensationIsAlwaysADag() {
        Random r = new Random(55);
        for (int t = 0; t < 50; t++) {
            int n = 1 + r.nextInt(80);
            Graph g = TestGraphs.randomDirected(n, r.nextInt(3 * n), r, 1, 9);
            SccResult s = Kosaraju.run(g);
            Graph c = CriticalPath.condense(g, s, false);
            assertEquals(s.count, c.n());
            assertFalse(Kahn.run(c).hasCycle);
        }
    }

    @Test void criticalPathOnSmallExample() {
        // components: {0,1} -> {2} -> {3,4}, plus a shortcut {0,1} -> {3,4}
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 1); g.addEdge(1, 0, 1);
        g.addEdge(1, 2, 4);
        g.addEdge(2, 3, 5);
        g.addEdge(3, 4, 1); g.addEdge(4, 3, 1);
        g.addEdge(0, 3, 6);
        CriticalPath cp = CriticalPath.run(g, Kosaraju.run(g));
        assertEquals(9, cp.length);                  // 4 + 5, not the shortcut 6
        assertEquals(3, cp.path.size());
    }

    @Test void longestPathMatchesBruteForceOnRandomDags() {
        Random r = new Random(66);
        for (int t = 0; t < 50; t++) {
            int n = 2 + r.nextInt(8);
            Graph g = TestGraphs.randomDag(n, r.nextInt(3 * n), r, 1, 9);
            SccResult s = Kosaraju.run(g);               // every vertex is its own SCC in a DAG
            assertEquals(n, s.count);
            long[] best = new long[n];
            int[] order = Kahn.run(g).order;
            long expected = 0;
            for (int u : order) for (int a = g.head(u); a != -1; a = g.next(a)) best[g.to(a)] = Math.max(best[g.to(a)], best[u] + g.weight(a));
            for (long b : best) expected = Math.max(expected, b);
            assertEquals(expected, CriticalPath.run(g, s).length);
        }
    }
}
