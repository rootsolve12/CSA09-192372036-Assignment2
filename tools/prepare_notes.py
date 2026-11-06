from pathlib import Path
import csv,json,statistics
root=Path(__file__).resolve().parents[1]
notes={
'Q1':('Ticket construction',
'''Build identical tickets using immutable String concatenation, StringBuilder and StringBuffer. Predict quadratic cumulative copying for repeated + as fixed-length field counts rise, and linear amortized content processing for builders. Compare one 5-field ticket, 10,000 50-field tickets, and 100 1,000-field tickets. Every field has six characters.

String immutability requires a new accumulated result after each concatenation. The source-level model for n fields of length l is l*n*(n+1)/2 copied characters, versus O(L) builder work for L output characters. These estimates are models, not allocations measured by a profiler. Repeated + can allocate O(n^2) characters cumulatively while peak reachable content remains O(n). Uncollected garbage can increase observed heap occupancy.

Use a separate StringBuilder per ticket. StringBuffer synchronizes methods; a logical record spanning several appends still needs one shared lock to prevent interleaving. The program validates 400 complete records from four threads. A real shared log also needs bounded buffering and persistence.

String.join("", fields) and a StringJoiner("") are linear-content alternatives, checked for equivalent output. String.format("PNR: %s%nFare: %.2f", pnr, fare) is useful for fixed templates, with parsing/conversion overhead. Do not repeatedly format the entire growing prefix.

Heap before/after values are snapshots, not total allocation or retained-memory measurements. Negative deltas are possible after garbage collection. Small-workload timings are noisy. StringBuffer may outperform StringBuilder in this run; JVM optimization and measurement order prevent inferring a universal ordering. Compare all raw repetitions before drawing conclusions.'''),
'Q2':('Playlist collection selection',
'''Predict O(1) ArrayList indexed access, O(n) front/middle shifts, O(1) amortized append; LinkedList has O(1) endpoint edits but O(n) indexed middle location. Iterating either is O(n). Test 100, 10,000 and 100,000 songs with separate insertion/removal batches, 1,000 indexed reads, and ten full traversals. Song objects are created before timing. Inserts grow the list by 1,000; removal setup creates the same-sized starting state outside timing. These are batch measurements, not isolated edits at exactly n elements.

| Operation | ArrayList | LinkedList | ArrayDeque |
|---|---|---|---|
| Append | O(1) amortized | O(1) | O(1) amortized |
| Insert/remove front | O(n) | O(1) | O(1) amortized |
| Indexed middle insertion/removal | O(n) | O(n) | No indexed API; O(n) manual reconstruction |
| Get kth | O(1) | O(min(k,n-1-k)) | No indexed API; O(n) traversal |
| Traverse | O(n) | O(n) | O(n) |
| Storage | reference array + spare capacity | node + two links per item | circular reference array + spare capacity |

A LinkedList iterator already positioned at a node can insert/remove in O(1), but locating that position is not free. A plain array provides O(1) get; System.arraycopy supports O(n) manual shifts, while full capacity requires replacement allocation.

Recommend ArrayList for append-heavy playlists with random track access and sequential display. Stop recommending it when measured repeated shifting exceeds an explicit UI budget (for example 16 ms for a whole UI update); total operation frequency matters more than an arbitrary size threshold. Keep Up Next in ArrayDeque for frequent endpoint edits. The deque end-cycle benchmark counts four endpoint operations per cycle. Its composite cycle is not directly equivalent to every list workload.'''),
'Q3':('Attendance equality and hashing',
'''Three predictions are printed before their tests: same reference twice gives size 1; distinct equal objects without hashCode usually give size 2; mutating a hashed key from A to B retains size 1 but prevents lookup/removal in this example. Inherited hash codes can collide, so the broken distinct-object result is an observation, not a guaranteed constant.

HashSet routes by a hash before checking candidate equality. If a.equals(b), both must have the same hashCode. Equal hashes do not imply equal objects. Mutating a relevant field leaves an entry stored under its original hash. The corrected supplied Student uses a final regNo for equals and hashCode. Keep before and after programs in separate build directories because both declare Student and Attendance.

| Structure | Per scan | Storage |
|---|---|---|
| HashSet with correct contract | O(1) average | O(n) |
| TreeSet with regNo comparator | O(log n) | O(n) |
| HashMap keyed by regNo | O(1) average | O(n) |

The investigation implements and checks all three corrected alternatives, plus equality symmetry, reflexivity, null handling and equal hashes. The after program must print Unique students: 2.'''),
'Q4':('Pharmacy map engineering',
'''Predict average O(1) HashMap/LinkedHashMap put/get and O(log n) TreeMap put/get. TreeMap iteration is O(n), LinkedHashMap iteration O(n), and HashMap iteration O(n+capacity). Test 100, 10,000 and 100,000 medicines, 100,000 seeded successful lookups per map and both complete reports. Input medicine allocation is outside timing; put timings include entry creation/resizing.

A TreeMap naturally gives code order; default LinkedHashMap gives insertion order. Sorting a value copy costs O(n log n). All entries retain a sequence number so insertion order is reconstructable even for HashMap and TreeMap. Without that metadata, insertion order cannot generally be recovered. Report timing includes ordering, materialization and traversal, but excludes console/file/network output. Separate query Strings avoid relying on stored-key reference identity; warmed query hash codes may be cached.

Define acceptable mean in-memory lookup as <1 ms at the largest size. This classroom criterion is not a clinical response standard and says nothing about tail latency. Inspect ACCEPT records and measured means. A LinkedHashMap is a defensible lookup-heavy design with daily code-sorted copies if its measured average meets the budget; insertion order requires no extra sort. Use TreeMap when frequent range/code-order queries justify it. Remaining cost includes hashing/comparison, allocation, GC and output. Concurrency and consistent reporting snapshots require additional design.

Hashtable is a legacy synchronized map; prefer appropriate confinement/locking or ConcurrentHashMap for concurrent access.

Sorted-array alternative: sort Medicine[] once and use Arrays.binarySearch with a code comparator (verified in code). Lookup O(log n), insertion O(n) shifts, code report O(n), storage O(n). HashMap gives average O(1) lookup/insertion plus report sorting; TreeMap gives O(log n) lookup/insertion and O(n) ordered reports.'''),
'Q5':('Reusable generic ranking',
'''Choose Comparator<? super T> in an unbounded generic Leaderboard<T>: the ranking policy is supplied independently of the item type. A Comparable implementation would instead use T extends Comparable<? super T>. List<? extends T> is useful for bulk input; the GenericBoard test demonstrates addAll. Comparator chains express points descending, goal difference descending, name ascending.

Lexicographic comparison is transitive because each component order is transitive and the first unequal component decides. Integer comparison avoids subtraction overflow. Reversing only the numeric component comparators preserves name A-Z. Fully equal ranking tuples compare as zero. Supplied Team uses identity equals, so equal rank is not object identity; use care with TreeSet, which treats comparison zero as duplicate.

The supplied output is [Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]. Bisons beats Panthers alphabetically after both numerical ties. Additional data includes repeated identical tuples and ties at every level. An adversarial Integer.MAX_VALUE/MIN_VALUE pair exposes subtraction overflow. Tests check sign symmetry, transitivity on the data, input preservation and stable-sort equivalence.

Comparable alternative: declare Team implements Comparable<Team> and implement compareTo using the same chain. A chained Comparator and Comparable each need one O(n log n) sort. Three stable sorts (name, goal difference descending, points descending) need three sorts, still O(n log n) asymptotically. Preserving the input costs O(n) for a copy; object sorting can require O(n) workspace. Comparator injection best supports players, colleges and changing policies.

The baseline benchmark measures 20 unchanged rankings at 100, 1,000 and 10,000 teams. Its repeated copy-and-sort operation is diagnosed and redesigned in Q7. Both tasks use the same seed and data-generation rules.'''),
'Q6':('Priority triage and fairness',
'''Design before the supplied code: represent each patient with severity and a unique arrival sequence. A heap ordered by (severity ascending, arrival ascending) serves urgent cases first and preserves FIFO among equal severities. A plain FIFO cannot prioritize a later critical case. poll removes the head; copying the heap and polling the copy displays ordered waiting patients without consuming the original. Heap iteration is not sorted.

The eight-patient hand order is Meena, Kumar, Lata, Noor, Ravi, John, Priya, Asha. The supplied six-patient program must print Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5).

Predict O(log n) heap work for admit/remove, with occasional O(n) backing-array growth for admission (amortized O(log n)). A random insertion batch may need fewer comparisons; timings cannot prove a worst-case bound. The benchmark tests batches of 100, 10,000 and 100,000 pre-created patients. Serving all patients has O(n log n) upper bound. Patient allocation is excluded.

| Design | Admit | Serve | Space |
|---|---|---|---|
| PriorityQueue | O(log n) amortized | O(log n) | O(n) |
| Sorted LinkedList | O(n) | O(1) | O(n) |
| Five ArrayDeque FIFO queues | O(1) amortized | O(1), scan at most 5 heads | O(n) |

Five queues exploit the fixed five severity levels, while a heap handles arbitrary priority policies more generally. Pseudocode for buckets: admit -> buckets[s-1].addLast(p); next -> inspect severity 1..5 and pollFirst from the first nonempty bucket. Sorted-list insertion scans until the comparator position, then inserts; pollFirst serves.

Strict priority can starve minor cases. Simulation rule: effectiveSeverity=max(1,originalSeverity-floor(wait/T)); retain original arrival for ties. Freeze time and rebuild the heap at each review. Never use a changing clock comparator without restoring heap order. The code demonstrates a frozen-time promotion. This modifies strict severity policy and is a software simulation, not an approved clinical rule.

5,000/day is about 0.058 arrivals/s (one every 17.28 s) on average, not a peak-load guarantee. Inspect measured queue costs and budget for anticipated peak waiting population. A complete deployment also needs clinical reassessment, concurrency, persistence, audit and recovery. PriorityQueue and the supplied counter are single-threaded.'''),
'Q7':('Diagnose and cache repeated rankings',
'''A. Diagnose: Q5 copies and sorts an unchanged leaderboard on every request. The bottleneck is repeated sorting and result-list creation, not comparator correctness. Use Q5 baseline and Q7 Original measurements as evidence; different JVM invocations can have different absolute timings.

B. Objective: preserve identical ordered results; reduce 20 unchanged requests from 20 sorts to one; invalidate after updates; prohibit caller mutation of the cached snapshot.

C. Redesign: keep items, immutable sorted snapshot and dirty flag. ADD appends then marks dirty. RANKING copies/sorts only when dirty, wraps the copy as unmodifiable, clears dirty and returns the snapshot. Teams have final fields; a future score-update method must invalidate the cache. No external in-place score mutation is supported. The old snapshot remains valid after a later update.

D. Predict: unchanged workload -> 20 versus 1 sort; changing workload (one update before every read) -> both 20 sorts and little/no speed benefit. Prediction lines are printed before experiments.

E. Validate: same seeded teams, comparator and 20 requests at n=100, 1,000 and 10,000. Timing uses an ordinary comparator; counts are measured separately. The cache starts dirty, so its first sort is included. Checks establish exact list equality, update invalidation, old-snapshot isolation and immutability. An update-before-every-read counterexample tests the operating boundary.

F. Evaluation: inspect observed times rather than assuming the cache always wins. Sort counts and comparison counts directly validate reduced repeated work. Original cost for r unchanged requests is O(r*n*log n); cache cost O(n*log n+r), excluding later traversal/rendering. Both need O(n) peak content storage, but the cache retains an O(n) snapshot. Each displayed full list adds O(n) traversal regardless of caching. Original may suit rare requests or changes before every request. Concurrency requires locking or safe snapshot publication. The returned-list contract changes from mutable copies to immutable snapshots.''')}
for q,(title,theory) in notes.items():
 task=root/q
 results=task/'results'
 text=f'# {q} — {title}\n\n## Aim and theory\n\n{theory}\n\n## Compile and run\n\nFrom the repository root:\n\n```bash\npython tools/run.py {q}\n```\n\nRequires Python 3 and a Java 17+ JDK. On Windows, use `py tools/run.py '+q+'` if `python` is unavailable. Compilation uses `javac`; the bundled Java compiler module is a fallback. No third-party libraries are required.\n\n'
 text+='## Executed checks\n\n```text\n'+(results/'checks.txt').read_text()+'```\n\n'
 if (results/'measurements.csv').exists():
  rows=list(csv.DictReader((results/'measurements.csv').open()))
  groups={}
  for row in rows:
   if row['kind']=='RAW':groups.setdefault(row['experiment'],[]).append(float(row['elapsed_ns']))
  text+='## Measured results\n\nTimes below are measured in the workspace, not the student device. Setup is excluded. Each mean uses five timed runs after three warmups. Results include observable consumption overhead.\n\n| Experiment | Operations/run | Mean batch ms | Mean ns/op | Sample CV |\n|---|---:|---:|---:|---:|\n'
  for row in rows:
   if row['kind']!='MEAN':continue
   samples=groups[row['experiment']];cv=statistics.stdev(samples)/statistics.mean(samples)*100
   text+=f"| {row['experiment']} | {row['operations']} | {float(row['elapsed_ns'])/1e6:.6f} | {float(row['ns_per_operation']):.3f} | {cv:.1f}% |\n"
  text+='\nCV is sample standard deviation divided by mean. High variation weakens fine-grained speed claims. A fixed algorithm order, one JVM per program, small samples, JIT and GC are limitations; these are educational measurements, not JMH results. Complexity is supported by reasoning and counts, not inferred solely from time ratios.\n\n'
 text+='## Files and evidence\n\n- Java sources: this task directory (Q3 separates before/after).\n- `results/execution.txt`: complete captured execution.\n- `results/environment.json`: actual JVM, system and execution origin.\n- `results/checks.txt`: correctness/operation-count summary.\n- `results/measurements.csv` and `results/averages.csv`: raw timings and averages where applicable.\n- `screenshots/compiler.jpg`: actual desktop terminal source/compilation capture.\n- `screenshots/output.jpg`: actual desktop terminal capture of saved execution checks; full raw output is in results.\n\nStudents must rerun and explain these programs on their own machine before submission. Workspace evidence is not represented as personal execution.\n'
 (task/'README.md').write_text(text)
 (task/'input_design.json').write_text(json.dumps({'task':q,'input_kind':'Deterministic in-source data; no interactive input required','see':'Java main methods and README.md for all sizes, fields, predictions and fixed seeds','register_number':'192372036'},indent=2)+'\n')
 (task/'screenshots').mkdir(exist_ok=True)
root_readme='''# CSA09 — Assignment 2 (CO2)

| Field | Value |
|---|---|
| Student name | Not provided — add before final submission |
| Register number | 192372036 |
| Course code | CSA09 — Programming in Java |
| Repository | https://github.com/rootsolve12/CSA09-192372036-Assignment2 |
| Tested JVM | OpenJDK 21.0.13; programs target Java 17+ |

## Scope

Q1–Q7 source code, completed supplied programs, designed inputs, measured execution evidence and analysis. Word/PDF reporting and GCR/SIMATS upload are deferred as requested.

## Run everything

Install a Java 17+ JDK and Python 3. No Java dependencies or build tool are required.

```bash
java -version
javac -version
python tools/run.py
```

On Windows, `py tools/run.py` also works. Run one task with `python tools/run.py Q3`. Each task compiles into its own ignored build directory. Q3 before/after are deliberately isolated because class names overlap. Java receives `-Xms128m -Xmx512m`. Run the same command again to regenerate results; preserve earlier measurements if comparing machines.

Manual alternative for the supplied programs:

```bash
cd Q5
javac LeaderboardDemo.java
java LeaderboardDemo
```

## Task index

| Task | Topic | Result |
|---|---|---|
| [Q1](Q1/README.md) | String construction | Identical ticket output; three workloads; shared-log check |
| [Q2](Q2/README.md) | Lists and deque | Three sizes; separate edit/read/iteration measurements |
| [Q3](Q3/README.md) | Equality and hashing | Before/after execution; three investigations; corrected de-duplication |
| [Q4](Q4/README.md) | Maps | 100,000 lookups per case; both report orders verified |
| [Q5](Q5/README.md) | Generic ranking | Expected ranking; ties, overflow, transitivity and baseline timings |
| [Q6](Q6/README.md) | Priority triage | Six- and eight-patient checks; growth benchmark; ageing simulation |
| [Q7](Q7/README.md) | Redesign | Cached leaderboard; unchanged and update-heavy validation |

## Evidence and interpretation

Actual workspace execution is recorded in each `results/environment.json`; it is not claimed to be execution on the student's computer. Three warmups precede five raw measurements for every benchmark case. All raw runs, means and checks are included. Screenshots capture a real desktop terminal, with source/compiler evidence and saved output checks. They do not replace full logs or repeated measurements.

Small samples, fixed benchmark order, JVM optimization, garbage collection and environment load limit timing conclusions. High variation is reported in task tables. Heap snapshots in Q1 are not allocation profiling. Per-operation means are averages, not worst-case or percentile latency. Task explanations include operating boundaries and alternatives.

## Before final submission

- Add the student name.
- Personally execute and review every program; retain code/compiler and output screenshots from that execution.
- Preserve input designs and raw measurements, and be ready to explain them.
- Prepare the required Word/PDF report later, with the faculty's filename format.
- Submit the report in both GCR and SIMATS.

Commits reflect actual preparation in this session; no dates or progressive-work history are fabricated.
'''
(root/'README.md').write_text(root_readme)
(root/'.gitignore').write_text('**/build/\n*.class\n__pycache__/\n')
print('Prepared task notes, measured tables, input manifests and README')
