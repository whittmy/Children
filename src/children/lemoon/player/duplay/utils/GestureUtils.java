package children.lemoon.player.duplay.utils;

import android.view.MotionEvent;
import java.util.HashMap;

public final class GestureUtils {
	private static final int DEGREE_15 = 15;
	private static final int DEGREE_30 = 30;
	private static final int DEGREE_60 = 60;
	private static final float TAN_15 = 0.26794F;
	private static final float TAN_30 = 0.5735F;
	private static final float TAN_60 = 1.73205F;
	private static HashMap<Integer, Float> sTanHashMap;

	static {
		if (sTanHashMap == null) {
			sTanHashMap = new HashMap<Integer, Float>();
			sTanHashMap.put(Integer.valueOf(DEGREE_15), Float.valueOf(TAN_15));
			sTanHashMap.put(Integer.valueOf(DEGREE_30), Float.valueOf(TAN_30));
			sTanHashMap.put(Integer.valueOf(DEGREE_60), Float.valueOf(TAN_60));
		}
	}

	public static boolean isHorizental(MotionEvent me1, MotionEvent me2, int paramInt) {
		float f;
		if (sTanHashMap.containsKey(Integer.valueOf(paramInt))) {
			f = ((Float) sTanHashMap.get(Integer.valueOf(paramInt))).floatValue();
		} else {
			f = (float) Math.tan(paramInt);
		}

		if (Math.abs(me2.getY() - me1.getY()) <= f * Math.abs(me2.getX() - me1.getX())) {
			return true;
		}
		return false;
	}

	public static boolean isVertical(MotionEvent me1, MotionEvent me2, int paramInt) {
		float f1;
		if (sTanHashMap.containsKey(Integer.valueOf(paramInt))) {
			f1 = ((Float) sTanHashMap.get(Integer.valueOf(paramInt))).floatValue();
		} else {
			f1 = (float) Math.tan(paramInt);
		}

		float f2 = Math.abs(me2.getY() - me1.getY());
		if (Math.abs(me2.getX() - me1.getX()) > f1 * f2) {
			return false;
		}
		return true;

	}
}
