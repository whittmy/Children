package children.lemoon.categrid;

//ok
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;



import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.ui.loading.CustomProgressDialog;
import children.lemoon.utils.BitmapUtil;

public class MoviesGridAdapter extends BaseAdapter {
	private static final String TAG = "MoviesGridAdapter";
 	private Context context;
	private LinkedList<PlayItemEntity> data;
 
	ImageLoader mLoader;
	private int[] mBGs = new int[]{R.drawable.grid_item_bg1, R.drawable.grid_item_bg2, R.drawable.grid_item_bg3};
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
			v = View.inflate(this.context, R.layout.act_cate_grid_item, null);
			hold = new Holder();
			hold.icon = ((ImageView) v.findViewById(R.id.iv_icon));
			hold.title = ((TextView) v.findViewById(R.id.tv_title));
			hold.bg = (ImageView)v.findViewById(R.id.hlist_item_bg);
			hold.bg.setLayoutParams(new RelativeLayout.LayoutParams(247, 157));
			v.setTag(hold);
		}
		hold = (Holder) v.getTag();
		PlayItemEntity pie = (PlayItemEntity) this.data.get(paramInt);
		hold.title.setText(pie.getName());
		
 
		//mLoader.displayImage(Configer.IMG_URL_PRE+pie.getPic(), hold.icon);
 
		
		//Log.e("", "pos:"+paramInt+", mod="+paramInt%3);
		hold.bg.setBackgroundResource(mBGs[paramInt%3]);

		return v;
	}

	class Holder {
		public ImageView icon, bg;
		public TextView title;

	}
}
