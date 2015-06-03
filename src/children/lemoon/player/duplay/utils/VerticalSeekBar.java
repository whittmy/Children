package children.lemoon.player.duplay.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewParent;
import android.widget.AbsSeekBar;

public class VerticalSeekBar extends AbsSeekBar {
	private int height;
	private OnSeekBarChangeListener mOnSeekBarChangeListener;
	private Drawable mThumb;
	private int width;

	public VerticalSeekBar(Context cx) {
		this(cx, null);
	}

	public VerticalSeekBar(Context cx, AttributeSet attr) {
		this(cx, attr, 16842875);
	}

	public VerticalSeekBar(Context cx, AttributeSet attr, int paramInt) {
		super(cx, attr, paramInt);
	}

	private void attemptClaimDrag() {
		if (getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
	}

	private void setThumbPos(int parm1, Drawable paramDrawable, float paramFloat, int paramInt2) {
		int i = parm1 + getPaddingLeft() - getPaddingRight();
		int j = paramDrawable.getIntrinsicWidth();
		int k = paramDrawable.getIntrinsicHeight();
		int m = (int) (paramFloat * (i - j + 2 * getThumbOffset()));
		Rect localRect;
		int n;
		if (paramInt2 == -2147483648) {
			localRect = paramDrawable.getBounds();
			paramInt2 = localRect.top;
			n = localRect.bottom;
		} else {
			n = paramInt2 + k;
		}

		paramDrawable.setBounds(m, paramInt2, j + m, n);
		return;

	}

	private void trackTouchEvent(MotionEvent paramMotionEvent) {
		int i = getHeight();
		int j = i - getPaddingBottom() - getPaddingTop();
		int k = (int) paramMotionEvent.getY();
		float f;
		if (k > i - getPaddingBottom()) {
			f = 0.0F;
		} else if (k < getPaddingTop()) {
			f = 1.0F;
		} else {
			f = (i - getPaddingBottom() - k) / j;
		}

		setProgress((int) (f * getMax()));
		return;
	}

	public boolean dispatchKeyEvent(KeyEvent paramKeyEvent) {
		if (paramKeyEvent.getAction() == 0) {
			KeyEvent localKeyEvent;
			switch (paramKeyEvent.getKeyCode()) {
			case 0x13: // s0
				localKeyEvent = new KeyEvent(0, 22);
				break;

			case 0x14: // s1
				localKeyEvent = new KeyEvent(0, 21);
				break;

			case 0x15: // s2
				localKeyEvent = new KeyEvent(0, 20);
				break;
			case 0x16: // s3
				localKeyEvent = new KeyEvent(0, 19);
				break;

			default:
				localKeyEvent = new KeyEvent(0, paramKeyEvent.getKeyCode());
			}
			return localKeyEvent.dispatch(this);
		}
		return false;
	}

	protected void onDraw(Canvas paramCanvas) {
		paramCanvas.rotate(-90.0F);
		paramCanvas.translate(-this.height, 0.0F);
		super.onDraw(paramCanvas);
	}

	protected void onMeasure(int parm1, int paramInt2) {
		try {
			// synchronized (this) {
			this.height = View.MeasureSpec.getSize(paramInt2);
			this.width = View.MeasureSpec.getSize(parm1);
			setMeasuredDimension(this.width, this.height);
			return;
			// }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void onProgressRefresh(float paramFloat, boolean paramBoolean) {
		Drawable localDrawable = this.mThumb;
		if (localDrawable != null) {
			setThumbPos(getHeight(), localDrawable, paramFloat, -2147483648);
			invalidate();
		}
		if (this.mOnSeekBarChangeListener != null) {
			this.mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), paramBoolean);
		}
	}

	protected void onSizeChanged(int parm1, int paramInt2, int paramInt3, int paramInt4) {
		super.onSizeChanged(paramInt2, parm1, paramInt3, paramInt4);
	}

	void onStartTrackingTouch() {
		if (this.mOnSeekBarChangeListener != null) {
			this.mOnSeekBarChangeListener.onStartTrackingTouch(this);
		}
	}

	void onStopTrackingTouch() {
		if (this.mOnSeekBarChangeListener != null) {
			this.mOnSeekBarChangeListener.onStopTrackingTouch(this);
		}
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		if (!isEnabled()) {
			return false;
		}
		switch (paramMotionEvent.getAction()) {
		case 0: // s0
			setPressed(true);
			onStartTrackingTouch();
			trackTouchEvent(paramMotionEvent);
			break;
		case 1: // s2
			trackTouchEvent(paramMotionEvent);
			onStopTrackingTouch();
			setPressed(false);
			break;
		case 2: // s1
			trackTouchEvent(paramMotionEvent);
			attemptClaimDrag();
			break;
		case 3: // s3
			onStopTrackingTouch();
			setPressed(false);
			break;
		}

		return true;
	}

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener paramOnSeekBarChangeListener) {
		this.mOnSeekBarChangeListener = paramOnSeekBarChangeListener;
	}

	public void setThumb(Drawable paramDrawable) {
		this.mThumb = paramDrawable;
		super.setThumb(paramDrawable);
	}

	public static abstract interface OnSeekBarChangeListener {
		public abstract void onProgressChanged(VerticalSeekBar paramVerticalSeekBar, int paramInt, boolean paramBoolean);

		public abstract void onStartTrackingTouch(VerticalSeekBar paramVerticalSeekBar);

		public abstract void onStopTrackingTouch(VerticalSeekBar paramVerticalSeekBar);
	}
}