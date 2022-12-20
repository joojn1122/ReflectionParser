package net.joojn.reflectionparser.debug;

public class Debugger {

    public static final boolean debug = true;

    public static void log(String message, Object... args)
    {
        if(!debug) return;

        System.out.println(String.format(message, args));
    }

}
