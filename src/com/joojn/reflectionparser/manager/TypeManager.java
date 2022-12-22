package com.joojn.reflectionparser.manager;

import com.joojn.reflectionparser.exception.ParseErrorException;

import java.util.regex.Pattern;

public enum TypeManager {
    VOID;

    private final static Pattern NUMBER_PATTERN = Pattern.compile("(-)?[0-9]+(\\.[0-9]+)?([fdl])?");

    public static boolean isMethod(String group)
    {
        return group.contains("(") && group.contains(")");
    }

    public static boolean isConstructor(String group)
    {
        return group.contains("<") && group.contains(">");
    }

    public static boolean isString(String group)
    {
        boolean found = group.startsWith("\"");
        boolean found2 = group.startsWith("'");

        if((found && !group.endsWith("\""))
                || (found2 && !group.endsWith("'")))
            throw new ParseErrorException("String is not closed!");

        return found || found2;
    }


    public static boolean isNumber(String group)
    {
        return NUMBER_PATTERN.matcher(group).matches();
    }

    public static boolean isBoolean(String group)
    {
        return group.equals("true") || group.equals("false");
    }
}
