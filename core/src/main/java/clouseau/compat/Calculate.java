package clouseau.compat;

import clouseau.Calculate.Result;
import clouseau.Mode;
import scala.collection.mutable.HashSet;
import scala.collection.mutable.Set;

public class Calculate {

    public static long sizeOf(Object o) {
        return clouseau.Calculate$.MODULE$.sizeOf(o);
    }

    public static long staticSizeOf(Object o) {
        return clouseau.Calculate$.MODULE$.staticSizeOf(o);
    }

    public static long fullSizeOf(Object o) {
        return clouseau.Calculate$.MODULE$.fullSizeOf(o);
    }

    public static Result calculate(Object o) {
        Set<Object> seen = new HashSet<Object>();
        Mode mode = Mode.JustClass$.MODULE$;
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }

    public static Result calculate(Object o, Set<Object> seen) {
        Mode mode = Mode.JustClass$.MODULE$;
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }

    public static Result calculate(Object o, Mode mode) {
        Set<Object> seen = new HashSet<Object>();
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }

    public static Result calculate(Object o, Set<Object> seen, Mode mode) {
        if (seen == null) seen = new HashSet<Object>();
        if (mode == null) mode = Mode.JustClass$.MODULE$;
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }
}
