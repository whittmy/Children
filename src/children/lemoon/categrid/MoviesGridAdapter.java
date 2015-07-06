package children.lemoon.categrid;

//ok
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import logger.lemoon.Logger;

import children.lemoon.R;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.ui.loading.CustomProgressDialog;
import children.lemoon.utils.BitmapUtil;

public class MoviesGridAdapter extends BaseAdapter {
	private static final String TAG = "MoviesGridAdapter";
 	private Context context;
	private LinkedList<PlayItemEntity> data;
 
	ImageLoader mLoader;
	
	public MoviesGridAdapter(Activity paramActivity, LinkedList<PlayItemEntity> paramLinkedList, ImageLoader loader) {
		this.context = paramActivity;
		this.data = paramLinkedList;
 
		mLoader = loader;
		
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
		if (v == null) {
			Logger.LOGD("position---" + paramInt + "----convertView.getWidth()=null");
			v = View.inflate(this.context, R.layout.act_cate_grid_item, null);
			hold = new Holder();
			hold.icon = ((ImageView) v.findViewById(R.id.iv_icon));
//			hold.icon.setAdjustViewBounds(true);
//			hold.icon.setMaxHeight((int) (1.39726F * i));
			hold.title = ((TextView) v.findViewById(R.id.tv_title));
			v.setTag(hold);
		}
		hold = (Holder) v.getTag();
		PlayItemEntity pie = (PlayItemEntity) this.data.get(paramInt);
		hold.title.setText(pie.getName());
		
		//hold.icon.setImageBitmap(this.defaultIcon);
		
		mLoader.displayImage(pie.getPic(), hold.icon);
		//mLoader.
		//hold.icon.setImageResource(R.drawable.mv_bg_default);
 

		return v;
	}

	class Holder {
		public ImageView icon;
		public TextView title;

	}
}
