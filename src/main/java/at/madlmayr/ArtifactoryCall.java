package at.madlmayr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
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

public class ArtifactoryCall implements RequestStreamHandler, ToolCall {

    private static final Logger LOGGER = LogManager.getLogger(ArtifactoryCall.class);
    private final static DynamoAbstraction db;
    private static final CloseableHttpClient httpClient;
    private static final ObjectMapper objectMapper;

    static {
        db = new DynamoAbstraction();
        httpClient = HttpClientBuilder.create().build();
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        ToolConfig toolConfig;
        try {
            // Handling De-Serialziation myself
            // https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-req-resp.html
            toolConfig = objectMapper.readValue(inputStream, ToolConfig.class);
        } catch (IOException e) {
            throw new ToolCallException(e);
        }
        db.writeRawData(toolConfig.generateKey(ToolEnum.ARTIFACTORY.getName()), processCall(toolConfig.getUrl(), toolConfig.getBearer()));
    }

    @Override
    public String processCall(final String url, final String bearer) {
        HttpGet request = new HttpGet(url);
        // Set Bearer Header
        request.setHeader("X-JFrog-Art-Api", bearer);

        try {
            HttpResponse response = httpClient.execute(request);
            String jsonString = EntityUtils.toString(response.getEntity());
            if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                LOGGER.info("HTTP Call to '{}' was successful (fetching list of Users)", url);

                JSONArray userList = new JSONArray(jsonString);
                LOGGER.info("Amount of Users: {} ", userList.length());

                JSONArray userDetailList = new JSONArray();
                for(int i = 0; i < userList.length(); i++){
                    HttpGet requestForUser = new HttpGet(new JSONObject(userList.get(i).toString()).get("uri").toString());
                    requestForUser.setHeader("X-JFrog-Art-Api", bearer);
                    HttpResponse responseForUser = httpClient.execute(requestForUser);
                    String jsonStringForUser = EntityUtils.toString(responseForUser.getEntity());
                    userDetailList.put(new JSONObject(jsonStringForUser));

                }
                return userDetailList.toString();
            } else {
                throw new ToolCallException(String.format("Call to '%s' was not successful. Ended with response: '%s'", url, jsonString));
            }
        } catch (IOException ioe) {
            throw new ToolCallException(ioe);
        }
    }

}
