package at.madlmayr;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.client.HttpClient;

import java.util.Map;


public class ReadConfig implements RequestHandler<Void, Void> {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(ReadConfig.class);
    private static final String CONFIG_TABLE_NAME = "Config";

    private static final AmazonDynamoDB dynamoClient;
    private static final HttpClient httpClient;

    private static final AWSLambdaAsync lambda;

    static {
        // AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.defaultRecorder());
        dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
        httpClient = HttpClientBuilder.create().build();
        lambda = AWSLambdaAsyncClientBuilder.defaultClient();
    }

    @Override
    public Void handleRequest(Void input, Context context)  {
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

                LOGGER.info("Tool: '{}'", ToolEnum.valueOf(returnedItems.get("Tool").getS()).getName());
                ToolConfig toolConfig = new ToolConfig(returnedItems.get("Company").getS(), returnedItems.get("URL").getS(), returnedItems.get("Bearer").getS());

                ObjectMapper mapper = new ObjectMapper();

                InvokeRequest req;
                try {
                    req = new InvokeRequest()
                            .withFunctionName(ToolEnum.valueOf(returnedItems.get("Tool").getS()).getFunctionName())
                            .withPayload(mapper.writeValueAsString(toolConfig));
                    lambda.invokeAsync(req);
                } catch (JsonProcessingException e) {
                    throw new ToolCallException(e);
                }
            } else {
                LOGGER.info("No item found in Config Table");
            }
        }
        // AWSXRay.endSegment();
        // });
        return null;
    }




}