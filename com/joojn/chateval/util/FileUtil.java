package com.joojn.chateval.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static String readFile(File file)
    {
        return String.join("\\n", readFile(file));
    }

    public static List<String> readLines(File file) throws IOException
    {
        List<String> lines = new ArrayList<>();

        if(!file.exists()) throw new IOException("File not found..");

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);

        String str;
        while ((str = reader.readLine()) != null) {
            lines.add(str);
        }

        return lines;
    }

}
