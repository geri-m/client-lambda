package at.madlmayr.artifactory;

import at.madlmayr.Account;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.io.Serializable;

@DynamoDBTable(tableName = Account.TABLE_NAME)
public class ArtifactoryUser implements Serializable, Account {

    private String companyToolTimestamp;

    @DynamoDBHashKey(attributeName = COLUMN_COMPANY_TOOL)
    @Override
    public String getCompanyToolTimestamp() {
        return companyToolTimestamp;
    }

    @Override
    public void setCompanyToolTimestamp(String companyToolTimestamp) {
        this.companyToolTimestamp = companyToolTimestamp;
    }

    private String name;

    private String email;

    private boolean admin;

    private boolean profileUpdatable;

    private boolean internalPasswordDisabled;

    private String[] groups;

    private String lastLoggedIn;

    private long lastLoggedInMillis;

    private boolean offlineMode;

    private boolean disableUIAccess;

    private String realm;

    public ArtifactoryUser() {
    }


    @DynamoDBRangeKey(attributeName = COLUMN_ID)
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
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
