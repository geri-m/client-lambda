package at.madlmayr.slack;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SlackMember implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("team_id")
    private String teamId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("color")
    private String color;

    @JsonProperty("real_name")
    private String realName;

    @JsonProperty("tz")
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

    @JsonProperty("updated")
    private long updated;

    @JsonProperty("profile")
    private SlackProfile profile;

    public SlackMember() {
    }


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
}
