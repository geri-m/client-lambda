package at.madlmayr;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.io.Serializable;

@DynamoDBTable(tableName = ToolCallResult.TABLE_NAME)
public class ToolCallResult implements Serializable {

    public static final String COLUMN_TOOL = "tool";
    public static final String TABLE_NAME = "CallResults";
    public static final String COLUMN_COMPANY_TIMESTAMP = "companyTimestamp";
    public static final String TIME_STAMP = "timestamp";

    private String company;

    private String tool;

    private int amountOfUsers;

    private long timestamp;

    private int numberOfToolsPerCompany;

    private int numberOfToolsPerCompanyProcessed;

    public ToolCallResult() {
    }

    public ToolCallResult(String company, String tool, int amountOfUsers, long timestamp, int numberOfToolsPerCompany) {
        this.company = company;
        this.tool = tool;
        this.amountOfUsers = amountOfUsers;
        this.timestamp = timestamp;
        this.numberOfToolsPerCompany = numberOfToolsPerCompany;
    }

    @DynamoDBIgnore
    public int getNumberOfToolsPerCompany() {
        return numberOfToolsPerCompany;
    }

    public void setNumberOfToolsPerCompany(int numberOfToolsPerCompany) {
        this.numberOfToolsPerCompany = numberOfToolsPerCompany;
    }

    @DynamoDBIgnore
    public int getNumberOfToolsPerCompanyProcessed() {
        return numberOfToolsPerCompanyProcessed;
    }

    public void setNumberOfToolsPerCompanyProcessed(int numberOfToolsPerCompanyProcessed) {
        this.numberOfToolsPerCompanyProcessed = numberOfToolsPerCompanyProcessed;
    }

    @DynamoDBIgnore
    public String getCompany() {
        return company;
    }

    @DynamoDBRangeKey(attributeName = ToolCallResult.COLUMN_TOOL)
    public String getTool() {
        return tool;
    }

    public int getAmountOfUsers() {
        return amountOfUsers;
    }

    public void setAmountOfUsers(int amountOfUsers) {
        this.amountOfUsers = amountOfUsers;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestampFormatted() {
        return Utils.standardTimeFormat(timestamp);
    }

    public void setTimestampFormatted(final String input) {
        timestamp = Utils.parseStandardTime(input);
    }

    @DynamoDBHashKey(attributeName = ToolCallResult.COLUMN_COMPANY_TIMESTAMP)
    public String getKey() {
        return getCompany() + "#" + getTimestampFormatted();
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
            setCompany(segments[0]);
            setTimestampFormatted(segments[1]);
        }
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }
}
