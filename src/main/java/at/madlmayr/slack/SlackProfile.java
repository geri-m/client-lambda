package at.madlmayr.slack;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.net.URL;

public class SlackProfile implements Serializable {

    @JsonProperty("title")
    private String title;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("skype")
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

}
