package at.madlmayr.slack;

import at.madlmayr.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;

public class SlackCall implements RequestStreamHandler, ToolCall {

    private static final Logger LOGGER = LogManager.getLogger(SlackCall.class);
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Call<SlackMember> call;

    public SlackCall() {
        call = new WriteAccountsToDb<>();
        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).setDefaultRequestConfig(WriteAccountsToDb.getRequestConfig()).build();
    }

    public SlackCall(int port) {
        call = new WriteAccountsToDb<>(port);
        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).setDefaultRequestConfig(WriteAccountsToDb.getRequestConfig()).build();
    }


    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        ToolCallConfig toolCallRequest;
        try {
            toolCallRequest = objectMapper.readValue(inputStream, ToolCallConfig.class);
            JSONArray users = processCall(toolCallRequest.getUrl(), toolCallRequest.getBearer());
            List<SlackMember> userList = objectMapper.readValue(users.toString(), new TypeReference<List<SlackMember>>() {
            });
            call.writeStuffToDatabase(userList, toolCallRequest);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            AWSXRay.getGlobalRecorder().getCurrentSegment().addException(e);
            throw new ToolCallException(e);
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
                LOGGER.info("HTTP WriteAccountsToDb to '{}' was successful", url);
                return new JSONObject(jsonString).getJSONArray("members");
            } else {
                throw new ToolCallException(String.format("WriteAccountsToDb to '%s' was not successful. Ended with response: '%s'", url, jsonString));
            }
        } catch (IOException ioe) {
            throw new ToolCallException(ioe);
        }

    }

}
