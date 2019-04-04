package at.madlmayr;

import at.madlmayr.localstack.TestLocalDynamoDb;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolCallRequest2DynamoDbTest {

    private static final Logger LOGGER = LogManager.getLogger(TestLocalDynamoDb.class);


    @BeforeAll
    public static void beforeAll() throws IOException {
        createConfigTable();
        insertData();
    }

    @AfterAll
    public static void afterAll() {
        deleteTable();
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

    private static void insertData() throws IOException {
        AmazonDynamoDB ddb = getConnectionLocalhost();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        for (ToolCallRequest obj : CallUtils.readToolConfigFromCVSFile()) {
            mapper.save(obj);
        }
    }

    private static void deleteTable() {
        final AmazonDynamoDB ddb = getConnectionLocalhost();
        DeleteTableResult result = ddb.deleteTable(ToolCallRequest.TABLE_NAME);
        LOGGER.info("Table '{}' created", result.getTableDescription().getTableName());
    }

    @Test
    public void checkAmountOfElementsInDB() {
        final AmazonDynamoDB ddb = getConnectionLocalhost();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        ToolCallRequest query = new ToolCallRequest();
        query.setCompany("gma");

        DynamoDBQueryExpression<ToolCallRequest> queryExpression = new DynamoDBQueryExpression<ToolCallRequest>()
                .withHashKeyValues(query);


        List<ToolCallRequest> itemList = mapper.query(ToolCallRequest.class, queryExpression);
        LOGGER.info("Amount of Element '{}'", itemList.size());
        assertThat(itemList.size() == 3);
    }


}
