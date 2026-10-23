package daa;

import daa.graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseTest {
    @Test void emptyAndIsolatedGraphsDoNotCrash() {
        Graph g = new Graph(0, false);
        assertEquals(0, daa.algorithms.Kruskal.run(g).components);
        assertEquals(0, daa.algorithms.Prim.run(g).components);
        Graph iso = new Graph(5, true);
        assertEquals(5, daa.algorithms.Kosaraju.run(iso).count);
        assertEquals(5, daa.algorithms.Tarjan.run(iso).count);
        assertEquals(5, daa.algorithms.Kahn.run(iso).size);
    }
}
