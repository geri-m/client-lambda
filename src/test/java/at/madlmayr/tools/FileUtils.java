package at.madlmayr.tools;

import at.madlmayr.CallUtils;

import java.io.*;

public class FileUtils {

    public static String readFromFile(final String fileName) throws IOException {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CallUtils.class.getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        }
        return buffer.toString();
    }

    public static void writeToFile(final String content, String filename) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
            out.print(content);
        }
    }

}
