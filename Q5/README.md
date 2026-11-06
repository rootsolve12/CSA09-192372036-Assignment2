# Q5 — Reusable generic ranking

## Aim and theory

Choose Comparator<? super T> in an unbounded generic Leaderboard<T>: the ranking policy is supplied independently of the item type. A Comparable implementation would instead use T extends Comparable<? super T>. List<? extends T> is useful for bulk input; the GenericBoard test demonstrates addAll. Comparator chains express points descending, goal difference descending, name ascending.

Lexicographic comparison is transitive because each component order is transitive and the first unequal component decides. Integer comparison avoids subtraction overflow. Reversing only the numeric component comparators preserves name A-Z. Fully equal ranking tuples compare as zero. Supplied Team uses identity equals, so equal rank is not object identity; use care with TreeSet, which treats comparison zero as duplicate.

The supplied output is [Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]. Bisons beats Panthers alphabetically after both numerical ties. Additional data includes repeated identical tuples and ties at every level. An adversarial Integer.MAX_VALUE/MIN_VALUE pair exposes subtraction overflow. Tests check sign symmetry, transitivity on the data, input preservation and stable-sort equivalence.

Comparable alternative: declare Team implements Comparable<Team> and implement compareTo using the same chain. A chained Comparator and Comparable each need one O(n log n) sort. Three stable sorts (name, goal difference descending, points descending) need three sorts, still O(n log n) asymptotically. Preserving the input costs O(n) for a copy; object sorting can require O(n) workspace. Comparator injection best supports players, colleges and changing policies.

The baseline benchmark measures 20 unchanged rankings at 100, 1,000 and 10,000 teams. Its repeated copy-and-sort operation is diagnosed and redesigned in Q7. Both tasks use the same seed and data-generation rules.

## Compile and run

From the repository root:

```bash
python tools/run.py Q5
```

Requires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py Q5` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.

## Executed checks

```text
=== LeaderboardDemo ===
[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]
PASS: ties, overflow, transitivity, stable-sort equivalence and copy isolation
Measured experiments: 3; each includes 3 warmups + 5 raw runs.
```

## Measured results

Times below are measured in the workspace, not the student device. Setup is excluded. Each mean uses five timed runs after three warmups. Results include observable consumption overhead.

| Experiment | Operations/run | Mean batch ms | Mean ns/op | Sample CV |
|---|---:|---:|---:|---:|
| Q5_n100_20_unchanged_rankings | 20 | 0.502239 | 25111.960 | 23.0% |
| Q5_n1000_20_unchanged_rankings | 20 | 5.373704 | 268685.220 | 23.9% |
| Q5_n10000_20_unchanged_rankings | 20 | 43.904207 | 2195210.340 | 7.0% |

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
