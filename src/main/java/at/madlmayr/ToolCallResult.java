package at.madlmayr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ToolCallResult implements Serializable {

    private static final String COLUMN_COMPANY = "company";
    private static final String COLUMN_TOOL = "tool";
    private static final String COLUMN_AMOUNT_OF_USER = "amountOfUsers";

    @JsonProperty(COLUMN_COMPANY)
    private String company;

    @JsonProperty(COLUMN_TOOL)
    private String tool;

    @JsonProperty(COLUMN_AMOUNT_OF_USER)
    private int amountOfUsers;

    public ToolCallResult() {
    }

    public ToolCallResult(String company, String tool, int amountOfUsers) {
        this.company = company;
        this.tool = tool;
        this.amountOfUsers = amountOfUsers;
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
}
