package daa.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * One graph class for directed and undirected graphs. Adjacency list stored in primitive
 * arrays (forward-star): head[u] -> first arc, next[a] -> next arc of the same vertex.
 * Vertices are 0..n-1, weights are int. Undirected edges are stored as two arcs
 * (a self-loop as one). The original edge list is kept as well (needed by Kruskal).
 */
public final class Graph {
    private final int n;
    private final boolean directed;

    // adjacency list (arcs)
    private final int[] head;
    private int[] next = new int[16], to = new int[16], wt = new int[16];
    private int arcs;

    // edge list (as added)
    private int[] eu = new int[16], ev = new int[16], ew = new int[16];
    private int m;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n < 0");
        this.n = n;
        this.directed = directed;
        this.head = new int[n];
        Arrays.fill(head, -1);
    }

    public void addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n) throw new IllegalArgumentException("vertex out of range: " + u + " " + v);
        if (m == eu.length) {
            eu = Arrays.copyOf(eu, m * 2);
            ev = Arrays.copyOf(ev, m * 2);
            ew = Arrays.copyOf(ew, m * 2);
        }
        eu[m] = u; ev[m] = v; ew[m] = w; m++;
        addArc(u, v, w);
        if (!directed && u != v) addArc(v, u, w);
    }

    private void addArc(int u, int v, int w) {
        if (arcs == to.length) {
            next = Arrays.copyOf(next, arcs * 2);
            to = Arrays.copyOf(to, arcs * 2);
            wt = Arrays.copyOf(wt, arcs * 2);
        }
        to[arcs] = v; wt[arcs] = w; next[arcs] = head[u]; head[u] = arcs++;
    }

    public int n() { return n; }
    public int m() { return m; }
    public boolean isDirected() { return directed; }

    // adjacency iteration: for (int a = g.head(u); a != -1; a = g.next(a)) { g.to(a); g.weight(a); }
    public int head(int u) { return head[u]; }
    public int next(int a) { return next[a]; }
    public int to(int a) { return to[a]; }
    public int weight(int a) { return wt[a]; }

    // edge list access
    public int edgeU(int i) { return eu[i]; }
    public int edgeV(int i) { return ev[i]; }
    public int edgeW(int i) { return ew[i]; }

    public int[] inDegrees() {
        int[] d = new int[n];
        for (int i = 0; i < m; i++) d[ev[i]]++;
        return d;
    }

    /** Directed graph with every edge reversed. */
    public Graph reversed() {
        Graph r = new Graph(n, true);
        for (int i = 0; i < m; i++) r.addEdge(ev[i], eu[i], ew[i]);
        return r;
    }

    /** Format: first line "n m directed|undirected", then m lines "u v w". Lines starting with # are ignored. */
    public static Graph read(Path file) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = nextLine(br);
            if (line == null) throw new IOException("empty file");
            String[] h = line.trim().split("\\s+");
            if (h.length != 3) throw new IOException("bad header: " + line);
            int n = Integer.parseInt(h[0]), m = Integer.parseInt(h[1]);
            boolean dir;
            if (h[2].equalsIgnoreCase("directed")) dir = true;
            else if (h[2].equalsIgnoreCase("undirected")) dir = false;
            else throw new IOException("expected directed|undirected: " + h[2]);
            Graph g = new Graph(n, dir);
            for (int i = 0; i < m; i++) {
                line = nextLine(br);
                if (line == null) throw new IOException("expected " + m + " edges, got " + i);
                String[] p = line.trim().split("\\s+");
                if (p.length != 3) throw new IOException("bad edge line: " + line);
                g.addEdge(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            }
            return g;
        }
    }

    private static String nextLine(BufferedReader br) throws IOException {
        String s;
        while ((s = br.readLine()) != null) {
            s = s.trim();
            if (!s.isEmpty() && !s.startsWith("#")) return s;
        }
        return null;
    }
}
