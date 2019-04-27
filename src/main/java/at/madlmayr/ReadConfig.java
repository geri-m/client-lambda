package at.madlmayr;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class ReadConfig implements RequestStreamHandler {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(ReadConfig.class);

    private final DynamoFactory.DynamoAbstraction db;
    private final AWSLambdaAsync lambda;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReadConfig() {
        db = new DynamoFactory().create();
        lambda = AWSLambdaAsyncClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
    }

    public ReadConfig(int port, final AWSLambdaAsync lambdaAsync) {
        db = new DynamoFactory().create(port);
        lambda = lambdaAsync;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        // this timestamp identifies all calls of company that go together
        long batchTimeStamp = System.currentTimeMillis();
        List<ToolCallConfig> requests = db.getAllToolCallRequest();
        for (ToolCallConfig toolCallRequest : requests) {
                try {
                    // a batch has size and amount of elements in the batch
                    toolCallRequest.setTimestamp(batchTimeStamp);
                    toolCallRequest.setNumberOfToolsPerCompany(requests.size());
                    LOGGER.info("Tool: '{}'", ToolEnum.valueOf(toolCallRequest.getTool().toUpperCase()).getName());

                    // writing empty record to database of Call Results; User Count it -1
                    db.writeCallResult(new ToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTool(), -1, toolCallRequest.getTimestamp(), requests.size()));

                    InvokeRequest req = new InvokeRequest()
                            .withFunctionName(ToolEnum.valueOf(toolCallRequest.getTool().toUpperCase()).getFunctionName())
                            .withPayload(objectMapper.writeValueAsString(toolCallRequest));

                    // Fire and Forget
                    lambda.invokeAsync(req);
                } catch (Exception e) {
                    LOGGER.error(e);
                    AWSXRay.getGlobalRecorder().getCurrentSegment().addException(e);
                }
        }
    }
}