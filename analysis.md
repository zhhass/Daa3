### Kruskal: dsuOps / E

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 1.791 | 2.019 | 2.182 | 2.232 | 2.248 | 1.791-2.248 |
| dense | 0.480 | 0.506 | 0.465 | 0.654 | 0.794 | 0.465-0.794 |

### Kruskal: dsuOps / edgeScans   (= 2 finds per examined edge + n-1 successful unions)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 2.324 | 2.283 | 2.259 | 2.252 | 2.250 | 2.250-2.324 |
| dense | 2.232 | 2.219 | 2.241 | 2.166 | 2.134 | 2.134-2.241 |

### Kruskal: comparisons / (E log2 E)   (sort of all E edges)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 0.896 | 0.928 | 0.940 | 0.926 | 0.949 | 0.896-0.949 |
| dense | 0.935 | 0.922 | 0.946 | 0.953 | 0.942 | 0.922-0.953 |

### Prim: edgeScans / (2E)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 1.000-1.000 |
| dense | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 1.000-1.000 |

### Prim: comparisons / (E log2 E)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 1.827 | 1.867 | 1.900 | 1.929 | 1.946 | 1.827-1.946 |
| dense | 1.826 | 1.849 | 1.868 | 1.880 | 1.892 | 1.826-1.892 |

### Kosaraju: edgeScans / (V + E)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 1.600 | 1.600 | 1.600 | 1.600 | 1.600 | 1.600-1.600 |
| dense | 1.905 | 1.905 | 1.905 | 1.905 | 1.905 | 1.905-1.905 |

### Kahn: edgeScans / (V + E)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 | 0.800-0.800 |
| dense | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 | 0.952-0.952 |

### DagShortestPath: edgeScans / (V + E)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 | range |
|---|---|---|---|---|---|---|
| sparse | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 | 0.800-0.800 |
| dense | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 | 0.952-0.952 |

### Log-log slopes against n (m = 4n or 20n, so n and m scale together)

| algorithm | density | slope edgeScans | slope comparisons | slope total ops |
|---|---|---|---|---|
| Kruskal | sparse | 1.055 | 1.105 | 1.096 |
| Kruskal | dense | 1.119 | 1.087 | 1.088 |
| Prim | sparse | 1.000 | 1.109 | 1.102 |
| Prim | dense | 1.000 | 1.090 | 1.085 |
| Kosaraju | sparse | 1.000 | - | 1.000 |
| Kosaraju | dense | 1.000 | - | 1.000 |
| Kahn | sparse | 1.000 | - | 1.000 |
| Kahn | dense | 1.000 | - | 1.000 |
| DagShortestPath | sparse | 1.000 | 1.000 | 1.000 |
| DagShortestPath | dense | 1.000 | 1.000 | 1.000 |

### Kruskal: share of edges examined (edgeScans / m)

| density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 |
|---|---|---|---|---|---|
| sparse | 0.770 | 0.885 | 0.966 | 0.991 | 0.999 |
| dense | 0.215 | 0.228 | 0.207 | 0.302 | 0.372 |

### Kruskal vs Prim: counted operations and median time

| density | n | Kruskal ops | Prim ops | Kruskal ms | Prim ms |
|---|---|---|---|---|---|
| sparse | 1000 | 53,134 | 95,445 | 2.7 | 7.8 |
| sparse | 3162 | 196,685 | 347,001 | 1.6 | 8.1 |
| sparse | 10000 | 700,629 | 1,242,001 | 9.1 | 23.1 |
| sparse | 31623 | 2,393,667 | 4,388,930 | 34.0 | 64.8 |
| sparse | 100000 | 8,364,095 | 15,284,200 | 62.9 | 539.7 |
| dense | 1000 | 281,192 | 561,818 | 7.8 | 15.2 |
| dense | 3162 | 976,432 | 1,991,263 | 14.3 | 21.3 |
| dense | 10000 | 3,467,079 | 6,977,379 | 28.3 | 126.8 |
| dense | 31623 | 12,221,670 | 24,182,684 | 88.9 | 737.8 |
| dense | 100000 | 41,754,259 | 83,207,301 | 329.2 | 3416.8 |

### Median time at n = 100 000 (ms)

| algorithm | graph | time_ms |
|---|---|---|
| Kruskal | n100000_dense | 329.2 |
| Kruskal | n100000_sparse | 62.9 |
| Prim | n100000_dense | 3416.8 |
| Prim | n100000_sparse | 539.7 |
| Kosaraju | n100000_dense | 640.4 |
| Kosaraju | n100000_sparse | 55.1 |
| Kahn | n100000_dense | 507.2 |
| Kahn | n100000_sparse | 10.9 |
| DagShortestPath | n100000_dense | 345.7 |
| DagShortestPath | n100000_sparse | 14.1 |

### Kosaraju explicit-stack maxDepth

| graph | maxDepth | maxDepth / n |
|---|---|---|
| n1000_dense | 934 | 0.934 |
| n3162_dense | 2913 | 0.921 |
| n10000_dense | 9243 | 0.924 |
| n31623_dense | 29024 | 0.918 |
| n100000_dense | 91705 | 0.917 |
| n1000_sparse | 623 | 0.623 |
| n3162_sparse | 1869 | 0.591 |
| n10000_sparse | 5965 | 0.597 |
| n31623_sparse | 18783 | 0.594 |
| n100000_sparse | 59397 | 0.594 |
