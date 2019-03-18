package at.madlmayr;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class ReadConfig implements RequestHandler<Void, Void> {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(ReadConfig.class);
    private static final String CONFIG_TABLE_NAME = "Config";
    private static final String RAWDATA_TABLE_NAME = "RawData";

    private static final AmazonDynamoDB dynamoClient;
    private static final HttpClient httpClient;

    static {
        dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public Void handleRequest(Void input, Context context) {
        AWSXRay.beginSegment("Create Request");

        // AWSXRay.createSubsegment("makeRequest", (subsegment) -> {
            LOGGER.info("handleRequest: {}", input);

            // Get all Element from the Table
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(CONFIG_TABLE_NAME);

            ScanResult result = dynamoClient.scan(scanRequest);
            LOGGER.info("Amount of Config found: {}", result.getItems().size());

            for (Map<String, AttributeValue> returnedItems : result.getItems()){
                if (returnedItems != null) {
                    if (!returnedItems.containsKey("URL")) {
                        LOGGER.error("URL not present in Record");
                        return null;
                    }

                    if (!returnedItems.containsKey("Bearer")) {
                        LOGGER.error("URL not present in Record");
                        return null;
                    }

                    HttpGet request = new HttpGet(returnedItems.get("URL").getS());
                    // Set Bearer Header
                    request.setHeader("Authorization", "Bearer " + returnedItems.get("Bearer").getS());
                    try {
                        HttpResponse response = httpClient.execute(request);
                        String jsonString = EntityUtils.toString(response.getEntity());
                        if((response.getStatusLine().getStatusCode() / 100) == 2){
                            LOGGER.info("HTTP Call to '{}' was successful", returnedItems.get("URL").getS());
                            Map<String, AttributeValue> item = new HashMap<>();
                            item.put("CompanyTool", new AttributeValue().withS(returnedItems.get("Company").getS() + "#" + returnedItems.get("Tool").getS()));
                            item.put("Timestamp", new AttributeValue().withN("" + Instant.now().getEpochSecond()));
                            item.put("Data", new AttributeValue().withS(jsonString));
                            dynamoClient.putItem(RAWDATA_TABLE_NAME, item);
                            LOGGER.info("Data successfully stored in RawData Table");
                        } else {
                            LOGGER.error("Call to '{}' was not successful. Ended with response: '{}'", returnedItems.get("URL").getS(), jsonString);
                        }
                    } catch (IOException ioe) {
                        LOGGER.error(ioe);
                    }
                } else {
                    LOGGER.info("No item found in Config Table");
                }
            }
        AWSXRay.endSegment();
        // });
        return null;
    }


}