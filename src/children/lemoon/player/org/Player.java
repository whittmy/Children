package children.lemoon.player.org;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import com.dd.database.DatabaseManager;
import com.dd.database.QueryExecutor;
import com.dd.my.MvCacheMgrDAO;
import com.devsmart.android.ui.HorizontalListView;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.PlayerService.MyReceiver;
import children.lemoon.music.util.MediaUtil;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.utils.HttpManger;
 

import logger.lemoon.Logger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;

import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Player extends BaseReqActivity implements SurfaceHolder.Callback {
	private MySurfaceView mSurfaceView;
	//private SurfaceHolder mHolder;
	private MediaPlayer mPlayer = null;

	int m_times = 0;
	private String mPlayPath = null;
	private Context mContext = null;
	final String TAG = "VideoViewPlayer";
	private boolean needResume;
	DisplayMetrics mDm = new DisplayMetrics();

	private long mKeyEvent_tm = 0;
	final int EVENT_CLICK = 1;
	final int FLOWCOUNT = 3;

	final int CMD_PLAY_PURL = 2000;
	final int CMD_REFRESH_SEEKBAR = 2001;
 
	private boolean isIdle = true;
	
	int mDuration;
	LinearLayout mHeaderContainer;
	RelativeLayout mCtrlContainer;
	TextView mMvTitle;
	ImageView mBattery;
	View mFill1, mFill2;
	TextView mTvCurtm, mTvDur;
	
	int mRunMode = Configer.RunMode.MODE_NETWORK; 
	
	private LinkedList<PlayItemEntity> mData = new LinkedList<PlayItemEntity>();
	public HorizontalListView mHListView;
	private HorizontalScrollViewAdapter mAdapter;
	private int mCurPg = 0;
	
	private boolean mBCtrlbarShow = false;
	SeekBar sBar;
	public Handler mHandler = new Handler(/*getMainLooper()*/) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case CMD_PLAY_PURL:
				String purl = (String) msg.getData().get("purl");
				myPlay(purl);
				break;

			case CMD_REFRESH_SEEKBAR:
				if (mPlayer == null || mDuration == 0)
					break;
				int position = mPlayer.getCurrentPosition();
				int ps = (position * sBar.getMax())/ mDuration;
				
				mTvCurtm.setText(MediaUtil.formatTime(position));
				sBar.setProgress(ps);
				break;

			}
		};
	};
	
	private boolean mbPause = false;

	private Button mBtnPlay;
	String mCurLocalPath = "";
	int mCurCateId;

	SharedPreferences mPref;
	final String SP_CUR_PAGE = "page"; 
	final String SP_CUR_CLICKID = "clickid"; 	
	
	
	private void saveState(final List<PlayItemEntity> data){
		//当前 已请求的页， 
		//当前正在播放的clickid
		//已请求的数据
		DatabaseManager.getInstance().executeQueryTask(new QueryExecutor() {
		    @Override
		    public void run(SQLiteDatabase database) {
		    	MvCacheMgrDAO udao = new MvCacheMgrDAO(database, Player.this); // your class
		    	List<PlayItemEntity> tmp = null;
		    	
		    	if(data == null || data.size()==0){
		    		tmp  = mData;
		    	}
		    	else{
		    		tmp = data;
		    	}
		    	
		    	for(PlayItemEntity pie : tmp){
		    		udao.insert(pie.getId(), pie.getName(), pie.getFileSize(), pie.getPlayCnt(), pie.getPic(), pie.getDownUrl(),mCurCateId);
		    	} 		    	
		    }
		});
		
		
		mPref.edit().putInt(SP_CUR_PAGE+"_"+mCurCateId, mCurPg).commit();
		mPref.edit().putInt(SP_CUR_CLICKID+"_"+mCurCateId, mHListView.getClickPos()).commit();
	}
	
	private void recoverState(){
		if(mRunMode == Configer.RunMode.MODE_LOCAL){
			queryPlayList(mCurPg+1);
			return;
		}
		
		//读取数据库， 当然是在子线程中执行的
//		DatabaseManager.getInstance().executeQueryTask(new QueryExecutor() {
//		    @Override
//		    public void run(SQLiteDatabase database) {
//		    	MvCacheMgrDAO udao = new MvCacheMgrDAO(database, Player.this); // your class
//		    	Cursor cursor = udao.selectAll(mCurCateId);
// 		    	if(cursor == null){
//		    		Logger.LOGD("", "have no data to ...");
//		    		return;
//		    	}
// 		    	mData.clear();
//				for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
//					PlayItemEntity pie = new PlayItemEntity();
//					pie.setId(cursor.getInt(cursor.getColumnIndex(MvCacheMgrDAO.COLUMNS_MID)));
//					pie.setName(cursor.getString(cursor.getColumnIndex(MvCacheMgrDAO.COLUMNS_NAME)));
//					pie.setFileSize(cursor.getInt(cursor.getColumnIndex(MvCacheMgrDAO.COLUMNS_FSIZE)));
//					pie.setPlayCnt(cursor.getInt(cursor.getColumnIndex(MvCacheMgrDAO.COLUMNS_CNT)));
//					pie.setPic(cursor.getString(cursor.getColumnIndex(MvCacheMgrDAO.COLUMNS_PIC)));
//					pie.setDownUrl(cursor.getString(cursor.getColumnIndex(MvCacheMgrDAO.COLUMNS_URL)));
//					
//					//rocking-------debug
//					mData.add(pie);
// 		    	} 
//				
//				//加载
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						int curpg = mPref.getInt(SP_CUR_PAGE+"_"+mCurCateId, -1);
//						int clickid = mPref.getInt(SP_CUR_CLICKID+"_"+mCurCateId, -1);
//						
//						if(curpg==-1 || clickid==-1){
//							mCurPg = 0;
//							clickid = 0;
//						}
//						else{
//							mCurPg = curpg;
//						}
//						
////						mHListView.setClickPos(clickid);
////						mHListView.initDatas(mAdapter);
////						initHScrollView(clickid);
//						mHListView.setClickPos(clickid);
//						
//						if(mData.size() == 0){
//							queryPlayList(mCurPg+1);
//						}
//						else{
//							mBfirstData = false;
//							myPlay(clickid);
//						}						
//					}
//				});
//		    }
//		});
		
		queryPlayList(mCurPg+1);
	}
	
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mContext = this;
		this.getWindowManager().getDefaultDisplay().getMetrics(mDm);
		setContentView(R.layout.mv_player);
		
		
		DatabaseManager.initializeInstance(this);
		mPref = getSharedPreferences("sv", 0);
		
		findViewById(R.id.go_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		mHListView = (HorizontalListView) findViewById(R.id.id_horizontalScrollView);
		mAdapter = new HorizontalScrollViewAdapter(this, mData);
		mHListView.setAdapter(mAdapter);
		
		mHListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Log.e("", "=================== onitemclick :"+ arg2);
				mPref.edit().putInt(SP_CUR_CLICKID+"_"+mCurCateId, arg2).commit();
				myPlay(arg2);
			}
		});
		mHListView.setOnTouchListener(new OnTouchListener() {
			long mLasttm = 0;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_MOVE){
					if(System.currentTimeMillis()-mLasttm > 1500){
						mLasttm = System.currentTimeMillis();
						ctrlShowMgr();
					}
				}
				return false;
			}
		});
 
		mHListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				Log.e("", "onScroll: "+ firstVisibleItem+","+visibleItemCount+","+totalItemCount);

				if(firstVisibleItem+visibleItemCount+12 >= totalItemCount //注意next函数，尽可能一致
					&& 	mData.size()>=30){ //如果实际数据没有满页（30），说明数据量就不够，何谈下一页，所以要限定。	
					queryPlayList(mCurPg+1);
				}
				 
			}
		});

		registerMyRcv();
		initSurfaceView();
		initMyView();
		initPlayer();
		startProgressUpdate();
 
		
		mPlayPath = getIntent().getStringExtra("video_path");

		//--------- 识别 运行模式： 本地 or 网络 , 分类标识 ---------------
		mCurCateId = getIntent().getIntExtra("cataId", -1);
		
		String localpath  = getIntent().getStringExtra("localpath");
		if(localpath != null){
			if(localpath.isEmpty() || !new File(localpath).exists()){
				Toast.makeText(this, "路径非法："+ localpath, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			
			mCurLocalPath = localpath;
			mRunMode = Configer.RunMode.MODE_LOCAL;
		}
		else{
			//mCurCateId = getIntent().getIntExtra("cataId", -1);
			if(mCurCateId == -1){
				Toast.makeText(this, "非法类别："+ mCurCateId, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			mRunMode = Configer.RunMode.MODE_NETWORK;
		}
		//-----------------------------------------------
		
		//queryPlayList(mCurPg+1);
		
		
		//加载数据,来源多种
		recoverState();
		
		// 模式是显示界面上的各控件的，几秒后消失。
		// 如果默认不显示，则在首次播放时，黑屏，不知道为什么
		ctrlShowMgr();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// getWindow().getDecorView().setSystemUiVisibility(8);
		Logger.LOGD(TAG, "-=-=-=-onResume-=-=-=-=");

//		if (mPlayer != null) {
//			myPlay(mPlayPath);
//		}
	}
	
	
	
	

	

	
	
	//------------        seekbar  control --------------
	public class DelayThread extends Thread {
		private boolean pasued = false;
		private String control = ""; // 只是需要一个对象而已，这个对象没有实际意义

		private boolean brunning = true;

		public void setPause(boolean pause) {
			if (!pause) {
				synchronized (control) {
					control.notifyAll();
				}
			}
			this.pasued = pause;
		}

		public boolean isPaused() {
			return this.pasued;
		}

		public void setStop() {
			brunning = false;
			setPause(false);
		}

		protected void runPersonelLogic() {
		};

		public void run() {
			while (brunning) {
				synchronized (control) {
					if (pasued) {
						try {
							control.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				this.runPersonelLogic();
			}
		}
	}

	DelayThread mSeekThrd;
	public void startProgressUpdate() {
		 //开辟Thread 用于定期刷新SeekBar
		if(mSeekThrd == null){
			mSeekThrd = new DelayThread(){
				@Override
				protected void runPersonelLogic() {
					// TODO Auto-generated method stub
					super.runPersonelLogic();
					try {
						mHandler.sendEmptyMessage(CMD_REFRESH_SEEKBAR);
						//Log.e("", "seekbar...refresh...");

						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						Log.e("", "interrupt!");
					}
				}
			};
			mSeekThrd.start();
		}
	}
	//--------------------------------------------------
	





	void initMyView() {
		mBtnPlay = (Button)findViewById(R.id.btn_play);
		mBtnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isPlaying()){
					Log.e("", "btn: isplaying!!, begin to pause");
					do_pause();
					//mBtnPlay.setBackgroundResource(R.drawable.mv_btn_play);
				}
				else if(mbPause){
					Log.e("", "btn: ispaused,begin to resmue!!");
					//mBtnPlay.setBackgroundResource(R.drawable.mv_btn_pause);
					do_resume();
				}
				
				ctrlShowMgr();
			}
		});
		mHeaderContainer = (LinearLayout) findViewById(R.id.header_container);
		mCtrlContainer = (RelativeLayout) findViewById(R.id.ctrlbar_container);
		mMvTitle = (TextView) findViewById(R.id.mv_title);
		mBattery = (ImageView) findViewById(R.id.battery);
		mFill1 = (View)findViewById(R.id.fill1);
		mFill2 = (View)findViewById(R.id.fill2);
		mFill1.setOnClickListener(new FillClickEvent());
		mFill2.setOnClickListener(new FillClickEvent());
		
		mTvCurtm = (TextView) findViewById(R.id.curtime); 
		mTvDur = (TextView) findViewById(R.id.dur);
		
		sBar = (SeekBar) findViewById(R.id.seekb);
		sBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.e("", "seekbar.......onStartTrackingTouch");
				ctrlShowMgr();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int dest = seekBar.getProgress();
				int sMax = sBar.getMax();
				mPlayer.seekTo(mDuration * dest / sMax);
				
				Log.e("", "seekbar.......onStopTrackingTouch");
				ctrlShowMgr();
			}
		});
	}

	
	private void prev(boolean bplay){
		if(bplay){
//			if(mHListView.getClickPos()  <=  0)
//				return;
//			mHListView.prev();
//			myPlay(mHListView.getClickPos());
			int pos = mHListView.getClickPos();
			if(pos <= 0 || mAdapter==null)
				return;
			mHListView.setClickPos(pos-1);
			myPlay(pos-1);
		}
	}
	
	private void next(boolean bplay){
		if(mRunMode == Configer.RunMode.MODE_NETWORK){
			if (mAdapter.getCount() - mHListView.getClickPos()  < 12) {  //这儿与onscroll事件是有些不一样的哦
				Log.e("", "!!!!!! begin to load pg: " + (mCurPg + 1));
				queryPlayList(mCurPg + 1);
			}
		}
		
		if(bplay){
//			if(mHListView.getClickPos()+1 >= mData.size())
//				return;
//			mHListView.next();
//			myPlay(mHListView.getClickPos());
			
			int pos = mHListView.getClickPos() + 1; 
			if(pos >= mData.size() || mAdapter==null)
				return;
			mHListView.setClickPos(pos);
			
			//仅当隐藏时，其才将当前播放的滚动到开头，避免用户在选择影片时，其突然跳到正在播放的条目
			if(mFill1.getVisibility() == View.INVISIBLE)
				mHListView.setSelection(pos);
			
 			myPlay(pos);
		}
	}
 
	private void initPlayer() {
		Logger.LOGD(TAG, "begint to new InitPlayer");
		if (mPlayer != null) {
			mPlayer.release();
			mSurfaceView.setVisibility(View.INVISIBLE);
		}

		mPlayer = new MediaPlayer();
		mSurfaceView.setVisibility(View.VISIBLE);
//		if (mHolder != null)
//			mPlayer.setDisplay(mHolder);
		mPlayer.setOnPreparedListener(mPrepareListener);
		mPlayer.setOnCompletionListener(mCompleteListener);
		mPlayer.setOnErrorListener(mErrorListener);
		mPlayer.setOnInfoListener(mInfoListener);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mPlayer.reset();

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	
	private void do_pause(){
		if(mPlayer!=null && mPlayer.isPlaying()){	//isPlaying()函数怪怪的
			Log.e("", "do_pause");
			mBtnPlay.setBackgroundResource(R.drawable.mv_btn_play);
			mPlayer.pause();
			mbPause = true;
		}
	}
	
	
	private void togglePlay(){
		if(mPlayer == null)
			return;
		
		if(mbPause){
			do_resume();
		}
		else if(mPlayer.isPlaying()){
			do_pause();
		}
	}
	
	private void do_resume(){
		Log.e("", "Log");
		if(mPlayer!=null && mbPause){
			Log.e("", "do_resume");
			mBtnPlay.setBackgroundResource(R.drawable.mv_btn_pause);
			mPlayer.start();
			mbPause = false;
		}
	}
	
	private boolean isPlaying(){
		if(mPlayer!=null && mPlayer.isPlaying() && !mbPause)
			return true;
		return false;
	}
	
 

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Logger.LOGD(TAG, "exit.....");
			if (mPlayer != null) {
				mPlayer.release();
				mPlayer = null;
			}
			finish();
			return true;
		 
		////// 按键控制 //////////////

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Logger.LOGD(TAG, "onStop");


		releasePlayer();
		unregisterMyRcv();
	}

	
	
	
	
	
	boolean mBfirstData = true;
	private boolean queryPlayList(int pgIdx/* , int pgSize */) {
		if(mRunMode == Configer.RunMode.MODE_LOCAL){
			if(!mBfirstData)
				return false;
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mData.clear();
					mCurPg = 0;
				
					File f = new File(mCurLocalPath);
					String[] l = f.list();
					if(l == null){
						//Toast.makeText(this, mCurLocalPath+ " 目录下没有内容", Toast.LENGTH_SHORT).show();
						finish();
						return ;
					}
					
					if(!mCurLocalPath.endsWith("/"))
						mCurLocalPath += "/";
					
					for(String file : l){
						//Log.e("", mCurLocalPath+file);
						PlayItemEntity pie = new PlayItemEntity();
						pie.setName(file);
						pie.setDownUrl(mCurLocalPath+file);
						mData.add(pie);
					}		
					mBfirstData = false;

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							//mHListView.initDatas(mAdapter);
							//initHScrollView(-1);
							mHListView.setClickPos(0);
							myPlay(mHListView.getClickPos());		
						}
					});
				}
			}).start();
			

			return true;
		}
		else{
			HashMap<String, Object> bodyRequest = new HashMap<String, Object>();
			bodyRequest.put("id", mCurCateId);
			bodyRequest.put("pageindex", pgIdx);
			bodyRequest.put("pagesize", "30");

			HttpManger http = new HttpManger(this, bHandler, this);
			return http.httpRequest(Configer.TYPE_QUICK_ENTRY_TEST, bodyRequest, false, ResponePList.class, false, false, true);
		}
	}

	@Override
	protected void onPostHandle(int requestType, Object data, boolean status, int paramInt2, Object paramObject2, Object paramObject3) {
		// TODO Auto-generated method stub
		super.onPostHandle(requestType, data, status, paramInt2, paramObject2, paramObject3);
		if (data == null) {
			return;
		}
 
		ResHeadAndBody rslt = (ResHeadAndBody) data;
		// gson 将请求的数据，转为了ResponePList数据类型
		ResponePList plist = (ResponePList) rslt.getBody();
		List<PlayItemEntity> pList = plist.getpList();
		if (pList == null || pList.isEmpty())
			return;

		mData.addAll(plist.getpList());
		//mHListView.initDatas(mAdapter);
		//initHScrollView(-1);
		mHListView.setClickPos(0);
		mCurPg++;

		if(	mBfirstData){
			mBfirstData = false;
			myPlay(mHListView.getClickPos());
		}
		/////////// 放在底部  每次请求要保存状态，保存状态  /////////////
		saveState(plist.getpList());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(event.getY() >= mCtrlContainer.getY()){
				if(!mBCtrlbarShow)
					toggleClick();	
				else
					ctrlShowMgr();
			}
			else
			{
				Log.e("", "!!!!!!!!!!!!!!!!!!!!!down");
				toggleClick();				
			}
 			return true;
		}
		return super.onTouchEvent(event);
	}

 
//	private void initHScrollView(int clickpos){
//		if(clickpos >= 0)
//			mHListView.setClickPos(clickpos);
//		mHListView.initDatas(mAdapter);
//	}
	
	
	// ------------------- player control ---------------------
	public void myPlay(int curClickPos){
		if(curClickPos == -1)
			curClickPos = 0;
		if(mData==null || curClickPos>=mData.size())
			return;
		
		PlayItemEntity pie = mData.get(curClickPos);
		if(pie.getDownUrl() == null || pie.getDownUrl().isEmpty()){
			next(true);
			return;
		}

		if (mPlayPath != null && mPlayPath.compareTo(pie.getDownUrl()) == 0)
			return;
		mPlayPath = pie.getDownUrl();
		myPlay(mPlayPath);
	}
	
	public void myPlay(String url) {
		if (url == null || url.length() == 0) {
			Toast.makeText(mContext, "无播放地址", Toast.LENGTH_SHORT).show();
			return;
		}
		
		
//		mSurfaceView.setVisibility(View.INVISIBLE);
//		mSurfaceView.setVisibility(View.VISIBLE);
//		mSurfaceView.requestFocus();
		
		mPlayPath = url;
		Logger.LOGD(TAG, "begin to play:" + mPlayPath);

		if (mPlayer == null) {
			Logger.LOGD(TAG, "mPlayer is null!!");
			initPlayer();
		}
		try {
			Logger.LOGD(TAG, "-=-=-=-=-=-= -=-=-reset-=--= -=-==-");
			mPlayer.reset();
			mPlayer.stop();
			mPlayer.setDataSource(url);
//			if(mRunMode == Configer.RunMode.MODE_LOCAL){
//				mPlayer.prepare();
//			}
//			else if(mRunMode == Configer.RunMode.MODE_NETWORK){
				mPlayer.prepareAsync();
//			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Logger.LOGD(TAG, e.toString());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			Logger.LOGD(TAG, e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Logger.LOGD(TAG, e.toString());
		}
	}	
	
	
	private void releasePlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}
	MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer player, int whatInfo, int extra) {
			switch (whatInfo) {

			case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
				break;

			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				if (player.isPlaying()) {
					Logger.LOGD(TAG, "resume to pause MEDIA_INFO_BUFFERING_START");
					needResume = true;
				}
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				if (needResume) {
					player.start();
					needResume = false;
					Logger.LOGD(TAG, "resume to play!!!! MEDIA_INFO_BUFFERING_END");
				}
				break;
			default:
				Logger.LOGD(TAG, "what=" + whatInfo + ",extra=" + extra);
				break;
			}
			return false;
		}
	};
	MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer player, int whatError, int extra) {
			Logger.LOGD(TAG, "onError called: " + (System.currentTimeMillis() - mKeyEvent_tm));
			switch (whatError) {
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				// initPlayer();
				Logger.LOGD("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
				break;
			default:
				break;
			}
			return true;
		}
	};
	MediaPlayer.OnCompletionListener mCompleteListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer player) {
			Logger.LOGD("Play Over:::", "onComletion called");
			//myPlay(mPlayPath);
			next(true);
		}
	};
	MediaPlayer.OnPreparedListener mPrepareListener = new MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mPlayer) {
			Logger.LOGD(TAG, "onPrepared called");
			
			int pos = mHListView.getClickPos()==-1?0:mHListView.getClickPos();
			mMvTitle.setText(mData.get(pos).getName());
			mDuration = mPlayer.getDuration();
			if(mDuration == 0)
				mDuration = 1200;
			mTvDur.setText(MediaUtil.formatTime(mDuration));
			mBtnPlay.setBackgroundResource(R.drawable.mv_btn_pause);
			mPlayer.start();
		}
	};
	//----------------------------------------------------------
	
	

	//---------------- 控件显示/隐藏 控制 ----------------
	class FillClickEvent implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.e("", "fillevent, to hide it");
			mBCtrlbarShow = true;
			toggleClick();
		}
	}
	//调用这个函数按理可以直接隐藏/显示控件的，在视频未播放时没有问题
	//播放时只能调用显示，却无隐藏效果，在其它平台没有问题， 后来将seekbar的刷新事件屏蔽或将 刷新变慢，就可以了。
	// 感觉是，只要seekbar上还有将要刷新的dongzuo
	void toggleClick() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mBCtrlbarShow) {
					Log.e("", "toggleClick begin to hide");
					// hide
					mBCtrlbarShow = false;
					mSeekThrd.setPause(true);
					mSeekThrd.interrupt();
					mHandler.removeMessages(CMD_REFRESH_SEEKBAR);// Message(CMD_REFRESH_SEEKBAR);
 
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
					mHeaderContainer.setVisibility(View.INVISIBLE);
					mCtrlContainer.setVisibility(View.INVISIBLE);
					mHListView.setVisibility(View.INVISIBLE);	
					mFill2.setVisibility(View.INVISIBLE);
					mFill1.setVisibility(View.INVISIBLE);
			 
					//mSurfaceView.setVisibility(View.VISIBLE);
					mSurfaceView.requestFocus();

				} else {
					Log.e("", "toggleClick begin to show");
					mBCtrlbarShow = true;
					mSeekThrd.setPause(false);
					
					mFill1.setVisibility(View.VISIBLE);
					mFill2.setVisibility(View.VISIBLE);
 				
					
					mHeaderContainer.setVisibility(View.VISIBLE);
					mCtrlContainer.setVisibility(View.VISIBLE);
					mHListView.setVisibility(View.VISIBLE);
					//mAdapter.notifyDataSetChanged();
					//解决初始控件设为invisible，在视频播放后，通过setvisible无法使控件显示的问题。
					mFill1.getParent().requestTransparentRegion(mSurfaceView);
					ctrlShowMgr();
				}
			}
		});

	}
	private Runnable ctrBarHidding = new Runnable() {
		public void run() {
			//Log.e("", "ctrBarHidding...");
			mBCtrlbarShow = true;
			toggleClick();
			mHandler.removeCallbacks(ctrBarHidding); //执行一次就取消

		}
	};

	void ctrlShowMgr() {
		Log.e("", "---ctrlShowMgr");
		mHandler.removeCallbacks(ctrBarHidding); // 停止Timer
		mHandler.postDelayed(ctrBarHidding, 4000); // 开始Timer
	}
	//--------------------------------------------------------------

	
	
	
	
	
	
	
	// ------------------ surface relative-------------------
	void initSurfaceView() {
		mSurfaceView = (MySurfaceView) this.findViewById(R.id.surface_view);
		//mHolder = mSurfaceView.getHolder();
		//mHolder.addCallback(this);
		mSurfaceView.getHolder().addCallback(this);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Logger.LOGD(TAG, "Surface Change:::");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Logger.LOGD(TAG, "surfaceCreated");
		mSurfaceView.setY(0);
		//mHolder.setFixedSize(mDm.widthPixels, mDm.heightPixels);
		mSurfaceView.getHolder().setFixedSize(mDm.widthPixels, mDm.heightPixels);
		mSurfaceView.mIsFullScreen = false;
		if (mPlayer != null)
			mPlayer.setDisplay(/*mHolder*/mSurfaceView.getHolder());

		//initPlayer();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Logger.LOGD("Surface Destory:::", "surfaceDestroyed called");
		//mHolder = null;
		//releasePlayer();

	}
	//------------------------------------------------------------
	
	
	//------------------------ receiver -------------------------
	MyReceiver myReceiver;
	private void registerMyRcv(){
		Log.e(TAG, "Service registerMyRcv");
		if(myReceiver == null){
			myReceiver = new MyReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Configer.Action.MV_CTL_PLAY);
			filter.addAction(Configer.Action.MV_CTL_PAUSE);
			filter.addAction(Configer.Action.MV_CTL_NEXT);
			filter.addAction(Configer.Action.MV_CTL_PREV);
			filter.addAction(Configer.Action.MV_CTL_PLAY_PAUSE);
			
			registerReceiver(myReceiver, filter);
		}
	}
	
	private void unregisterMyRcv(){
		if(myReceiver != null)
			unregisterReceiver(myReceiver);
	}
	
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Configer.Action.MV_CTL_PLAY)){
				do_resume();
			}
			else if(action.equals(Configer.Action.MV_CTL_PAUSE)){
				do_pause();
			}
			else if(action.equals(Configer.Action.MV_CTL_PLAY_PAUSE)){
				togglePlay();
			}
			else if(action.equals(Configer.Action.MV_CTL_NEXT)){
				next(true);
			}
			else if(action.equals(Configer.Action.MV_CTL_PREV)){
				prev(true);
			}
		}
	}
	//-----------------------------------------------------------------
}
