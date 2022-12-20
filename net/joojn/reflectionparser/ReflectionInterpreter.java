package net.joojn.reflectionparser;

import net.joojn.reflectionparser.exception.ParseErrorException;
import net.joojn.reflectionparser.manager.ImportManager;
import net.joojn.reflectionparser.manager.VariableManager;
import net.joojn.reflectionparser.util.AdvancedReflector;
import net.joojn.reflectionparser.util.Reflector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionInterpreter {

    private final VariableManager variableManager;
    private final ImportManager   importManager;

    public ReflectionInterpreter(VariableManager variableManager, ImportManager importManager)
    {
        this.variableManager = variableManager;
        this.importManager   = importManager;
    }

    public Object interpret(String code)
    {
        String[] groups = splitCurrentStack(code);

        Object stack = null;
        boolean init = true;

        for(String group : groups)
        {
            stack = addToStack(group, stack, init);
            init  = false;
        }

        return stack;
    }

    private String[] splitCurrentStack(String code)
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
                            && c == '>'
                            && currentBuilder.charAt(currentBuilder.length() - 1) == '-')
            {

                // remove last char (-)
                currentBuilder.setLength(currentBuilder.length() - 1);
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
                    throw new ParseErrorException("Weird brackets?");
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
            // constructor
            // Test<> or Test<1, 2>
            if(VariableManager.isConstructor(group))
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

        if(VariableManager.isMethod(group))
        {
            return callMethod(stack, group);
        }

        if(VariableManager.isConstructor(group))
            throw new ParseErrorException("Trying to create new instance, while stack is not init? '%s'", group);

        return getField(stack, group);
    }

    private Object callMethod(Object stack, String group) {
        // TODO:
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
                .orElseThrow(new ParseErrorException("Could not find field '%s' in '%s'", fieldName, stack));
    }

    private final Object createNewInstance(String code)
    {
        // TODO:
    }

    public static void main(String[] args)
    {
        System.out.println(
                Arrays.toString(
                        new ReflectionInterpreter(null, null).splitCurrentStack("player->getStatistic(Statistic->PLAY_ONE_TICK)")));
    }

}
