package at.madlmayr;

import at.madlmayr.tools.AWSLambdaAsyncMock;
import at.madlmayr.tools.LocalDynamoDbServer;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;


public class ReadConfigTest {

    private static final Logger LOGGER = LogManager.getLogger(LambdaTest.class);
    private static final String FAKE_COMPANY_1 = "gma";
    private static LocalDynamoDbServer localDynamoDbServer;
    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void beforeAll() throws Exception {
        // handle issues, in case segments are not there and disable therefore xray.
        CallUtils.disableXray();
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort()); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        localDynamoDbServer = new LocalDynamoDbServer();
        localDynamoDbServer.start();
        LOGGER.debug("DynamoDB: {}", localDynamoDbServer.getPort());
        localDynamoDbServer.createConfigTable();
        localDynamoDbServer.createAccountTable();
        localDynamoDbServer.createCallResultTable();
        insertData();
        JiraCallTest.initWiremock("/jira_single.json");
        ArtifactoryCallTest.initWiremock("/artifactory_single.json", wireMockServer.port());
        SlackCallTest.initWiremock("/slack_single.json");
    }

    private static void insertData() {
        List<ToolCallRequest> callList = new ArrayList<>();
        callList.add(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.SLACK.getName(), "somekey", "http://localhost:" + wireMockServer.port() + "/api/users.list/"}, 1L));
        callList.add(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.ARTIFACTORY.getName(), "somekey", "http://localhost:" + wireMockServer.port() + "/gma/api/security/users"}, 1L));
        callList.add(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.JIRA.getName(), "somekey", "http://localhost:" + wireMockServer.port() + "/rest/api/2/user/search"}, 1L));
        localDynamoDbServer.insertConfig(callList);
    }


    @AfterAll
    public static void afterAll() {
        localDynamoDbServer.deleteCallResultTable();
        localDynamoDbServer.deleteConfigTable();
        localDynamoDbServer.deleteAccountTable();
        localDynamoDbServer.stop();
        wireMockServer.stop();
    }

    @Test
    public void userListTest() throws Exception {
        RequestStreamHandler call = new ReadConfig(localDynamoDbServer.getPort(), new AWSLambdaAsyncMock(localDynamoDbServer.getPort()));
        call.handleRequest(null, null, null);
        List<ToolCallResult> res = localDynamoDbServer.getAllToolCallResult();

        for (ToolCallResult result : res) {
            LOGGER.info("Result: {}", result.getTool());
        }

        // we fire 3 Call, as we expect 3 results in the table.
        assertThat(res.size()).isEqualTo(3);

    }
}
