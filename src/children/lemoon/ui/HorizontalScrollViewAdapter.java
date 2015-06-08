package children.lemoon.ui;

import java.util.LinkedList;
import java.util.List;

import children.lemoon.R;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.utils.MyImageLoadTask;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HorizontalScrollViewAdapter {

	private MyHorizontalScrollView mView;
	private LayoutInflater mInflater;
	private LinkedList<PlayItemEntity> mDatas;

	public HorizontalScrollViewAdapter(Context cx, MyHorizontalScrollView v, LinkedList<PlayItemEntity> mDatas) {
		this.mView = v;
		mInflater = LayoutInflater.from(cx);
		this.mDatas = mDatas;
	}

	public int getCount() {
		return mDatas.size();
	}

	public Object getItem(int position) {
		return mDatas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

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
 

		//convertView.setBackgroundColor(Color.WHITE);
		
		
		
		PlayItemEntity pie = mDatas.get(position);
//		MyImageLoadTask imgLoader = new MyImageLoadTask(viewHolder.mImg, 80, 80);
//		if (!TextUtils.isEmpty(pie.getPic())) {
//			imgLoader.execute(new String[] { pie.getPic() });
//		}
		viewHolder.mImg.setImageResource(R.drawable.mv_bg_default);
		
		if(mView.getClickPos() == position){
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
