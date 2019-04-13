package at.madlmayr.tools;

import at.madlmayr.CallUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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


    public static String generateRandomName(final String firstname, final String lastname) {
        switch ((firstname + lastname).hashCode() % 4) {
            case 0:
                // mad0001g
                if (lastname.length() >= 3)
                    return (lastname.substring(0, 3) + ((firstname + lastname).hashCode() + "").substring(3, 7) + firstname.substring(0, 1)).toLowerCase();
                else
                    return firstname;
            case 1:
                return firstname;
            case 2:
            default:
                return firstname.substring(0, 1) + "." + lastname;
            case 3:
        }
        return firstname + " " + lastname;
    }

    public static List<String> readNames(final String name) throws IOException {
        List<String> names = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CallUtils.class.getResourceAsStream(name)))) {
            String line;
            while ((line = br.readLine()) != null) {
                names.add(line);
            }
        }
        return names;
    }

}
