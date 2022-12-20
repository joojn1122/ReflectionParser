package net.joojn.reflectionparser.manager;

import net.joojn.reflectionparser.debug.Debugger;
import net.joojn.reflectionparser.exception.ParseErrorException;
import net.joojn.reflectionparser.util.AdvancedReflector;
import net.joojn.reflectionparser.util.Reflector;

import java.util.HashSet;
import java.util.Set;

public class ImportManager {

    private final Set<Class<?>> imported = new HashSet<>();

    public void addImport(String line) {

        String[] lines = line.split(" ");
        ParseErrorException.assertThat(ImportManager.class, lines.length > 1);

        try
        {
            Class<?> clazz = ImportManager.class.getClassLoader().loadClass(lines[1]);
            imported.add(clazz);
            Debugger.log("Found class: '%s'", clazz.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new ParseErrorException("Could not find class by name: '%s'", lines[0]);
        }
    }

    public Class<?> findClass(String name)
    {
        Class<?> clazz = Reflector.getClass(name);
        if(clazz != null) return clazz;

        clazz = Reflector.getClass("java.lang." + name);
        if(clazz != null) return clazz;

        for(Class<?> c : imported)
        {
            if(c.getSimpleName().equals(name)) {
                return c;
            }
        }

        throw new ParseErrorException("Could not find class by name: '%s'", name);
    }
}
