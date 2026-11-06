# Q4 — Pharmacy map engineering

## Aim and theory

Predict average O(1) HashMap/LinkedHashMap put/get and O(log n) TreeMap put/get. TreeMap iteration is O(n), LinkedHashMap iteration O(n), and HashMap iteration O(n+capacity). Test 100, 10,000 and 100,000 medicines, 100,000 seeded successful lookups per map and both complete reports. Input medicine allocation is outside timing; put timings include entry creation/resizing.

A TreeMap naturally gives code order; default LinkedHashMap gives insertion order. Sorting a value copy costs O(n log n). All entries retain a sequence number so insertion order is reconstructable even for HashMap and TreeMap. Without that metadata, insertion order cannot generally be recovered. Report timing includes ordering, materialization and traversal, but excludes console/file/network output. Separate query Strings avoid relying on stored-key reference identity; warmed query hash codes may be cached.

Define acceptable mean in-memory lookup as <1 ms at the largest size. This classroom criterion is not a clinical response standard and says nothing about tail latency. Inspect ACCEPT records and measured means. A LinkedHashMap is a defensible lookup-heavy design with daily code-sorted copies if its measured average meets the budget; insertion order requires no extra sort. Use TreeMap when frequent range/code-order queries justify it. Remaining cost includes hashing/comparison, allocation, GC and output. Concurrency and consistent reporting snapshots require additional design.

Hashtable is a legacy synchronized map; prefer appropriate confinement/locking or ConcurrentHashMap for concurrent access.

Sorted-array alternative: sort Medicine[] once and use Arrays.binarySearch with a code comparator (verified in code). Lookup O(log n), insertion O(n) shifts, code report O(n), storage O(n). HashMap gives average O(1) lookup/insertion plus report sorting; TreeMap gives O(log n) lookup/insertion and O(n) ordered reports.

## Compile and run

From the repository root:

```bash
python tools/run.py Q4
```

Requires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py Q4` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.

## Executed checks

```text
ACCEPT,HashMap,n=100,mean_lookup_under_1ms=true
ACCEPT,TreeMap,n=100,mean_lookup_under_1ms=true
ACCEPT,LinkedHashMap,n=100,mean_lookup_under_1ms=true
ACCEPT,HashMap,n=10000,mean_lookup_under_1ms=true
ACCEPT,TreeMap,n=10000,mean_lookup_under_1ms=true
ACCEPT,LinkedHashMap,n=10000,mean_lookup_under_1ms=true
ACCEPT,HashMap,n=100000,mean_lookup_under_1ms=true
ACCEPT,TreeMap,n=100000,mean_lookup_under_1ms=true
ACCEPT,LinkedHashMap,n=100000,mean_lookup_under_1ms=true
PASS: all maps satisfy both report orders; sorted-array lookup verified
Measured experiments: 36; each includes 3 warmups + 5 raw runs.
```

## Measured results

Times below are measured in the workspace, not the student device. Setup is excluded. Each mean uses five timed runs after three warmups. Results include observable consumption overhead.

| Experiment | Operations/run | Mean batch ms | Mean ns/op | Sample CV |
|---|---:|---:|---:|---:|
| HashMap_n100_put | 100 | 0.094403 | 944.026 | 79.0% |
| HashMap_n100_lookup | 100000 | 0.994013 | 9.940 | 47.1% |
| HashMap_n100_code_report | 1 | 0.129148 | 129148.000 | 25.5% |
| HashMap_n100_insertion_report | 1 | 0.081788 | 81787.800 | 21.9% |
| TreeMap_n100_put | 100 | 0.152737 | 1527.372 | 58.1% |
| TreeMap_n100_lookup | 100000 | 6.494930 | 64.949 | 5.1% |
| TreeMap_n100_code_report | 1 | 0.050457 | 50457.200 | 91.4% |
| TreeMap_n100_insertion_report | 1 | 0.104860 | 104859.800 | 18.7% |
| LinkedHashMap_n100_put | 100 | 0.115654 | 1156.540 | 76.6% |
| LinkedHashMap_n100_lookup | 100000 | 0.909491 | 9.095 | 13.9% |
| LinkedHashMap_n100_code_report | 1 | 0.118414 | 118414.000 | 18.1% |
| LinkedHashMap_n100_insertion_report | 1 | 0.010756 | 10756.200 | 4.9% |
| HashMap_n10000_put | 10000 | 1.852282 | 185.228 | 25.7% |
| HashMap_n10000_lookup | 100000 | 2.485041 | 24.850 | 25.3% |
| HashMap_n10000_code_report | 1 | 3.316822 | 3316822.000 | 31.0% |
| HashMap_n10000_insertion_report | 1 | 2.580619 | 2580619.000 | 10.8% |
| TreeMap_n10000_put | 10000 | 3.078247 | 307.825 | 16.5% |
| TreeMap_n10000_lookup | 100000 | 19.933389 | 199.334 | 5.6% |
| TreeMap_n10000_code_report | 1 | 0.699817 | 699816.800 | 30.1% |
| TreeMap_n10000_insertion_report | 1 | 3.741728 | 3741728.400 | 17.7% |
| LinkedHashMap_n10000_put | 10000 | 0.611977 | 61.198 | 6.0% |
| LinkedHashMap_n10000_lookup | 100000 | 2.763162 | 27.632 | 29.0% |
| LinkedHashMap_n10000_code_report | 1 | 3.142980 | 3142979.800 | 14.2% |
| LinkedHashMap_n10000_insertion_report | 1 | 0.080702 | 80702.000 | 14.8% |
| HashMap_n100000_put | 100000 | 26.969367 | 269.694 | 75.1% |
| HashMap_n100000_lookup | 100000 | 3.671989 | 36.720 | 12.4% |
| HashMap_n100000_code_report | 1 | 14.180188 | 14180187.800 | 9.9% |
| HashMap_n100000_insertion_report | 1 | 25.590797 | 25590796.600 | 17.5% |
| TreeMap_n100000_put | 100000 | 80.889520 | 808.895 | 8.5% |
| TreeMap_n100000_lookup | 100000 | 50.233908 | 502.339 | 2.7% |
| TreeMap_n100000_code_report | 1 | 1.019359 | 1019358.800 | 4.9% |
| TreeMap_n100000_insertion_report | 1 | 26.775485 | 26775485.400 | 12.3% |
| LinkedHashMap_n100000_put | 100000 | 28.197148 | 281.971 | 39.2% |
| LinkedHashMap_n100000_lookup | 100000 | 7.413168 | 74.132 | 30.0% |
| LinkedHashMap_n100000_code_report | 1 | 83.575968 | 83575967.600 | 11.9% |
| LinkedHashMap_n100000_insertion_report | 1 | 0.746921 | 746921.000 | 52.4% |

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
