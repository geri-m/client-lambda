package at.madlmayr;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.io.Serializable;

import static at.madlmayr.ToolCallResult.TABLE_NAME;

@DynamoDBTable(tableName = TABLE_NAME)
public class ToolCallResult implements Serializable {

    public static final String TABLE_NAME = "Results";
    public static final String COLUMN_COMPANY_TOOL = "companyTool";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private String company;

    private String tool;

    private int amountOfUsers;

    private long timestamp;

    private int numberOfToolsPerCompany;

    public ToolCallResult() {
    }

    public ToolCallResult(String company, String tool, int amountOfUsers, long timestamp, int numberOfToolsPerCompany) {
        this.company = company;
        this.tool = tool;
        this.amountOfUsers = amountOfUsers;
        this.timestamp = timestamp;
        this.numberOfToolsPerCompany = numberOfToolsPerCompany;
    }

    public int getNumberOfToolsPerCompany() {
        return numberOfToolsPerCompany;
    }

    public void setNumberOfToolsPerCompany(int numberOfToolsPerCompany) {
        this.numberOfToolsPerCompany = numberOfToolsPerCompany;
    }

    public String getCompany() {
        return company;
    }

    public String getTool() {
        return tool;
    }

    public int getAmountOfUsers() {
        return amountOfUsers;
    }

    public void setAmountOfUsers(int amountOfUsers) {
        this.amountOfUsers = amountOfUsers;
    }

    @DynamoDBIgnore
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBRangeKey(attributeName = ToolCallResult.COLUMN_TIMESTAMP)
    public String getTimestampFormatted() {
        return ISODateTimeFormat.dateTime().print(new DateTime(timestamp, DateTimeZone.UTC));
    }

    public void setTimestampFormatted(final String input) {
        timestamp = ISODateTimeFormat.dateTime().parseDateTime(input).getMillis();
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
            setCompany(segments[0]);
            setTool(segments[1]);
        }
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }
}
