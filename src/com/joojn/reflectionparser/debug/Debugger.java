package com.joojn.reflectionparser.debug;

import com.joojn.reflectionparser.ReflectionParser;

public class Debugger {

    public static void log(String message, Object... args)
    {
        if(!ReflectionParser.DEBUG) return;

        System.out.println(String.format(message, args));
    }

}
