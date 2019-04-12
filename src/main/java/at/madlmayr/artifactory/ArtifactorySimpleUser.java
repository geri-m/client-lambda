package at.madlmayr.artifactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


public class ArtifactorySimpleUser implements Serializable {

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("realm")
    private String realm;

    @JsonProperty("name")
    private String name;

    public ArtifactorySimpleUser() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
