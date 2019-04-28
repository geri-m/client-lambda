package at.madlmayr;

import org.apache.http.client.config.RequestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class WriteAccountsToDb<T extends Account> implements Call<T> {

    private static final Logger LOGGER = LogManager.getLogger(WriteAccountsToDb.class);
    private final DynamoFactory.DynamoAbstraction db;

    public WriteAccountsToDb() {
        db = new DynamoFactory().create();
    }

    public WriteAccountsToDb(int port) {
        db = new DynamoFactory().create(port);
    }


    public static RequestConfig getRequestConfig() {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        int TIMEOUT_IN_MS = 5000;
        requestBuilder.setConnectTimeout(TIMEOUT_IN_MS);
        requestBuilder.setConnectionRequestTimeout(TIMEOUT_IN_MS);
        requestBuilder.setSocketTimeout(TIMEOUT_IN_MS);
        return requestBuilder.build();
    }

    @Override
    public void writeStuffToDatabase(final List<T> userList, final ToolCallConfig toolCallRequest) {
        LOGGER.info("Amount of Users: {} ", userList.size());
        for (T member : userList) {
            member.setCompanyToolTimestamp(toolCallRequest.getCompany() + "#" + toolCallRequest.getTool() + "#" + toolCallRequest.getTimestampFormatted());
        }
        LOGGER.info("Start writing to db");
        db.writeMembersBatch(userList);
        LOGGER.info("End writing to db");

        ToolCallResult result = new ToolCallResult(toolCallRequest.getCompany(), toolCallRequest.getTool(), userList.size(), toolCallRequest.getTimestamp(), toolCallRequest.getNumberOfToolsPerCompany());
        db.writeCallResult(result);
        LOGGER.info("current result {}, Users: {}", result.getKey(), result.getAmountOfUsers());

        // also write the same element with Timestamp 0 into the DB, to indicate, this is the latest one.
        result.setTimestamp(0L);
        db.writeCallResult(result);

        List<ToolCallResult> unfinishedCalls = db.getAllToolCallResultUnfinished(toolCallRequest.getCompany(), toolCallRequest.getTimestamp());
        if (unfinishedCalls.isEmpty()) {
            LOGGER.info("All calls done");
        } else {
            LOGGER.info("Still waiting for other calls: {}", unfinishedCalls.size());
            for (ToolCallResult unfinishedJob : unfinishedCalls) {
                LOGGER.info("calls '{}' still running", unfinishedJob.getTool());
            }
        }
    }

}
