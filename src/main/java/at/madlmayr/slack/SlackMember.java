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

}
