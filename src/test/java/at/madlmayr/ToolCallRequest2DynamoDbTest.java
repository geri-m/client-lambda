package at.madlmayr;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.*;
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
    private static DynamoDBProxyServer server;
    private static final String FAKE_COMPANY_1 = "gma";
    private static final String FAKE_COMPANY_2 = "another";

    @BeforeAll
    public static void beforeAll() throws Exception {
        System.setProperty("sqlite4java.library.path", "native-libs");
        String port = "8000";
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", port});
        server.start();
        createConfigTable();
        insertData();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        deleteTable();
        server.stop();
    }

    private static AmazonDynamoDB getConnectionLocalhost() {
        return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", Regions.EU_CENTRAL_1.getName()))
                .build();
    }

    private static void createConfigTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                        ToolCallRequest.COLUMN_COMPANY, ScalarAttributeType.S))
                .withAttributeDefinitions(new AttributeDefinition(
                        ToolCallRequest.COLUMN_TOOL, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(ToolCallRequest.COLUMN_COMPANY, KeyType.HASH), new KeySchemaElement(ToolCallRequest.COLUMN_TOOL, KeyType.RANGE))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                .withTableName(ToolCallRequest.TABLE_NAME);

        final AmazonDynamoDB ddb = getConnectionLocalhost();
        CreateTableResult result = ddb.createTable(request);
        LOGGER.info("Table '{}' created", result.getTableDescription().getTableName());
    }

    private static void insertData() {
        AmazonDynamoDB ddb = getConnectionLocalhost();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.SLACK.getName(), "somekey", "http://localhost:8080/api/users.list/"}, 1L));
        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.ARTIFACTORY.getName(), "somekey", "http://localhost:8080/gma/api/security/users"}, 1L));
        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_1, ToolEnum.JIRA.getName(), "somekey", "http://localhost:8080/rest/api/2/user/search"}, 1L));
        mapper.save(new ToolCallRequest(new String[]{FAKE_COMPANY_2, ToolEnum.JIRA.getName(), "somekey", "http://localhost:8080/rest/api/2/user/search"}, 1L));
    }

    private static void deleteTable() {
        final AmazonDynamoDB ddb = getConnectionLocalhost();
        DeleteTableResult result = ddb.deleteTable(ToolCallRequest.TABLE_NAME);
        LOGGER.info("Table '{}' deleted", result.getTableDescription().getTableName());
    }

    @Test
    public void checkAmountOfElementsInDB() {
        final AmazonDynamoDB ddb = getConnectionLocalhost();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        ToolCallRequest query = new ToolCallRequest();
        query.setCompany(FAKE_COMPANY_1);

        DynamoDBQueryExpression<ToolCallRequest> queryExpression = new DynamoDBQueryExpression<ToolCallRequest>()
                .withHashKeyValues(query);

        List<ToolCallRequest> itemList = mapper.query(ToolCallRequest.class, queryExpression);

        assertThat(itemList.size() == 3);

        Set<String> toolNames = new HashSet<>();
        for (ToolCallRequest call : itemList) {
            toolNames.add(call.getTool());
        }

        assertThat(toolNames.contains(ToolEnum.SLACK.getName()));
        assertThat(toolNames.contains(ToolEnum.JIRA.getName()));
        assertThat(toolNames.contains(ToolEnum.ARTIFACTORY.getName()));
    }


}

