package at.madlmayr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

public class JiraCall implements RequestStreamHandler, ToolCall {

    // JIRA v7.4.3 - https://docs.atlassian.com/software/jira/docs/api/REST/7.4.3/
    // API Version 2

    private static final int MAX_RESULT_COUNT_GET_PARAMETER = 1000;
    private static final String AVOID_TOO_MUCH_DEEPTH = "pros";
    private static final char[] SEARCH_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

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
        db.writeRawData(toolConfig.generateKey(ToolEnum.SLACK.getName()), processCall(toolConfig.getUrl(), toolConfig.getBearer()));
    }

    @Override
    public String processCall(final String url, final String bearer) {
        int totalUsers = recursiveCallForUser("", url, bearer);
        LOGGER.info("Total User: {}", totalUsers);
        return "";
    }

    private int recursiveCallForUser(final String preFix, final String url, final String bearer) {
        HttpGet request = null;
        int userCounter = 0;

        for (char searchChar : SEARCH_CHARS) {
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
                    JSONArray user = new JSONArray(jsonString);

                    LOGGER.info("Currently '{}' members in jira response (active, inactive, pp)", user.length());

                    // run a recursion, if the amount of result exceeds the max results
                    // Issue: the search also goes thru the email, so if the string matches the email, you will get all users.
                    if (user.length() == MAX_RESULT_COUNT_GET_PARAMETER) {
                        if ((preFix + searchChar).equals(AVOID_TOO_MUCH_DEEPTH)) {
                            LOGGER.info("This char sequence should be avoided ...");
                            break;
                        }
                        LOGGER.info("Recursion required");
                        userCounter += recursiveCallForUser(preFix + searchChar, url, bearer);
                    } else {
                        LOGGER.info("No Recursion required");
                        userCounter += user.length();
                    }


                } else {
                    throw new ToolCallException(String.format("Call to '%s' was not successful. Ended with response: '%s'", url, jsonString));
                }
            } catch (IOException ioe) {
                throw new ToolCallException(ioe);
            }

        }
        LOGGER.info("Total User: {}", userCounter);
        return userCounter;
    }
}
