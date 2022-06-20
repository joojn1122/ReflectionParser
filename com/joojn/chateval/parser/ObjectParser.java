package com.joojn.chateval.parser;

import com.joojn.chateval.bukkit.ObjectList;
import com.joojn.chateval.logger.Logger;
import com.joojn.chateval.reflection.Env;
import com.joojn.chateval.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ObjectParser {

    public static Object parseInput(String input, String type) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, NullPointerException {

        if(type.equalsIgnoreCase("string"))
        {
            return input.substring(input.indexOf('"') + 1, input.lastIndexOf('"'));
        }

        else if(type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer"))
        {
            return Integer.parseInt(input.replace(" ", ""));
        }

        else if(type.equalsIgnoreCase("float"))
        {
            return Float.parseFloat(input.replace(" ", ""));
        }

        else if(type.equalsIgnoreCase("boolean"))
        {
            return Boolean.parseBoolean(input.replace(" ", ""));
        }

        else if(type.equalsIgnoreCase("double"))
        {
            return Double.parseDouble(input.replace(" ", ""));
        }
        else if(type.equalsIgnoreCase("char") || type.equalsIgnoreCase("character"))
        {
            return input.substring(input.indexOf("'") + 1).toCharArray()[0];
        }
        else if(type.equalsIgnoreCase("object"))
        {
            return parseObject(input);
        }
        else if(type.equalsIgnoreCase("void"))
        {
            parseObject(input);
            return new NULL();
        }
        else if(type.equalsIgnoreCase("class"))
        {
            return Env.findClass(input.replace(" ", ""));
        }

        throw new NullPointerException("Invalid data type!");
    }

    public static Object parseObject(String input) {

        String[] parts = input.split("->");
        Object currentObject = null;

        for(String part : parts)
        {
            part = part.replace(" ", "");

            boolean is_constructor = part.endsWith(">");
            boolean is_method = part.endsWith(")");
            boolean is_var = part.startsWith("%") && part.endsWith("%");

            if(is_constructor)
            {
                String name = part.substring(0, part.indexOf("<"));
                String args = part.substring(part.indexOf("<"));

                if(currentObject != null)
                {
                    Logger.error("Tried to create constructor while current object is not null!");
                    return null;
                }
                else
                {
                    Class<?> clazz = null;

                    Object o = ObjectList.getObject(name);
                    if(o instanceof Class<?>) clazz = (Class<?>) o;

                    if(clazz == null) clazz = Env.findClass(name);

                    if(clazz == null)
                    {
                        Logger.error("Class not found while trying to create a new constructor");
                        return null;
                    }

                    currentObject = Env.classConstructor(
                            clazz,
                            parseArgs(args)
                    );
                }
            }
            else if(is_method)
            {
                if(currentObject == null)
                {
                    Logger.error("Current object is null while trying to call method!");
                    return null;
                }

                String name = part.substring(0, part.indexOf("("));
                String args = part.substring(part.indexOf("("));

                currentObject = Env.callMethod(
                        getClass(currentObject),
                        name,
                        currentObject,
                        parseArgs(args)
                );
            }
            else if(is_var)
            {
                if(currentObject == null)
                {
                    Object o = ObjectList.getObject(part);

                    if(o == null)
                    {
                        Logger.error("Object (variable) not found or is null!");
                        return null;
                    }
                    currentObject = o;
                }
                else
                {
                    Logger.error("Invalid object called ?");
                    return null;
                }
            }
            // field or class
            else
            {
                if(currentObject == null)
                {
                    Object o = ObjectList.getObject(part);

                    if(o instanceof Class<?>)
                    {
                        currentObject = (Class<?>) ObjectList.getObject(part);
                    }
                    else
                    {
                        Class<?> clazz = Env.findClass(part);
                        if(clazz == null)
                        {
                            Logger.error("Class not found: " + part);
                            return null;
                        }

                        currentObject = clazz;
                    }
                }
                else
                {
                    currentObject = Env.getField(
                            getClass(currentObject),
                            part,
                            currentObject
                    );
                }
            }
        }

        return currentObject;
    }

    public static List<Pair<Class<?>, Object>> parseArgs(String input)
    {
        // remove first and last char
        input = input.substring(1, input.length() - 1).replace(" ", "");

        if(input.isEmpty()) return null;

        List<Pair<Class<?>, Object>> objects = new ArrayList<>();

        for(String arg : input.split(","))
        {
            Object o;
            Class<?> clazz;

            if(arg.contains(":"))
            {
                String clazzName = arg.split(":")[0];
                clazz = Env.getClassByName(clazzName);

                // if class wasn't found
                if(clazz == null && ObjectList.getObject(clazzName) instanceof Class<?>)
                {
                    clazz = (Class<?>) ObjectList.getObject(clazzName);
                }

                if(clazz == null) clazz = Env.findClass(arg.split(":")[0]);

                o = ObjectList.getObject(arg.split(":")[1]);
            }
            else
            {
                o = ObjectList.getObject(arg);
                clazz = getClass(o);

                // convert Double to double etc.
                if(Env.getSimpleClass(clazz.getSimpleName()) != null)
                {
                    clazz = Env.getSimpleClass(clazz.getSimpleName());
                }
            }

            objects.add(new Pair<>(clazz, o));
        }

        return objects;
    }

    public static Class<?> getClass(Object o)
    {
        if(o instanceof Class<?>) return (Class<?>) o;
        else return o.getClass();
    }

    public static class NULL{}
}
