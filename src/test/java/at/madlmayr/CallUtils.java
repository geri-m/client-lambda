package at.madlmayr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallUtils {

    private final static String CREDENTIAL_FILE = "toolconfig.txt";
    private final static String COMMA_DELIMITER = ",";

    public static List<ToolConfig> readToolConfigFromCVSFile() throws IOException {
        List<ToolConfig> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CallUtils.class.getResourceAsStream("/" + CREDENTIAL_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                if (values.length == 4) {
                    ToolConfig config = new ToolConfig(values, new Date().getTime());
                    records.add(config);
                } else {
                    throw new IOException(String.format("Line '%s' does not have four elements", line));
                }
            }
        }

        return records;
    }


}
