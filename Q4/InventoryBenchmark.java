import java.util.*;
public class InventoryBenchmark {
    static final class Medicine {
        final String code;
        final int stock,sequence;
        Medicine(String code,int stock,int sequence){
            this.code=code;this.stock=stock;this.sequence=sequence;
        }
    }
    static Map<String,Medicine> make(String type,List<Medicine> medicines) {
        Map<String,Medicine> map=type.equals("TreeMap")?new TreeMap<>():
                type.equals("LinkedHashMap")?new LinkedHashMap<>():new HashMap<>();
        for(Medicine m:medicines)map.put(m.code,m);
        return map;
    }
    static List<Medicine> report(Map<String,Medicine> map,boolean sorted) {
        List<Medicine> rows=new ArrayList<>(map.values());
        if(sorted&&!(map instanceof SortedMap))
            rows.sort(Comparator.comparing(m->m.code));
        if(!sorted&&!(map instanceof LinkedHashMap))
            rows.sort(Comparator.comparingInt(m->m.sequence));
        return rows;
    }
    static long consume(List<Medicine> rows) {
        Bench.retain(rows);long sum=0;
        for(Medicine m:rows)sum+=m.stock+m.sequence;
        return sum;
    }
    static boolean correct(List<Medicine> rows,boolean sorted) {
        for(int i=1;i<rows.size();i++) {
            Medicine a=rows.get(i-1),b=rows.get(i);
            if(sorted?a.code.compareTo(b.code)>0:a.sequence>b.sequence)return false;
        }
        return true;
    }
    public static void main(String[] args) {
        Bench.header();
        for(int n:new int[]{100,10000,100000}) {
            List<String> codes=new ArrayList<>();
            for(int i=0;i<n;i++)codes.add(String.format(Locale.ROOT,"MED%06d",i));
            List<String> insertion=new ArrayList<>(codes);
            Collections.shuffle(insertion,new Random(36));
            List<Medicine> input=new ArrayList<>();
            for(int i=0;i<n;i++)input.add(new Medicine(insertion.get(i),i%500,i));
            Random random=new Random(192372036L);
            String[] queries=new String[100000];
            // Separate String objects avoid assuming the query is the stored key reference.
            for(int i=0;i<queries.length;i++)queries[i]=new String(codes.get(random.nextInt(n)));
            for(String type:new String[]{"HashMap","TreeMap","LinkedHashMap"}) {
                Map<String,Medicine> map=make(type,input);
                Bench.measure(type+"_n"+n+"_put",n,
                        ()->make(type,Collections.emptyList()),state->{
                            for(Medicine m:input)state.put(m.code,m);return state.size();});
                double ns=Bench.measure(type+"_n"+n+"_lookup",queries.length,()->map,state->{
                    long sum=0;for(String key:queries)sum+=state.get(key).stock;return sum;});
                System.out.println("ACCEPT,"+type+",n="+n+",mean_lookup_under_1ms="
                        +(ns/queries.length<1000000));
                for(boolean sorted:new boolean[]{true,false}) {
                    String kind=sorted?"code_report":"insertion_report";
                    Bench.measure(type+"_n"+n+"_"+kind,1,()->map,
                            state->consume(report(state,sorted)));
                    Bench.check(correct(report(map,sorted),sorted),"Incorrect report order");
                }
                Bench.check(map.get("MED000000")!=null&&map.get("MISSING")==null,
                        "Lookup correctness");
            }
            Medicine[] sorted=input.toArray(new Medicine[0]);
            Arrays.sort(sorted,Comparator.comparing(m->m.code));
            int index=Arrays.binarySearch(sorted,new Medicine("MED000000",0,0),
                    Comparator.comparing(m->m.code));
            Bench.check(index==0,"Sorted-array lookup");
        }
        System.out.println("PASS: all maps satisfy both report orders; sorted-array lookup verified");
    }
}
