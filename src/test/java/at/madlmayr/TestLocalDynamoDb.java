package at.madlmayr;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled
public class TestLocalDynamoDb {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(TestLocalDynamoDb.class);
    private final static String TABLE_NAME = "Config";

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
                .withTableName(TABLE_NAME);

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

    @Test
    public void testReadElementToTable() {
        String keyName = "Name";

        LOGGER.info(String.format("Retrieving item \"%s\" from \"%s\"",
                keyName, TABLE_NAME));

        HashMap<String,AttributeValue> keyList = new HashMap<>();
        keyList.put(keyName, new AttributeValue(keyName));

        GetItemRequest request = new GetItemRequest()
                    .withKey(keyList)
                    .withTableName(TABLE_NAME);

        final AmazonDynamoDB ddb = getConnectionLocalhost();

        try {


            Map<String,AttributeValue> returnedItems =
                    ddb.getItem(request).getItem();
            if (returnedItems != null) {
                Set<String> keys = returnedItems.keySet();
                for (String key : keys) {
                    LOGGER.info(String.format("%s: %s",
                            key, returnedItems.get(key).getS()));
                }
            } else {
                LOGGER.info(String.format("No item found with the key %s!", keyName));
            }
        } catch (AmazonServiceException e) {
            LOGGER.error(e.getErrorMessage());
            fail();
        }
    }

     @Test
    public void testAddElement(){
        String keyName = "Name";
        ArrayList<String[]> additionalFields = new ArrayList<>();
        additionalFields.add(new String[]{"field1", "value"});
        LOGGER.info(String.format("Adding \"%s\" to \"%s\"", keyName, TABLE_NAME));
        if (additionalFields.size() > 0) {
            LOGGER.info("Additional fields:");
            for (String[] field : additionalFields) {
                LOGGER.info(String.format("  %s: %s", field[0], field[1]));
            }
        }

        HashMap<String,AttributeValue> item_values = new HashMap<>();

        item_values.put(keyName, new AttributeValue(keyName));

        for (String[] field : additionalFields) {
            item_values.put(field[0], new AttributeValue(field[1]));
        }

        final AmazonDynamoDB ddb = getConnectionLocalhost();

        try {
            ddb.putItem(TABLE_NAME, item_values);
        } catch (ResourceNotFoundException e) {
            LOGGER.error(String.format("Error: The table \"%s\" can't be found.", TABLE_NAME));
            LOGGER.error("Be sure that it exists and that you've typed its name correctly!");
            fail();
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            fail();
        }
        LOGGER.info("Done!");
    }

    @Test
    public void testHttpCall(){
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("https://slack.com/api/users.list/");
        // Set Bearer Header
        request.setHeader("Authorization", "Bearer " + "xoxb");
        boolean is2xx = false;

        LOGGER.info(request.toString());

        try {
            HttpResponse response = httpClient.execute(request);
            String jsonString = EntityUtils.toString(response.getEntity());

            // TODO: https://stackoverflow.com/questions/27500749/dynamodb-object-to-attributevalue
            // TODO: https://www.baeldung.com/java-org-json
            JSONObject jsonResponse = new JSONObject(jsonString);
            is2xx = (response.getStatusLine().getStatusCode() / 100) == 2;
            LOGGER.info("Result: {}", jsonResponse.toString(2));
            LOGGER.debug("Status Code: {}", is2xx);
        } catch (IOException ioe) {
            LOGGER.error(ioe);
        }
    }


}