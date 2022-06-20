package com.joojn.chateval.parser;

import com.joojn.chateval.logger.Logger;
import com.joojn.chateval.bukkit.ObjectList;
import com.joojn.chateval.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ReflectionParser {

    public static boolean parseFile(File file)
    {
        try
        {
            int i = 0;
            for(String line : FileUtil.readLines(file))
            {
                i++;

                if(line.length() < 5 || line.startsWith("//")) continue;

                if(!parseLine(line))
                {
                    Logger.error("Error at line " + i);
                    return false;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean parseLine(String line)
    {
        String namer = line.split("=")[0];
        String name = namer.split(" ")[1].trim();
        String type = namer.split(" ")[0].trim();

        String command = line.split("=")[1];

        Object o;

        try
        {

            o = ObjectParser.parseInput(command, type);

        }
        catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | NullPointerException e) {

            e.printStackTrace();
            return false;

        }

        if(o instanceof ObjectParser.NULL) return true;

        ObjectList.objects.put(name, o);

//        if(sender != null)
//        {
//            ErrorHandler.info("Created new instance for " + name + " of type " + o.getClass().getName());
//        }
        return true;
    }
}
