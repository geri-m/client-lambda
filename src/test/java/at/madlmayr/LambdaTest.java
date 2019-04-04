package at.madlmayr;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class LambdaTest {
    private static final Logger LOGGER = LogManager.getLogger(LambdaTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void beforeAllTests() {
        // handle issues, in case segments are not there and disalbe therefore xray.
        AWSXRay.getGlobalRecorder().setContextMissingStrategy((s, aClass) -> LOGGER.warn("Context for XRay is missing"));

    }

    @Test
    public void testSlackCall() throws IOException {
        List<ToolCallRequest> configList = CallUtils.readToolConfigFromCVSFile();
        ToolCallRequest slack = null;
        for (ToolCallRequest config : configList) {
            if (config.getTool().equals(ToolEnum.SLACK.getName())) {
                slack = config;
                break;
            }
        }
        assertThat(slack != null);
        RequestStreamHandler call = new SlackCall();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);
        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers() >= 0);
    }


    @Test
    public void testArtifactoryCall() throws IOException {
        List<ToolCallRequest> configList = CallUtils.readToolConfigFromCVSFile();
        ToolCallRequest artifactory = null;
        for (ToolCallRequest config : configList) {
            if (config.getTool().equals(ToolEnum.ARTIFACTORY.getName())) {
                artifactory = config;
                break;
            }
        }
        assertThat(artifactory != null);
        RequestStreamHandler call = new ArtifactoryCall();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(artifactory).toString().getBytes());
        call.handleRequest(targetStream, null, null);
        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers() >= 0);
    }

    @Test
    public void testJiraCall() throws IOException {


        List<ToolCallRequest> configList = CallUtils.readToolConfigFromCVSFile();
        ToolCallRequest jira = null;
        for (ToolCallRequest config : configList) {
            if (config.getTool().equals(ToolEnum.JIRA.getName())) {
                jira = config;
                break;
            }
        }
        assertThat(jira != null);
        RequestStreamHandler call = new JiraV2Call();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(jira).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);
        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers() >= 0);
    }
}
