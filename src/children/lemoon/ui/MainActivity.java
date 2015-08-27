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
import children.lemoon.ui.toast.ToastUtils;
import children.lemoon.ui.view.BatteryImgView;
 
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
import android.view.MotionEvent;
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
import android.widget.Toast;

public class MainActivity extends Activity{
	private Context mContext;
	BtnClickListener listener = new BtnClickListener();
	
	BatteryImgView battery;
	boolean bexit= false;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		battery = (BatteryImgView)findViewById(R.id.battery);
		
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
		

//		new Thread(new Runnable(){
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				int i = 0;
//				while(!bexit && i<=100){
//					battery.drawByScale(i);
//					i+=2;
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				
//			}
//		}).start();
		
		
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		if(event.getAction()==MotionEvent.ACTION_DOWN)
			ToastUtils.showIconToast(this, "xxxxxxxxxx");
		return super.onTouchEvent(event);
	}
	
	 @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		bexit = true;
	}
	
	class BtnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == R.id.btn_1){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "生活常识");
				it.putExtra("cataId", 3);					
				
				startActivity(it);
			}
			else if(v.getId() == R.id.btn_2){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata", "国学教育");
				it.putExtra("cataId", 2);					
				
				startActivity(it);			 
			}
			else if(v.getId() == R.id.btn_3){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "数理思维");
				it.putExtra("cataId", 6);				
				
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
				it.putExtra("cataId", 4);				
				
				startActivity(it);				 
			}
			else if(v.getId() == R.id.btn_6){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "语言发展");
				it.putExtra("cataId", 5);				
				
				startActivity(it);				 
			}
			else if(v.getId() == R.id.btn_10){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "动画城");
				it.putExtra("cataId", 1);				
				
				startActivity(it);				 
			}			
			else if(v.getId() == R.id.btn_7){
				Intent it = new Intent(MainActivity.this, MoviesGridActivity.class);
				it.putExtra("curCata",  "音乐");
				it.putExtra("cataId", 7);				
				
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
