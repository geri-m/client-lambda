package at.madlmayr.jira;

import at.madlmayr.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public static final int MAX_RESULT_COUNT_GET_PARAMETER = 1000;
    public static final int MAX_RECURSION_DEPTH = 3;
    public static final char[] SEARCH_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static final Logger LOGGER = LogManager.getLogger(JiraV2Call.class);
    private final DynamoFactory.DynamoAbstraction db;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JiraV2Call() {
        db = new DynamoFactory().create();
        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).build();
    }

    public JiraV2Call(int port) {
        db = new DynamoFactory().create(port);
        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).build();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        ToolCallRequest toolCallRequest;
        try {
            // Handling De-Serialization myself
            // https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-req-resp.html
            toolCallRequest = objectMapper.readValue(inputStream, ToolCallRequest.class);
            JSONArray users = processCall(toolCallRequest.getUrl(), toolCallRequest.getBearer());

            JiraSearchResultElement[] userArrayDetails = objectMapper.readValue(users.toString(), JiraSearchResultElement[].class);
            LOGGER.info("Amount of Users: {} ", userArrayDetails.length);

            for (JiraSearchResultElement user : userArrayDetails) {
                user.setCompanyToolTimestamp(toolCallRequest.getCompany() + "#" + toolCallRequest.getTool() + "#" + Utils.standardTimeFormat(toolCallRequest.getTimestamp()));
                db.writeJiraUser(user);
            }

            ToolCallResult result = new ToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTool(), users.length());
            outputStream.write(objectMapper.writeValueAsString(result).getBytes());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            AWSXRay.getCurrentSegment().addException(e);
            throw new ToolCallException(e);
        }
    }

    @Override
    public JSONArray processCall(final String url, final String bearer) {
        Map<String, JiraSearchResultElement> allUsers = recursiveCallForUser("", url, bearer);
        JSONArray userArray = new JSONArray();
        for (Map.Entry<String, JiraSearchResultElement> entry : allUsers.entrySet()) {
            try {
                userArray.put(new JSONObject(objectMapper.writeValueAsString(entry.getValue())));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return userArray;
    }

    private Map<String, JiraSearchResultElement> recursiveCallForUser(final String preFix, final String url, final String bearer) {

        Map<String, JiraSearchResultElement> result = new HashMap<>();

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
                    JSONArray users = new JSONArray(jsonString);

                    JiraSearchResultElement[] userList = objectMapper.readValue(users.toString(), JiraSearchResultElement[].class);

                    // run a recursion, if the amount of result exceeds the max results
                    // Issue: the search also goes thru the email, so if the string matches the email, you will get all users.
                    if (users.length() == MAX_RESULT_COUNT_GET_PARAMETER) {
                        if ((preFix + searchChar).length() >= MAX_RECURSION_DEPTH) {
                            LOGGER.warn("Not Crawling deeper on prefix '{}'.", preFix + searchChar);
                        } else {
                            Map<String, JiraSearchResultElement> resultFromRecursion = recursiveCallForUser(preFix + searchChar, url, bearer);
                            result.putAll(resultFromRecursion);
                        }
                    } else {
                        for (Object user : users) {
                            JiraSearchResultElement singleUser = objectMapper.readValue(user.toString(), JiraSearchResultElement.class);
                            result.put(singleUser.getKey(), singleUser);
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
