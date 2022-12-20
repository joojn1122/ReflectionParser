package net.joojn.reflectionparser;

import net.joojn.reflectionparser.manager.ImportManager;
import net.joojn.reflectionparser.manager.VariableManager;

public class ReflectionParser {

    private final VariableManager        variableManager = new VariableManager();
    private final ImportManager          importManager   = new ImportManager();
    private final ReflectionInterpreter  interpreter     = new ReflectionInterpreter(variableManager, importManager);

    public static String COMMENT = "#";

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

        if(variableManager.isVariableAssignment(code))
        {
            variableManager.addVariable(interpreter, code);
        }

        // Call method?
        interpreter.interpret(code);
    }


}
