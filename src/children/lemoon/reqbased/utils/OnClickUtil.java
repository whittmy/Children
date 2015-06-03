package children.lemoon.reqbased.utils;

//ok
public class OnClickUtil {
	static long postTime;

	public static boolean isMostPost() {
		long l1 = System.currentTimeMillis();
		long l2 = l1 - postTime;
		if ((l2 > 0L) && (l2 < 500L)) {
			return true;
		}
		postTime = l1;
		return false;
	}
}
