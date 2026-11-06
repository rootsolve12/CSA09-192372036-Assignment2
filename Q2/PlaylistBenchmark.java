import java.util.*;

public class PlaylistBenchmark {
    static final class Song {
        final String title;
        Song(String title) {this.title=title;}
    }
    static final Song MARKER=new Song("Inserted");
    static List<Song> list(boolean linked,Song[] songs) {
        List<Song> result=linked?new LinkedList<>():new ArrayList<>();
        Collections.addAll(result,songs);
        return result;
    }
    public static void main(String[] args) {
        Bench.header();
        int repeats=1000;
        for(int n:new int[]{100,10000,100000}) {
            Song[] songs=new Song[n];
            for(int i=0;i<n;i++) songs[i]=new Song("Song"+i);
            for(boolean linked:new boolean[]{false,true}) {
                String prefix=(linked?"LinkedList":"ArrayList")+"_n"+n;
                Bench.measure(prefix+"_append",n,
                        ()->list(linked,new Song[0]),data->{
                            Collections.addAll(data,songs);return data.size();});
                for(boolean front:new boolean[]{true,false}) {
                    String position=front?"front":"middle";
                    Bench.measure(prefix+"_insert_"+position,repeats,
                            ()->list(linked,songs),data->{
                                for(int i=0;i<repeats;i++)
                                    data.add(front?0:data.size()/2,MARKER);
                                return data.size();});
                    Bench.measure(prefix+"_remove_"+position,repeats,
                            ()->{List<Song> data=list(linked,songs);
                                for(int i=0;i<repeats;i++)
                                    data.add(front?0:data.size()/2,MARKER);
                                return data;},data->{
                                long sum=0;
                                for(int i=0;i<repeats;i++)
                                    sum+=data.remove(front?0:data.size()/2).title.length();
                                return sum;});
                }
                Bench.measure(prefix+"_indexed_get",repeats,
                        ()->list(linked,songs),data->{
                            long sum=0;
                            for(int i=0;i<repeats;i++)
                                sum+=data.get((i*37)%n).title.length();
                            return sum;});
                Bench.measure(prefix+"_iterate",10L*n,
                        ()->list(linked,songs),data->{
                            long sum=0;
                            for(int pass=0;pass<10;pass++)
                                for(Song song:data) sum+=song.title.length();
                            return sum;});
            }
            Bench.measure("ArrayDeque_n"+n+"_end_cycle",4000,
                    ()->new ArrayDeque<>(Arrays.asList(songs)),queue->{
                        long sum=0;
                        for(int i=0;i<1000;i++) {
                            queue.addFirst(MARKER);sum+=queue.removeFirst().title.length();
                            queue.addLast(MARKER);sum+=queue.removeFirst().title.length();
                        }
                        return sum+queue.size();});
            List<Song> a=list(false,songs),b=list(true,songs);
            a.add(n/2,MARKER);b.add(n/2,MARKER);
            Bench.check(a.equals(b),"Different insertion result");
            a.remove(n/2);b.remove(n/2);
            Bench.check(a.equals(b)&&a.size()==n,"Different removal result");
        }
        System.out.println("PASS: playlist insertion/removal equivalence at all three sizes");
    }
}
