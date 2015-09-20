package children.lemoon.player.org;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
 

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.myrespone.PlayItemEntity;

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

public class SetsHListAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private LinkedList<String> mDatas = new LinkedList<String>();
	Player mAct;
 
 	public SetsHListAdapter(Context cx) {
		mInflater = LayoutInflater.from(cx);
 		mAct = (Player)cx;
	}
	
 	public void setSetCnt(int cnt){
 		mDatas.clear();
 		int misc = cnt % mAct.PAGE_SIZE;
 		int groupcnt = (cnt / mAct.PAGE_SIZE) + (misc==0?0:1);
 		for(int i=1; i <= groupcnt;i++){
			int tmp = i*mAct.PAGE_SIZE;

 			if(i == groupcnt){
 				mDatas.add(String.format("%d-%d", tmp-mAct.PAGE_SIZE+1, cnt)); 
 			}
 			else{
 				mDatas.add(String.format("%d-%d", tmp-mAct.PAGE_SIZE+1, tmp)); 
 			}
 		}
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
			convertView = mInflater.inflate(R.layout.mv_player_sets_item, parent, false);
			viewHolder.mText = (TextView) convertView.findViewById(R.id.mv_sets_item_text);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
 
		if(mAct.getCurSetGrpIdx() == position){
			viewHolder.mText.setTextColor(Color.rgb(255,255,0));
		}
		else{
			viewHolder.mText.setTextColor(Color.argb(0xaa, 255, 255, 255));
		}
		
		viewHolder.mText.setText(mDatas.get(position));

		return convertView;
	}

	private class ViewHolder {
		TextView mText;
	}

}
