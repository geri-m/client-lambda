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

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public SlackMember[] getMembers() {
        return members;
    }

    public void setMembers(SlackMember[] members) {
        this.members = members;
    }

    public int getCache_ts() {
        return cache_ts;
    }

    public void setCache_ts(int cache_ts) {
        this.cache_ts = cache_ts;
    }
}
