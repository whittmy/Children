package children.lemoon.player.duplay.utils;


import children.lemoon.utils.Logger;
import android.os.SystemClock;
import android.view.MotionEvent;

public class VideoGestureDetector {
	private static final long DOUBLE_FINGERS_TAP_TIME_OUT = 200L;
	private static final int GESTURE_DEGREE = 30;
	private static final int IDEL_STATE = 0;
	public static final int INFO_SHOW_CONTROL_STATE_START = 11;
	private static final float MIN_DISTANCE = 5.0F;
	public static final int PLAY_CONTROL_STATE_ONGOING = 9;
	public static final int PLAY_CONTROL_STATE_START = 8;
	public static final int PLAY_CONTROL_STATE_STOP = 10;
	private static final int PREPARE_STATE = 1;
	public static final int SEEK_CONTROL_STATE_ONGOING = 6;
	public static final int SEEK_CONTROL_STATE_START = 5;
	public static final int SEEK_CONTROL_STATE_STOP = 7;
	private static final String TAG = "VideoGestureDetector";
	public static final int VERTICAL_CONTROL_STATE_ONGOING = 3;
	public static final int VERTICAL_CONTROL_STATE_START = 2;
	public static final int VERTICAL_CONTROL_STATE_STOP = 4;
	private VideoGestureDetectListenner mListenner;
	private long mPreTime;
	private MotionEvent mStartEvent;
	private int mState = 0;

	public VideoGestureDetector(VideoGestureDetectListenner paramVideoGestureDetectListenner) {
		this.mListenner = paramVideoGestureDetectListenner;
	}

	private boolean isGreaterDistance(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat) {
		float f1 = paramMotionEvent2.getX() - paramMotionEvent1.getX();
		float f2 = paramMotionEvent2.getY() - paramMotionEvent1.getY();
		return f1 * f1 + f2 * f2 > paramFloat * paramFloat;
	}

	private void transformState(MotionEvent paramMotionEvent) {
		int action = paramMotionEvent.getAction();
		Logger.LOGE("transformState: action=" + action + ", mstate=" + mState);
		switch (this.mState) {
		case IDEL_STATE:// pswitch_1
			if ((0xFF & action) == MotionEvent.ACTION_DOWN) {
				this.mState = 1;
				this.mStartEvent = MotionEvent.obtainNoHistory(paramMotionEvent);
				this.mPreTime = SystemClock.elapsedRealtime();
			}
			return;
		case PREPARE_STATE:// pswitch_2
			if ((0xFF & action) == 5) {
				if (SystemClock.elapsedRealtime() - this.mPreTime < DOUBLE_FINGERS_TAP_TIME_OUT) {
					this.mState = 8;
					if (this.mListenner != null)
						this.mListenner.onStart(8, this.mStartEvent);

					this.mState = 9;
					if (this.mListenner != null)
						this.mListenner.onGoing(9, this.mStartEvent, paramMotionEvent);
				}
			} else if ((0xFF & action) == MotionEvent.ACTION_UP) {
				this.mState = 11;
				if (this.mListenner != null)
					this.mListenner.onStart(11, this.mStartEvent);

				this.mState = 0;
				return;
			} else if (((0xFF & action) == MotionEvent.ACTION_MOVE) && (isGreaterDistance(this.mStartEvent, paramMotionEvent, MIN_DISTANCE))) {
				if (GestureUtils.isHorizental(this.mStartEvent, paramMotionEvent, GESTURE_DEGREE)) {
					this.mState = 5;
					if (this.mListenner != null) {
						this.mListenner.onStart(5, this.mStartEvent);
					}
					this.mState = 6;
					if (this.mListenner != null)
						this.mListenner.onGoing(6, this.mStartEvent, paramMotionEvent);

				} else if (GestureUtils.isVertical(this.mStartEvent, paramMotionEvent, GESTURE_DEGREE)) {
					this.mState = 2;
					if (this.mListenner != null) {
						this.mListenner.onStart(2, this.mStartEvent);
					}
					this.mState = 3;
					if (this.mListenner != null) {
						this.mListenner.onGoing(3, this.mStartEvent, paramMotionEvent);
					}
				}
			}

			return;

		case VERTICAL_CONTROL_STATE_ONGOING:// pswitch_3
			if ((0xFF & action) == MotionEvent.ACTION_MOVE) {
				this.mState = 3;
				if (this.mListenner != null)
					this.mListenner.onGoing(3, this.mStartEvent, paramMotionEvent);

			} else if ((0xFF & action) == MotionEvent.ACTION_UP) {
				this.mState = 4;
				if (this.mListenner != null)
					this.mListenner.onStop(4, this.mStartEvent, paramMotionEvent);

				this.mState = 0;
			}
			return;
		case SEEK_CONTROL_STATE_ONGOING:// pswitch_4
			if ((0xFF & action) == MotionEvent.ACTION_MOVE) {
				this.mState = 6;
				if (this.mListenner != null)
					this.mListenner.onGoing(6, this.mStartEvent, paramMotionEvent);

			} else if ((0xFF & action) == MotionEvent.ACTION_UP) {
				this.mState = 7;
				if (this.mListenner != null)
					this.mListenner.onStop(7, this.mStartEvent, paramMotionEvent);
				this.mState = 0;
			}
			return;
		case PLAY_CONTROL_STATE_ONGOING:// pswitch_5
			if ((0xFF & action) == MotionEvent.ACTION_MOVE) {
				this.mState = 9;
				if (this.mListenner != null)
					this.mListenner.onGoing(9, this.mStartEvent, paramMotionEvent);
			} else if ((0xFF & action) == MotionEvent.ACTION_UP) {
				this.mState = 10;
				if (this.mListenner != null)
					this.mListenner.onStop(10, this.mStartEvent, paramMotionEvent);
				this.mState = 0;
			}
			return;

		case 2:// pswitch_0
		case 4:// pswitch_0
		case 5:// pswitch_0
		case 7:// pswitch_0
		case 8:// pswitch_0
			return;
		}

	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		transformState(paramMotionEvent);
		return true;
	}

	public static abstract interface VideoGestureDetectListenner {
		public abstract void onGoing(int paramInt, MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2);

		public abstract void onStart(int paramInt, MotionEvent paramMotionEvent);

		public abstract void onStop(int paramInt, MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2);
	}
}
