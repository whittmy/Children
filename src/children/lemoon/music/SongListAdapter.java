package children.lemoon.music;

//ok
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.RecommendEntity;
import children.lemoon.utils.BitmapUtil;



public class SongListAdapter extends BaseAdapter {
	private static final String TAG = "SongListAdapter";
	private MuPlayer context;
	private LinkedList<PlayItemEntity> data;
 

	public SongListAdapter(MuPlayer paramActivity, LinkedList<PlayItemEntity> paramLinkedList) {
		context = paramActivity;
		data = paramLinkedList;
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
		if(data==null || paramInt>=data.size())
			return v;
		
		Holder holder1;
		if (v == null) {
			v = View.inflate(context, R.layout.mu_player_item, null);
			holder1 = new Holder();
			holder1.title = ((TextView) v.findViewById(R.id.tv_title));
			v.setTag(holder1);
		}
		
		holder1 = (Holder) v.getTag();
		holder1.title.setText(data.get(paramInt).getName());
		
		if(paramInt == context.getCurPos()){
			holder1.title.setTextColor(Color.rgb(255, 255, 0));
		}
		else{
			holder1.title.setTextColor(Color.rgb(255, 255, 255));
		}
		
		return v;
	}

	class Holder {
		public TextView title;

		Holder() { }
	}
}
