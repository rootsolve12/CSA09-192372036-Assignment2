import java.util.*;
public class RankingTests {
    static final Comparator<Team> ORDER=Comparator.comparingInt((Team t)->t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t)->t.goalDiff).reversed())
            .thenComparing(t->t.name);
    static final class GenericBoard<T> {
        final List<T> items=new ArrayList<>();
        final Comparator<? super T> order;
        GenericBoard(Comparator<? super T> order){this.order=order;}
        void addAll(List<? extends T> input){items.addAll(input);}
        List<T> ranking(){List<T> copy=new ArrayList<>(items);copy.sort(order);return copy;}
    }
    static void verify(List<Team> teams) {
        for(Team a:teams)for(Team b:teams)for(Team c:teams) {
            int ab=ORDER.compare(a,b),ba=ORDER.compare(b,a);
            Bench.check(Integer.signum(ab)==-Integer.signum(ba),"Sign symmetry");
            Bench.check(ORDER.compare(a,a)==0,"Reflexivity");
            if(ab<=0&&ORDER.compare(b,c)<=0)
                Bench.check(ORDER.compare(a,c)<=0,"Transitivity");
        }
    }
    public static void main(String[] args) {
        List<Team> input=Arrays.asList(new Team("Zulu",9,4),new Team("Alpha",9,4),
                new Team("Beta",9,6),new Team("Gamma",6,100),new Team("Delta",9,6),
                new Team("Alpha",9,4));
        verify(input);
        GenericBoard<Team> board=new GenericBoard<>(ORDER);board.addAll(input);
        List<Team> ranked=board.ranking();
        System.out.println("Tie data: "+ranked);
        Bench.check(ranked.toString().equals("[Beta(9,6), Delta(9,6), Alpha(9,4), Alpha(9,4), Zulu(9,4), Gamma(6,100)]"),"Tie order");
        Bench.check(board.items.equals(input),"Original input changed");
        Team high=new Team("High",Integer.MAX_VALUE,0),low=new Team("Low",Integer.MIN_VALUE,0);
        Comparator<Team> faulty=(a,b)->b.points-a.points;
        System.out.println("Faulty subtraction high-vs-low="+faulty.compare(high,low));
        System.out.println("Safe comparison high-vs-low="+ORDER.compare(high,low));
        Bench.check(faulty.compare(high,low)>0&&ORDER.compare(high,low)<0,"Overflow test");
        List<Team> stable=new ArrayList<>(input);
        stable.sort(Comparator.comparing(t->t.name));
        stable.sort(Comparator.comparingInt((Team t)->t.goalDiff).reversed());
        stable.sort(Comparator.comparingInt((Team t)->t.points).reversed());
        Bench.check(stable.equals(ranked),"Stable multi-sort equivalence");
        Bench.header();
        for(int n:new int[]{100,1000,10000}) {
            Random random=new Random(36);List<Team> teams=new ArrayList<>();
            for(int i=0;i<n;i++)teams.add(new Team("Team"+i,random.nextInt(100),random.nextInt(30)));
            Bench.measure("Q5_n"+n+"_20_unchanged_rankings",20,
                    ()->{Leaderboard<Team> b=new Leaderboard<>(ORDER);for(Team t:teams)b.add(t);return b;},
                    b->{long sum=0;for(int i=0;i<20;i++){List<Team> r=b.ranking();Bench.retain(r);sum+=r.size()+r.get(0).points;}return sum;});
        }
        System.out.println("PASS: ties, overflow, transitivity, stable-sort equivalence and copy isolation");
    }
}
