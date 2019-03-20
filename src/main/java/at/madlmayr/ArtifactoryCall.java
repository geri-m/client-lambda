package at.madlmayr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ArtifactoryCall implements RequestHandler<ToolConfig, Void>, ToolCall {

    private static final Logger LOGGER = LogManager.getLogger(ArtifactoryCall.class);
    private final static DynamoAbstraction db;
    private static final CloseableHttpClient httpClient;

    static {
        db = new DynamoAbstraction();
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public Void handleRequest(ToolConfig toolConfig, Context context)  {
        db.writeRawData(toolConfig.generateKey(ToolEnum.ARTIFACTORY.getName()), processCall(toolConfig.getUrl(), toolConfig.getBearer()));
        return null;
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
