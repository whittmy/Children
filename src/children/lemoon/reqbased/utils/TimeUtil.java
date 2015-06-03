package children.lemoon.reqbased.utils;

//ok
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	public static String converTime(long paramLong) {
		long l = (System.currentTimeMillis() - paramLong) / 1000L;
		if (l <= 86400L) {
			return "今天";
		}
		return l / 86400L + "天前";
	}

	public static String converTimeName(String paramString) {
		try {
			SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date localDate1 = localSimpleDateFormat.parse(paramString); // v2
																		// date
			// goto0
			long l1 = localDate1.getTime(); // v8 timestamp
			long l2 = (System.currentTimeMillis() - l1) / 1000L; // v5 timeGap
			if (l2 > 86400L) {
				// goto_1
				return l2 / 86400L + "天前";
			}

			// cond0
			if (l2 > 3600L) {
				return "刚刚";
			}

			// cond1
			if (l2 > 60L) {
				return l2 / 60L + "分钟前";
			}

			// cond2
			return l2 / 3600L + "小时前";

		} catch (ParseException localParseException) {
			localParseException.printStackTrace();
		}

		return "";
	}

	// ver3
	public static String getTimeStamp() {
		Long tsLong = Long.valueOf(System.currentTimeMillis() / 0x3e8);
		String ts = tsLong.toString();
		return ts;
	}

}
