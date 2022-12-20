import com.joojn.reflectionparser.ReflectionParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) throws IOException
    {
        File file = new File("Example.txt");

        ReflectionParser parser = new ReflectionParser();
        Files.readAllLines(file.toPath()).forEach(parser::parseLine);
    }
}
