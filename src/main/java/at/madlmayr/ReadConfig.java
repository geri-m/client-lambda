package at.madlmayr;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


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
        List<Future<InvokeResult>> futures = new ArrayList<>();
        for (ToolCallRequest toolCallRequest : db.getAllToolCallRequest()) {
                try {
                    toolCallRequest.setTimestamp(System.currentTimeMillis());
                    LOGGER.info("Tool: '{}'", ToolEnum.valueOf(toolCallRequest.getTool().toUpperCase()).getName());
                    InvokeRequest req = new InvokeRequest()
                            .withFunctionName(ToolEnum.valueOf(toolCallRequest.getTool().toUpperCase()).getFunctionName())
                            .withPayload(objectMapper.writeValueAsString(toolCallRequest));
                    futures.add(lambda.invokeAsync(req));
                } catch (Exception e) {
                    LOGGER.error(e);
                    AWSXRay.getGlobalRecorder().getCurrentSegment().addException(e);
                }
        }

        // 'remove()' of the List causes ConcurrentModificationException
        List<Future<InvokeResult>> futuresFinished = new ArrayList<>();

        // loop over the futures as long as input list is not the same size as finished list.
        while (futuresFinished.size() != futures.size()) {
            for (Future<InvokeResult> localFuture : futures) {
                // futures which are done, but not yet in the finished list.
                if (localFuture.isDone() && !futuresFinished.contains(localFuture)) {
                    LOGGER.debug("New Future is done ...");
                    // finished Features go into a separate list.
                    futuresFinished.add(localFuture);
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    throw new ToolCallException(e);
                }
            }
        }

        for (Future<InvokeResult> future : futuresFinished) {
            try {
                ToolCallResult resultFromCall = objectMapper.readValue(new String(future.get().getPayload().array(), StandardCharsets.UTF_8), ToolCallResult.class);
                LOGGER.info("Response from Method '{}', # Users: '{}'", resultFromCall.getTool(), resultFromCall.getAmountOfUsers());
                db.writeCallResult(resultFromCall);
            } catch (final IOException | ExecutionException e) {
                LOGGER.error(e.getMessage());
                AWSXRay.getGlobalRecorder().getCurrentSegment().addException(e);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
                AWSXRay.getGlobalRecorder().getCurrentSegment().addException(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }

    }
}