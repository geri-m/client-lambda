package at.madlmayr;

public class ToolCallException extends RuntimeException {

    public ToolCallException(final Exception e){
        super(e);
    }

    public ToolCallException(final String e){
        super(e);
    }
}
