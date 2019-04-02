package at.madlmayr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
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
    private final static DynamoAbstraction db;
    private static final CloseableHttpClient httpClient;
    private static final ObjectMapper objectMapper;
    private static final AWSXRayRecorder recorder;

    static {
        db = new DynamoAbstraction();
        recorder = new AWSXRayRecorder();
        recorder.setContextMissingStrategy((s, aClass) -> LOGGER.warn("Context for XRay is missing"));
        httpClient = HttpClientBuilder.create().setRecorder(recorder).build();
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        ToolConfig toolConfig;
        try {
            // Handling De-Serialization myself
            // https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-req-resp.html
            toolConfig = objectMapper.readValue(inputStream, ToolConfig.class);
        } catch (IOException e) {
            throw new ToolCallException(e);
        }
        JSONArray users = processCall(toolConfig.getUrl(), toolConfig.getBearer());
        AWSXRay.getGlobalRecorder().putRuntimeContext("Slack Users:", users.length());
        db.writeRawData(toolConfig.generateKey(ToolEnum.SLACK.getName()), processCall(toolConfig.getUrl(), toolConfig.getBearer()).toString());
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
