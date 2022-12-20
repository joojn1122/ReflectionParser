package net.joojn.reflectionparser.manager;

import net.joojn.reflectionparser.ReflectionInterpreter;
import net.joojn.reflectionparser.exception.ParseErrorException;

import java.util.HashMap;
import java.util.regex.Pattern;

public class VariableManager {

    private static final Pattern VARIABLE_ASSIGNMENT = Pattern.compile("(?<!=)=[^=]+", Pattern.MULTILINE);

    private final HashMap<String, Object> variables = new HashMap<>();

    public boolean isVariableAssignment(String code)
    {
        return VARIABLE_ASSIGNMENT.matcher(code).find();
    }

    public void addVariable(ReflectionInterpreter interpreter, String code)
    {
        String[] strings = code.split("=");
        String name  = strings[0].replace(" ", "");

        if(name.isEmpty()) throw new ParseErrorException("Variable name cannot be empty!");
        if(Character.isDigit(name.charAt(0))) throw new ParseErrorException("Variable name cannot start with a number!");

        Object value = interpreter.interpret(strings[1]);

        variables.put(name, value);
    }

    public Object getVariable(String name)
    {
        return variables.get(name);
    }

    public boolean variableExists(String name)
    {
        return variables.containsKey(name);
    }

    public static boolean isMethod(String group)
    {
        return group.contains("(") && group.contains(")");
    }

    public static boolean isConstructor(String group)
    {
        return group.contains("<") && group.contains(">");
    }
}
