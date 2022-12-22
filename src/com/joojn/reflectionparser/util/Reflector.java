package com.joojn.reflectionparser.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Reflector {


    public static Class<?> getClass(String name)
    {
        switch(name)
        {
            case "int": return int.class;
            case "long": return long.class;
            case "short": return short.class;
            case "byte": return byte.class;
            case "float": return float.class;
            case "double": return double.class;
            case "boolean": return boolean.class;
            case "char": return char.class;
            case "void": return void.class;
            default: break;
        }

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

    public static Object invokeMethod(Object stack, Method method, Object[] argObjects)
    {
        try
        {
            return method.invoke(stack, argObjects);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Object newInstance(
            Constructor<?> constructor,
            Object[] argObjects
    )
    {
        try
        {
            return constructor.newInstance(argObjects);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private interface ClassWalker {
        boolean walk(Class<?> clazz);
    }

    private static void classWalker(Class<?> topClass, ClassWalker walker)
    {
        Class<?> currentClass = topClass;
        do
        {
            if(walker.walk(currentClass)) break;
        }
        while((currentClass = currentClass.getSuperclass()) != Object.class);

        // don't forget to check the Object class
        walker.walk(Object.class);
    }

    public static Constructor<?> findInstanceConstructor(
            Class<?> targetClass,
            Class<?>[] argClasses
    )
    {
        Constructor<?>[] constructors = new Constructor[1];

        classWalker(targetClass, clazz -> {
            for(Constructor<?> constructor : clazz.getDeclaredConstructors())
            {
                if(paramsEquals(constructor.getParameterTypes(), argClasses))
                {
                    constructors[0] = constructor;
                    return true;
                }
            }

            return false;
        });

        return constructors[0];
    }

    public static Method findInstanceMethod(
            Class<?> targetClass,
            String methodName,
            Class<?>[] argClasses
    )
    {
        Method[] targetMethod = new Method[1];

        classWalker(
                targetClass,
                clazz -> {
                    try
                    {
                        for(Method method : clazz.getDeclaredMethods())
                        {
                            if(
                                    method.getName().equals(methodName) &&
                                            paramsEquals(method.getParameterTypes(), argClasses))
                            {
                                method.setAccessible(true);
                                targetMethod[0] = method;
                                return true;
                            }
                        }

                        Method method = clazz.getDeclaredMethod(methodName, argClasses);
                        method.setAccessible(true);
                        return true;
                    }
                    catch (NoSuchMethodException ignored)
                    {
                        return false;
                    }
                }
        );

        return targetMethod[0];
    }

    private static boolean paramsEquals(Class<?>[] parameterTypes, Class<?>[] argClasses)
    {
        if(parameterTypes.length != argClasses.length) return false;

        for(int i = 0; i < parameterTypes.length; i++)
        {
            if(!isInstance(parameterTypes[i], argClasses[i])) return false;
        }

        return true;
    }

    public static boolean isInstance(Class<?> clazz, Class<?> clazz2)
    {
        if(clazz == clazz2) return true;

        if(clazz.isPrimitive()) return isPrimitive(clazz, clazz2);
        if(clazz2.isPrimitive()) return isPrimitive(clazz2, clazz);

        return clazz.isAssignableFrom(clazz2);
    }

    public static boolean isPrimitive(Class<?> primitive, Class<?> clazz2)
    {
        switch(primitive.getName())
        {
            case "boolean": return clazz2 == Boolean.class;
            case "byte": return clazz2 == Byte.class;
            case "char": return clazz2 == Character.class;
            case "short": return clazz2 == Short.class;
            case "int": return clazz2 == Integer.class;
            case "long": return clazz2 == Long.class;
            case "float": return clazz2 == Float.class;
            case "double": return clazz2 == Double.class;
            default: return false;
        }
    }
}
