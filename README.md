# CSA09 — Assignment 2 (CO2)

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
