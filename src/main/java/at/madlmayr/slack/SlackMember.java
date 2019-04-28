package at.madlmayr.slack;

import at.madlmayr.Account;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.net.URL;

@DynamoDBTable(tableName = Account.TABLE_NAME)
public class SlackMember implements Serializable, Account {

    private String companyToolTimestamp;

    public SlackMember() {

    }

    @DynamoDBHashKey(attributeName = COLUMN_COMPANY_TOOL)
    @Override
    public String getCompanyToolTimestamp() {
        return companyToolTimestamp;
    }

    private String id;

    @JsonProperty("team_id")
    private String teamId;

    private String name;

    private boolean deleted;

    private String color;

    @JsonProperty("real_name")
    private String realName;

    private String tz;

    @JsonProperty("tz_label")
    private String tzLabel;

    @JsonProperty("tz_offset")
    private int tzOffset;

    @JsonProperty("is_admin")
    private boolean admin;

    @JsonProperty("is_owner")
    private boolean owner;

    @JsonProperty("is_primary_owner")
    private boolean primaryOwner;

    @JsonProperty("is_restricted")
    private boolean restricted;

    @JsonProperty("is_ultra_restricted")
    private boolean ultraRestricted;

    @JsonProperty("is_bot")
    private boolean bot;

    @JsonProperty("is_app_user")
    private boolean appUser;

    private long updated;

    private SlackProfile profile;

    @Override
    public void setCompanyToolTimestamp(String companyToolTimestamp) {
        this.companyToolTimestamp = companyToolTimestamp;
    }

    @DynamoDBRangeKey(attributeName = COLUMN_ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getTz() {
        return tz;
    }

    public void setTz(String tz) {
        this.tz = tz;
    }

    public String getTzLabel() {
        return tzLabel;
    }

    public void setTzLabel(String tzLabel) {
        this.tzLabel = tzLabel;
    }

    public int getTzOffset() {
        return tzOffset;
    }

    public void setTzOffset(int tzOffset) {
        this.tzOffset = tzOffset;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isPrimaryOwner() {
        return primaryOwner;
    }

    public void setPrimaryOwner(boolean primaryOwner) {
        this.primaryOwner = primaryOwner;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public boolean isUltraRestricted() {
        return ultraRestricted;
    }

    public void setUltraRestricted(boolean ultraRestricted) {
        this.ultraRestricted = ultraRestricted;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public boolean isAppUser() {
        return appUser;
    }

    public void setAppUser(boolean appUser) {
        this.appUser = appUser;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public SlackProfile getProfile() {
        return profile;
    }

    public void setProfile(SlackProfile profile) {
        this.profile = profile;
    }

    @Override
    public String getEmail() {
        return profile.getEmail();
    }

    public void setEmail(final String email) {
        profile.setEmail(email);
    }


    @DynamoDBDocument
    public static class SlackProfile implements Serializable {

        private String title;

        private String phone;

        private String skype;

        @JsonProperty("real_name")
        private String realName;

        @JsonProperty("real_name_normalized")
        private String realNameNormalized;

        @JsonProperty("display_name")
        private String displayName;

        @JsonProperty("display_name_normalized")
        private String displayNameNormalized;

        @JsonProperty("status_text")
        private String statusText;

        @JsonProperty("status_emoji")
        private String statusEmoji;

        @JsonProperty("status_expiration")
        private String statusExpiration;

        @JsonProperty("avatar_hash")
        private String avatarHash;

        @JsonProperty("bot_id")
        private String botIt;

        @JsonProperty("api_app_id")
        private String apiAppId;

        @JsonProperty("always_active")
        private boolean alwaysActive;

        @JsonProperty("guest_channels")
        private String guestChannels;

        @JsonProperty("guest_invited_by")
        private String guestInvitedBy;

        @JsonProperty("guest_expiration_ts")
        private int guestExpirationTs;

        @JsonProperty("image_original")
        private URL imageOriginal;

        @JsonProperty("email")
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("image_24")
        private URL image24;

        @JsonProperty("image_32")
        private URL image32;

        @JsonProperty("image_48")
        private URL image48;

        @JsonProperty("image_72")
        private URL image72;

        @JsonProperty("image_192")
        private URL image192;

        @JsonProperty("image_512")
        private URL image512;

        @JsonProperty("image_1024")
        private URL image1024;

        @JsonProperty("status_text_canonical")
        private String statusTextCanonical;

        @JsonProperty("team")
        private String team;

        @JsonProperty("is_custom_image")
        private boolean isCustomImage;

        @JsonProperty("fields")
        private String fields;

        public SlackProfile() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSkype() {
            return skype;
        }

        public void setSkype(String skype) {
            this.skype = skype;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getRealNameNormalized() {
            return realNameNormalized;
        }

        public void setRealNameNormalized(String realNameNormalized) {
            this.realNameNormalized = realNameNormalized;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayNameNormalized() {
            return displayNameNormalized;
        }

        public void setDisplayNameNormalized(String displayNameNormalized) {
            this.displayNameNormalized = displayNameNormalized;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public String getStatusEmoji() {
            return statusEmoji;
        }

        public void setStatusEmoji(String statusEmoji) {
            this.statusEmoji = statusEmoji;
        }

        public String getStatusExpiration() {
            return statusExpiration;
        }

        public void setStatusExpiration(String statusExpiration) {
            this.statusExpiration = statusExpiration;
        }

        public String getAvatarHash() {
            return avatarHash;
        }

        public void setAvatarHash(String avatarHash) {
            this.avatarHash = avatarHash;
        }

        public String getBotIt() {
            return botIt;
        }

        public void setBotIt(String botIt) {
            this.botIt = botIt;
        }

        public String getApiAppId() {
            return apiAppId;
        }

        public void setApiAppId(String apiAppId) {
            this.apiAppId = apiAppId;
        }

        public boolean isAlwaysActive() {
            return alwaysActive;
        }

        public void setAlwaysActive(boolean alwaysActive) {
            this.alwaysActive = alwaysActive;
        }

        public String getGuestChannels() {
            return guestChannels;
        }

        public void setGuestChannels(String guestChannels) {
            this.guestChannels = guestChannels;
        }

        public String getGuestInvitedBy() {
            return guestInvitedBy;
        }

        public void setGuestInvitedBy(String guestInvitedBy) {
            this.guestInvitedBy = guestInvitedBy;
        }

        public int getGuestExpirationTs() {
            return guestExpirationTs;
        }

        public void setGuestExpirationTs(int guestExpirationTs) {
            this.guestExpirationTs = guestExpirationTs;
        }

        public URL getImageOriginal() {
            return imageOriginal;
        }

        public void setImageOriginal(URL imageOriginal) {
            this.imageOriginal = imageOriginal;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public URL getImage24() {
            return image24;
        }

        public void setImage24(URL image24) {
            this.image24 = image24;
        }

        public URL getImage32() {
            return image32;
        }

        public void setImage32(URL image32) {
            this.image32 = image32;
        }

        public URL getImage48() {
            return image48;
        }

        public void setImage48(URL image48) {
            this.image48 = image48;
        }

        public URL getImage72() {
            return image72;
        }

        public void setImage72(URL image72) {
            this.image72 = image72;
        }

        public URL getImage192() {
            return image192;
        }

        public void setImage192(URL image192) {
            this.image192 = image192;
        }

        public URL getImage512() {
            return image512;
        }

        public void setImage512(URL image512) {
            this.image512 = image512;
        }

        public URL getImage1024() {
            return image1024;
        }

        public void setImage1024(URL image1024) {
            this.image1024 = image1024;
        }

        public String getStatusTextCanonical() {
            return statusTextCanonical;
        }

        public void setStatusTextCanonical(String statusTextCanonical) {
            this.statusTextCanonical = statusTextCanonical;
        }

        public String getTeam() {
            return team;
        }

        public void setTeam(String team) {
            this.team = team;
        }

        public boolean isCustomImage() {
            return isCustomImage;
        }

        public void setCustomImage(boolean customImage) {
            isCustomImage = customImage;
        }

        public String getFields() {
            return fields;
        }

        public void setFields(String fields) {
            this.fields = fields;
        }
    }

}
