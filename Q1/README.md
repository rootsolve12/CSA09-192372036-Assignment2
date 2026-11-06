# Q1 — Ticket construction

## Aim and theory

Build identical tickets using immutable String concatenation, StringBuilder and StringBuffer. Predict quadratic cumulative copying for repeated + as fixed-length field counts rise, and linear amortized content processing for builders. Compare one 5-field ticket, 10,000 50-field tickets, and 100 1,000-field tickets. Every field has six characters.

String immutability requires a new accumulated result after each concatenation. The source-level model for n fields of length l is l*n*(n+1)/2 copied characters, versus O(L) builder work for L output characters. These estimates are models, not allocations measured by a profiler. Repeated + can allocate O(n^2) characters cumulatively while peak reachable content remains O(n). Uncollected garbage can increase observed heap occupancy.

Use a separate StringBuilder per ticket. StringBuffer synchronizes methods; a logical record spanning several appends still needs one shared lock to prevent interleaving. The program validates 400 complete records from four threads. A real shared log also needs bounded buffering and persistence.

String.join("", fields) and a StringJoiner("") are linear-content alternatives, checked for equivalent output. String.format("PNR: %s%nFare: %.2f", pnr, fare) is useful for fixed templates, with parsing/conversion overhead. Do not repeatedly format the entire growing prefix.

Heap before/after values are snapshots, not total allocation or retained-memory measurements. Negative deltas are possible after garbage collection. Small-workload timings are noisy. StringBuffer may outperform StringBuilder in this run; JVM optimization and measurement order prevent inferring a universal ordering. Compare all raw repetitions before drawing conclusions.

## Compile and run

From the repository root:

```bash
python tools/run.py Q1
```

Requires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py Q1` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.

## Executed checks

```text
CHECK,shared_log_records=400,PASS
PASS: all ticket builders produce identical content
Measured experiments: 9; each includes 3 warmups + 5 raw runs.
```

## Measured results

Times below are measured in the workspace, not the student device. Setup is excluded. Each mean uses five timed runs after three warmups. Results include observable consumption overhead.

| Experiment | Operations/run | Mean batch ms | Mean ns/op | Sample CV |
|---|---:|---:|---:|---:|
| StringPlus_tickets1_fields5 | 1 | 0.009572 | 9572.400 | 23.0% |
| StringBuilder_tickets1_fields5 | 1 | 0.006446 | 6445.600 | 94.1% |
| StringBuffer_tickets1_fields5 | 1 | 0.002458 | 2457.600 | 36.8% |
| StringPlus_tickets10000_fields50 | 10000 | 44.045213 | 4404.521 | 156.3% |
| StringBuilder_tickets10000_fields50 | 10000 | 19.895016 | 1989.502 | 105.2% |
| StringBuffer_tickets10000_fields50 | 10000 | 3.804057 | 380.406 | 18.6% |
| StringPlus_tickets100_fields1000 | 100 | 28.695382 | 286953.824 | 9.4% |
| StringBuilder_tickets100_fields1000 | 100 | 0.830868 | 8308.684 | 12.1% |
| StringBuffer_tickets100_fields1000 | 100 | 0.962686 | 9626.862 | 15.9% |

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
