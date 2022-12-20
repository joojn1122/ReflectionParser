package com.joojn.reflectionparser;

import com.joojn.reflectionparser.manager.ImportManager;
import com.joojn.reflectionparser.manager.VariableManager;

public class ReflectionParser {

    public static final boolean DEBUG   = false;

    private final VariableManager        variableManager = new VariableManager();
    private final ImportManager          importManager   = new ImportManager();
    private final ReflectionInterpreter  interpreter     = new ReflectionInterpreter(variableManager, importManager);

    public final String COMMENT;

    public ReflectionParser()
    {
        // default comment is hash
        this("#");
    }

    public ReflectionParser(String comment)
    {
        this.COMMENT = comment;
    }

    public void parseLine(String code)
    {
        // remove comments
        code = code.split(COMMENT)[0];

        if(code.replace(" ", "").isEmpty()) return;

        if(code.startsWith("import"))
        {
            importManager.addImport(code);
            return;
        }

        // remove all unnecessary whitespace
        code = removeCharsExceptQuotes(code, ' ');

        if(variableManager.isVariableAssignment(code))
        {
            variableManager.addVariable(interpreter, code);
            return;
        }

        // Call method?
        interpreter.interpret(code);
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
