package com.joojn.chateval.reflection;

import com.joojn.chateval.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Env {

   public static Class<?> findClass(String name){
       try {

           return Env.class.getClassLoader().loadClass(name);

       } catch (ClassNotFoundException e) {

           return null;

       }
   }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... classes){
        try {

            return clazz.getDeclaredMethod(name, classes);

        } catch (NoSuchMethodException e) {

            return null;
        }
    }

    public static Object getField(Class<?> clazz, String name, Object child){
        try {

            return clazz.getDeclaredField(name).get(child);

       } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public static Object classConstructor(Class<?> clazz) throws InstantiationException, IllegalAccessException {

        return clazz.newInstance();

    }

    public static Object classConstructor(Class<?> clazz, List<Pair<Class<?>, Object>> params)
    {
        if(params == null)
        {
            try
            {
                return clazz.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        try {

            Constructor<?> c = clazz.getConstructor(params.stream().map(Pair::getKey).toArray(Class<?>[]::new));
            return c.newInstance(params.stream().map(Pair::getValue).toArray(Object[]::new));

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object classConstructor(Class<?> clazz, Object... params) {

        try
        {

            Constructor<?> c = clazz.getConstructor(toClasses(params));
            return c.newInstance(params);

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object callMethod(Class<?> clazz, String name, Object child, List<Pair<Class<?>, Object>> params){

        try {

            return findMethod(clazz,
                    name,
                    params == null ? null : params.stream()
                            .map(Pair::getKey).toArray(Class<?>[]::new))

                    .invoke(child, params == null ? null :
                            params.stream().map(Pair::getValue).toArray(Object[]::new));

        } catch (IllegalAccessException | InvocationTargetException e) {

            return null;

        }
    }

    private static Class<?>[] toClasses(Object... params){
        ArrayList<Class<?>> classes = new ArrayList<>();

        for(Object param : params) {

            if (Integer.class.equals(param.getClass()))
            {
                classes.add(int.class);
            }
            else if(Double.class.equals(param.getClass()))
            {
                classes.add(double.class);
            }
            else if(Float.class.equals(param.getClass()))
            {
                classes.add(float.class);
            }
            else if(Boolean.class.equals(param.getClass()))
            {
                classes.add(boolean.class);
            }
            else
            {
                classes.add(param.getClass());
            }
        }

        return classes.toArray(new Class<?>[0]);
    }

    public static Class<?> getClassByName(String clazzName)
    {
        List<Class<?>> classes = Arrays.asList(
                Integer.class,
                Boolean.class,
                Double.class,
                Character.class,
                Float.class,
                Short.class,
                Long.class,
                int.class,
                boolean.class,
                double.class,
                char.class,
                float.class,
                short.class,
                long.class
        );

        for(Class<?> clazz : classes)
        {
            if(clazz.getSimpleName().equals(clazzName)) return clazz;
        }

        return null;
    }

    public static Class<?> getSimpleClass(String clazzName)
    {
        switch(clazzName.toLowerCase())
        {
            case "integer":
            case "int":
                return int.class;
            case "boolean":
                return boolean.class;
            case "double":
                return double.class;
            case "character":
            case "char":
                return char.class;
            case "float":
                return float.class;
            case "short":
                return short.class;
            case "long":
                return long.class;
        }

        return null;
    }
}
