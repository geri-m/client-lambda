package at.madlmayr;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@DynamoDBTable(tableName = ToolCallResult.TABLE_NAME)
public class ToolCallResult implements Serializable {

    public static final String COLUMN_COMPANY = "company";
    public static final String COLUMN_TOOL = "tool";
    public static final String TABLE_NAME = "CallResults";
    public static final String COLUMN_COMPANY_TOOL = "companyTool";
    private static final String COLUMN_AMOUNT_OF_USER = "amountOfUsers";
    public static final String TIME_STAMP = "timestamp";

    @JsonProperty(COLUMN_COMPANY)
    private String company;

    @JsonProperty(COLUMN_TOOL)
    private String tool;

    @JsonProperty(COLUMN_AMOUNT_OF_USER)
    private int amountOfUsers;

    @JsonProperty(TIME_STAMP)
    private long timeStamp;

    public ToolCallResult() {
    }

    public ToolCallResult(String company, String tool, int amountOfUsers, long timeStamp) {
        this.company = company;
        this.tool = tool;
        this.amountOfUsers = amountOfUsers;
        this.timeStamp = timeStamp;
    }

    @DynamoDBIgnore
    public String getCompany() {
        return company;
    }

    @DynamoDBIgnore
    public String getTool() {
        return tool;
    }

    public int getAmountOfUsers() {
        return amountOfUsers;
    }

    public void setAmountOfUsers(int amountOfUsers) {
        this.amountOfUsers = amountOfUsers;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @DynamoDBRangeKey(attributeName = ToolCallResult.TIME_STAMP)
    public String getTimeStampformated() {
        return Utils.standardTimeFormat(timeStamp);
    }

    public void setTimeStampformated(final String input) {
        timeStamp = Utils.parseStandardTime(input);
    }

    @DynamoDBHashKey(attributeName = ToolCallResult.COLUMN_COMPANY_TOOL)
    public String getKey() {
        return getCompany() + "#" + getTool();
    }

    public void setKey(final String key) {
        if (key == null)
            throw new ToolCallException("Key is null");

        if (!key.contains("#"))
            throw new ToolCallException(String.format("Key '%s 'does not contain '#' symbol for split.", key));

        String[] segments = key.split("#");
        if (segments.length != 2 || segments[0] == null || segments[1] == null) {
            throw new ToolCallException(String.format("Invalid Key '%s'.", key));
        } else {
            company = segments[0];
            tool = segments[1];
        }
    }

}
