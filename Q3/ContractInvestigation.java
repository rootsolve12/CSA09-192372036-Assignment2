import java.util.*;
public class ContractInvestigation {
    static final class Broken {
        final String regNo;
        Broken(String id) {regNo=id;}
        @Override public boolean equals(Object o) {
            return o instanceof Broken&&regNo.equals(((Broken)o).regNo);
        }
    }
    static final class Mutable {
        String regNo;
        Mutable(String id) {regNo=id;}
        @Override public boolean equals(Object o) {
            return o instanceof Mutable&&regNo.equals(((Mutable)o).regNo);
        }
        @Override public int hashCode(){return regNo.hashCode();}
    }
    static final class Fixed {
        final String regNo,name;
        Fixed(String id,String name){this.regNo=id;this.name=name;}
        @Override public boolean equals(Object o) {
            return o instanceof Fixed&&regNo.equals(((Fixed)o).regNo);
        }
        @Override public int hashCode(){return regNo.hashCode();}
    }
    static void check(boolean condition,String message){
        if(!condition)throw new AssertionError(message);
    }
    public static void main(String[] args) {
        System.out.println("PREDICT test1: same object twice -> size 1");
        Broken same=new Broken("192372036");
        Set<Broken> one=new HashSet<>();one.add(same);one.add(same);
        System.out.println("OBSERVED test1 size="+one.size());
        check(one.size()==1,"Same-reference check");
        System.out.println("PREDICT test2: distinct equal objects -> usually size 2");
        Broken a=new Broken("192372036"),b=new Broken("192372036");
        Set<Broken> two=new HashSet<>();two.add(a);two.add(b);
        System.out.println("OBSERVED equals="+a.equals(b)+",hashA="+a.hashCode()
                +",hashB="+b.hashCode()+",size="+two.size());
        System.out.println("PREDICT test3: mutation A -> B retains size 1; lookup fails");
        Mutable value=new Mutable("A");Set<Mutable> three=new HashSet<>();three.add(value);
        System.out.println("OBSERVED before mutation contains="+three.contains(value));
        value.regNo="B";
        boolean found=three.contains(value),removed=three.remove(value);
        System.out.println("OBSERVED after mutation contains="+found+",removed="+removed
                +",size="+three.size());
        check(!found&&!removed&&three.size()==1,"Mutation demonstration");
        Set<Fixed> fixed=new HashSet<>();
        Set<Fixed> tree=new TreeSet<>(Comparator.comparing(s->s.regNo));
        Map<String,Fixed> map=new HashMap<>();
        for(Fixed scan:new Fixed[]{new Fixed("001","Arun"),new Fixed("002","Divya"),
                new Fixed("001","Arun")}) {
            fixed.add(scan);tree.add(scan);map.putIfAbsent(scan.regNo,scan);
        }
        check(fixed.size()==2&&tree.size()==2&&map.size()==2,"Alternative deduplication");
        Fixed x=new Fixed("001","Arun"),y=new Fixed("001","Updated name");
        check(x.equals(y)&&y.equals(x)&&x.hashCode()==y.hashCode(),"Equality contract");
        check(x.equals(x)&&!x.equals(null),"Reflexive/null contract");
        System.out.println("PASS: fixed HashSet, TreeSet and HashMap each retain 2 students");
    }
}
