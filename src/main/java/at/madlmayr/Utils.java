package at.madlmayr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Utils {

    private final static String pattern = "yyyy-MM-dd_HH:mm:ss.SSS";
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    static {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
    }

    public static String standardTimeFormat(final long time) {
        return simpleDateFormat.format(time);
    }

    public static long parseStandardTime(final String timestamp) {
        try {
            return simpleDateFormat.parse(timestamp).getTime();
        } catch (ParseException e) {
            throw new ToolCallException(e);
        }
    }
}
