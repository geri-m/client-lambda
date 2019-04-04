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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class JiraV2Call implements RequestStreamHandler, ToolCall {

    // JIRA v7.4.3 - https://docs.atlassian.com/software/jira/docs/api/REST/7.4.3/
    // API Version 2

    private static final int MAX_RESULT_COUNT_GET_PARAMETER = 1000;
    private static final int MAX_RECURSION_DEPTH = 3;
    private static final char[] SEARCH_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static final Logger LOGGER = LogManager.getLogger(SlackCall.class);
    private final DynamoAbstraction db;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JiraV2Call() {
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
            // Handling De-Serialization myself
            // https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-req-resp.html
            toolCallRequest = objectMapper.readValue(inputStream, ToolCallRequest.class);
        } catch (IOException e) {
            throw new ToolCallException(e);
        }
        JSONArray users = processCall(toolCallRequest.getUrl(), toolCallRequest.getBearer());
        // DynamoDb allows only 400 K of Data per Record. We have > 1 MB. (4000 Users)
        // db.writeRawData(toolCallRequest.generateKey(ToolEnum.JIRA.getName()),users, toolCallRequest.getTimestamp());

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
        Map<String, String> allUsers = recursiveCallForUser("", url, bearer);
        JSONArray userArray = new JSONArray();
        for (Map.Entry<String, String> entry : allUsers.entrySet()) {
            userArray.put(new JSONObject(entry.getValue()));
        }
        return userArray;
    }

    private Map<String, String> recursiveCallForUser(final String preFix, final String url, final String bearer) {

        Map<String, String> result = new HashMap<>();

        for (char searchChar : SEARCH_CHARS) {
            HttpGet request;
            try {
                URIBuilder builder = new URIBuilder(url);
                // we don't use startAt Parameter, as we fetch the max results at once.
                builder.setParameter("maxResults", Integer.toString(MAX_RESULT_COUNT_GET_PARAMETER));
                builder.setParameter("username", preFix + searchChar);
                request = new HttpGet(builder.build());
            } catch (URISyntaxException e) {
                throw new ToolCallException(e);
            }

            // Set Bearer Header
            request.setHeader("Authorization", "Basic " + bearer);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonString = EntityUtils.toString(response.getEntity());
                if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                    LOGGER.info("HTTP Call to '{}' was successful", request.getURI().toURL());
                    JSONArray users = new JSONArray(jsonString);

                    LOGGER.info("Currently '{}' members in jira response (active, inactive, pp)", users.length());

                    // run a recursion, if the amount of result exceeds the max results
                    // Issue: the search also goes thru the email, so if the string matches the email, you will get all users.
                    if (users.length() == MAX_RESULT_COUNT_GET_PARAMETER) {
                        if ((preFix + searchChar).length() >= MAX_RECURSION_DEPTH) {
                            LOGGER.warn("Not Crawling deeper on prefix '{}'.", preFix + searchChar);
                            break;
                        }
                        Map<String, String> resultFromRecursion = recursiveCallForUser(preFix + searchChar, url, bearer);
                        result.putAll(resultFromRecursion);
                    } else {
                        for (Object user : users) {
                            // remove Avator URL, in order to save some space.
                            ((JSONObject) user).remove("avatarUrls");
                            result.put(((JSONObject) user).getString("name"), user.toString());
                        }
                    }
                } else {
                    throw new ToolCallException(String.format("Call to '%s' was not successful. Ended with response: '%s'", url, jsonString));
                }
            } catch (IOException ioe) {
                throw new ToolCallException(ioe);
            }

        }
        LOGGER.info("Sub-Total User: {}", result.size());
        return result;
    }
}
