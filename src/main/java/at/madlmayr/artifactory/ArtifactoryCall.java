package at.madlmayr.artifactory;

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

public class ArtifactoryCall implements RequestStreamHandler, ToolCall {

    private final static Logger LOGGER = LogManager.getLogger(ArtifactoryCall.class);
    private final DynamoFactory.DynamoAbstraction db;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public ArtifactoryCall() {
        db = new DynamoFactory().create();
        // We use XRay, hence the {@link HttpClients.createDefault();} is not used

        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).setDefaultRequestConfig(Call.getRequestConfig()).build();
    }

    public ArtifactoryCall(int port) {
        db = new DynamoFactory().create(port);
        // We use XRay, hence the {@link HttpClients.createDefault();} is not used
        httpClient = HttpClientBuilder.create().setRecorder(AWSXRay.getGlobalRecorder()).setDefaultRequestConfig(Call.getRequestConfig()).build();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {

        ToolCallRequest toolCallRequest;
        try {
            // Handling De-Serialization myself
            // https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-req-resp.html
            toolCallRequest = objectMapper.readValue(inputStream, ToolCallRequest.class);

            JSONArray listElements = processCall(toolCallRequest.getUrl(), toolCallRequest.getBearer());
            List<ArtifactoryListElement> userArray = objectMapper.readValue(listElements.toString(), new TypeReference<List<ArtifactoryListElement>>() {
            });
            LOGGER.info("Amount of Users: {} ", userArray.size());

            // We do the sync call for several reasons
            // 1) Xray works out of the box
            // 2) Not much mor expensive than Sync all (yes, 100 % is something, but its cents)
            // 3) less effort for testing.
            JSONArray detailUsers = doClientCallsSync(userArray, toolCallRequest.getBearer());
            List<ArtifactoryUser> userArrayDetails = objectMapper.readValue(detailUsers.toString(), new TypeReference<List<ArtifactoryUser>>() {
            });
            LOGGER.info("Amount of Detailed Users: {} ", userArrayDetails.size());

            for (ArtifactoryUser user : userArrayDetails) {
                user.setCompanyToolTimestamp(toolCallRequest.getCompany() + "#" + toolCallRequest.getTool() + "#" + Utils.standardTimeFormat(toolCallRequest.getTimestamp()));
            }
            LOGGER.info("Start writing to db");
            db.writeArtifactoryMembersBatch(userArrayDetails);
            LOGGER.info("End writing to db");

            // Synchronizing the different Lambda Jobs
            ToolCallResult result = new ToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTool(), listElements.length(), toolCallRequest.getTimestamp(), toolCallRequest.getNumberOfToolsPerCompany());
            db.writeCallResult(result);
            LOGGER.info("current result {}", result.getKey());

            // also write the same element with Timestamp 0 into the DB, to indicate, this is the latest one.
            result.setTimestamp(0L);
            db.writeCallResult(result);

            int amountOfCallsFinished = db.getAllToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTimestamp()).size();
            if (amountOfCallsFinished == toolCallRequest.getNumberOfToolsPerCompany()) {
                LOGGER.info("All calls done");
            } else {
                LOGGER.info("Still waiting for other Jobs");
            }
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
        request.setHeader("X-JFrog-Art-Api", bearer);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                LOGGER.info("HTTP Call to '{}' was successful (fetching list of Users)", url);
                return new JSONArray(jsonString);
            } else {
                throw new ToolCallException(String.format("Call to '%s' was not successful. Ended with response: '%s'", url, jsonString));
            }
        } catch (IOException ioe) {
            throw new ToolCallException(ioe);
        }
    }

    public JSONArray doClientCallsSync(final List<ArtifactoryListElement> userList, final String bearer) {
        JSONArray userDetailList = new JSONArray();
        for (ArtifactoryListElement user : userList) {
            String uri = user.getUri();
            HttpGet requestForUser = new HttpGet(uri);
            requestForUser.setHeader("X-JFrog-Art-Api", bearer);
            try (CloseableHttpResponse responseForUser = httpClient.execute(requestForUser)) {
                if ((responseForUser.getStatusLine().getStatusCode() / 100) == 2) {
                    String jsonStringForUser = EntityUtils.toString(responseForUser.getEntity());
                    userDetailList.put(new JSONObject(jsonStringForUser));
                } else {
                    LOGGER.error("Failed to to Detailed User Call using URL '{}'", uri);
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return userDetailList;
    }
}
