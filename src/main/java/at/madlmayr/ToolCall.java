package at.madlmayr;

import org.json.JSONArray;

public interface ToolCall {

    JSONArray processCall(final String url, final String bearer);
}
