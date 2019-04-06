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
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ReadConfig implements RequestStreamHandler {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(ReadConfig.class);

    private final AmazonDynamoDB dynamo;
    private final AWSLambdaAsync lambda;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReadConfig() {
        dynamo = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
        lambda = AWSLambdaAsyncClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        Subsegment seg = AWSXRay.beginSubsegment("Read Config");

        // Get all Element from the Table
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(ToolCallRequest.TABLE_NAME);

        ScanResult result = dynamo.scan(scanRequest);
        LOGGER.info("Amount of Config found: {}", result.getItems().size());
        seg.putMetadata("Amount of Configs", result.getItems().size());

        long timestampOfBatch = Instant.now().getEpochSecond();
        List<Future<InvokeResult>> futures = new ArrayList<>();

        for (Map<String, AttributeValue> returnedItems : result.getItems()) {
            if (returnedItems != null) {
                try {
                    ToolCallRequest toolCallRequest = new ToolCallRequest(returnedItems, timestampOfBatch);
                    LOGGER.info("Tool: '{}'", ToolEnum.valueOf(toolCallRequest.getTool().toUpperCase()).getName());
                    InvokeRequest req = new InvokeRequest()
                            .withFunctionName(ToolEnum.valueOf(toolCallRequest.getTool().toUpperCase()).getFunctionName())
                            .withPayload(objectMapper.writeValueAsString(toolCallRequest));
                    futures.add(lambda.invokeAsync(req));
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.info("No item found in Config Table");
            }
        }

        // 'remove()' of the List causes ConcurrentModificationException
        List<Future<InvokeResult>> futuresFinished = new ArrayList<>();

        // loop over the futures as long as input list is not the same size as finished list.
        while (futuresFinished.size() != futures.size()) {
            for (Future<InvokeResult> localFuture : futures) {
                // futures which are done, but not yet in the finished list.
                if (localFuture.isDone() && !futuresFinished.contains(localFuture)) {
                    try {
                        ToolCallResult resultFromCall = objectMapper.readValue(new String(localFuture.get().getPayload().array(), StandardCharsets.UTF_8), ToolCallResult.class);
                        LOGGER.info("Response from Method '{}', # Users: '{}'", resultFromCall.getTool(), resultFromCall.getAmountOfUsers());
                    } catch (final IOException | InterruptedException | ExecutionException e) {
                        LOGGER.error(e.getMessage());
                    }

                    // finished Features go into a separate list.
                    futuresFinished.add(localFuture);
                }
            }
        }
        AWSXRay.endSubsegment();
    }


}