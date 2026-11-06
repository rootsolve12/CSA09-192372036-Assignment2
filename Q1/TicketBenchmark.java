import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TicketBenchmark {
    static String build(String[] fields,int method) {
        if(method==0) {
            String value="";
            for(String field:fields) value=value+field;
            return value;
        }
        if(method==1) {
            StringBuilder value=new StringBuilder();
            for(String field:fields) value.append(field);
            return value.toString();
        }
        StringBuffer value=new StringBuffer();
        for(String field:fields) value.append(field);
        return value.toString();
    }
    static long batch(String[] fields,int tickets,int method) {
        long sum=0;
        for(int i=0;i<tickets;i++) {
            String ticket=build(fields,method);
            Bench.retain(ticket);
            sum+=ticket.length();
        }
        return sum;
    }
    static long heap() {
        Runtime r=Runtime.getRuntime();
        return r.totalMemory()-r.freeMemory();
    }
    static void sharedLogCheck() throws InterruptedException {
        StringBuffer log=new StringBuffer();
        AtomicLong lines=new AtomicLong();
        Thread[] threads=new Thread[4];
        for(int t=0;t<threads.length;t++) {
            final int id=t;
            threads[t]=new Thread(()->{
                for(int i=0;i<100;i++) {
                    StringBuilder record=new StringBuilder();
                    record.append("thread=").append(id).append(";ticket=").append(i);
                    synchronized(log) { log.append(record).append('\n'); }
                    lines.incrementAndGet();
                }
            });
            threads[t].start();
        }
        for(Thread thread:threads) thread.join();
        Bench.check(log.toString().lines().count()==400,"Interleaved/lost records");
        System.out.println("CHECK,shared_log_records="+lines.get()+",PASS");
    }
    public static void main(String[] args) throws Exception {
        Bench.header();
        int[][] workloads={{1,5},{10000,50},{100,1000}};
        String[] names={"StringPlus","StringBuilder","StringBuffer"};
        for(int[] workload:workloads) {
            int tickets=workload[0],n=workload[1];
            String[] fields=new String[n];
            for(int i=0;i<n;i++) fields[i]=String.format(Locale.ROOT,"F%04d|",i);
            String expected=String.join("",fields);
            StringJoiner joiner=new StringJoiner("");
            for(String field:fields) joiner.add(field);
            Bench.check(expected.equals(joiner.toString()),"Joiner mismatch");
            for(int method=0;method<3;method++) {
                final int m=method;
                Bench.check(expected.equals(build(fields,m)),"Content mismatch");
                String label=names[m]+"_tickets"+tickets+"_fields"+n;
                Bench.measure(label,tickets,()->fields,f->batch(f,tickets,m));
                long before=heap();
                batch(fields,tickets,m);
                long after=heap();
                System.out.println("HEAP,"+label+","+before+","+after+","+(after-before));
            }
            long plusCopied=(long)tickets*6*n*(n+1L)/2;
            System.out.println("MODEL,tickets="+tickets+",fields="+n
                    +",plus_result_strings="+(long)tickets*n
                    +",plus_nonfinal_strings="+(long)tickets*(n-1)
                    +",plus_copied_chars="+plusCopied
                    +",output_chars="+(long)tickets*6*n);
        }
        sharedLogCheck();
        System.out.println("PASS: all ticket builders produce identical content");
    }
}
