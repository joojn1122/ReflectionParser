package net.joojn.reflectionparser.util;

public class Reflector {


    public static Class<?> getClass(String name)
    {
        try
        {
            return AdvancedReflector.class.getClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException ignored)
        {
            return null;
        }
    }

    public static Class<?> getObjectClass(Object instance)
    {
        if(instance instanceof Class<?>) return (Class<?>) instance;

        return instance.getClass();
    }
}
