package at.madlmayr.slack;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SlackResponse implements Serializable {

    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("members")
    private SlackMember[] members;

    @JsonProperty("cache_ts")
    private int cache_ts;

    public SlackResponse() {

    }


}
