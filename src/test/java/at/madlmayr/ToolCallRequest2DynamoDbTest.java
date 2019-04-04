package at.madlmayr;

import at.madlmayr.localstack.TestLocalDynamoDb;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ToolCallRequest2DynamoDbTest {

    private static final Logger LOGGER = LogManager.getLogger(TestLocalDynamoDb.class);


    private AmazonDynamoDB getConnectionLocalhost() {
        return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-central-1"))
                .build();
    }

    private void createConfigTable() {
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
        LOGGER.info(result.getTableDescription().getTableName());
    }


}
