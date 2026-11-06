import java.util.*;
public class TriageBenchmark {
    static final class Entry {
        final String name;final int severity;final long arrival;
        Entry(String name,int severity,long arrival){
            if(severity<1||severity>5)throw new IllegalArgumentException("Severity must be 1..5");
            this.name=name;this.severity=severity;this.arrival=arrival;
        }
        @Override public String toString(){return name+"(S"+severity+")";}
    }
    static final Comparator<Entry> ORDER=Comparator.comparingInt((Entry p)->p.severity)
            .thenComparingLong(p->p.arrival);
    static PriorityQueue<Entry> populated(Entry[] data){
        PriorityQueue<Entry> q=new PriorityQueue<>(ORDER);Collections.addAll(q,data);return q;
    }
    static List<String> names(PriorityQueue<Entry> q){
        List<String> names=new ArrayList<>();while(!q.isEmpty())names.add(q.poll().name);return names;
    }
    static int aged(Entry p,long now,long interval){
        long steps=Math.max(0,now-p.arrival)/interval;
        return (int)Math.max(1L,p.severity-steps);
    }
    public static void main(String[] args){
        Entry[] example={new Entry("Ravi",3,0),new Entry("Meena",1,1),
            new Entry("John",3,2),new Entry("Asha",5,3),new Entry("Kumar",1,4),
            new Entry("Lata",2,5),new Entry("Noor",2,6),new Entry("Priya",4,7)};
        List<String> expected=Arrays.asList("Meena","Kumar","Lata","Noor","Ravi","John","Priya","Asha");
        System.out.println("PREDICT: "+expected);
        PriorityQueue<Entry> live=populated(example);
        System.out.println("Heap iteration (not treatment order): "+live);
        List<String> observed=names(new PriorityQueue<>(live));
        Bench.check(observed.equals(expected)&&live.size()==8,"Hand order/snapshot");
        System.out.println("OBSERVED: "+observed);
        // Fixed-time simulation: timestamps and interval use the same arbitrary unit.
        Entry oldMinor=new Entry("OldMinor",5,0),newCritical=new Entry("NewCritical",1,100);
        List<Entry> waiting=Arrays.asList(oldMinor,newCritical);long now=100,interval=20;
        Comparator<Entry> frozen=Comparator.comparingInt((Entry p)->aged(p,now,interval))
                .thenComparingLong(p->p.arrival);
        PriorityQueue<Entry> agedQueue=new PriorityQueue<>(frozen);agedQueue.addAll(waiting);
        Bench.check(agedQueue.poll()==oldMinor,"Ageing rebuild");
        System.out.println("Ageing simulation: OldMinor promoted to effective severity 1");
        Bench.header();
        for(int n:new int[]{100,10000,100000}){
            Random random=new Random(36);Entry[] patients=new Entry[n];
            for(int i=0;i<n;i++)patients[i]=new Entry("P"+i,1+random.nextInt(5),i);
            Bench.measure("PriorityQueue_n"+n+"_admit",n,()->new PriorityQueue<Entry>(ORDER),q->{
                Collections.addAll(q,patients);return q.size();});
            Bench.measure("PriorityQueue_n"+n+"_serve",n,()->populated(patients),q->{
                long sum=0;while(!q.isEmpty())sum+=q.poll().arrival;return sum;});
        }
        System.out.println("PASS: hand order, non-destructive display and frozen-time ageing");
    }
}
