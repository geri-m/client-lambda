package at.madlmayr;

import at.madlmayr.tools.LocalDynamoDbServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolCallRequest2DynamoDbTest {

    private static final Logger LOGGER = LogManager.getLogger(ToolCallRequest2DynamoDbTest.class);
    private static LocalDynamoDbServer localDynamoDbServer;
    private static final String FAKE_COMPANY_1 = "gma";
    private static final String FAKE_COMPANY_2 = "another";

    @BeforeAll
    public static void beforeAll() {
        localDynamoDbServer = new LocalDynamoDbServer();
        localDynamoDbServer.start();
        LOGGER.debug("DynamoDB: {}", localDynamoDbServer.getPort());
        localDynamoDbServer.createConfigTable();
        insertData();
    }

    @AfterAll
    public static void afterAll() {
        localDynamoDbServer.deleteTable();
        localDynamoDbServer.stop();
    }


    private static void insertData() {
        List<ToolCallRequest> callList = new ArrayList<>();
        callList.add(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.SLACK.getName(), "somekey", "http://localhost:8080/api/users.list/"}, 1L));
        callList.add(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.ARTIFACTORY.getName(), "somekey", "http://localhost:8080/gma/api/security/users"}, 1L));
        callList.add(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.JIRA.getName(), "somekey", "http://localhost:8080/rest/api/2/user/search"}, 1L));
        callList.add(new ToolCallRequest(new String[]{FAKE_COMPANY_2, ToolEnum.JIRA.getName(), "somekey", "http://localhost:8080/rest/api/2/user/search"}, 1L));
        localDynamoDbServer.insertConfig(callList);
    }


    @Test
    public void checkAmountOfElementsInDBTest() {
        List<ToolCallRequest> itemList = localDynamoDbServer.getToolCallRequests(FAKE_COMPANY_1);

        assertThat(itemList.size()).isEqualTo(3);

        Set<String> toolNames = new HashSet<>();
        for (ToolCallRequest call : itemList) {
            toolNames.add(call.getTool());
        }

        assertThat(toolNames.contains(ToolEnum.SLACK.getName()));
        assertThat(toolNames.contains(ToolEnum.JIRA.getName()));
        assertThat(toolNames.contains(ToolEnum.ARTIFACTORY.getName()));
    }


}

