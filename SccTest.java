package daa;

import daa.algorithms.*;
import daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SccTest {
    /** Brute force: BFS from every vertex along the edges (reach) - u and v are in one SCC iff each reaches the other. */
    static boolean[][] reach(Graph g) {
        int n = g.n();
        boolean[][] r = new boolean[n][n];
        for (int s = 0; s < n; s++) {
            ArrayDeque<Integer> q = new ArrayDeque<>();
            q.add(s); r[s][s] = true;
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int a = g.head(u); a != -1; a = g.next(a)) if (!r[s][g.to(a)]) { r[s][g.to(a)] = true; q.add(g.to(a)); }
            }
        }
        return r;
    }

    static void assertMatchesBruteForce(Graph g, SccResult scc) {
        boolean[][] r = reach(g);
        int n = g.n();
        for (int u = 0; u < n; u++) for (int v = 0; v < n; v++)
            assertEquals(r[u][v] && r[v][u], scc.comp[u] == scc.comp[v], "u=" + u + " v=" + v);
    }

    @Test void kosarajuAndTarjanMatchBruteForce() {
        Random rnd = new Random(5);
        for (int t = 0; t < 150; t++) {
            int n = 1 + rnd.nextInt(200);
            Graph g = TestGraphs.randomDirected(n, rnd.nextInt(3 * n + 1), rnd, 1, 9);
            SccResult k = Kosaraju.run(g), tj = Tarjan.run(g);
            assertMatchesBruteForce(g, k);
            assertMatchesBruteForce(g, tj);
            assertEquals(k.count, tj.count);
        }
    }

    @Test void cyclicFileHasExpectedComponents() throws Exception {
        Graph g = Graph.read(Path.of("data/jobs_cyclic.txt"));
        SccResult s = Kosaraju.run(g);
        assertEquals(4, s.count);            // {0}, {1,2,3}, {4,5}, {6}
        assertEquals(s.comp[1], s.comp[3]);
        assertEquals(s.comp[4], s.comp[5]);
        assertNotEquals(s.comp[0], s.comp[6]);
    }

    @Test void edgeScansAreExactlyTwoMAndM() {
        Graph g = TestGraphs.randomDirected(500, 2000, new Random(9), 1, 9);
        assertEquals(2L * g.m(), Kosaraju.run(g).metrics.edgeScans);
        assertEquals(g.m(), Tarjan.run(g).metrics.edgeScans);
    }

    @Test void stackSafetyOnPathOf100000Vertices() {
        int n = 100_000;
        Graph forward = TestGraphs.path(n);
        SccResult k = assertDoesNotThrow(() -> Kosaraju.run(forward));
        assertEquals(n, k.count);
        assertEquals(n, k.metrics.maxDepth);          // the explicit stack really went n deep
        assertEquals(n, assertDoesNotThrow(() -> Tarjan.run(forward)).count);
        assertEquals(n, assertDoesNotThrow(() -> Kosaraju.run(forward.reversed())).count);

        Graph cycle = TestGraphs.path(n);              // path closed into one big cycle
        cycle.addEdge(n - 1, 0, 1);
        assertEquals(1, assertDoesNotThrow(() -> Kosaraju.run(cycle)).count);
        assertEquals(1, assertDoesNotThrow(() -> Tarjan.run(cycle)).count);
    }
}
