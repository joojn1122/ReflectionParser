package com.joojn.reflectionparser;

import com.joojn.reflectionparser.exception.ParseErrorException;
import com.joojn.reflectionparser.manager.ImportManager;
import com.joojn.reflectionparser.manager.TypeManager;
import com.joojn.reflectionparser.manager.VariableManager;

import java.util.Arrays;

public class ReflectionParser {

    public static final boolean DEBUG = false;

    private final VariableManager        variableManager = new VariableManager();
    private final ImportManager          importManager   = new ImportManager();
    private final ReflectionInterpreter  interpreter     = new ReflectionInterpreter(variableManager, importManager);

    private final String COMMENT;

    public ReflectionParser()
    {
        // default comment is hash
        this("#");
    }

    public ReflectionParser(String comment)
    {
        this.COMMENT = comment;
    }

    /**
     * Parses the given code and returns the result if any.
     * @author joojn
     * @param code The code to parse.
     * @return the result of the code or `TypeManager.VOID` if no result is returned.
     */
    public Object parseLine(String code)
    {
        try
        {
            return parseLine_(code);
        }
        catch (Exception e)
        {
            throw new ParseErrorException(
                    "Error at line: '%s', message: '%s'", code, e.getMessage()
            );
        }
    }

    private Object parseLine_(String code)
    {
        // remove comments
        code = code.split(COMMENT)[0];

        if(code.replace(" ", "").isEmpty()) return TypeManager.VOID;

        if(code.startsWith("import"))
        {
            importManager.addImport(code);
            return TypeManager.VOID;
        }

        // remove all unnecessary whitespace
        code = removeCharsExceptQuotes(code, ' ');

        if(variableManager.isVariableAssignment(code))
        {
            variableManager.addVariable(interpreter, code);
            return TypeManager.VOID;
        }

        // Call method?
        return interpreter.interpret(code);
    }

    private String removeCharsExceptQuotes(String group, char c) {
        StringBuilder builder = new StringBuilder();

        boolean inQuotes = false;
        boolean inDoubleQuotes = false;

        for(char ch : group.toCharArray())
        {
            if(ch == '\'' && !inDoubleQuotes)
                inQuotes = !inQuotes;
            else if(ch == '"' && !inQuotes)
                inDoubleQuotes = !inDoubleQuotes;

            if(ch == c && !inQuotes && !inDoubleQuotes)
                continue;

            builder.append(ch);
        }

        return builder.toString();
    }

    public void reset()
    {
        variableManager.reset();
        importManager.reset();
    }
}
