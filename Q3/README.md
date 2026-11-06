# Q3 — Attendance equality and hashing

## Aim and theory

Three predictions are printed before their tests: same reference twice gives size 1; distinct equal objects without hashCode usually give size 2; mutating a hashed key from A to B retains size 1 but prevents lookup/removal in this example. Inherited hash codes can collide, so the broken distinct-object result is an observation, not a guaranteed constant.

HashSet routes by a hash before checking candidate equality. If a.equals(b), both must have the same hashCode. Equal hashes do not imply equal objects. Mutating a relevant field leaves an entry stored under its original hash. The corrected supplied Student uses a final regNo for equals and hashCode. Keep before and after programs in separate build directories because both declare Student and Attendance.

| Structure | Per scan | Storage |
|---|---|---|
| HashSet with correct contract | O(1) average | O(n) |
| TreeSet with regNo comparator | O(log n) | O(n) |
| HashMap keyed by regNo | O(1) average | O(n) |

The investigation implements and checks all three corrected alternatives, plus equality symmetry, reflexivity, null handling and equal hashes. The after program must print Unique students: 2.

## Compile and run

From the repository root:

```bash
python tools/run.py Q3
```

Requires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py Q3` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.

## Executed checks

```text
=== Attendance_before ===
Unique students: 3
=== Attendance_after ===
Unique students: 2
=== ContractInvestigation ===
PREDICT test1: same object twice -> size 1
OBSERVED test1 size=1
PREDICT test2: distinct equal objects -> usually size 2
OBSERVED equals=true,hashA=692404036,hashB=1554874502,size=2
PREDICT test3: mutation A -> B retains size 1; lookup fails
OBSERVED before mutation contains=true
OBSERVED after mutation contains=false,removed=false,size=1
PASS: fixed HashSet, TreeSet and HashMap each retain 2 students
PREDICT test1: same object twice -> size 1
OBSERVED test1 size=1
PREDICT test2: distinct equal objects -> usually size 2
OBSERVED equals=true,hashA=692404036,hashB=1554874502,size=2
PREDICT test3: mutation A -> B retains size 1; lookup fails
OBSERVED before mutation contains=true
OBSERVED after mutation contains=false,removed=false,size=1
PASS: fixed HashSet, TreeSet and HashMap each retain 2 students
```

## Files and evidence

- Java sources: this task directory (Q3 separates before/after).
- `results/execution.txt`: complete captured execution.
- `results/environment.json`: actual JVM, system and execution origin.
- `results/checks.txt`: correctness/operation-count summary.
- `results/measurements.csv` and `results/averages.csv`: raw timings and averages where applicable.
- `screenshots/compiler.jpg`: actual desktop terminal source/compilation capture.
- `screenshots/output.jpg`: actual desktop terminal capture of saved execution checks; full raw output is in results.

Students must rerun and explain these programs on their own machine before submission. Workspace evidence is not represented as personal execution.
