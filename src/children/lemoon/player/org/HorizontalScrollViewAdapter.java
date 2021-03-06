package children.lemoon.player.org;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
 
import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.myrespone.PlayItemEntity;
import dodola.example.android.bitmapfun.util.VolleyImageCache;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HorizontalScrollViewAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private LinkedList<PlayItemEntity> mDatas;
	Player mAct;
 
	String mPic = null;
	ImageLoader mLoader;
	public HorizontalScrollViewAdapter(Context cx, LinkedList<PlayItemEntity> mDatas, String pic) {
		mInflater = LayoutInflater.from(cx);
		this.mDatas = mDatas;
		mAct = (Player)cx;
		mPic = pic;
//		mLoader = ImageLoader.getInstance();
//		File cacheDir = StorageUtils.getCacheDirectory(cx);
//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(cx)
//		        .memoryCacheExtraOptions(112, 130) // default = device screen dimensions
//		        .diskCacheExtraOptions(112, 130, null)
//		        .threadPoolSize(3) // default 3
//		        .threadPriority(Thread.NORM_PRIORITY - 2) // default
//		        .tasksProcessingOrder(QueueProcessingType.FIFO) // default
//		        .denyCacheImageMultipleSizesInMemory()
//		        //.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
//		        //.memoryCacheSize(2 * 1024 * 1024)
//		        //.memoryCacheSizePercentage(13) // default
//		        .diskCache(new UnlimitedDiskCache(cacheDir)) // default,  设置带时限的文件缓存是不和要求的，如果我获得不了之前文件，哪怕其过期了，我也删除不了
//		        .diskCacheSize(500 * 1024 *1024)		//500M    			//所以缓存啊，还是我定期去清理
//		        .diskCacheFileCount(10000)			//10000 pics    
//		        //.writeDebugLogs()				// Logger.LOGD()
//		        .defaultDisplayImageOptions(new Builder()
//		        								.cacheOnDisc(true)
//		        								.cacheOnDisk(true)
//		        								//.cacheInMemory(true)
//		        								.showImageForEmptyUri(Configer.Res.get_icon_for_player())
//		        								.showImageOnLoading(Configer.Res.get_icon_for_player())
//		        								.showImageOnFail(Configer.Res.get_icon_for_player())
//		        								.build())		        
//		        .build();
//		mLoader.init(config);
		
		
		mLoader = new ImageLoader(Volley.newRequestQueue(mAct), VolleyImageCache.getInstance());
	}
	
	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	boolean isEmtpy(String s){
		if(s==null || s.isEmpty())
			return true;
		return false;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.mv_player_selector_item, parent, false);
			viewHolder.mImg = (ImageView) convertView.findViewById(R.id.mv_selector_item_img);
			viewHolder.mImgPlaying = (ImageView) convertView.findViewById(R.id.mv_selector_item_img_playing);
			viewHolder.mText = (TextView) convertView.findViewById(R.id.mv_selector_item_text);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
 
		PlayItemEntity pie = mDatas.get(position);
 
		ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(mAct, viewHolder.mImg, Configer.Res.get_icon_for_player(), Configer.Res.get_icon_for_player(), 0,0);
		
		//mLoader.displayImage(isEmtpy(pie.getPic())?"": (Configer.IMG_URL_PRE+pie.getPic()), viewHolder.mImg);
		//Logger.LOGD("", "pos:"+position);
		//mLoader.get(isEmtpy(pie.getPic())?"": (Configer.IMG_URL_PRE+pie.getPic()), imageListener);
		
		if(!isEmtpy(pie.getPic())){
			mLoader.get(pie.getPic(), imageListener);
		}
		else if(!isEmtpy(mPic)){
			mLoader.get(mPic, imageListener);
		}
		else{
			mLoader.get("", imageListener);
		}
		//mLoader.get(isEmtpy(pie.getPic())?"": pie.getPic(), imageListener);
		
		
		if(mAct.mHListView.getClickPos() == position){
			viewHolder.mImgPlaying.setVisibility(View.VISIBLE);
		}
		else{
			viewHolder.mImgPlaying.setVisibility(View.INVISIBLE);
		}
		
		viewHolder.mText.setText(pie.getName());

		return convertView;
	}

	private class ViewHolder {
		ImageView mImg;
		ImageView mImgPlaying;
		TextView mText;
	}

}
