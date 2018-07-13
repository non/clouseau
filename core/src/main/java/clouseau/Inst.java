package clouseau;

import java.lang.instrument.Instrumentation;

public class Inst {
    private static volatile Instrumentation inst = null;

    public static boolean initialized() {
        return inst != null;
    }

    public static void premain(String args, Instrumentation inst) {
        if (Inst.inst != null) throw new AssertionError("premain called twice?");
        Inst.inst = inst;
    }

    public static Instrumentation instrumentation() {
        if (Inst.inst == null) throw new AssertionError("init failed; did you include -javaagent:clouseau.jar ?");
        return inst;
    }
}
