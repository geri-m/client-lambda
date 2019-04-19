package at.madlmayr;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@DynamoDBTable(tableName = ToolCallRequest.TABLE_NAME)
public class ToolCallRequest implements Serializable {
    public static final String TABLE_NAME = "Config";
    public static final String COLUMN_COMPANY = "company";
    public static final String COLUMN_TOOL = "tool";
    private static final String COLUMN_BEARER = "bearer";
    private static final String COLUMN_URL = "url";
    private static final String TIME_STAMP = "timestamp";
    public static final String COLUMN_BATCH_SIZE = "batchSize";

    private static final int AMOUNT_OF_PARAMETER = 4;

    @JsonProperty(COLUMN_COMPANY)
    private String company;

    @JsonProperty(COLUMN_BATCH_SIZE)
    private int batchSize;

    @JsonProperty(COLUMN_TOOL)
    private String tool;

    @JsonProperty(COLUMN_BEARER)
    private String bearer;

    @JsonProperty(COLUMN_URL)
    private String url;

    @JsonProperty(TIME_STAMP)
    private long timestamp;

    // Required for Jackson to create an Object.
    public ToolCallRequest() {
    }

    public ToolCallRequest(final String[] values, final long timestamp, final int batchSize) throws ToolCallException {
        if (values.length != AMOUNT_OF_PARAMETER) {
            throw new IllegalArgumentException(String.format("Incorrect Length of Parameter to create ToolCallRequest. Required %s, given '%s'", AMOUNT_OF_PARAMETER, values.length));
        }

        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put(COLUMN_COMPANY, new AttributeValue(values[0].trim()));
        dataFromDynamo.put(COLUMN_TOOL, new AttributeValue(values[1].trim()));
        dataFromDynamo.put(COLUMN_BEARER, new AttributeValue(values[2].trim()));
        dataFromDynamo.put(COLUMN_URL, new AttributeValue(values[3].trim()));
        assignLocalVales(dataFromDynamo, timestamp, batchSize);
    }


    public ToolCallRequest(Map<String, AttributeValue> dataFromDynamo, final long timestamp, final int batchSize) throws ToolCallException {
        assignLocalVales(dataFromDynamo, timestamp, batchSize);
    }

    private void assignLocalVales(Map<String, AttributeValue> dataFromDynamo, final long timestamp, final int batchSize) {
        if (!dataFromDynamo.containsKey(COLUMN_COMPANY) || dataFromDynamo.get(COLUMN_COMPANY).getS().isEmpty()) {
            throw new ToolCallException("Company not present in Record");
        }

        if (!dataFromDynamo.containsKey(COLUMN_TOOL) || dataFromDynamo.get(COLUMN_TOOL).getS().isEmpty()) {
            throw new ToolCallException("Tool not present in Record");
        }

        if (!dataFromDynamo.containsKey(COLUMN_BEARER) || dataFromDynamo.get(COLUMN_BEARER).getS().isEmpty()) {
            throw new ToolCallException("Bearer not present in Record");
        }

        if (!dataFromDynamo.containsKey(COLUMN_URL) || dataFromDynamo.get(COLUMN_URL).getS().isEmpty()) {
            throw new ToolCallException("URL not present in Record");
        }

        this.url = dataFromDynamo.get(COLUMN_URL).getS();
        this.bearer = dataFromDynamo.get(COLUMN_BEARER).getS();
        this.company = dataFromDynamo.get(COLUMN_COMPANY).getS();
        this.tool = dataFromDynamo.get(COLUMN_TOOL).getS();
        this.timestamp = timestamp;
        this.batchSize = batchSize;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBHashKey(attributeName = ToolCallRequest.COLUMN_COMPANY)
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @DynamoDBAttribute(attributeName = ToolCallRequest.COLUMN_URL)
    public String getUrl() {
        return url;
    }

    @DynamoDBIgnore
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @DynamoDBAttribute(attributeName = ToolCallRequest.COLUMN_BEARER)
    public String getBearer() {
        return bearer;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    @DynamoDBRangeKey(attributeName = ToolCallRequest.COLUMN_TOOL)
    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String generateKey(final String tool){
        return company + "#" + tool;
    }

    @DynamoDBIgnore
    public long getTimestamp() {
        return timestamp;
    }
}
