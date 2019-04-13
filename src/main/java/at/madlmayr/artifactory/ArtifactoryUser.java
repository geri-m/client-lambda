package at.madlmayr.artifactory;

import at.madlmayr.Account;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@DynamoDBTable(tableName = Account.TABLE_NAME)
public class ArtifactoryUser implements Serializable {

    private String companyToolTimestamp;

    @DynamoDBHashKey(attributeName = Account.COLUMN_COMPANY_TOOL)
    public String getCompanyToolTimestamp() {
        return companyToolTimestamp;
    }

    public void setCompanyToolTimestamp(String companyToolTimestamp) {
        this.companyToolTimestamp = companyToolTimestamp;
    }

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


    @DynamoDBRangeKey(attributeName = Account.COLUMN_ID)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isProfileUpdatable() {
        return profileUpdatable;
    }

    public void setProfileUpdatable(boolean profileUpdatable) {
        this.profileUpdatable = profileUpdatable;
    }

    public boolean isInternalPasswordDisabled() {
        return internalPasswordDisabled;
    }

    public void setInternalPasswordDisabled(boolean internalPasswordDisabled) {
        this.internalPasswordDisabled = internalPasswordDisabled;
    }

    // we cant't write arrays to DynamoDB without having a dedicated datatype for them.
    @DynamoDBIgnore
    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public String getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(String lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public long getLastLoggedInMillis() {
        return lastLoggedInMillis;
    }

    public void setLastLoggedInMillis(long lastLoggedInMillis) {
        this.lastLoggedInMillis = lastLoggedInMillis;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean isDisableUIAccess() {
        return disableUIAccess;
    }

    public void setDisableUIAccess(boolean disableUIAccess) {
        this.disableUIAccess = disableUIAccess;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
