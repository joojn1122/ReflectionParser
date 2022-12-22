package com.joojn.reflectionparser;

import com.joojn.reflectionparser.debug.Debugger;
import com.joojn.reflectionparser.exception.ParseErrorException;
import com.joojn.reflectionparser.manager.ImportManager;
import com.joojn.reflectionparser.manager.TypeManager;
import com.joojn.reflectionparser.manager.VariableManager;
import com.joojn.reflectionparser.util.AdvancedReflector;
import com.joojn.reflectionparser.util.Reflector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionInterpreter {

    private static final class Pair<K, V>{
        public K key;
        public V value;

        public Pair(K key, V value){
            this.key = key;
            this.value = value;
        }
    }

    private final VariableManager variableManager;
    private final ImportManager   importManager;

    public ReflectionInterpreter(VariableManager variableManager, ImportManager importManager)
    {
        this.variableManager = variableManager;
        this.importManager   = importManager;
    }

    public Object interpret(String code)
    {
        List<String> movedGroups = new ArrayList<>();
        boolean wasNumber = false;

        for(String group : splitCurrentStack(code, '.'))
        {
            if(TypeManager.isNumber(group))
            {
                if(wasNumber)
                {
                    int last = movedGroups.size() - 1;
                    String lastNumber = movedGroups.get(last);
                    movedGroups.set(last, lastNumber + "." + group);

                    wasNumber = false;
                    continue;
                }

                wasNumber = true;
            }

            movedGroups.add(group);
        }

        Debugger.log("Moved groups: " + movedGroups);

        Object stack = null;
        boolean init = true;

        for(String group : movedGroups)
        {
            stack = addToStack(group, stack, init);
            init  = false;
        }

        return stack;
    }

    private String[] splitCurrentStack(String code, char splitter)
    {
        // player->getStatistic(Statistic->PLAY_ONE_TICK)
        // should return player, getStatistic(..)

        int brackets  = 0;

        StringBuilder currentBuilder = new StringBuilder();
        List<String> values = new ArrayList<>();

        for(char c : code.toCharArray())
        {
            if(
                    currentBuilder.length() != 0
                            && brackets == 0
                            && c == splitter)
            {

                values.add(currentBuilder.toString());
                currentBuilder.setLength(0);

                // skip adding char (>)
                continue;
            }
            else if(c == '(')
            {
                brackets++;
            }
            else if(c == ')')
            {
                brackets--;

                if(brackets == -1)
                    throw new ParseErrorException("Weird brackets? %s", code);
            }

            currentBuilder.append(c);
        }

        if(currentBuilder.length() != 0)
        {
            values.add(currentBuilder.toString());
        }

        return values.toArray(new String[0]);
    }

    private Object addToStack(String group, Object stack, boolean init)
    {
        if(!init && stack == null)
        {
            throw new ParseErrorException("Tried to call %s on null", group);
        }

        if(init)
        {
            if(group.equals("null"))
                return null;

            if(TypeManager.isString(group))
            {
                return group.substring(1, group.length() - 1);
            }

            if(TypeManager.isNumber(group))
            {
                try
                {
                    if(endsIgnoreCase(group, 'f'))
                        return Float.parseFloat(group.substring(0, group.length() - 1));
                    if(endsIgnoreCase(group, 'd'))
                        return Double.parseDouble(group.substring(0, group.length() - 1));
                    if(endsIgnoreCase(group, 'l'))
                        return Long.parseLong(group.substring(0, group.length() - 1));

                    if(group.contains("."))
                        return Double.parseDouble(group);
                    else
                        return Integer.parseInt(group);
                }
                catch (NumberFormatException ignored) {}

                throw new ParseErrorException("Invalid number %s", group);
            }

            if(TypeManager.isBoolean(group))
            {
                return group.equals("true");
            }

            // constructor
            // Test<> or Test<1, 2>
            if(TypeManager.isConstructor(group))
            {
                return createNewInstance(group);
            }

            if(variableManager.variableExists(group))
            {
                return variableManager.getVariable(group);
            }

            // TODO: add primitive types

            // could it be anything else?
            return importManager.findClass(group);
        }

        // getStatistic(Statistic->PLAY_ONE_TICK)

        // ..->getStatistic(World<>) // Not problem
        // ..->World<> // Problem

        if(TypeManager.isMethod(group))
        {
            return callMethod(stack, group);
        }

        if(TypeManager.isConstructor(group))
            throw new ParseErrorException("Trying to create new instance, while stack is not init? '%s'", group);

        return getField(stack, group);
    }

    private boolean endsIgnoreCase(String group, char l)
    {
        char last = group.charAt(group.length() - 1);
        return last == l || last == Character.toUpperCase(l);
    }

    private Object callMethod(Object stack, String group) {
        // group: getStatistic(Statistic->PLAY_ONE_TICK("", 1), 2)
        // stack: player

        Class<?> targetClass = Reflector.getObjectClass(stack);
        String methodName = group.substring(0, group.indexOf('('));

        String[] args = splitCurrentStack(group.substring(
                group.indexOf('(') + 1, group.lastIndexOf(')')
        ), ',');

        // args: [Statistic->PLAY_ONE_TICK]
        Class<?>[] argClasses = new Class<?>[args.length];
        Object[]   argObjects = new Object[args.length];

        for(int i = 0; i < args.length; i++)
        {
            Pair<Class<?>, Object> arg = parseArgument(args[i]);
            argClasses[i] = arg.key;
            argObjects[i] = arg.value;
        }

        Method method = Reflector.findInstanceMethod(targetClass, methodName, argClasses);
        if(method == null) throw new ParseErrorException("Could not find method %s(%s) in %s", methodName, Arrays.toString(argClasses), targetClass);

        return Reflector.invokeMethod(stack, method, argObjects);
    }

    private Pair<Class<?>, Object> parseArgument(String arg)
    {
        // full: player->getStatistic(Statistic->PLAY_ONE_TICK)
        // arg: Statistic->PLAY_ONE_TICK

        String[] groups = splitCurrentStack(arg, ':');
        String object = groups[0];
        String strType = groups.length == 2 ? groups[1] : null;

        Object value = interpret(object);

        Class<?> type;
        if(strType == null) type = value.getClass();
        else {
            type = importManager.findClass(strType);
            if(type == null) throw new ParseErrorException("Could not find class %s", strType);
        }

        return new Pair<>(type, value);
    }

    private Object getField(Object stack, String fieldName)
    {
        // String->CASE_INSENSITIVE_ORDER
        // group: CASE_INSENSITIVE_ORDER
        // stack: Class<String>

        Class<?> objectClass = Reflector.getObjectClass(stack);

        return AdvancedReflector
                .For(objectClass)
                .searchAll(true)
                .name(fieldName)
                .exists(
                        (e) -> e.with(stack).get()
                )
                .orElseThrow(new ParseErrorException("Could not find field '%s' in '%s'", fieldName, stack))
                .build();
    }

    private Object createNewInstance(String code)
    {
        Class<?> targetClass = importManager.findClass(code.substring(0, code.indexOf('<')));

        String[] args = splitCurrentStack(code.substring(
                code.indexOf('<') + 1, code.lastIndexOf('>')
        ), ',');

        Class<?>[] argClasses = new Class<?>[args.length];
        Object[]   argObjects = new Object[args.length];

        for(int i = 0; i < args.length; i++)
        {
            Pair<Class<?>, Object> arg = parseArgument(args[i]);
            argClasses[i] = arg.key;
            argObjects[i] = arg.value;
        }

        Constructor<?> constructor = Reflector.findInstanceConstructor(targetClass, argClasses);
        if(constructor == null)
            throw new ParseErrorException("Could not find constructor %s(%s)",
                    targetClass.getName(),
                    Arrays.toString(argClasses)
            );

        return Reflector.newInstance(constructor, argObjects);
    }
}
