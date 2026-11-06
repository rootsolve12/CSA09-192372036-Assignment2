# Q6 — Priority triage and fairness

## Aim and theory

Design before the supplied code: represent each patient with severity and a unique arrival sequence. A heap ordered by (severity ascending, arrival ascending) serves urgent cases first and preserves FIFO among equal severities. A plain FIFO cannot prioritize a later critical case. poll removes the head; copying the heap and polling the copy displays ordered waiting patients without consuming the original. Heap iteration is not sorted.

The eight-patient hand order is Meena, Kumar, Lata, Noor, Ravi, John, Priya, Asha. The supplied six-patient program must print Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5).

Predict O(log n) heap work for admit/remove, with occasional O(n) backing-array growth for admission (amortized O(log n)). A random insertion batch may need fewer comparisons; timings cannot prove a worst-case bound. The benchmark tests batches of 100, 10,000 and 100,000 pre-created patients. Serving all patients has O(n log n) upper bound. Patient allocation is excluded.

| Design | Admit | Serve | Space |
|---|---|---|---|
| PriorityQueue | O(log n) amortized | O(log n) | O(n) |
| Sorted LinkedList | O(n) | O(1) | O(n) |
| Five ArrayDeque FIFO queues | O(1) amortized | O(1), scan at most 5 heads | O(n) |

Five queues exploit the fixed five severity levels, while a heap handles arbitrary priority policies more generally. Pseudocode for buckets: admit -> buckets[s-1].addLast(p); next -> inspect severity 1..5 and pollFirst from the first nonempty bucket. Sorted-list insertion scans until the comparator position, then inserts; pollFirst serves.

Strict priority can starve minor cases. Simulation rule: effectiveSeverity=max(1,originalSeverity-floor(wait/T)); retain original arrival for ties. Freeze time and rebuild the heap at each review. Never use a changing clock comparator without restoring heap order. The code demonstrates a frozen-time promotion. This modifies strict severity policy and is a software simulation, not an approved clinical rule.

5,000/day is about 0.058 arrivals/s (one every 17.28 s) on average, not a peak-load guarantee. Inspect measured queue costs and budget for anticipated peak waiting population. A complete deployment also needs clinical reassessment, concurrency, persistence, audit and recovery. PriorityQueue and the supplied counter are single-threaded.

## Compile and run

From the repository root:

```bash
python tools/run.py Q6
```

Requires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py Q6` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.

## Executed checks

```text
=== Triage ===
Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)
PREDICT: [Meena, Kumar, Lata, Noor, Ravi, John, Priya, Asha]
OBSERVED: [Meena, Kumar, Lata, Noor, Ravi, John, Priya, Asha]
Ageing simulation: OldMinor promoted to effective severity 1
PASS: hand order, non-destructive display and frozen-time ageing
Measured experiments: 6; each includes 3 warmups + 5 raw runs.
```

## Measured results

Times below are measured in the workspace, not the student device. Setup is excluded. Each mean uses five timed runs after three warmups. Results include observable consumption overhead.

| Experiment | Operations/run | Mean batch ms | Mean ns/op | Sample CV |
|---|---:|---:|---:|---:|
| PriorityQueue_n100_admit | 100 | 0.087995 | 879.950 | 28.5% |
| PriorityQueue_n100_serve | 100 | 0.071481 | 714.806 | 40.6% |
| PriorityQueue_n10000_admit | 10000 | 0.715286 | 71.529 | 14.9% |
| PriorityQueue_n10000_serve | 10000 | 2.192688 | 219.269 | 10.9% |
| PriorityQueue_n100000_admit | 100000 | 2.340898 | 23.409 | 5.7% |
| PriorityQueue_n100000_serve | 100000 | 37.772387 | 377.724 | 27.2% |

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
