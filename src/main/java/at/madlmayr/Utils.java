package at.madlmayr;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class Utils {

    // DynamoDb wants ISO/UTC: https://docs.aws.amazon.com/de_de/amazondynamodb/latest/developerguide/DynamoDBMapper.DataTypes.html
    // https://en.wikipedia.org/wiki/ISO_8601

    public static String standardTimeFormat(final long time) {
        DateTime jodaTime = new DateTime(time, DateTimeZone.UTC);
        DateTimeFormatter parser1 = ISODateTimeFormat.dateTime();
        return parser1.print(jodaTime);
    }

    public static long parseStandardTime(final String timestamp) {
        DateTimeFormatter parser1 = ISODateTimeFormat.dateTime();
        return parser1.parseDateTime(timestamp).getMillis();
    }
}
