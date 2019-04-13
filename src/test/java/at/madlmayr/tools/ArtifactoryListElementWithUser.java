package at.madlmayr.tools;

import at.madlmayr.artifactory.ArtifactoryListElement;
import at.madlmayr.artifactory.ArtifactoryUser;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArtifactoryListElementWithUser {

    @JsonProperty("user")
    private ArtifactoryUser user;

    @JsonProperty("listElement")
    private ArtifactoryListElement listElement;

    public ArtifactoryListElementWithUser() {
    }

    public ArtifactoryUser getUser() {
        return user;
    }

    public void setUser(ArtifactoryUser user) {
        this.user = user;
    }

    public ArtifactoryListElement getListElement() {
        return listElement;
    }

    public void setListElement(ArtifactoryListElement listElement) {
        this.listElement = listElement;
    }
}
