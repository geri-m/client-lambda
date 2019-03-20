package at.madlmayr;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class ReadConfig implements RequestHandler<Void, Void> {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(ReadConfig.class);
    private static final String CONFIG_TABLE_NAME = "Config";
    private static final String RAWDATA_TABLE_NAME = "RawData";

    private static final String CONFIG_SLACK = "slack";
    private static final String CONFIG_ARTIFACTORY = "artifactory";

    private static final AmazonDynamoDB dynamoClient;
    private static final HttpClient httpClient;

    static {
        // AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.defaultRecorder());
        dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public Void handleRequest(Void input, Context context) {
        // AWSXRay.beginSegment("Create Request");
        // AWSXRay.createSubsegment("makeRequest", (subsegment) -> {
        LOGGER.info("handleRequest: {}", input);

        // Get all Element from the Table
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(CONFIG_TABLE_NAME);

        ScanResult result = dynamoClient.scan(scanRequest);
        LOGGER.info("Amount of Config found: {}", result.getItems().size());

        for (Map<String, AttributeValue> returnedItems : result.getItems()) {
            if (returnedItems != null) {
                if (!returnedItems.containsKey("URL")) {
                    LOGGER.error("URL not present in Record");
                    return null;
                }

                if (!returnedItems.containsKey("Bearer")) {
                    LOGGER.error("URL not present in Record");
                    return null;
                }

                if (!returnedItems.containsKey("Tool")) {
                    LOGGER.error("Tool not present in Record");
                    return null;
                }

                String resultString = null;

                switch (returnedItems.get("Tool").getS()){
                    case CONFIG_SLACK:
                        resultString = processSlack(returnedItems.get("URL").getS(), returnedItems.get("Bearer").getS());
                        break;
                    case CONFIG_ARTIFACTORY:
                        resultString = processArtifactory(returnedItems.get("URL").getS(), returnedItems.get("Bearer").getS());
                        break;
                    default:
                        LOGGER.error("Tool '{}' unknown", returnedItems.get("Tool").getS());

                }

                if(resultString != null){
                    Map<String, AttributeValue> item = new HashMap<>();
                    item.put("CompanyTool", new AttributeValue().withS(returnedItems.get("Company").getS() + "#" + returnedItems.get("Tool").getS()));
                    item.put("Timestamp", new AttributeValue().withN("" + Instant.now().getEpochSecond()));
                    item.put("Data", new AttributeValue().withS(resultString));
                    dynamoClient.putItem(RAWDATA_TABLE_NAME, item);
                    LOGGER.info("Data successfully stored in RawData Table");
                } else {
                    LOGGER.error("No Results from Parsing response from HTTP Calls");
                }


            } else {
                LOGGER.info("No item found in Config Table");
            }
        }
        // AWSXRay.endSegment();
        // });
        return null;
    }


    String processSlack(final String url, final String bearer){
        HttpGet request = new HttpGet(url);

        // Set Bearer Header
        request.setHeader("Authorization", "Bearer " + bearer);
        try {
            HttpResponse response = httpClient.execute(request);
            String jsonString = EntityUtils.toString(response.getEntity());
            if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                LOGGER.info("HTTP Call to '{}' was successful", url);
                return jsonString;
            } else {
                LOGGER.error("Call to '{}' was not successful. Ended with response: '{}'", url, jsonString);
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe);
        }
        return null;
    }


    String processArtifactory(final String url, final String bearer){
        HttpGet request = new HttpGet(url);

        // Set Bearer Header

        request.setHeader("X-JFrog-Art-Api", bearer);
        try {
            HttpResponse response = httpClient.execute(request);
            String jsonString = EntityUtils.toString(response.getEntity());
            if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                LOGGER.info("HTTP Call to '{}' was successful (fetching list of Users)", url);

                JSONArray userList = new JSONArray(jsonString);
                LOGGER.info("Amount of Users: {} ", userList.length());

                JSONArray userDetailList = new JSONArray();
                for(int i = 0; i < userList.length(); i++){
                    // LOGGER.info(new JSONObject(userList.get(i).toString()).get("uri"));
                    HttpGet requestForUser = new HttpGet(new JSONObject(userList.get(i).toString()).get("uri").toString());
                    requestForUser.setHeader("X-JFrog-Art-Api", bearer);
                    HttpResponse responseForUser = httpClient.execute(requestForUser);
                    String jsonStringForUser = EntityUtils.toString(responseForUser.getEntity());
                    userDetailList.put(new JSONObject(jsonStringForUser));
                }
                return userDetailList.toString();
            } else {
                LOGGER.error("Call to '{}' was not successful. Ended with response: '{}'", url, jsonString);
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe);
        }
        return null;
    }


}