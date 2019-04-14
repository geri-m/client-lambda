package at.madlmayr;

import org.apache.http.client.config.RequestConfig;

public class Call {

    private static int TIMEOUT_IN_MS = 5000;

    public static RequestConfig getRequestConfig() {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(TIMEOUT_IN_MS);
        requestBuilder.setConnectionRequestTimeout(TIMEOUT_IN_MS);
        requestBuilder.setSocketTimeout(TIMEOUT_IN_MS);
        return requestBuilder.build();
    }

}
