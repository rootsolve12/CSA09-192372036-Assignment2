import java.util.*;
public class LeaderboardRedesign {
    static final class Team {
        final String name;final int points,goalDiff;
        Team(String name,int points,int goalDiff){this.name=name;this.points=points;this.goalDiff=goalDiff;}
    }
    static final Comparator<Team> ORDER=Comparator.comparingInt((Team t)->t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t)->t.goalDiff).reversed())
            .thenComparing(t->t.name);
    static final class CountingOrder implements Comparator<Team>{
        long comparisons;
        public int compare(Team a,Team b){comparisons++;return ORDER.compare(a,b);}
    }
    interface Board{void add(Team team);List<Team> ranking();long sorts();}
    static final class Original implements Board{
        final List<Team> items=new ArrayList<>();final Comparator<Team> order;long sorts;
        Original(Comparator<Team> order){this.order=order;}
        public void add(Team team){items.add(team);}
        public List<Team> ranking(){List<Team> r=new ArrayList<>(items);r.sort(order);sorts++;return r;}
        public long sorts(){return sorts;}
    }
    static final class Cached implements Board{
        final List<Team> items=new ArrayList<>();final Comparator<Team> order;
        List<Team> snapshot=Collections.emptyList();boolean dirty=true;long sorts;
        Cached(Comparator<Team> order){this.order=order;}
        public void add(Team team){items.add(team);dirty=true;}
        public List<Team> ranking(){
            if(dirty){List<Team> copy=new ArrayList<>(items);copy.sort(order);
                snapshot=Collections.unmodifiableList(copy);dirty=false;sorts++;}
            return snapshot;
        }
        public long sorts(){return sorts;}
    }
    static Board make(boolean cached,List<Team> teams,Comparator<Team> order){
        Board board=cached?new Cached(order):new Original(order);
        for(Team team:teams)board.add(team);return board;
    }
    static long read(Board board,int requests){
        long sum=0;for(int i=0;i<requests;i++){
            List<Team> ranking=board.ranking();Bench.retain(ranking);
            sum+=ranking.size()+ranking.get(0).points;
        }return sum;
    }
    static long changed(Board board,int requests){
        long sum=0;for(int i=0;i<requests;i++){
            board.add(new Team("Update"+i,1000+i,i));
            sum+=read(board,1);
        }return sum;
    }
    static void verify(List<Team> teams){
        Board original=make(false,teams,ORDER),cached=make(true,teams,ORDER);
        for(int i=0;i<20;i++)Bench.check(original.ranking().equals(cached.ranking()),"Ranking mismatch");
        List<Team> old=cached.ranking();
        Team leader=new Team("NewLeader",Integer.MAX_VALUE,0);
        original.add(leader);cached.add(leader);
        Bench.check(original.ranking().equals(cached.ranking()),"Invalidation mismatch");
        Bench.check(cached.ranking().get(0)==leader&&!old.contains(leader),"Snapshot isolation");
        try{cached.ranking().clear();throw new AssertionError("Mutable cache");}
        catch(UnsupportedOperationException expected){}
    }
    public static void main(String[] args){
        System.out.println("PREDICT: 20 unchanged reads -> original 20 sorts; cached 1 sort");
        System.out.println("PREDICT: update before every read -> both perform 20 sorts");
        Bench.header();
        for(int n:new int[]{100,1000,10000}){
            Random random=new Random(36);List<Team> teams=new ArrayList<>();
            for(int i=0;i<n;i++)teams.add(new Team("Team"+i,random.nextInt(100),random.nextInt(30)));
            verify(teams);
            for(boolean changing:new boolean[]{false,true}){
                String mode=changing?"changing":"unchanged";
                double original=Bench.measure("Original_n"+n+"_"+mode,20,
                        ()->make(false,teams,ORDER),b->changing?changed(b,20):read(b,20));
                double cached=Bench.measure("Cached_n"+n+"_"+mode,20,
                        ()->make(true,teams,ORDER),b->changing?changed(b,20):read(b,20));
                System.out.printf(Locale.ROOT,"SPEEDUP,n=%d,mode=%s,value=%.3f%n",n,mode,original/cached);
                for(boolean useCache:new boolean[]{false,true}){
                    CountingOrder counter=new CountingOrder();Board board=make(useCache,teams,counter);
                    if(changing)changed(board,20);else read(board,20);
                    long expected=useCache&&!changing?1:20;
                    Bench.check(board.sorts()==expected,"Unexpected sort count");
                    System.out.println("COUNTS,n="+n+",mode="+mode+",cached="+useCache
                            +",sorts="+board.sorts()+",comparisons="+counter.comparisons);
                }
            }
        }
        System.out.println("PASS: equality, invalidation, immutable snapshots, and sort counts");
    }
}
