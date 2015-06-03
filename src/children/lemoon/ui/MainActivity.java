package children.lemoon.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.MuPlayer;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.RecommendEntity;
import children.lemoon.myrespone.ResponeChildClassList;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.player.org.Player;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.ui.MyHorizontalScrollView.CurrentImageChangeListener;
import children.lemoon.ui.MyHorizontalScrollView.OnItemClickListener;
import children.lemoon.utils.MyImageLoadTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

public class MainActivity extends Activity{
	private Context mContext;

	BtnClickListener listener = new BtnClickListener();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		findViewById(R.id.btn_audio).setOnClickListener(listener);
		findViewById(R.id.btn_video).setOnClickListener(listener);
	}


	class BtnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == R.id.btn_audio){
				Intent it = new Intent(MainActivity.this, MuPlayer.class);
				it.putExtra("curCata",  "_debug:儿童分类");
				//it.putExtra("localpath", "/mnt/external_sd/音乐");
				MainActivity.this.startActivity(it);
			}
			else if(v.getId() == R.id.btn_video){
				Intent it = new Intent(MainActivity.this, Player.class);
				//it.putExtra("localpath", "/mnt/external_sd/儿歌");
				MainActivity.this.startActivity(it);
			}
		}
		
	}
}
