package children.lemoon.categrid;
//ok
 

import logger.lemoon.Logger;
import children.lemoon.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class Nouse_PullToRefreshView
  extends LinearLayout
{
  final String TAG = "PullToRefreshView";
  private static final int PULL_DOWN_STATE = 1;
  private static final int PULL_TO_REFRESH = 2;
  private static final int PULL_UP_STATE = 0;
  private static final int REFRESHING = 4;
  private static final int RELEASE_TO_REFRESH = 3;
  private AdapterView<?> mAdapterView;
  private RotateAnimation mFlipAnimation;
  private ImageView mFooterImageView;
  private ProgressBar mFooterProgressBar;
  private int mFooterState;
  private TextView mFooterTextView;
  private View mFooterView;
  private int mFooterViewHeight;
  private boolean mHaveMoreRecord = false;
  private ImageView mHeaderImageView;
  private ProgressBar mHeaderProgressBar;
  private int mHeaderState;
  private TextView mHeaderTextView;
  private TextView mHeaderUpdateTextView;
  private View mHeaderView;
  private int mHeaderViewHeight;
  private LayoutInflater mInflater;
  private int mLastMotionY;
  private OnFooterRefreshListener mOnFooterRefreshListener;
  private OnHeaderRefreshListener mOnHeaderRefreshListener;
  private int mPullState;
  private RotateAnimation mReverseFlipAnimation;
  private ScrollView mScrollView;
  
  public Nouse_PullToRefreshView(Context paramContext)
  {
    super(paramContext);
    init();
  }
  
  public Nouse_PullToRefreshView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void addFooterView()
  {
    this.mFooterView = this.mInflater.inflate(R.layout.refresh_footer, this, false);
    this.mFooterImageView = ((ImageView)this.mFooterView.findViewById(R.id.pull_to_load_image));
    this.mFooterTextView = ((TextView)this.mFooterView.findViewById(R.id.pull_to_load_text));
    this.mFooterProgressBar = ((ProgressBar)this.mFooterView.findViewById(R.id.pull_to_load_progress));
    measureView(this.mFooterView);
    this.mFooterViewHeight = this.mFooterView.getMeasuredHeight();
    LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(-1, this.mFooterViewHeight);
    addView(this.mFooterView, localLayoutParams);
  }
  
  private void addHeaderView()
  {
    this.mHeaderView = this.mInflater.inflate(R.layout.refresh_header, this, false);
    this.mHeaderImageView = ((ImageView)this.mHeaderView.findViewById(R.id.pull_to_refresh_image));
    this.mHeaderTextView = ((TextView)this.mHeaderView.findViewById(R.id.pull_to_refresh_text));
    this.mHeaderUpdateTextView = ((TextView)this.mHeaderView.findViewById(R.id.pull_to_refresh_updated_at));
    this.mHeaderProgressBar = ((ProgressBar)this.mHeaderView.findViewById(R.id.pull_to_refresh_progress));
    measureView(this.mHeaderView);
    this.mHeaderViewHeight = this.mHeaderView.getMeasuredHeight();
    LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(-1, this.mHeaderViewHeight);
    localLayoutParams.topMargin = (-this.mHeaderViewHeight);
    addView(this.mHeaderView, localLayoutParams);
  }
  
  private int changingHeaderViewTopMargin(int paramInt)
  {
    LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams)this.mHeaderView.getLayoutParams();
    float f = localLayoutParams.topMargin + 0.3F * paramInt;
    if ((paramInt > 0) && (this.mPullState == 0) && (Math.abs(localLayoutParams.topMargin) <= this.mHeaderViewHeight)) {
      return localLayoutParams.topMargin;
    }
    if ((paramInt < 0) && (this.mPullState == 1) && (Math.abs(localLayoutParams.topMargin) >= this.mHeaderViewHeight)) {
      return localLayoutParams.topMargin;
    }
    localLayoutParams.topMargin = ((int)f);
    this.mHeaderView.setLayoutParams(localLayoutParams);
    invalidate();
    return localLayoutParams.topMargin;
  }
  
  private void footerPrepareToRefresh(int paramInt)
  {
    int i = changingHeaderViewTopMargin(paramInt);
    if ((Math.abs(i) >= this.mHeaderViewHeight + this.mFooterViewHeight) && (this.mFooterState != 3)) {
      if (this.mHaveMoreRecord)
      {
        this.mFooterImageView.clearAnimation();
        this.mFooterImageView.startAnimation(this.mFlipAnimation);
        this.mFooterTextView.setText(R.string.pull_to_refresh_footer_release_label);
      }
      else{
    	  //cond1
    	  this.mFooterTextView.setText(R.string.pull_to_refresh_footer_no_more_label);
      }
      //goto0
      this.mFooterState = 3;
      //cond0/goto1
      return;
    }

    //cond2
    if (Math.abs(i) >= this.mHeaderViewHeight + this.mFooterViewHeight) {
        return;
    }
    									
    if (this.mHaveMoreRecord)
    {
      this.mFooterImageView.clearAnimation();
      this.mFooterImageView.startAnimation(this.mFlipAnimation);
      this.mFooterTextView.setText(R.string.pull_to_refresh_footer_pull_label);
    }
    else{
    //cond3
      this.mFooterTextView.setText(R.string.pull_to_refresh_footer_no_more_label);
    }
    
    //goto2
    this.mFooterState = 2;
    return;
  }
  
  private void footerRefreshing()
  {
    this.mFooterState = 4;
    setHeaderTopMargin(-(this.mHeaderViewHeight + this.mFooterViewHeight));
    this.mFooterImageView.setVisibility(8);
    this.mFooterImageView.clearAnimation();
    this.mFooterImageView.setImageDrawable(null);
    this.mFooterTextView.setText(R.string.pull_to_refresh_footer_refreshing_label);
    if (this.mOnFooterRefreshListener != null) {
      this.mOnFooterRefreshListener.onFooterRefresh(this);
    }
  }
  
  private int getHeaderTopMargin()
  {
    return ((LinearLayout.LayoutParams)this.mHeaderView.getLayoutParams()).topMargin;
  }
  
  private void headerPrepareToRefresh(int paramInt)
  {
    int i = changingHeaderViewTopMargin(paramInt);
    if ((i >= 0) && (this.mHeaderState != 3))
    {
      this.mHeaderTextView.setText(R.string.pull_to_refresh_release_label);
      this.mHeaderUpdateTextView.setVisibility(0);
      this.mHeaderImageView.clearAnimation();
      this.mHeaderImageView.startAnimation(this.mFlipAnimation);
      this.mHeaderState = 3;
      return;
    }
     
    //cond1
    if ((i >= 0) || (i <= -this.mHeaderViewHeight)) {
      return;
    }
    this.mHeaderImageView.clearAnimation();
    this.mHeaderImageView.startAnimation(this.mFlipAnimation);
    this.mHeaderTextView.setText(R.string.pull_to_refresh_pull_label);
    this.mHeaderState = 2;
  }
  
  private void headerRefreshing()
  {
    this.mHeaderState = 4;
    setHeaderTopMargin(0);
    this.mHeaderImageView.setVisibility(8);
    this.mHeaderImageView.clearAnimation();
    this.mHeaderImageView.setImageDrawable(null);
    this.mHeaderProgressBar.setVisibility(0);
    this.mHeaderTextView.setText(R.string.pull_to_refresh_refreshing_label);
    if (this.mOnHeaderRefreshListener != null) {
      this.mOnHeaderRefreshListener.onHeaderRefresh(this);
    }
  }
  
  private void init()
  {
    setOrientation(1);
    this.mFlipAnimation = new RotateAnimation(0.0F, -180.0F, 1, 0.5F, 1, 0.5F);
    this.mFlipAnimation.setInterpolator(new LinearInterpolator());
    this.mFlipAnimation.setDuration(250L);
    this.mFlipAnimation.setFillAfter(true);
    this.mReverseFlipAnimation = new RotateAnimation(-180.0F, 0.0F, 1, 0.5F, 1, 0.5F);
    this.mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
    this.mReverseFlipAnimation.setDuration(250L);
    this.mReverseFlipAnimation.setFillAfter(true);
    this.mInflater = LayoutInflater.from(getContext());
    addHeaderView();
  }
  
  private void initContentAdapterView()
  {
    int i = getChildCount();
    if (i < 3) {
      throw new IllegalArgumentException("This layout must contain 3 child views,and AdapterView or ScrollView must in the second position!");
    }
    
    //cond_0
    //goto0
    for (int j = 0;j < i - 1; j++)
    {
    	//cond_1
    	View localView = getChildAt(j);
    	if ((localView instanceof AdapterView)) {
          this.mAdapterView = ((AdapterView)localView);
        }
         
        //cond2
        if ((localView instanceof ScrollView)) {
          this.mScrollView = ((ScrollView)localView);
        }
        //go goto0
    }
    
    if ((this.mAdapterView == null) && (this.mScrollView == null)) {
       	throw new IllegalArgumentException("must contain a AdapterView or ScrollView in this layout!");
    }
  }
  
  private boolean isRefreshViewScroll(int paramInt)
  {
    if ((this.mHeaderState == 4) || (this.mFooterState == 4)) {
    	return false;	
    }
    
    //cond_1
    View localView1;
    View localView2;
    View localView3;
    
 
    if (this.mAdapterView != null) {
    	 if (paramInt <= 0) {
             //cond3
                 if (paramInt < 0) {
               	  localView2 = this.mAdapterView.getChildAt(-1 + this.mAdapterView.getChildCount());
               	  if (localView2 == null){
               		  return false;
               	  }
   	        		if ((localView2.getBottom() <= getHeight()) && (this.mAdapterView.getLastVisiblePosition() == -1 + this.mAdapterView.getCount()))
   			        {
   			          this.mPullState = 0;
   			          return true;
   			        }
                 }
	      }
          else{
        	  localView3 = this.mAdapterView.getChildAt(0);
            	if (localView3 == null){
            		return false;
            	}
   
			    if ((this.mAdapterView.getFirstVisiblePosition() == 0) && (localView3.getTop() == 0))
			    {
		            this.mPullState = 1;
		            return true;
			    }            			
        			  
        	 //cond2
	          int i = localView3.getTop();
	          int j = this.mAdapterView.getPaddingTop();
	          if ((this.mAdapterView.getFirstVisiblePosition() == 0) && (Math.abs(i - j) <= 8)) {
	             this.mPullState = 1;
	        	 return true; 
	          }
    	 }
    }
   
    //cond4
    if (this.mScrollView != null){
    	localView1 = this.mScrollView.getChildAt(0);
        if ((paramInt > 0) && (this.mScrollView.getScrollY() == 0))
        {
          this.mPullState = 1;
          return true;
        }
        
        //cond5
	    if ((paramInt < 0) && (localView1.getMeasuredHeight() <= getHeight() + this.mScrollView.getScrollY())){
		    this.mPullState = 0;
		    return true;			        	
	     }
     }

     return false;
  }
  
  private void measureView(View paramView)
  {
    ViewGroup.LayoutParams localLayoutParams = paramView.getLayoutParams();
    if (localLayoutParams == null) {
      localLayoutParams = new ViewGroup.LayoutParams(-1, -2);
    }
    int i = ViewGroup.getChildMeasureSpec(0, 0, localLayoutParams.width);
    int j = localLayoutParams.height;
    int k = 0;
    if (j > 0) {
    	k = View.MeasureSpec.makeMeasureSpec(j, 1073741824);
    }
    else{
    	//cond1
    	k = View.MeasureSpec.makeMeasureSpec(0, 0);
    }
    
	//goto0
	paramView.measure(i, k);
	return;
  }
  
  private void setHeaderTopMargin(int paramInt)
  {
    LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams)this.mHeaderView.getLayoutParams();
    localLayoutParams.topMargin = paramInt;
    this.mHeaderView.setLayoutParams(localLayoutParams);
    invalidate();
  }
  
  protected void onFinishInflate()
  {
    super.onFinishInflate();
    addFooterView();
    initContentAdapterView();
  }
  
  public void onFooterRefreshComplete()
  {
    setHeaderTopMargin(-this.mHeaderViewHeight);
    this.mFooterImageView.setVisibility(0);
    this.mFooterImageView.setImageResource(R.drawable.ic_pulltorefresh_arrow_up);
    this.mFooterTextView.setText(R.string.pull_to_refresh_footer_pull_label);
    this.mFooterProgressBar.setVisibility(8);
    this.mFooterState = 2;
  }
  
  public void onHeaderRefreshComplete()
  {
    setHeaderTopMargin(-this.mHeaderViewHeight);
    this.mHeaderImageView.setVisibility(0);
    this.mHeaderImageView.setImageResource(R.drawable.ic_pulltorefresh_arrow);
    this.mHeaderTextView.setText(R.string.pull_to_refresh_pull_label);
    this.mHeaderProgressBar.setVisibility(8);
    this.mHeaderState = 2;
  }
  
  public void onHeaderRefreshComplete(CharSequence paramCharSequence)
  {
    setLastUpdated(paramCharSequence);
    onHeaderRefreshComplete();
  }
  

  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = (int)paramMotionEvent.getRawY();
    Logger.LOGD(TAG, "onInterceptTouchEvent.getAction():"+paramMotionEvent.getAction()+", i="+i);
    switch (paramMotionEvent.getAction())	//ok
    {
    case 0:	//case 1
    	this.mLastMotionY = i;
    	return false;
    case 1:	//case 0
    	return false;
    case 2:	//case2
    	if(isRefreshViewScroll(i - this.mLastMotionY))
    		return true;
    }
     return false;
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = (int)paramMotionEvent.getRawY();
    switch (paramMotionEvent.getAction())	//ok
    {
    case 0:	//case 0
    	break;
    case 2:	//case 1
        int k = i - this.mLastMotionY;
        if ((this.mPullState != 1) && (this.mPullState == 0)) {
          footerPrepareToRefresh(k);
        }
        //cond1
        this.mLastMotionY = i;    	
    	break;
    case 1: //case 2
    case 3:
        int j = getHeaderTopMargin();
        if (this.mPullState == 1)
        {
          if (j >= 0) {
            headerRefreshing();
          } else {
            setHeaderTopMargin(-this.mHeaderViewHeight);
          }
        }
        else if (this.mPullState == 0) {
          if (Math.abs(j) >= this.mHeaderViewHeight + this.mFooterViewHeight) {
            footerRefreshing();
          } else {
            setHeaderTopMargin(-this.mHeaderViewHeight);
          }
        }
    	break;
    }
    return super.onTouchEvent(paramMotionEvent);
  }
  
  public void saveRecordLoadState(boolean bHaveMoreRecord)
  {
    this.mHaveMoreRecord = bHaveMoreRecord;
  }
  
  public void setLastUpdated(CharSequence paramCharSequence)
  {
    if (paramCharSequence != null)
    {
      this.mHeaderUpdateTextView.setVisibility(0);
      this.mHeaderUpdateTextView.setText(paramCharSequence);
      return;
    }
    this.mHeaderUpdateTextView.setVisibility(8);
  }
  
  public void setOnFooterRefreshListener(OnFooterRefreshListener paramOnFooterRefreshListener)
  {
    this.mOnFooterRefreshListener = paramOnFooterRefreshListener;
  }
  
  public void setOnHeaderRefreshListener(OnHeaderRefreshListener paramOnHeaderRefreshListener)
  {
    this.mOnHeaderRefreshListener = paramOnHeaderRefreshListener;
  }
  
  public static abstract interface OnFooterRefreshListener
  {
    public abstract void onFooterRefresh(Nouse_PullToRefreshView paramPullToRefreshView);
  }
  
  public static abstract interface OnHeaderRefreshListener
  {
    public abstract void onHeaderRefresh(Nouse_PullToRefreshView paramPullToRefreshView);
  }
}

