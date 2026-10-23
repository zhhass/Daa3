package daa;

import daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    @Test void readsFilesInData() throws Exception {
        Graph c = Graph.read(Path.of("data/city_connected.txt"));
        assertEquals(6, c.n());
        assertEquals(9, c.m());
        assertFalse(c.isDirected());
        Graph j = Graph.read(Path.of("data/jobs_cyclic.txt"));
        assertTrue(j.isDirected());
        assertEquals(9, j.m());
    }

    @Test void undirectedEdgeIsVisibleFromBothEnds() {
        Graph g = new Graph(3, false);
        g.addEdge(0, 1, 7);
        g.addEdge(2, 2, 1);               // self-loop is stored as one arc
        int arcs0 = 0; for (int a = g.head(0); a != -1; a = g.next(a)) { assertEquals(1, g.to(a)); arcs0++; }
        int arcs1 = 0; for (int a = g.head(1); a != -1; a = g.next(a)) { assertEquals(0, g.to(a)); arcs1++; }
        int arcs2 = 0; for (int a = g.head(2); a != -1; a = g.next(a)) arcs2++;
        assertEquals(1, arcs0); assertEquals(1, arcs1); assertEquals(1, arcs2);
    }

    @Test void badVertexIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Graph(2, true).addEdge(0, 2, 1));
    }
}
