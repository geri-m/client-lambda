package at.madlmayr;

import at.madlmayr.slack.SlackCall;
import at.madlmayr.slack.SlackMember;
import at.madlmayr.slack.SlackResponse;
import at.madlmayr.tools.FileUtils;
import at.madlmayr.tools.LocalDynamoDbServer;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        CallUtils.disableXray();
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort()); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        localDynamoDbServer = new LocalDynamoDbServer();
        localDynamoDbServer.start();
        LOGGER.debug("Wiremock: {}", wireMockServer.port());
        LOGGER.debug("DynamoDB: {}", localDynamoDbServer.getPort());
        localDynamoDbServer.createAccountTable();
        localDynamoDbServer.createCallResultTable();
    }

    @AfterAll
    public static void afterAll() {
        localDynamoDbServer.deleteAccountTable();
        localDynamoDbServer.deleteCallResultTable();
        localDynamoDbServer.stop();
        wireMockServer.stop();
    }


    public static SlackResponse initWiremock(String fileName) throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        String response = FileUtils.readFromFile(fileName);

        SlackResponse responseFromFile = localMapper.readValue(response, SlackResponse.class);

        // WireMock.reset();
        stubFor(get(urlEqualTo("/api/users.list/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(response)));

        return responseFromFile;
    }


    @Test
    public void userListTest() throws Exception {
        WireMock.reset();

        Set<String> memberIds = new HashSet<>();
        for (SlackMember m : initWiremock("/slack_01.json").getMembers()) {
            memberIds.add(m.getId());
        }

        ToolCallConfig slack = new ToolCallConfig(new String[]{"gma", ToolEnum.SLACK.getName(), "sometoken", "http://localhost:" + wireMockServer.port() + "/api/users.list/"}, 1L, 1);
        RequestStreamHandler call = new SlackCall(localDynamoDbServer.getPort());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);

        List<ToolCallResult> resultList = localDynamoDbServer.getAllToolCallResult("gma", ToolEnum.SLACK, 1L);
        assertThat(resultList.size()).isEqualTo(2);
        assertThat(resultList.get(0).getAmountOfUsers()).isEqualTo(163);
        assertThat(resultList.get(1).getAmountOfUsers()).isEqualTo(163);

        List<ToolCallResult> resultListLatest = localDynamoDbServer.getLatestToolCallResult("gma", ToolEnum.SLACK);
        assertThat(resultListLatest.size()).isEqualTo(1);
        assertThat(resultListLatest.get(0).getAmountOfUsers()).isEqualTo(163);
        assertThat(resultListLatest.get(0).getTimestampFormatted()).isEqualTo("1970-01-01T00:00:00.000Z");

        List<SlackMember> itemList = localDynamoDbServer.getSlackMemberListByCompanyToolTimestamp("gma#" + ToolEnum.SLACK.getName() + "#1970-01-01T00:00:00.001Z");

        for (SlackMember m : itemList) {
            assertThat(memberIds.contains(m.getId()));
            memberIds.remove(m.getId());
        }

        assertThat(memberIds.size()).isEqualTo(0);
        assertThat(itemList.size()).isEqualTo(163);
    }
}
