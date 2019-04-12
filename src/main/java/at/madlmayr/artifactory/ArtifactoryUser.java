package at.madlmayr.artifactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ArtifactoryUser implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("admin")
    private boolean admin;

    @JsonProperty("profileUpdatable")
    private boolean profileUpdatable;

    @JsonProperty("internalPasswordDisabled")
    private boolean internalPasswordDisabled;

    @JsonProperty("groups")
    private String[] groups;

    @JsonProperty("lastLoggedIn")
    private String lastLoggedIn;

    @JsonProperty("lastLoggedInMillis")
    private long lastLoggedInMillis;

    @JsonProperty("offlineMode")
    private boolean offlineMode;

    @JsonProperty("disableUIAccess")
    private boolean disableUIAccess;

    @JsonProperty("realm")
    private String realm;

    public ArtifactoryUser() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
