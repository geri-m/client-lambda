package at.madlmayr;

import at.madlmayr.artifactory.ArtifactoryCall;
import at.madlmayr.artifactory.ArtifactoryListElement;
import at.madlmayr.artifactory.ArtifactoryUser;
import at.madlmayr.jira.JiraSearchResultElement;
import at.madlmayr.jira.JiraV2Call;
import at.madlmayr.slack.SlackCall;
import at.madlmayr.tools.ArtifactoryListElementWithUser;
import at.madlmayr.tools.FileUtils;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class LambdaTest {
    private static final Logger LOGGER = LogManager.getLogger(LambdaTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void beforeAllTests() {
        CallUtils.disableXray();
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
        ToolCallResult resultFromCall = objectMapper.readValue(outputStream.toString(), ToolCallResult.class);
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
        ToolCallResult resultFromCall = objectMapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers() >= 0);
    }

    @Test
    public void testArtifactoryTestDataCall() throws IOException {
        List<ToolCallRequest> configList = CallUtils.readToolConfigFromCVSFile();
        ToolCallRequest artifactory = null;
        for (ToolCallRequest config : configList) {
            if (config.getTool().equals(ToolEnum.ARTIFACTORY.getName())) {
                artifactory = config;
                break;
            }
        }
        assertThat(artifactory != null);
        ArtifactoryCall call = new ArtifactoryCall();


        JSONArray listElements = call.processCall(artifactory.getUrl(), artifactory.getBearer());
        ArtifactoryListElement[] userArray = objectMapper.readValue(listElements.toString(), ArtifactoryListElement[].class);
        LOGGER.info("Amount of Users: {} ", userArray.length);

        // We do the sync call for several reasons
        // 1) Xray works out of the box
        // 2) Not much mor expensive than Sync all (yes, 100 % is something, but its cents)
        // 3) less effort for testing.

        List<ArtifactoryListElementWithUser> fullUserList = new ArrayList<>();

        for (Object singleListElement : listElements) {
            JSONArray singleElementARray = new JSONArray();
            singleElementARray.put(singleListElement);
            JSONArray detailUsers = call.doClientCallsSync(singleElementARray, artifactory.getBearer());

            ArtifactoryListElement[] listElement = objectMapper.readValue(singleElementARray.toString(), ArtifactoryListElement[].class);
            ArtifactoryUser[] userElement = objectMapper.readValue(detailUsers.toString(), ArtifactoryUser[].class);

            ArtifactoryListElementWithUser fullElement = new ArtifactoryListElementWithUser();
            fullElement.setListElement(listElement[0]);
            fullElement.setUser(userElement[0]);
            fullUserList.add(fullElement);
        }
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        LOGGER.info(objectMapper.writeValueAsString(fullUserList));
        LOGGER.info("Amount of Users: {} ", fullUserList.size());

        FileUtils.writeToFile(objectMapper.writeValueAsString(fullUserList), "artifactory_01.raw");

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
        ToolCallResult resultFromCall = objectMapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers() >= 0);
    }

    @Test
    public void testJiraTestDataCall() throws Exception {
        List<ToolCallRequest> configList = CallUtils.readToolConfigFromCVSFile();
        ToolCallRequest jira = null;
        for (ToolCallRequest config : configList) {
            if (config.getTool().equals(ToolEnum.JIRA.getName())) {
                jira = config;
                break;
            }
        }
        assertThat(jira != null);
        JiraV2Call call = new JiraV2Call();
        JSONArray listElements = call.processCall(jira.getUrl(), jira.getBearer());
        JiraSearchResultElement[] userList = objectMapper.readValue(listElements.toString(), JiraSearchResultElement[].class);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        LOGGER.info("Amount of Users: {} ", userList.length);
        FileUtils.writeToFile(objectMapper.writeValueAsString(userList), "jira_01.raw");
    }

}
