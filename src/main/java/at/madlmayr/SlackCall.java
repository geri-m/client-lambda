package at.madlmayr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SlackCall implements RequestStreamHandler, ToolCall {

    private static final Logger LOGGER = LogManager.getLogger(SlackCall.class);
    private final DynamoAbstraction db;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SlackCall() {
        db = new DynamoAbstraction();
        AWSXRayRecorder recorder = new AWSXRayRecorder();
        recorder.setContextMissingStrategy((s, aClass) -> LOGGER.warn("Context for XRay is missing"));
        httpClient = HttpClientBuilder.create().setRecorder(recorder).build();
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        ToolCallRequest toolCallRequest;
        try {

            toolCallRequest = objectMapper.readValue(inputStream, ToolCallRequest.class);
        } catch (IOException e) {
            throw new ToolCallException(e);
        }
        JSONArray users = processCall(toolCallRequest.getUrl(), toolCallRequest.getBearer());
        db.writeRawData(toolCallRequest.generateKey(ToolEnum.SLACK.getName()), users.toString(), toolCallRequest.getTimestamp());

        try {
            ToolCallResult result = new ToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTool(), users.length());
            outputStream.write(objectMapper.writeValueAsString(result).getBytes());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            AWSXRay.getCurrentSegment().addException(e);
        }

    }

    @Override
    public JSONArray processCall(final String url, final String bearer) {
        HttpGet request = new HttpGet(url);
        // Set Bearer Header
        request.setHeader("Authorization", "Bearer " + bearer);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                LOGGER.info("HTTP Call to '{}' was successful", url);
                return new JSONObject(jsonString).getJSONArray("members");
            } else {
                throw new ToolCallException(String.format("Call to '%s' was not successful. Ended with response: '%s'", url, jsonString));
            }
        } catch (IOException ioe) {
            throw new ToolCallException(ioe);
        }

    }

}
