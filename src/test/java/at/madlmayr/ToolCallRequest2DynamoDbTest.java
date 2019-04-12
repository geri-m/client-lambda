package at.madlmayr;

import at.madlmayr.tools.LocalDynamoDbServer;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolCallRequest2DynamoDbTest {

    private static final Logger LOGGER = LogManager.getLogger(ToolCallRequest2DynamoDbTest.class);
    private static LocalDynamoDbServer server;
    private static final String FAKE_COMPANY_1 = "gma";
    private static final String FAKE_COMPANY_2 = "another";

    @BeforeAll
    public static void beforeAll() throws Exception {
        server = new LocalDynamoDbServer();
        server.start();
        LOGGER.debug("DynamoDB: {}", server.getPort());
        server.createConfigTable();
        insertData();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        server.deleteTable();
        server.stop();
    }


    private static void insertData() {
        AmazonDynamoDB ddb = server.getClient();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.SLACK.getName(), "somekey", "http://localhost:8080/api/users.list/"}, 1L));
        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.ARTIFACTORY.getName(), "somekey", "http://localhost:8080/gma/api/security/users"}, 1L));
        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.JIRA.getName(), "somekey", "http://localhost:8080/rest/api/2/user/search"}, 1L));
        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_2, ToolEnum.JIRA.getName(), "somekey", "http://localhost:8080/rest/api/2/user/search"}, 1L));
    }

    @Test
    public void checkAmountOfElementsInDBTest() {
        final AmazonDynamoDB ddb = server.getClient();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        ToolCallRequest query = new ToolCallRequest();
        query.setCompany(FAKE_COMPANY_1);

        DynamoDBQueryExpression<ToolCallRequest> queryExpression = new DynamoDBQueryExpression<ToolCallRequest>()
                .withHashKeyValues(query);

        List<ToolCallRequest> itemList = mapper.query(ToolCallRequest.class, queryExpression);

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

