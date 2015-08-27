package children.lemoon.ui;

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

import java.io.PrintStream;
import java.util.LinkedList;

 
import children.lemoon.R;
import children.lemoon.R.drawable;
import children.lemoon.R.id;
import children.lemoon.R.layout;
import children.lemoon.myrespone.RecommendEntity;
import children.lemoon.utils.BitmapUtil;
import children.lemoon.utils.Logger;



public class ChildCateAdapter extends BaseAdapter {
	private static final String TAG = "ChildCateAdapter";
	public static int screenWidth;
	private Context context;
	private LinkedList<RecommendEntity> data;
	int mItemHeight = 0;
	int mItemWidth = 0;
	float mScale = 0.0F;
	private Bitmap defaultIcon;

	public ChildCateAdapter(Activity paramActivity, LinkedList<RecommendEntity> paramLinkedList) {
		context = paramActivity;
		data = paramLinkedList;

		screenWidth = paramActivity.getWindowManager().getDefaultDisplay().getWidth();
		mScale = 1.4F;

		mItemWidth = (screenWidth / 4);
		mItemHeight = ((int) (mItemWidth * mScale));
		defaultIcon = BitmapUtil.getBitMapFromResource(paramActivity, R.drawable.p);
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int paramInt) {
		return data.get(paramInt);
	}

	public long getItemId(int paramInt) {
		return paramInt;
	}

	public View getView(int paramInt, View v, ViewGroup vGroup) {
		int i = screenWidth / 4;
		if (v == null) {
			Logger.LOGD("position---" + paramInt + "----convertView.getWidth()=null");
			v = View.inflate(context, R.layout.act_childcate_grid_item, null);
			Holder holder2 = new Holder();
			holder2.icon = ((ImageView) v.findViewById(R.id.iv_icon));
			holder2.icon.setAdjustViewBounds(true);
			holder2.icon.setMaxHeight((int) (1.39726F * i));
			holder2.title = ((TextView) v.findViewById(R.id.tv_title));
			v.setTag(holder2);
		}
		Holder holder1 = (Holder) v.getTag();
		RecommendEntity re = (RecommendEntity) data.get(paramInt);
		holder1.title.setText(re.getTitle());
		holder1.icon.setImageBitmap(defaultIcon);
//		MyImageLoadTask imgLoader = new MyImageLoadTask(holder1.icon, mItemWidth, mItemHeight);
//
//		if (!TextUtils.isEmpty(re.getImages())) {
//			imgLoader.execute(new String[] { re.getImages() });
//		}

		return v;
	}

	class Holder {
		public ImageView icon;
		public TextView title;

		Holder() {
		}
	}
}
