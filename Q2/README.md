# Q2 — Playlist collection selection

## Aim and theory

Predict O(1) ArrayList indexed access, O(n) front/middle shifts, O(1) amortized append; LinkedList has O(1) endpoint edits but O(n) indexed middle location. Iterating either is O(n). Test 100, 10,000 and 100,000 songs with separate insertion/removal batches, 1,000 indexed reads, and ten full traversals. Song objects are created before timing. Inserts grow the list by 1,000; removal setup creates the same-sized starting state outside timing. These are batch measurements, not isolated edits at exactly n elements.

| Operation | ArrayList | LinkedList | ArrayDeque |
|---|---|---|---|
| Append | O(1) amortized | O(1) | O(1) amortized |
| Insert/remove front | O(n) | O(1) | O(1) amortized |
| Indexed middle insertion/removal | O(n) | O(n) | No indexed API; O(n) manual reconstruction |
| Get kth | O(1) | O(min(k,n-1-k)) | No indexed API; O(n) traversal |
| Traverse | O(n) | O(n) | O(n) |
| Storage | reference array + spare capacity | node + two links per item | circular reference array + spare capacity |

A LinkedList iterator already positioned at a node can insert/remove in O(1), but locating that position is not free. A plain array provides O(1) get; System.arraycopy supports O(n) manual shifts, while full capacity requires replacement allocation.

Recommend ArrayList for append-heavy playlists with random track access and sequential display. Stop recommending it when measured repeated shifting exceeds an explicit UI budget (for example 16 ms for a whole UI update); total operation frequency matters more than an arbitrary size threshold. Keep Up Next in ArrayDeque for frequent endpoint edits. The deque end-cycle benchmark counts four endpoint operations per cycle. Its composite cycle is not directly equivalent to every list workload.

## Compile and run

From the repository root:

```bash
python tools/run.py Q2
```

Requires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py Q2` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.

## Executed checks

```text
PASS: playlist insertion/removal equivalence at all three sizes
Measured experiments: 45; each includes 3 warmups + 5 raw runs.
```

## Measured results

Times below are measured in the workspace, not the student device. Setup is excluded. Each mean uses five timed runs after three warmups. Results include observable consumption overhead.

| Experiment | Operations/run | Mean batch ms | Mean ns/op | Sample CV |
|---|---:|---:|---:|---:|
| ArrayList_n100_append | 100 | 0.019681 | 196.814 | 70.9% |
| ArrayList_n100_insert_front | 1000 | 0.130917 | 130.917 | 28.2% |
| ArrayList_n100_remove_front | 1000 | 0.098386 | 98.386 | 9.7% |
| ArrayList_n100_insert_middle | 1000 | 0.101168 | 101.168 | 22.0% |
| ArrayList_n100_remove_middle | 1000 | 0.095308 | 95.308 | 3.0% |
| ArrayList_n100_indexed_get | 1000 | 0.078649 | 78.649 | 30.8% |
| ArrayList_n100_iterate | 1000 | 0.089720 | 89.720 | 17.6% |
| LinkedList_n100_append | 100 | 0.021498 | 214.980 | 63.0% |
| LinkedList_n100_insert_front | 1000 | 0.166211 | 166.211 | 42.3% |
| LinkedList_n100_remove_front | 1000 | 0.119636 | 119.636 | 41.7% |
| LinkedList_n100_insert_middle | 1000 | 0.699178 | 699.178 | 37.0% |
| LinkedList_n100_remove_middle | 1000 | 0.429411 | 429.411 | 15.4% |
| LinkedList_n100_indexed_get | 1000 | 0.259660 | 259.660 | 92.2% |
| LinkedList_n100_iterate | 1000 | 0.144529 | 144.529 | 58.5% |
| ArrayDeque_n100_end_cycle | 4000 | 0.202632 | 50.658 | 57.5% |
| ArrayList_n10000_append | 10000 | 0.178634 | 17.863 | 20.6% |
| ArrayList_n10000_insert_front | 1000 | 0.663118 | 663.118 | 16.3% |
| ArrayList_n10000_remove_front | 1000 | 0.673650 | 673.650 | 22.3% |
| ArrayList_n10000_insert_middle | 1000 | 0.263556 | 263.556 | 6.3% |
| ArrayList_n10000_remove_middle | 1000 | 0.312970 | 312.970 | 8.4% |
| ArrayList_n10000_indexed_get | 1000 | 0.071326 | 71.326 | 61.6% |
| ArrayList_n10000_iterate | 100000 | 0.886032 | 8.860 | 32.0% |
| LinkedList_n10000_append | 10000 | 0.065375 | 6.538 | 9.2% |
| LinkedList_n10000_insert_front | 1000 | 0.051412 | 51.412 | 14.9% |
| LinkedList_n10000_remove_front | 1000 | 0.054810 | 54.810 | 4.1% |
| LinkedList_n10000_insert_middle | 1000 | 7.606740 | 7606.740 | 23.3% |
| LinkedList_n10000_remove_middle | 1000 | 7.091422 | 7091.422 | 9.7% |
| LinkedList_n10000_indexed_get | 1000 | 3.531167 | 3531.167 | 9.9% |
| LinkedList_n10000_iterate | 100000 | 0.518485 | 5.185 | 27.2% |
| ArrayDeque_n10000_end_cycle | 4000 | 0.214343 | 53.586 | 14.1% |
| ArrayList_n100000_append | 100000 | 0.395006 | 3.950 | 26.3% |
| ArrayList_n100000_insert_front | 1000 | 4.897171 | 4897.171 | 3.8% |
| ArrayList_n100000_remove_front | 1000 | 5.209504 | 5209.504 | 6.8% |
| ArrayList_n100000_insert_middle | 1000 | 2.761106 | 2761.106 | 4.3% |
| ArrayList_n100000_remove_middle | 1000 | 2.527907 | 2527.907 | 4.0% |
| ArrayList_n100000_indexed_get | 1000 | 0.051453 | 51.453 | 3.9% |
| ArrayList_n100000_iterate | 1000000 | 4.576549 | 4.577 | 23.5% |
| LinkedList_n100000_append | 100000 | 0.343231 | 3.432 | 34.0% |
| LinkedList_n100000_insert_front | 1000 | 0.009999 | 9.999 | 21.2% |
| LinkedList_n100000_remove_front | 1000 | 0.011397 | 11.397 | 3.5% |
| LinkedList_n100000_insert_middle | 1000 | 62.101488 | 62101.488 | 1.4% |
| LinkedList_n100000_remove_middle | 1000 | 61.742324 | 61742.324 | 3.8% |
| LinkedList_n100000_indexed_get | 1000 | 23.371320 | 23371.320 | 7.7% |
| LinkedList_n100000_iterate | 1000000 | 5.996588 | 5.997 | 23.7% |
| ArrayDeque_n100000_end_cycle | 4000 | 0.406549 | 101.637 | 31.2% |

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
