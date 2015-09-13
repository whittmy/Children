package children.lemoon.categrid;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;



import com.devsmart.android.ui.HorizontalListView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.categrid.MoviesGridAdapter.Holder;
import children.lemoon.myrespone.PlayItemEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HScrollAdapter extends BaseAdapter {
	private static final String TAG = "HorizontalScrollViewAdapter";
	private Context context;
	private LinkedList<PlayItemEntity> data;
	private int[] mBGs = new int[]{R.drawable.grid_item_bg1, R.drawable.grid_item_bg2, R.drawable.grid_item_bg3};
	ImageLoader mLoader;
	
	private int mIdx = -1;	//代表该adapter再其数组中的下标
	private String mTypeid = "-1"; //代表该adapter对应数据的类别的id，便于再次或许下一页的数据
	private int mPgidx = 1; // 当前的页码
	LayoutInflater mFlater;
	
	int itemWidth, itemHeight;
	
	HashMap<String,Long> mCourseAndDownIdMap ; //保存课程id与其下载id的对应关系
	LongSparseArray<View> mDownIdAndViewMap; //保存下载id与其对应的进度view的对应关系
	
	public HScrollAdapter(Activity paramActivity, LinkedList<PlayItemEntity> paramLinkedList, ImageLoader loader, HashMap<String,Long> ar1, LongSparseArray<View>ar2 ) {
		this.context = paramActivity;
		this.data = paramLinkedList;
		mFlater = LayoutInflater.from(paramActivity);
		mLoader = loader;
		
		DisplayMetrics metric = new DisplayMetrics();
		paramActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		itemWidth = 20;//metric.widthPixels / 6;     // 屏幕宽度（像素）
		itemHeight = 100;   // 屏幕高度（像素）
		
		mCourseAndDownIdMap = ar1;
		mDownIdAndViewMap = ar2;
	}

	
	public void setIdx(int i){
		mIdx = i;
	}
	public int getIdx(){
		return mIdx;
	}
	
	public void setTypeId(String i){
		mTypeid = i;
	}
	public String getTypeId(){
		return mTypeid;
	}
	
	public void setPgIdx(int i){
		mPgidx = i;
	}
	public int getPgIdx(){
		return mPgidx;
	}	
	
	
	public void addData(List<PlayItemEntity> d){
		if(d!=null)
			this.data.addAll(d);
	}
	
	public int getCount() {
		return this.data.size();
	}

	public Object getItem(int paramInt) {
		return this.data.get(paramInt);
	}

	public long getItemId(int paramInt) {
		return paramInt;
	}

	 
	public View getView(int paramInt, View v, ViewGroup paramViewGroup) {
		Holder hold;
		PlayItemEntity pie = (PlayItemEntity) this.data.get(paramInt);
		if (v == null) {
			v = mFlater.inflate(R.layout.act_cate_hlist_item, null); //因为 HorizontalListView extends AdapterView，而AdapterView又不支持addView，所以无法使用paramViewGroup
			
			hold = new Holder();
			hold.icon = ((ImageView) v.findViewById(R.id.iv_icon));
			//hold.icon.setLayoutParams(new FrameLayout.LayoutParams(151, 90));
			
			hold.title = ((TextView) v.findViewById(R.id.tv_title));
			
			//hold.bg = (ImageView)v.findViewById(R.id.hlist_item_bg);
			hold.bg = (RelativeLayout)v.findViewById(R.id.hlist_item_bg);
			//hold.bg.setLayoutParams(new RelativeLayout.LayoutParams(247, 157));  //2015.8.14，修正频繁回调horizontallistview的onLayout函数问题，具体见act_cate_hlist_item.xml文件
			v.setTag(hold);
			
			//若是课件，便考虑下载的事情
			if(pie.getType() == 10){
				String id = pie.getIds();
				Long downid = mCourseAndDownIdMap.get(id); //查询是否有元素已经触发下载了
				if(downid != null){
					//有下载记录，则保存view与downid的对应关系
					mDownIdAndViewMap.put(downid, v.findViewById(R.id.dlprogress));
				}
			}
			
			//hold.bg.setBackgroundResource(mBGs[paramInt%3]);
		}
 
		hold = (Holder) v.getTag();
		
		String name = pie.getName();
		//Log.e("", "len="+name.length());
		hold.title.setText(name.length()>7? name.substring(0, 6)+"..":name);
		//Log.e("", pie.getName());
		mLoader.displayImage(Configer.IMG_URL_PRE+pie.getPic(), hold.icon);

		hold.bg.setBackgroundResource(mBGs[paramInt%3]);
		
		
		return v;
	}

	class Holder {
		public ImageView icon;
		//public ImageView  bg;
		public RelativeLayout bg;
		public TextView title;

	}
}
