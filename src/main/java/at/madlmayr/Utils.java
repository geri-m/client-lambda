package at.madlmayr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Utils {

    private final static String PATTERN = "yyyy-MM-dd_HH:mm:ss.SSS";
    private final static String TIME_ZONE = "Europe/Vienna";

    public static String standardTimeFormat(final long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return simpleDateFormat.format(time);
    }

    public static long parseStandardTime(final String timestamp) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
            return simpleDateFormat.parse(timestamp).getTime();
        } catch (ParseException e) {
            throw new ToolCallException(e);
        }
    }
}
