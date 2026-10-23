package daa.algorithms;

/** Disjoint Set Union: union by rank + path compression (iterative, so it is stack-safe). */
public final class DSU {
    private int[] parent;
    private int[] rank;
    private int components;
    private long finds;    // find calls
    private long unions;   // successful union calls

    public DSU(int n) { makeSet(n); }

    /** (Re)creates n singleton sets 0..n-1 and resets the counters. */
    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        components = n;
        finds = 0;
        unions = 0;
    }

    public int find(int x) {
        finds++;
        int r = x;
        while (parent[r] != r) r = parent[r];
        while (parent[x] != r) { int p = parent[x]; parent[x] = r; x = p; } // path compression
        return r;
    }

    /** @return true if x and y were in different sets (successful union). */
    public boolean union(int x, int y) {
        int a = find(x), b = find(y);
        if (a == b) return false;
        if (rank[a] < rank[b]) { int t = a; a = b; b = t; } // union by rank
        parent[b] = a;
        if (rank[a] == rank[b]) rank[a]++;
        components--;
        unions++;
        return true;
    }

    public int components() { return components; }
    public long finds() { return finds; }
    public long unions() { return unions; }
    public long ops() { return finds + unions; }
}
