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
    private final DynamoFactory.DynamoAbstraction db;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public SlackCall() {
        db = new DynamoFactory().create();
        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).setDefaultRequestConfig(Call.getRequestConfig()).build();
    }

    public SlackCall(int port) {
        db = new DynamoFactory().create(port);
        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).setDefaultRequestConfig(Call.getRequestConfig()).build();
    }


    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        ToolCallRequest toolCallRequest;
        try {
            toolCallRequest = objectMapper.readValue(inputStream, ToolCallRequest.class);

            JSONArray users = processCall(toolCallRequest.getUrl(), toolCallRequest.getBearer());
            List<SlackMember> userList = objectMapper.readValue(users.toString(), new TypeReference<List<SlackMember>>() {
            });

            for (SlackMember member : userList) {
                member.setCompanyToolTimestamp(toolCallRequest.getCompany() + "#" + toolCallRequest.getTool() + "#" + Utils.standardTimeFormat(toolCallRequest.getTimestamp()));
                member.setId(member.getId());
            }

            db.writeSlackMembersBatch(userList);

            ToolCallResult result = new ToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTool(), users.length(), toolCallRequest.getTimestamp());
            outputStream.write(objectMapper.writeValueAsString(result).getBytes());

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            AWSXRay.getCurrentSegment().addException(e);
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
