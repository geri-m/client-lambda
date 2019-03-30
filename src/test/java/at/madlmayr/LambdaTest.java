package at.madlmayr;

import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class LambdaTest {


    @Test
    public void testSlackCall() throws IOException {
        List<ToolConfig> configList = CallUtils.readToolConfigFromCVSFile();
        ToolConfig slack = null;
        for (ToolConfig config : configList) {
            if (config.getTool().equals("slack")) {
                slack = config;
                break;
            }
        }

        assertThat(slack != null);
        SlackCall call = new SlackCall();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, null, null);
    }
}
