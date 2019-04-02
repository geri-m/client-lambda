package at.madlmayr;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.json.JSONObject;
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


    @Test
    public void testSlackCall() throws IOException {
        List<ToolConfig> configList = CallUtils.readToolConfigFromCVSFile();
        ToolConfig slack = null;
        for (ToolConfig config : configList) {
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
        assertThat(Integer.parseInt(outputStream.toString()) >= 0);
    }


    @Test
    public void testArtifactoryCall() throws IOException {
        List<ToolConfig> configList = CallUtils.readToolConfigFromCVSFile();
        ToolConfig artifactory = null;
        for (ToolConfig config : configList) {
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
        assertThat(Integer.parseInt(outputStream.toString()) >= 0);
    }


    @Test
    public void testJiraCall() throws IOException {
        List<ToolConfig> configList = CallUtils.readToolConfigFromCVSFile();
        ToolConfig jira = null;
        for (ToolConfig config : configList) {
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
        assertThat(Integer.parseInt(outputStream.toString()) >= 0);
    }
}
