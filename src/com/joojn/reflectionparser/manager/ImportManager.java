package com.joojn.reflectionparser.manager;

import com.joojn.reflectionparser.debug.Debugger;
import com.joojn.reflectionparser.exception.ParseErrorException;
import com.joojn.reflectionparser.util.Reflector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ImportManager {

    private final HashMap<String, Class<?>> imported = new HashMap<>();

    public void addImport(String line) {

        String[] lines = line.split(" ");

        if(lines.length != 2 && lines.length != 4) throw new ParseErrorException("Invalid import statement: " + line);

        String className = lines[1];

        try
        {
            Class<?> clazz = ImportManager.class.getClassLoader().loadClass(className);
            String alias = lines.length == 4 ? lines[3] : clazz.getSimpleName();

            if(imported.containsKey(alias)) throw new ParseErrorException("Duplicate import alias: " + alias);
            imported.put(alias, clazz);

            Debugger.log("Found class: '%s'", clazz.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new ParseErrorException("Could not find class by name: '%s'", className);
        }
    }

    public Class<?> findClass(String name)
    {
        Class<?> clazz = Reflector.getClass(name);
        if(clazz != null) return clazz;

        clazz = Reflector.getClass("java.lang." + name);
        if(clazz != null) return clazz;

        for(String className : imported.keySet())
        {
            if(className.equals(name)) return imported.get(className);
        }

        throw new ParseErrorException("Could not find class by name: '%s'", name);
    }

    public void reset()
    {
        imported.clear();
    }
}
