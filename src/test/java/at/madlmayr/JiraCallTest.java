package at.madlmayr;

import at.madlmayr.jira.JiraSearchResultElement;
import at.madlmayr.jira.JiraV2Call;
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
import java.util.*;

import static at.madlmayr.jira.JiraV2Call.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class JiraCallTest {


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
    }

    @AfterAll
    public static void afterAll() {
        localDynamoDbServer.deleteAccountTable();
        localDynamoDbServer.stop();
        wireMockServer.stop();
    }


    public static JiraSearchResultElement[] initWiremock(int wiremockPort, ObjectMapper mapper) throws Exception {
        String response = FileUtils.readFromFile("/jira_01.json");


        JiraSearchResultElement[] responseFromFile = mapper.readValue(response, JiraSearchResultElement[].class);
        Map<String, List<JiraSearchResultElement>> result = search("", responseFromFile, 1);

        for (Map.Entry<String, List<JiraSearchResultElement>> entry : result.entrySet()) {
            stubFor(get(urlEqualTo(String.format("/rest/api/2/user/search?maxResults=%s&username=%s", JiraV2Call.MAX_RESULT_COUNT_GET_PARAMETER, entry.getKey())))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json; charset=utf-8")
                            .withBody(mapper.writeValueAsString(entry.getValue()))));

        }

        return responseFromFile;
    }

    private static Map<String, List<JiraSearchResultElement>> search(final String prefix, JiraSearchResultElement[] userList, int deep) {
        Map<String, List<JiraSearchResultElement>> result = new HashMap<>();
        for (char searchChar : SEARCH_CHARS) {
            List<JiraSearchResultElement> tempResult = recursiveSearch(prefix + searchChar, userList);
            result.put(prefix + searchChar, tempResult);
            if (tempResult.size() == 1000 && deep < MAX_RECURSION_DEPTH) {
                Map<String, List<JiraSearchResultElement>> resultFromRecursion = search(prefix + searchChar, userList, deep + 1);
                result.putAll(resultFromRecursion);
            }

        }
        return result;
    }

    private static List<JiraSearchResultElement> recursiveSearch(final String prefix, JiraSearchResultElement[] userList) {
        int counter = 0;
        List<JiraSearchResultElement> list = new ArrayList<>();
        for (JiraSearchResultElement jiraUser : userList) {
            if (jiraUser.searchSearchString(prefix)) {
                list.add(jiraUser);
                counter++;
                // if we have 1000 Elements in the match, we stop.
                if (counter == MAX_RESULT_COUNT_GET_PARAMETER)
                    break;
            }
        }
        return list;
    }

    @Test
    public void userListTest() throws Exception {
        WireMock.reset();

        Set<String> memberIds = new HashSet<>();

        for (JiraSearchResultElement m : initWiremock(wireMockServer.port(), mapper)) {
            memberIds.add(m.getKey());
        }

        ToolCallRequest slack = new ToolCallRequest(new String[]{"gma", ToolEnum.JIRA.getName(), "sometoken", "http://localhost:" + wireMockServer.port() + "/rest/api/2/user/search"}, 1L);
        RequestStreamHandler call = new JiraV2Call(localDynamoDbServer.getPort());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);

        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers()).isEqualTo(4197);

        List<JiraSearchResultElement> itemList = localDynamoDbServer.getJiraUserListByCompanyToolTimestamp("gma#" + ToolEnum.JIRA.getName() + "#" + Utils.standardTimeFormat(1L));

        for (JiraSearchResultElement m : itemList) {
            assertThat(memberIds.contains(m.getKey()));
            memberIds.remove(m.getKey());
        }

        // make sure, each and every key was found (and remove from the temporary list
        assertThat(memberIds.size()).isEqualTo(0);
        assertThat(itemList.size()).isEqualTo(4197);
    }
}
