# Q7 — Diagnose and cache repeated rankings

## Aim and theory

A. Diagnose: Q5 copies and sorts an unchanged leaderboard on every request. The bottleneck is repeated sorting and result-list creation, not comparator correctness. Use Q5 baseline and Q7 Original measurements as evidence; different JVM invocations can have different absolute timings.

B. Objective: preserve identical ordered results; reduce 20 unchanged requests from 20 sorts to one; invalidate after updates; prohibit caller mutation of the cached snapshot.

C. Redesign: keep items, immutable sorted snapshot and dirty flag. ADD appends then marks dirty. RANKING copies/sorts only when dirty, wraps the copy as unmodifiable, clears dirty and returns the snapshot. Teams have final fields; a future score-update method must invalidate the cache. No external in-place score mutation is supported. The old snapshot remains valid after a later update.

D. Predict: unchanged workload -> 20 versus 1 sort; changing workload (one update before every read) -> both 20 sorts and little/no speed benefit. Prediction lines are printed before experiments.

E. Validate: same seeded teams, comparator and 20 requests at n=100, 1,000 and 10,000. Timing uses an ordinary comparator; counts are measured separately. The cache starts dirty, so its first sort is included. Checks establish exact list equality, update invalidation, old-snapshot isolation and immutability. An update-before-every-read counterexample tests the operating boundary.

F. Evaluation: inspect observed times rather than assuming the cache always wins. Sort counts and comparison counts directly validate reduced repeated work. Original cost for r unchanged requests is O(r*n*log n); cache cost O(n*log n+r), excluding later traversal/rendering. Both need O(n) peak content storage, but the cache retains an O(n) snapshot. Each displayed full list adds O(n) traversal regardless of caching. Original may suit rare requests or changes before every request. Concurrency requires locking or safe snapshot publication. The returned-list contract changes from mutable copies to immutable snapshots.

## Compile and run

From the repository root:

```bash
python tools/run.py Q7
```

Requires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py Q7` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.

## Executed checks

```text
PREDICT: 20 unchanged reads -> original 20 sorts; cached 1 sort
PREDICT: update before every read -> both perform 20 sorts
SPEEDUP,n=100,mode=unchanged,value=16.263
COUNTS,n=100,mode=unchanged,cached=false,sorts=20,comparisons=10820
COUNTS,n=100,mode=unchanged,cached=true,sorts=1,comparisons=541
SPEEDUP,n=100,mode=changing,value=0.441
COUNTS,n=100,mode=changing,cached=false,sorts=20,comparisons=12113
COUNTS,n=100,mode=changing,cached=true,sorts=20,comparisons=12113
SPEEDUP,n=1000,mode=unchanged,value=28.647
COUNTS,n=1000,mode=unchanged,cached=false,sorts=20,comparisons=173300
COUNTS,n=1000,mode=unchanged,cached=true,sorts=1,comparisons=8665
SPEEDUP,n=1000,mode=changing,value=1.043
COUNTS,n=1000,mode=changing,cached=false,sorts=20,comparisons=174512
COUNTS,n=1000,mode=changing,cached=true,sorts=20,comparisons=174512
SPEEDUP,n=10000,mode=unchanged,value=21.653
COUNTS,n=10000,mode=unchanged,cached=false,sorts=20,comparisons=2408080
COUNTS,n=10000,mode=unchanged,cached=true,sorts=1,comparisons=120404
SPEEDUP,n=10000,mode=changing,value=1.033
COUNTS,n=10000,mode=changing,cached=false,sorts=20,comparisons=2409097
COUNTS,n=10000,mode=changing,cached=true,sorts=20,comparisons=2409097
PASS: equality, invalidation, immutable snapshots, and sort counts
Measured experiments: 12; each includes 3 warmups + 5 raw runs.
```

## Measured results

Times below are measured in the workspace, not the student device. Setup is excluded. Each mean uses five timed runs after three warmups. Results include observable consumption overhead.

| Experiment | Operations/run | Mean batch ms | Mean ns/op | Sample CV |
|---|---:|---:|---:|---:|
| Original_n100_unchanged | 20 | 0.683114 | 34155.700 | 23.4% |
| Cached_n100_unchanged | 20 | 0.042004 | 2100.210 | 34.0% |
| Original_n100_changing | 20 | 0.418218 | 20910.910 | 26.4% |
| Cached_n100_changing | 20 | 0.948323 | 47416.150 | 36.1% |
| Original_n1000_unchanged | 20 | 3.420363 | 171018.130 | 32.2% |
| Cached_n1000_unchanged | 20 | 0.119398 | 5969.880 | 28.8% |
| Original_n1000_changing | 20 | 2.016095 | 100804.770 | 21.5% |
| Cached_n1000_changing | 20 | 1.932629 | 96631.460 | 9.2% |
| Original_n10000_unchanged | 20 | 44.431224 | 2221561.180 | 8.3% |
| Cached_n10000_unchanged | 20 | 2.051959 | 102597.940 | 3.6% |
| Original_n10000_changing | 20 | 45.357791 | 2267889.530 | 6.4% |
| Cached_n10000_changing | 20 | 43.893505 | 2194675.260 | 8.9% |

CV is sample standard deviation divided by mean. High variation weakens fine-grained speed claims. A fixed algorithm order, one JVM per program, small samples, JIT and GC are limitations; these are educational measurements, not JMH results. Complexity is supported by reasoning and counts, not inferred solely from time ratios.

## Files and evidence

- Java sources: this task directory (Q3 separates before/after).
- `results/execution.txt`: complete captured execution.
- `results/environment.json`: actual JVM, system and execution origin.
- `results/checks.txt`: correctness/operation-count summary.
- `results/measurements.csv` and `results/averages.csv`: raw timings and averages where applicable.
- `screenshots/compiler.jpg`: actual desktop terminal source/compilation capture.
- `screenshots/output.jpg`: actual desktop terminal capture of saved execution checks; full raw output is in results.

Students must rerun and explain these programs on their own machine before submission. Workspace evidence is not represented as personal execution.
