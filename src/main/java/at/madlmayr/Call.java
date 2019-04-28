package at.madlmayr;

import java.util.List;

public interface Call<T extends Account> {

    void writeStuffToDatabase(final List<T> userList, final ToolCallConfig toolCallRequest);

}
