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
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


public class ReadConfig implements RequestStreamHandler {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(ReadConfig.class);
    private static final String CONFIG_TABLE_NAME = "Config";


    private static final AmazonDynamoDB dynamoClient;

    private static final AWSLambdaAsync lambda;

    static {
        dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
        lambda = AWSLambdaAsyncClientBuilder.defaultClient();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        Subsegment seg = AWSXRay.beginSubsegment("Read Config");

        // Get all Element from the Table
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(CONFIG_TABLE_NAME);

        ScanResult result = dynamoClient.scan(scanRequest);
        LOGGER.info("Amount of Config found: {}", result.getItems().size());
        seg.putMetadata("Amount of Configs", result.getItems().size());

        for (Map<String, AttributeValue> returnedItems : result.getItems()) {
            if (returnedItems != null) {
                try {
                    ToolConfig toolConfig = new ToolConfig(returnedItems);
                    LOGGER.info("Tool: '{}'", ToolEnum.valueOf(toolConfig.getTool().toUpperCase()).getName());
                    ObjectMapper mapper = new ObjectMapper();
                    InvokeRequest req = new InvokeRequest()
                            .withFunctionName(ToolEnum.valueOf(toolConfig.getTool().toUpperCase()).getFunctionName())
                            .withPayload(mapper.writeValueAsString(toolConfig));
                    lambda.invokeAsync(req);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.info("No item found in Config Table");
            }
        }
        AWSXRay.endSubsegment();
        // });
    }




}