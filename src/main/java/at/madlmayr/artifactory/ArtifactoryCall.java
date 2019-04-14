package at.madlmayr.artifactory;

import at.madlmayr.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
            ArtifactoryListElement[] userArray = objectMapper.readValue(listElements.toString(), ArtifactoryListElement[].class);
            LOGGER.info("Amount of Users: {} ", userArray.length);

            // We do the sync call for several reasons
            // 1) Xray works out of the box
            // 2) Not much mor expensive than Sync all (yes, 100 % is something, but its cents)
            // 3) less effort for testing.
            JSONArray detailUsers = doClientCallsSync(listElements, toolCallRequest.getBearer());
            List<ArtifactoryUser> userArrayDetails = objectMapper.readValue(detailUsers.toString(), new TypeReference<List<ArtifactoryUser>>() {
            });
            LOGGER.info("Amount of Users: {} ", userArrayDetails.size());

            for (ArtifactoryUser user : userArrayDetails) {
                user.setCompanyToolTimestamp(toolCallRequest.getCompany() + "#" + toolCallRequest.getTool() + "#" + Utils.standardTimeFormat(toolCallRequest.getTimestamp()));
            }

            db.writeArtifactoryMembersBatch(userArrayDetails);

            ToolCallResult result = new ToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTool(), listElements.length(), toolCallRequest.getTimestamp());
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

    public JSONArray doClientCallsSync(final JSONArray userList, final String bearer) {
        JSONArray userDetailList = new JSONArray();
        for (Object jsonUserObject : userList) {
            String uri = ((JSONObject) jsonUserObject).get("uri").toString();
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

    private JSONArray doClientCallsAsync(final JSONArray userList, final String bearer) throws IOReactorException, ToolCallException {
        Subsegment userSubSegment = AWSXRay.beginSubsegment("## Fetch Users");
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm =
                new PoolingNHttpClientConnectionManager(ioReactor);

        try (CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom().setConnectionManager(cm).build()) {
            httpAsyncClient.start();
            final CountDownLatch latchForUserCalls = new CountDownLatch(userList.length());

            // Response Element
            JSONArray userDetailList = new JSONArray();

            for (int i = 0; i < userList.length(); i++) {
                String uri = new JSONObject(userList.get(i).toString()).get("uri").toString();

                HttpGet requestForUser = new HttpGet(uri);
                requestForUser.setHeader("X-JFrog-Art-Api", bearer);
                httpAsyncClient.execute(requestForUser, new FutureCallback<HttpResponse>() {

                    public void completed(final HttpResponse response) {
                        try {
                            String jsonStringForUser = EntityUtils.toString(response.getEntity());
                            userDetailList.put(new JSONObject(jsonStringForUser));
                        } catch (IOException e) {
                            throw new ToolCallException(e);
                        } finally {
                            latchForUserCalls.countDown();
                        }
                    }

                    public void failed(final Exception ex) {
                        latchForUserCalls.countDown();
                        LOGGER.error(requestForUser.getRequestLine() + "->" + ex);
                    }

                    public void cancelled() {
                        latchForUserCalls.countDown();
                        LOGGER.error(requestForUser.getRequestLine() + " cancelled");
                    }
                });
            }
            // Wait till all calls come back.
            latchForUserCalls.await();
            userSubSegment.putMetadata("Amount of Users", userList.length());
            return userDetailList;
        } catch (IOException | InterruptedException e) {
            userSubSegment.addException(e);
            throw new ToolCallException(e);
        } finally {
            AWSXRay.endSubsegment();
        }

    }
}
