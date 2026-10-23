package daa;

import daa.algorithms.DSU;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DsuTest {
    /** Naive reference: relabel all members of one set (O(n) per union). */
    static final class Ref {
        final int[] label; Ref(int n) { label = new int[n]; for (int i = 0; i < n; i++) label[i] = i; }
        void union(int a, int b) { int la = label[a], lb = label[b]; if (la == lb) return; for (int i = 0; i < label.length; i++) if (label[i] == lb) label[i] = la; }
        boolean same(int a, int b) { return label[a] == label[b]; }
        int sets() { return (int) java.util.Arrays.stream(label).distinct().count(); }
    }

    @Test void agreesWithReferenceAfterRandomUnions() {
        Random r = new Random(1);
        for (int round = 0; round < 50; round++) {
            int n = 1 + r.nextInt(100);
            DSU d = new DSU(n);
            Ref ref = new Ref(n);
            for (int i = 0; i < r.nextInt(150); i++) {
                int a = r.nextInt(n), b = r.nextInt(n);
                boolean expectNew = !ref.same(a, b);
                assertEquals(expectNew, d.union(a, b));
                ref.union(a, b);
            }
            for (int a = 0; a < n; a++) for (int b = 0; b < n; b++)
                assertEquals(ref.same(a, b), d.find(a) == d.find(b));
            assertEquals(ref.sets(), d.components());
        }
    }

    @Test void countersAndMakeSet() {
        DSU d = new DSU(4);
        assertTrue(d.union(0, 1));
        assertFalse(d.union(1, 0));
        assertEquals(1, d.unions());
        assertEquals(4, d.finds());          // two finds per union call
        assertEquals(5, d.ops());
        d.makeSet(10);
        assertEquals(10, d.components());
        assertEquals(0, d.ops());
    }

    @Test void longChainStaysShallowAndIterative() {
        int n = 200_000;
        DSU d = new DSU(n);
        for (int i = 0; i + 1 < n; i++) d.union(i, i + 1);
        assertEquals(1, d.components());
        assertEquals(d.find(0), d.find(n - 1));
    }
}
