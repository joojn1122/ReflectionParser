import com.joojn.reflectionparser.ReflectionParser;
import com.joojn.reflectionparser.manager.TypeManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) throws IOException
    {
        File file = new File("Example.txt");

        ReflectionParser parser = new ReflectionParser();

        Files.readAllLines(file.toPath()).forEach(line -> {
            Object returned = parser.parseLine(line);

            // this only happens when you for example
            // call method without assigning it to a variable
            // or just writing variable's name
            // you can remove this if you want
            if(returned != TypeManager.VOID && returned != null)
                System.out.println(returned);
        });
    }
}
