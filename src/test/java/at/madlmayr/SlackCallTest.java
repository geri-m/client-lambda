package at.madlmayr;

import at.madlmayr.slack.SlackCall;
import at.madlmayr.slack.SlackMember;
import at.madlmayr.slack.SlackResponse;
import at.madlmayr.tools.FileUtils;
import at.madlmayr.tools.LocalDynamoDbServer;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class SlackCallTest {

    // @Rule
    // public static WireMockRule wireMockServer = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LogManager.getLogger(LambdaTest.class);
    // TODO: Check if @Rule would be more suitable for handling the tests
    private static WireMockServer wireMockServer;
    private static LocalDynamoDbServer localDynamoDbServer;

    @BeforeAll
    public static void beforeAll() {
        // handle issues, in case segments are not there and disable therefore xray.
        AWSXRay.getGlobalRecorder().setContextMissingStrategy((s, aClass) -> LOGGER.warn("Context for XRay is unset for Testing"));
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort()); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        localDynamoDbServer = new LocalDynamoDbServer();
        localDynamoDbServer.start();
        LOGGER.debug("Wiremock: {}", wireMockServer.port());
        LOGGER.debug("DynamoDB: {}", localDynamoDbServer.getPort());
        localDynamoDbServer.createAccountTable();
    }

    @AfterAll
    public static void afterAll() {
        localDynamoDbServer.deleteAccountTable();
        localDynamoDbServer.stop();
        wireMockServer.stop();
    }

    @Test
    public void userListTest() throws Exception {
        WireMock.reset();
        String response = FileUtils.readFromFile("/slackdata_01.json");

        List<String> memberIds = new ArrayList<>();
        SlackResponse responseFromFile = mapper.readValue(response, SlackResponse.class);
        for (SlackMember m : responseFromFile.getMembers()) {
            memberIds.add(m.getId());
        }

        // WireMock.reset();
        stubFor(get(urlEqualTo("/api/users.list/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(response)));

        ToolCallRequest slack = new ToolCallRequest(new String[]{"gma", ToolEnum.SLACK.getName(), "sometoken", "http://localhost:" + wireMockServer.port() + "/api/users.list/"}, 1L);
        RequestStreamHandler call = new SlackCall(localDynamoDbServer.getPort());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);

        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers()).isEqualTo(163);

        List<SlackMember> itemList = localDynamoDbServer.getSlackMemberListByCompanyToolTimestamp("gma#" + ToolEnum.SLACK.getName() + "#" + Utils.standardTimeFormat(1L));

        for (SlackMember m : itemList) {
            assertThat(memberIds.contains(m.getId()));
            memberIds.remove(m.getId());
        }

        assertThat(memberIds.size()).isEqualTo(0);
        assertThat(itemList.size()).isEqualTo(163);
    }
}
