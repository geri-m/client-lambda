package at.madlmayr;

import com.amazonaws.xray.AWSXRay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallUtils {

    private final static String CREDENTIAL_FILE = "toolconfig.txt";
    private final static String COMMA_DELIMITER = ",";
    private static final Logger LOGGER = LogManager.getLogger(CallUtils.class);

    public static List<ToolCallRequest> readToolConfigFromCVSFile() throws IOException {
        List<ToolCallRequest> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CallUtils.class.getResourceAsStream("/" + CREDENTIAL_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                if (values.length == 4) {
                    ToolCallRequest config = new ToolCallRequest(values, new Date().getTime());
                    records.add(config);
                } else {
                    throw new IOException(String.format("Line '%s' does not have four elements", line));
                }
            }
        }

        return records;
    }

    public static void disableXray() {
        // handle issues, in case segments are not there and disable therefore xray.
        AWSXRay.getGlobalRecorder().setContextMissingStrategy((s, aClass) -> LOGGER.trace("Context for XRay unset for Testing"));
    }

}
