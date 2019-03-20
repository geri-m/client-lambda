package at.madlmayr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ToolConfig implements Serializable {

    @JsonProperty("url")
    private String url;

    @JsonProperty("bearer")
    private String bearer;

    @JsonProperty("company")
    private String company;

    public ToolConfig(String company, String url, String bearer) {
        this.url = url;
        this.bearer = bearer;
        this.company = company;
    }

    public String getUrl() {
        return url;
    }

    public String getBearer() {
        return bearer;
    }

    public String generateKey(final String tool){
        return company + "#" + tool;
    }
}
