import java.util.*;
import java.util.function.*;

/** Classroom benchmark: setup is excluded; three warmups and five raw runs. */
public final class Bench {
    private static volatile Object retained;
    private static volatile long sink;
    public static void retain(Object value) { retained = value; }
    public static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static <T> double measure(String label, long operations,
            Supplier<T> setup, ToLongFunction<T> action) {
        for (int i=0;i<3;i++) sink=action.applyAsLong(setup.get());
        double total=0;
        for(int run=1;run<=5;run++) {
            T state=setup.get();
            long start=System.nanoTime();
            long value=action.applyAsLong(state);
            long elapsed=System.nanoTime()-start;
            sink=value;
            total+=elapsed;
            System.out.printf(Locale.ROOT,"RAW,%s,%d,%d,%d,%.3f%n",
                    label,run,operations,elapsed,(double)elapsed/operations);
        }
        double mean=total/5;
        System.out.printf(Locale.ROOT,"MEAN,%s,0,%d,%.1f,%.3f%n",
                label,operations,mean,mean/operations);
        return mean;
    }
    public static void header() {
        System.out.println("kind,experiment,run,operations,elapsed_ns,ns_per_operation");
    }
}
