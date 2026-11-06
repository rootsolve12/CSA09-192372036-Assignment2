"""Short terminal view for genuine screenshots; complete logs remain in results/."""
from pathlib import Path
import sys
import run
q=sys.argv[1];mode=sys.argv[2];root=Path(__file__).resolve().parents[1]
print(q+' | CSA09 | Register 192372036 | Workspace evidence\n',flush=True)
if mode=='compiler':
    specs=run.TASKS[q]
    main,sources,variant=specs[-1]
    base=root/q/variant if variant in ('before','after') else root/q
    source=base/sources[-1]
    print('Source: '+str(source.relative_to(root)),flush=True)
    print('\n'.join(f'{i+1:2}: {line}' for i,line in enumerate(source.read_text().splitlines()[:10])),flush=True)
    print('\nCompiling with: '+' '.join(run.compiler()),flush=True)
    run.compile_task(q)
    for name,_,variant in specs:print('COMPILE PASS: '+variant+'/'+name,flush=True)
else:
    lines=(root/q/'results'/'checks.txt').read_text().splitlines()
    if len(lines)>15:lines=lines[:2]+['[middle checks omitted; see results/checks.txt]']+lines[-11:]
    print('\n'.join(lines),flush=True)
    print('\nFull captured execution: '+q+'/results/execution.txt',flush=True)
