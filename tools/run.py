#!/usr/bin/env python3
"""Compile and execute isolated tasks. Uses javac or the bundled compiler module."""
from pathlib import Path
import argparse, csv, json, os, platform, shutil, subprocess, sys, time
ROOT=Path(__file__).resolve().parents[1]
TASKS={
 'Q1':[('TicketBenchmark',['Bench.java','TicketBenchmark.java'],'.')],
 'Q2':[('PlaylistBenchmark',['Bench.java','PlaylistBenchmark.java'],'.')],
 'Q3':[('Attendance',['Attendance.java'],'before'),('Attendance',['Attendance.java'],'after'),
       ('ContractInvestigation',['ContractInvestigation.java'],'.')],
 'Q4':[('InventoryBenchmark',['Bench.java','InventoryBenchmark.java'],'.')],
 'Q5':[('LeaderboardDemo',['LeaderboardDemo.java'],'demo'),
       ('RankingTests',['Bench.java','LeaderboardDemo.java','RankingTests.java'],'.')],
 'Q6':[('Triage',['Triage.java'],'demo'),
       ('TriageBenchmark',['Bench.java','TriageBenchmark.java'],'.')],
 'Q7':[('LeaderboardRedesign',['Bench.java','LeaderboardRedesign.java'],'.')]
}
def compiler():
 return ['javac'] if shutil.which('javac') else ['java','-m','jdk.compiler/com.sun.tools.javac.Main']
def compile_task(q,visible=False):
 task=ROOT/q
 for main,sources,variant in TASKS[q]:
  source_dir=task/variant if variant in ('before','after') else task
  target=task/'build'/variant.replace('.','main')/main
  target.mkdir(parents=True,exist_ok=True)
  command=compiler()+['-encoding','UTF-8','-d',str(target)]+[str(source_dir/s) for s in sources]
  if visible:
   print('Source:',(source_dir/sources[-1]).relative_to(ROOT),flush=True)
   lines=(source_dir/sources[-1]).read_text().splitlines()
   print('\n'.join(f'{i+1:3}: {s}' for i,s in enumerate(lines[:16])),flush=True)
   print('\nCompiler: '+' '.join(compiler())+' -encoding UTF-8 -d build ...',flush=True)
  result=subprocess.run(command,capture_output=True,text=True)
  if result.returncode: raise RuntimeError(result.stdout+result.stderr)
  if visible: print(f'COMPILE PASS: {q}/{variant}/{main}\n',flush=True)
 return True

def run_task(q):
 compile_task(q)
 task=ROOT/q;out=task/'results';out.mkdir(exist_ok=True)
 env=subprocess.run(['java','-version'],capture_output=True,text=True)
 metadata={'register_number':'192372036','course':'CSA09','task':q,
           'executed_utc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime()),
           'origin':'Executed in the ChatGPT workspace, not on the student device',
           'java':(env.stdout+env.stderr).strip(),'platform':platform.platform(),
           'processor':platform.processor(),'compiler':' '.join(compiler()),
           'warmup_runs':3,'measured_runs':5,'random_seed':36,
           'jvm_options':['-Xms128m','-Xmx512m']}
 (out/'environment.json').write_text(json.dumps(metadata,indent=2)+'\n')
 records=[];combined=[];checks=[]
 for main,sources,variant in TASKS[q]:
  target=task/'build'/variant.replace('.','main')/main
  cmd=['java','-Xms128m','-Xmx512m','-cp',str(target),main]
  result=subprocess.run(cmd,capture_output=True,text=True,timeout=180)
  label=main+('_'+variant if variant in ('before','after') else '')
  text=result.stdout+result.stderr
  (out/(label+'.txt')).write_text(text)
  if result.returncode:raise RuntimeError(q+' '+text)
  if q=='Q3' and variant=='after' and text.strip()!='Unique students: 2':
   raise AssertionError('Corrected Attendance output mismatch')
  if main=='LeaderboardDemo' and text.strip()!='[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]':
   raise AssertionError('Leaderboard expected output mismatch')
  if main=='Triage' and text.strip()!='Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)':
   raise AssertionError('Triage expected output mismatch')
  combined+=['=== '+label+' ===',text]
  if not any(line.startswith('RAW,') for line in text.splitlines()):
   checks+=['=== '+label+' ===']+text.splitlines()
  for line in text.splitlines():
   if line.startswith(('RAW,','MEAN,')):records.append(next(csv.reader([line])))
   elif line.startswith(('PASS','CHECK','COUNTS','SPEEDUP','ACCEPT','PREDICT','OBSERVED','Ageing')):
    checks.append(line)
 (out/'execution.txt').write_text('\n'.join(combined)+'\n')
 if records:
  with (out/'measurements.csv').open('w',newline='') as stream:
   writer=csv.writer(stream);writer.writerow(['kind','experiment','run','operations','elapsed_ns','ns_per_operation']);writer.writerows(records)
  means=[r for r in records if r[0]=='MEAN']
  with (out/'averages.csv').open('w',newline='') as stream:
   writer=csv.writer(stream);writer.writerow(['experiment','operations','mean_elapsed_ns','mean_ns_per_operation']);writer.writerows([r[1:2]+r[3:] for r in means])
  checks.append('Measured experiments: '+str(len(means))+'; each includes 3 warmups + 5 raw runs.')
 (out/'checks.txt').write_text('\n'.join(checks)+'\n')
 print(q+': compiled and executed successfully; '+str(len(records))+' measurement rows',flush=True)

def main():
 p=argparse.ArgumentParser();p.add_argument('task',nargs='?',choices=list(TASKS));p.add_argument('--compile-only',action='store_true');p.add_argument('--show',action='store_true');args=p.parse_args()
 tasks=[args.task] if args.task else list(TASKS)
 for q in tasks:
  if args.show:
   print(q+' | Register 192372036 | CSA09 | workspace execution evidence')
   print((ROOT/q/'results'/'checks.txt').read_text())
  elif args.compile_only:compile_task(q,visible=True)
  else:run_task(q)
if __name__=='__main__':main()
