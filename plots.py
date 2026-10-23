#!/usr/bin/env python3
"""Reads results/results.csv, writes the three plots and results/analysis.md (ratios + slopes)."""
import csv, math, sys
from collections import defaultdict
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

src = sys.argv[1] if len(sys.argv) > 1 else "results/results.csv"
out = sys.argv[2] if len(sys.argv) > 2 else "results"
rows = list(csv.DictReader(open(src)))
for r in rows:
    for k in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
        r[k] = int(r[k])
    r["time_ms"] = float(r["time_ms"])
    r["density"] = r["graph"].split("_")[1]
    r["ops"] = r["dsu_ops"] + r["edge_scans"] + r["comparisons"]

series = defaultdict(list)           # (algorithm, density) -> rows sorted by n
for r in rows:
    series[(r["algorithm"], r["density"])].append(r)
for v in series.values():
    v.sort(key=lambda r: r["n"])

def slope(xs, ys):
    lx, ly = [math.log10(x) for x in xs], [math.log10(y) for y in ys]
    mx, my = sum(lx) / len(lx), sum(ly) / len(ly)
    return sum((a - mx) * (b - my) for a, b in zip(lx, ly)) / sum((a - mx) ** 2 for a in lx)

ALGS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COL = dict(zip(ALGS, ["tab:red", "tab:blue", "tab:green", "tab:orange", "tab:purple"]))
STY = {"sparse": "-", "dense": "--"}
MK = {"sparse": "o", "dense": "s"}

# ---- Plot 1: operations vs n
plt.figure(figsize=(8, 6))
for a in ALGS:
    for d in ("sparse", "dense"):
        s = series[(a, d)]
        plt.loglog([r["n"] for r in s], [r["ops"] for r in s], STY[d], marker=MK[d], color=COL[a], label=f"{a} ({d})")
plt.xlabel("n (vertices)"); plt.ylabel("counted operations (dsuOps + edgeScans + comparisons)")
plt.title("Operations vs n (log-log)\nKosaraju (2m) lies exactly under DagShortestPath (m scans + m comparisons)", fontsize=10); plt.grid(True, which="both", alpha=.3); plt.legend(fontsize=8, ncol=2)
plt.tight_layout(); plt.savefig(f"{out}/plots/operations_vs_n.png", dpi=150); plt.close()

# ---- Plot 2: scans per edge
plt.figure(figsize=(10, 6))
for a in ALGS:
    for d in ("sparse", "dense"):
        s = series[(a, d)]
        plt.semilogx([r["n"] for r in s], [r["edge_scans"] / r["m"] for r in s], STY[d], marker=MK[d], color=COL[a], label=f"{a} ({d})")
plt.xlabel("n (vertices)"); plt.ylabel("edgeScans / m")
plt.title("Scans per edge: a flat line means linear work\nPrim and Kosaraju overlap at 2.0, Kahn and DagShortestPath at 1.0 (sparse and dense)", fontsize=10); plt.grid(True, which="both", alpha=.3)
plt.ylim(0, 2.3); plt.legend(fontsize=8, loc="center left", bbox_to_anchor=(1.01, 0.5))
plt.tight_layout(); plt.savefig(f"{out}/plots/scans_per_edge.png", dpi=150); plt.close()

# ---- Plot 3: share of edges Kruskal examines
plt.figure(figsize=(7, 5))
for d, c in (("sparse", "tab:red"), ("dense", "tab:blue")):
    s = series[("Kruskal", d)]
    plt.semilogx([r["n"] for r in s], [r["edge_scans"] / r["m"] for r in s], marker="o", color=c, label=f"Kruskal, {d} (m = {'4n' if d == 'sparse' else '20n'})")
plt.axhline(1.0, color="gray", lw=.8, ls=":")
plt.xlabel("n (vertices)"); plt.ylabel("edges examined / m"); plt.ylim(0, 1.1)
plt.title("Share of edges Kruskal examines before the tree is complete")
plt.grid(True, which="both", alpha=.3); plt.legend()
plt.tight_layout(); plt.savefig(f"{out}/plots/kruskal_edge_share.png", dpi=150); plt.close()

# ---- analysis.md
L = []
def table(title, header, body):
    L.append(f"### {title}\n")
    L.append("| " + " | ".join(header) + " |")
    L.append("|" + "|".join(["---"] * len(header)) + "|")
    for b in body: L.append("| " + " | ".join(b) + " |")
    L.append("")

def ratio_rows(alg, f):
    body = []
    for d in ("sparse", "dense"):
        s = series[(alg, d)]
        vals = [f(r) for r in s]
        body.append([d] + [f"{v:.3f}" for v in vals] + [f"{min(vals):.3f}-{max(vals):.3f}"])
    return body

ns = [r["n"] for r in series[("Kruskal", "sparse")]]
hdr = ["density"] + [f"n={n}" for n in ns] + ["range"]
log2 = lambda x: math.log2(x)
table("Kruskal: dsuOps / E", hdr, ratio_rows("Kruskal", lambda r: r["dsu_ops"] / r["m"]))
table("Kruskal: dsuOps / edgeScans   (= 2 finds per examined edge + n-1 successful unions)", hdr, ratio_rows("Kruskal", lambda r: r["dsu_ops"] / r["edge_scans"]))
table("Kruskal: comparisons / (E log2 E)   (sort of all E edges)", hdr, ratio_rows("Kruskal", lambda r: r["comparisons"] / (r["m"] * log2(r["m"]))))
table("Prim: edgeScans / (2E)", hdr, ratio_rows("Prim", lambda r: r["edge_scans"] / (2 * r["m"])))
table("Prim: comparisons / (E log2 E)", hdr, ratio_rows("Prim", lambda r: r["comparisons"] / (r["m"] * log2(r["m"]))))
table("Kosaraju: edgeScans / (V + E)", hdr, ratio_rows("Kosaraju", lambda r: r["edge_scans"] / (r["n"] + r["m"])))
table("Kahn: edgeScans / (V + E)", hdr, ratio_rows("Kahn", lambda r: r["edge_scans"] / (r["n"] + r["m"])))
table("DagShortestPath: edgeScans / (V + E)", hdr, ratio_rows("DagShortestPath", lambda r: r["edge_scans"] / (r["n"] + r["m"])))

body = []
for a in ALGS:
    for d in ("sparse", "dense"):
        s = series[(a, d)]
        ns_ = [r["n"] for r in s]
        body.append([a, d, f"{slope(ns_, [r['edge_scans'] for r in s]):.3f}",
                     f"{slope(ns_, [r['comparisons'] for r in s]):.3f}" if s[0]["comparisons"] else "-",
                     f"{slope(ns_, [r['ops'] for r in s]):.3f}"])
table("Log-log slopes against n (m = 4n or 20n, so n and m scale together)", ["algorithm", "density", "slope edgeScans", "slope comparisons", "slope total ops"], body)

body = []
for d in ("sparse", "dense"):
    s = series[("Kruskal", d)]
    body.append([d] + [f"{r['edge_scans'] / r['m']:.3f}" for r in s])
table("Kruskal: share of edges examined (edgeScans / m)", ["density"] + [f"n={n}" for n in ns], body)

body = []
for d in ("sparse", "dense"):
    for r_k, r_p in zip(series[("Kruskal", d)], series[("Prim", d)]):
        body.append([d, str(r_k["n"]), f"{r_k['ops']:,}", f"{r_p['ops']:,}", f"{r_k['time_ms']:.1f}", f"{r_p['time_ms']:.1f}"])
table("Kruskal vs Prim: counted operations and median time", ["density", "n", "Kruskal ops", "Prim ops", "Kruskal ms", "Prim ms"], body)

body = []
for a in ALGS:
    for r in series[(a, "dense")][-1:] + series[(a, "sparse")][-1:]:
        body.append([a, r["graph"], f"{r['time_ms']:.1f}"])
table("Median time at n = 100 000 (ms)", ["algorithm", "graph", "time_ms"], body)

body = [[r["graph"], str(r["max_depth"]), f"{r['max_depth'] / r['n']:.3f}"] for r in sorted(series[("Kosaraju", "sparse")] + series[("Kosaraju", "dense")], key=lambda r: (r["density"], r["n"]))]
table("Kosaraju explicit-stack maxDepth", ["graph", "maxDepth", "maxDepth / n"], body)

open(f"{out}/analysis.md", "w").write("\n".join(L))
print("wrote plots and", f"{out}/analysis.md")
