package clouseau.compat;

import clouseau.IdentitySet;
import clouseau.Calculate.Result;
import clouseau.Mode;
import java.util.IdentityHashMap;

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

    private static IdentitySet identitySet() {
        return new IdentitySet(new IdentityHashMap());
    }

    public static Result calculate(Object o) {
        IdentitySet seen = identitySet();
        Mode mode = Mode.JustClass$.MODULE$;
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }

    public static Result calculate(Object o, IdentitySet seen) {
        Mode mode = Mode.JustClass$.MODULE$;
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }

    public static Result calculate(Object o, Mode mode) {
        IdentitySet seen = identitySet();
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }

    public static Result calculate(Object o, IdentitySet seen, Mode mode) {
        if (seen == null) seen = identitySet();
        if (mode == null) mode = Mode.JustClass$.MODULE$;
        return clouseau.Calculate$.MODULE$.calculate(o, seen, mode);
    }
}
