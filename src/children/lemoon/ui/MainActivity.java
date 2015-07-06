package children.lemoon.ui;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.categrid.MoviesGridActivity;
import children.lemoon.music.MuPlayer;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.RecommendEntity;
import children.lemoon.myrespone.ResponeChildClassList;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.player.org.Player;
import children.lemoon.player.org.MyHorizontalScrollView.CurrentImageChangeListener;
import children.lemoon.player.org.MyHorizontalScrollView.OnItemClickListener;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.ui.loading.CustomProgressDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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
		findViewById(R.id.btn_local).setOnClickListener(listener);
		findViewById(R.id.btn_local_video).setOnClickListener(listener);
		findViewById(R.id.btn_prev).setOnClickListener(listener);
		findViewById(R.id.btn_play).setOnClickListener(listener);
		findViewById(R.id.btn_pause).setOnClickListener(listener);
		findViewById(R.id.btn_next).setOnClickListener(listener);
		
		
		findViewById(R.id.btn_home).setOnClickListener(listener);
		
		
		findViewById(R.id.btn_1).setOnClickListener(listener);
		findViewById(R.id.btn_2).setOnClickListener(listener);
		findViewById(R.id.btn_3).setOnClickListener(listener);
		findViewById(R.id.btn_4).setOnClickListener(listener);
		findViewById(R.id.btn_5).setOnClickListener(listener);
		findViewById(R.id.btn_6).setOnClickListener(listener);
		findViewById(R.id.btn_7).setOnClickListener(listener);
		findViewById(R.id.btn_8).setOnClickListener(listener);
		findViewById(R.id.btn_9).setOnClickListener(listener);	
		findViewById(R.id.btn_10).setOnClickListener(listener);	
		
		
//		CustomProgressDialog dlg = CustomProgressDialog.createDialog(this);
//		dlg.show();
//		
	}


	class BtnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == R.id.btn_audio){
//				Intent it = new Intent(MainActivity.this, MuPlayer.class);
//				it.putExtra("curCata",  "_debug:儿童分类");
//				it.putExtra("cataId", 100);
//				MainActivity.this.startActivity(it);
				
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata", "国学教育");
				it.putExtra("cataId", 1000);				
				startActivity(it);				
				
				
			}
			else if(v.getId() == R.id.btn_home){
				//Intent i = new Intent(Intent.ACTION_VIEW);
				//i.setData(Uri.parse("http://192.168.2.104/html5/lufylegend.js-lufylegend-1.9.9/examples/demo/rpg/"));
				
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "_debug:儿童分类");
				it.putExtra("cataId", 1001);				
				
				startActivity(it);
			}
			else if(v.getId() == R.id.btn_local){
				Intent it = new Intent(MainActivity.this, MuPlayer.class);
				it.putExtra("curCata",  "_debug:本地音乐测试");
				it.putExtra("cataId", 1);
				it.putExtra("localpath", "/mnt/extsd/音乐");
				MainActivity.this.startActivity(it);
			}
			else if(v.getId() == R.id.btn_video){
				Intent it = new Intent(MainActivity.this, Player.class);
				//it.putExtra("localpath", "/mnt/external_sd/儿歌");
				it.putExtra("cataId", 2);
				MainActivity.this.startActivity(it);
			}
			else if(v.getId() == R.id.btn_local_video){
				Intent it = new Intent(MainActivity.this, Player.class);
				it.putExtra("localpath", "/mnt/extsd/儿歌");
				it.putExtra("cataId", 23);
				MainActivity.this.startActivity(it);				
			}
			else if(v.getId() == R.id.btn_next){
				String []args = new String[2];
				//args[0] = "listPosition"; args[1] = String.format("%d", mIService.getCurPos());
				args[0] = "MSG"; args[1]= String.format("%d", Configer.PlayerMsg.NEXT_MSG);
				Configer.sendNotice(MainActivity.this, Configer.Action.SVR_CTL_ACTION, args);	
			}
			else if(v.getId() == R.id.btn_play){
				String[] param = new String[2];
				param[0] = "MSG"; param[1] = String.format("%d", Configer.PlayerMsg.CONTINUE_MSG) ;
				Configer.sendNotice(MainActivity.this, Configer.Action.SVR_CTL_ACTION, param);
			}			
			else if(v.getId() == R.id.btn_pause){
				String[] param = new String[2];
				param[0] = "MSG"; param[1] = String.format("%d", Configer.PlayerMsg.PAUSE_MSG) ;
				Configer.sendNotice(MainActivity.this, Configer.Action.SVR_CTL_ACTION, param);
			}
			else if(v.getId() == R.id.btn_prev){
				String []args = new String[2];
				args[0] = "MSG"; args[1]= String.format("%d", Configer.PlayerMsg.PRIVIOUS_MSG);
				Configer.sendNotice(MainActivity.this, Configer.Action.SVR_CTL_ACTION, args);
			}
			else if(v.getId() == R.id.btn_1){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "生活常识");
				it.putExtra("cataId", 10000009);				
				
				startActivity(it);
			}
			else if(v.getId() == R.id.btn_2){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "国学教育");
				it.putExtra("cataId", 10000009);				
				
				startActivity(it);			 
			}
			else if(v.getId() == R.id.btn_3){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "数理思维");
				it.putExtra("cataId", 10000009);				
				
				startActivity(it);				 
			}
			else if(v.getId() == R.id.btn_4){
				//涂鸦
//				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
//				it.putExtra("curCata",  "");
//				it.putExtra("cataId", 10000009);				
//				
//				startActivity(it);				 
			}
			else if(v.getId() == R.id.btn_5){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "自然科学");
				it.putExtra("cataId", 10000009);				
				
				startActivity(it);				 
			}
			else if(v.getId() == R.id.btn_6){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "语言发展");
				it.putExtra("cataId", 10000009);				
				
				startActivity(it);				 
			}
			else if(v.getId() == R.id.btn_10){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "动画城");
				it.putExtra("cataId", 10000009);				
				
				startActivity(it);				 
			}			
			else if(v.getId() == R.id.btn_7){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "音乐");
				it.putExtra("cataId", 10000009);				
				
				startActivity(it);		 
			}
			else if(v.getId() == R.id.btn_8){
				//本地内容
//				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
//				it.putExtra("curCata",  "本地内容");
//				it.putExtra("cataId", 10000009);				
//				
//				startActivity(it);	
			}
			else if(v.getId() == R.id.btn_9){
				//"设置"
			}
			
			
		}
		
	}
}
