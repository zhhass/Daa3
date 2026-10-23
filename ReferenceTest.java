package daa;

import daa.algorithms.*;
import daa.benchmark.GraphGenerator;
import daa.graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The generator + algorithms must reproduce the reference table of the assignment (smaller sizes). */
class ReferenceTest {
    @Test void n1000Sparse() { check(1000, 4000, 147970, 27, 396274); }
    @Test void n1000Dense()  { check(1000, 20000, 30363, 1, 252411); }
    @Test void n3162Sparse() { check(3162, 12648, 468911, 137, 1271911); }
    @Test void n3162Dense()  { check(3162, 63240, 97589, 1, 723157); }

    private static void check(int n, int m, long mst, int scc, long dag) {
        Graph u = GraphGenerator.undirected(n, m), d = GraphGenerator.directed(n, m), g = GraphGenerator.dag(n, m);
        assertEquals(mst, Kruskal.run(u).totalWeight);
        assertEquals(mst, Prim.run(u).totalWeight);
        assertEquals(scc, Kosaraju.run(d).count);
        assertEquals(scc, Tarjan.run(d).count);
        Kahn.Result k = Kahn.run(g);
        assertTrue(Kahn.isValidOrder(g, k.order));
        assertEquals(dag, DagShortestPath.run(g, k.order).sumOfDistances);
    }
}
