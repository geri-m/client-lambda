package at.madlmayr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class SlackCall implements RequestHandler<ToolConfig, Void>,  ToolCall {

    private static final Logger LOGGER = LogManager.getLogger(SlackCall.class);
    private final static DynamoAbstraction db;
    private static final HttpClient httpClient;

    static {
        db = new DynamoAbstraction();
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public Void handleRequest(ToolConfig toolConfig, Context context) {
        db.writeRawData(toolConfig.generateKey(ToolEnum.SLACK.getName()), processCall(toolConfig.getUrl(), toolConfig.getBearer()));
        return null;
    }

    @Override
    public String processCall(final String url, final String bearer) {
        HttpGet request = new HttpGet(url);
        // Set Bearer Header
        request.setHeader("Authorization", "Bearer " + bearer);
        HttpResponse response;
        try {
            response = httpClient.execute(request);
            String jsonString = EntityUtils.toString(response.getEntity());
            if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                LOGGER.info("HTTP Call to '{}' was successful", url);
                return jsonString;
            } else {
                throw new ToolCallException(String.format("Call to '%s' was not successful. Ended with response: '%s'", url, jsonString));
            }
        } catch (IOException ioe) {
            throw new ToolCallException(ioe);
        }

    }

}
