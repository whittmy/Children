package children.lemoon.ui;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class MyHorizontalScrollView extends HorizontalScrollView implements
		OnClickListener 
{

	/**
	 * 图片滚动时的回调接口
	 * 
	 * @author zhy
	 * 
	 */
	public interface CurrentImageChangeListener
	{
		void onCurrentImgChanged(int position, View viewIndicator);
	}
	
	
	public interface TouchListener{
		void onHadToched();
	}
	
	

	/**
	 * 条目点击时的回调
	 * 
	 * @author zhy
	 * 
	 */
	public interface OnItemClickListener
	{
		void onClick(View view, int pos);
	}

	private CurrentImageChangeListener mListener;

	private OnItemClickListener mOnClickListener;

	
	private TouchListener mTouchListener;
	
	private static final String TAG = "MyHorizontalScrollView";

	/**
	 * HorizontalListView中的LinearLayout
	 */
	private LinearLayout mContainer;

	/**
	 * 子元素的宽度
	 */
	private int mChildWidth;
	/**
	 * 子元素的高度
	 */
	private int mChildHeight;
	/**
	 * 当前最后一张图片的index， 一方面记录到哪儿了，另一方面用于定位mAdapter数据
	 */
	private int mCurrentIndex;
	/**
	 * 当前第一张图片的下标, 一方面记录前面有多少个元素，另一方面用于定位mAdapter数据
	 */
	private int mFristIndex;
	/**
	 * 当前第一个View
	 */
	private View mFirstView;
	/**
	 * 数据适配器
	 */
	private HorizontalScrollViewAdapter mAdapter;
	/**
	 * 每屏幕最多显示的个数
	 */
	private int mCountOneScreen;
	/**
	 * 屏幕的宽度
	 */
	private int mScreenWitdh;

	//是否首次初始化
	private boolean bfirst=true;
	
	
	private int mClickPos;
	private View mOldClickView;
	
	/**
	 * 保存View与位置的键值对
	 */
	private Map<View, Integer> mViewPos = new HashMap<View, Integer>();

	public MyHorizontalScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// 获得屏幕宽度
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWitdh = outMetrics.widthPixels;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mContainer = (LinearLayout) getChildAt(0);
	}

	
	public void loadIdx(int pos){
		if(pos<0 || pos>=(mAdapter.getCount()-1)){
			return;
		}
		
		//这儿还没有考虑到页面的获取哦的判断哦
		
		mContainer = (LinearLayout) getChildAt(0);
		mContainer.removeAllViews();
		mViewPos.clear();

		//设定一屏最后一个即代表该pos的
		mCurrentIndex = pos;
		mClickPos = pos;
		
		
		int b = mCurrentIndex - mCountOneScreen;
		if(b >= 0){
			mFristIndex = b;
			for (; b<mCurrentIndex; b++) {
				View view = mAdapter.getView(b, null, mContainer);
				view.setOnClickListener(this);
				mContainer.addView(view);
				mViewPos.put(view, b);
				
				if(mClickPos == b)
					mOldClickView = view;
			}
			//如果设置了滚动监听则触发
			if (mListener != null) {
				notifyCurrentImgChanged();
			}
		}
		else{
			mFristIndex = 0;
			initFirstScreenChildren(mCountOneScreen);
		}
	}
	
	
	
	/**
	 * 加载下一张图片
	 */
	protected void loadNextImg()
	{
		// 数组边界值计算
		if (mCurrentIndex == mAdapter.getCount() - 1)
		{
			return;
		}
		//移除第一张图片，且将水平滚动位置置0
		scrollTo(0, 0);
		mViewPos.remove(mContainer.getChildAt(0));
		mContainer.removeViewAt(0);
		
		//获取下一张图片，并且设置onclick事件，且加入容器中
		View view = mAdapter.getView(++mCurrentIndex, null, mContainer);
		view.setOnClickListener(this);
		mContainer.addView(view);
		mViewPos.put(view, mCurrentIndex);
		
 		//当前第一张图片小标
		mFristIndex++;
		//如果设置了滚动监听则触发
		if (mListener != null)
		{
			notifyCurrentImgChanged();
		}

	}
	/**
	 * 加载前一张图片
	 */
	protected void loadPreImg()
	{
		//如果当前已经是第一张，则返回
		if (mFristIndex == 0)
			return;
		//获得当前应该显示为第一张图片的下标
		int index = mCurrentIndex - mCountOneScreen;
		if (index >= 0)
		{
//			mContainer = (LinearLayout) getChildAt(0);
			//移除最后一张
			int oldViewPos = mContainer.getChildCount() - 1;
			mViewPos.remove(mContainer.getChildAt(oldViewPos));
			mContainer.removeViewAt(oldViewPos);
			
			//将此View放入第一个位置
			View view = mAdapter.getView(index, null, mContainer);
			mViewPos.put(view, index);
			mContainer.addView(view, 0);
			view.setOnClickListener(this);
			//水平滚动位置向左移动view的宽度个像素
			scrollTo(mChildWidth, 0);
			//当前位置--，当前第一个显示的下标--
			mCurrentIndex--;
			mFristIndex--;
			
			//rocking
			if(mFristIndex < 0)
				mFristIndex = 0;
			//回调
			if (mListener != null)
			{
				notifyCurrentImgChanged();

			}
		}
	}

	/**
	 * 滑动时的回调
	 */
	public void notifyCurrentImgChanged()
	{
		//先清除所有的背景色，点击时会设置为蓝色
//rocking		for (int i = 0; i < mContainer.getChildCount(); i++)
//		{
//			mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
//		}
		
		mListener.onCurrentImgChanged(mFristIndex, mContainer.getChildAt(0));

	}

	public int getClickPos(){
		return mClickPos;
	}
	
	public void setClickPos(int pos){
		mClickPos = pos;
	}
	
	public int getScrollPos(){
		return mFristIndex;
	}
	
	public int getItemCntConst(){
		return mCountOneScreen;
	}
	
	/**
	 * 初始化数据，设置数据适配器
	 * 
	 * @param mAdapter
	 */
	public void initDatas(HorizontalScrollViewAdapter mAdapter)
	{
		this.mAdapter = mAdapter;
		if(mAdapter ==null || mAdapter.getCount() == 0)
			return;
 
		// 强制计算当前View的宽和高
		if (mChildWidth == 0 && mChildHeight == 0) {
 			mContainer = (LinearLayout) getChildAt(0);
			// 获得适配器中第一个View
			final View view = mAdapter.getView(0, null, mContainer);
			mContainer.addView(view);
			
			int w = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			view.measure(w, h);
			mChildHeight = view.getMeasuredHeight();
			mChildWidth = view.getMeasuredWidth();
			Log.e(TAG, view.getMeasuredWidth() + "," + view.getMeasuredHeight());
			mChildHeight = view.getMeasuredHeight();
			// 计算每次加载多少个View
			mCountOneScreen = (mScreenWitdh / mChildWidth == 0)?mScreenWitdh / mChildWidth+1:mScreenWitdh / mChildWidth+2;
		}
		

		//bug fix 如果view个数，没有屏幕能够显示的个数多，那么就用view的个数，为显示的个数
		if(mCountOneScreen>mAdapter.getCount()){
			mCountOneScreen=mAdapter.getCount();
		}
		
		Log.e(TAG, "mCountOneScreen = " + mCountOneScreen
				+ " ,mChildWidth = " + mChildWidth);
		
		
		
		//初始化第一屏幕的元素
		if(bfirst){
			initFirstScreenChildren(mCountOneScreen);
			bfirst = false;
		}
		
	}

	/**
	 * 加载第一屏的View
	 * 
	 * @param mCountOneScreen
	 */
	public void initFirstScreenChildren(int mCountOneScreen)
	{
		mContainer = (LinearLayout) getChildAt(0);
		mContainer.removeAllViews();
		mViewPos.clear();

		for (int i = 0; i < mCountOneScreen; i++)
		{
			View view = mAdapter.getView(i, null, mContainer);
			view.setOnClickListener(this);
			mContainer.addView(view);
			mViewPos.put(view, i);
			mCurrentIndex = i;
			
			//初始化view时，顺便记录下当前获得焦点的view, 前提是初始化之前先设置好 clickpos，其默认为0
			if(mClickPos == i)
				mOldClickView = view;
		}

		if (mListener != null)
		{
			notifyCurrentImgChanged();
		}

	}
 
	//save mClickPos
	public void switchClick(View oldClickView, int newClickpos){
		if(newClickpos < 0)
			newClickpos = 0;
		
		if(oldClickView == null){
			oldClickView = mContainer.getChildAt(0);
		}
		
		mClickPos = newClickpos;
		
		// handle old, 有可能不存在了(因滑动被删除了)，为避免因不存在却还要插入的情况，需要先判断。
		// 也所以，oldClickPos不可以传数字编号，必须是view方可判断其是否存在
		Integer oldClickPos = mViewPos.get(oldClickView);
		if(oldClickPos != null){
			int c_idx = oldClickPos - mFristIndex;
			Log.e("", String.format("===========handle_old:  oldpos=%d, oldridx=%d", oldClickPos, c_idx));
			mViewPos.remove(/*mContainer.getChildAt(c_idx)*/oldClickView);
			mContainer.removeViewAt(c_idx);
			//获取下一张图片，并且设置onclick事件，且加入容器中
			View view = mAdapter.getView(oldClickPos, null, mContainer);
			view.setOnClickListener(this);
			mContainer.addView(view, c_idx);
			mViewPos.put(view, oldClickPos);	
		}
		
		//handle new
		int c_idx = newClickpos-mFristIndex;
		Log.e("", String.format("===========handle_new: newpos=%d,  newridx=%d", newClickpos,  c_idx));
		mViewPos.remove(mContainer.getChildAt(c_idx));
		mContainer.removeViewAt(c_idx);
		//获取下一张图片，并且设置onclick事件，且加入容器中
		View view = mAdapter.getView(newClickpos, null, mContainer);
		view.setOnClickListener(this);
		mContainer.addView(view, c_idx);
		mViewPos.put(view, newClickpos);
		
		mOldClickView = view;
	}
	
	public void prev(){
		switchClick(mOldClickView, --mClickPos);
		loadPreImg();
	}
	public void next(){
		switchClick(mOldClickView, ++mClickPos);
		loadNextImg();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		switch (ev.getAction())
		{
		case MotionEvent.ACTION_MOVE:
			//Log.e(TAG, getScrollX() + "");

			int scrollX = getScrollX();
			// 如果当前scrollX为view的宽度，加载下一张，移除第一张
			if (scrollX >= mChildWidth)
			{
				loadNextImg();
			}
			// 如果当前scrollX = 0， 往前设置一张，移除最后一张
			else if (scrollX == 0)
			{
				loadPreImg();
			}
			
			if(mTouchListener!=null)
				mTouchListener.onHadToched();
			break;

		}
		return super.onTouchEvent(ev);
	}
 
	@Override
	public void onClick(View v)
	{
		if (mOnClickListener != null)
		{
//	rocking		for (int i = 0; i < mContainer.getChildCount(); i++)
//			{
//				mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
//			}
			
			//注意调用顺序
			switchClick(mOldClickView, mViewPos.get(v));
			//mOldClickView = v;	//保存这个v是不对的，因为其在switchclick函数中被改变了，所以要在switchclick函数中去保存新的view
			mOnClickListener.onClick(v, mClickPos); //这里返回的位置信息是相对于完整的数据的,而非界面上的位置，因为界面的item最多只显示一整屏幕而已。
													//但是我们要保存的点击也应该相对于 完整的数据，否则会乱掉。
		}
	}

	public void setOnItemClickListener(OnItemClickListener mOnClickListener)
	{
		this.mOnClickListener = mOnClickListener;
	}

	public void setCurrentImageChangeListener(
			CurrentImageChangeListener mListener)
	{
		this.mListener = mListener;
	}
	
	
	
	public void setTouchListener(TouchListener l){
		this.mTouchListener = l;
	}

}
