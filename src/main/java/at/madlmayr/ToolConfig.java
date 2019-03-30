package at.madlmayr;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class ToolConfig implements Serializable {

    private static final String COLUMN_COMPANY = "Company";
    private static final String COLUMN_URL = "URL";
    private static final String COLUMN_BEARER = "Bearer";
    private static final String COLUMN_TOOL = "Tool";

    @JsonProperty(COLUMN_URL)
    private String url;

    @JsonProperty(COLUMN_BEARER)
    private String bearer;

    @JsonProperty(COLUMN_COMPANY)
    private String company;

    @JsonProperty(COLUMN_TOOL)
    private String tool;

    // Required for Jackson to create an Object.
    public ToolConfig() {

    }

    public ToolConfig(final String url, final String bearer, final String company, final String tool) {

    }


    public ToolConfig(Map<String, AttributeValue> returnedItems) throws ToolCallException {

        if (!returnedItems.containsKey(COLUMN_COMPANY) || returnedItems.get(COLUMN_COMPANY).getS().isEmpty()) {
            throw new ToolCallException("Company not present in Record");
        }

        if (!returnedItems.containsKey(COLUMN_URL) || returnedItems.get(COLUMN_URL).getS().isEmpty()) {
            throw new ToolCallException("URL not present in Record");
        }

        if (!returnedItems.containsKey(COLUMN_BEARER) || returnedItems.get(COLUMN_BEARER).getS().isEmpty()) {
            throw new ToolCallException("Bearer not present in Record");
        }

        if (!returnedItems.containsKey(COLUMN_TOOL) || returnedItems.get(COLUMN_TOOL).getS().isEmpty()) {
            throw new ToolCallException("Tool not present in Record");
        }

        this.url = returnedItems.get(COLUMN_URL).getS();
        this.bearer = returnedItems.get(COLUMN_BEARER).getS();
        this.company = returnedItems.get(COLUMN_COMPANY).getS();
        this.tool = returnedItems.get(COLUMN_TOOL).getS();
    }

    public String getUrl() {
        return url;
    }

    public String getBearer() {
        return bearer;
    }

    public String getTool() {
        return tool;
    }

    public String generateKey(final String tool){
        return company + "#" + tool;
    }
}
