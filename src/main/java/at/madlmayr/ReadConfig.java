package at.madlmayr;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ReadConfig implements RequestHandler<Void, String>{

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(ReadConfig.class);
    private static final String CONFIG_TABLE_NAME = "Config";

    private static final AmazonDynamoDB dynamoClient;
    private static final HttpClient httpClient;

    static {
        dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public String handleRequest(Void input, Context context) {
        LOGGER.info("handleRequest: {}", input);

        // Get all Element from the Table
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(CONFIG_TABLE_NAME);

        ScanResult result = dynamoClient.scan(scanRequest);

        LOGGER.info("Amount of Config found: {}", result.getItems().size());

        for (Map<String, AttributeValue> returnedItems : result.getItems()){
            if (returnedItems != null) {
                Set<String> keys = returnedItems.keySet();
                LOGGER.info(returnedItems);
                for (String key : keys) {
                    LOGGER.info(String.format("%s: %s",
                            key, returnedItems.get(key).getS()));
                }

                // TODO: Add Xray later: https://docs.aws.amazon.com/de_de/lambda/latest/dg/java-tracing.html
                if(returnedItems.containsKey("URL") && returnedItems.containsKey("Bearer")){
                    HttpGet request = new HttpGet(returnedItems.get("URL").getS());
                    // Set Bearer Header
                    request.setHeader("Authorization", "Bearer " + returnedItems.get("Bearer").getS());
                    boolean is2xx = false;

                    try {
                        HttpResponse response = httpClient.execute(request);
                        String jsonString = EntityUtils.toString(response.getEntity());

                        // TODO: https://stackoverflow.com/questions/27500749/dynamodb-object-to-attributevalue
                        // TODO: https://www.baeldung.com/java-org-json
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        is2xx = (response.getStatusLine().getStatusCode() / 100) == 2;
                        if(is2xx){
                            LOGGER.debug("HTTP Call to '{}' was successful", returnedItems.get("URL").getS());
                            Map<String, AttributeValue> item = new HashMap<>();
                            item.put("CompanyTool", new AttributeValue().withS(returnedItems.get("Company").getS() + "#" +returnedItems.get("Tool").getS()));
                            item.put("Timestamp", new AttributeValue().withN("" + Instant.now().getEpochSecond()));
                            item.put("Data", new AttributeValue().withS(jsonResponse.toString()));
                            dynamoClient.putItem("RawData", item);
                            LOGGER.debug("Data successfully stored in DB");
                        } else {
                            LOGGER.error("Call to '{}' was not successful. Ended with response: '{}'", returnedItems.get("URL").getS(), jsonResponse.toString());
                        }
                    } catch (IOException ioe) {
                        LOGGER.error(ioe);
                    }
                }
            } else {
                LOGGER.info("No item found");
            }
        }

        return "handleRequest finished";
    }


}