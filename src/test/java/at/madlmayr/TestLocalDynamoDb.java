package at.madlmayr;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;


public class TestLocalDynamoDb {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(TestLocalDynamoDb.class);


    private AmazonDynamoDB getConnectionLocalhost(){
        return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                .build();
    }

    private AmazonDynamoDB getConnectionDefault(){
        return AmazonDynamoDBClientBuilder.defaultClient();
    }


    @Test
    public void testCreateDynamoDbTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                        "Name", ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement("Name", KeyType.HASH))
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
                .withTableName("Config");

        final AmazonDynamoDB ddb = getConnectionLocalhost();

        try {
            CreateTableResult result = ddb.createTable(request);
            LOGGER.info(result.getTableDescription().getTableName());
        } catch (AmazonServiceException e) {
            LOGGER.error(e.getErrorMessage());
            fail();
        }
    }

    @Test
    public void testShowTables(){
        final AmazonDynamoDB ddb =  getConnectionLocalhost();
        final int maxAmountOfTablesInResult = 10;
        LOGGER.info("Your DynamoDB tables:");

        ListTablesRequest request;

        // the response element has space for 10 Tables, so we have to run this multiple times.
        boolean responseHasStillTables = true;
        String nameOfLastTable = null;
        List<String> listOfTablesNames = new ArrayList<>();

        while(responseHasStillTables) {
            try {
                // first request
                if (nameOfLastTable == null) {
                    request = new ListTablesRequest().withLimit(maxAmountOfTablesInResult);
                }
                // consecutive requests
                else {
                    request = new ListTablesRequest()
                            .withLimit(maxAmountOfTablesInResult)
                            .withExclusiveStartTableName(nameOfLastTable);
                }

                ListTablesResult resultOfListOfTables = ddb.listTables(request);
                listOfTablesNames.addAll(resultOfListOfTables.getTableNames());

                nameOfLastTable = resultOfListOfTables.getLastEvaluatedTableName();
                if (nameOfLastTable == null) {
                    responseHasStillTables = false;
                }

            } catch (AmazonServiceException e) {
                LOGGER.error(e.getErrorMessage());
                fail();
            }
        }

        if (listOfTablesNames.size() > 0) {
            for (String cur_name : listOfTablesNames) {
                LOGGER.info(String.format("- %s", cur_name));
            }
        } else {
            LOGGER.info("No tables found!");
            return;
        }

        LOGGER.info("Done!");
    }

}