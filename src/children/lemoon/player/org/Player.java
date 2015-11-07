package children.lemoon.player.org;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
 
import com.dd.database.DatabaseManager;
import com.dd.database.QueryExecutor;
import com.dd.my.MvCacheMgrDAO;
import com.devsmart.android.ui.HorizontalListView;
import com.devsmart.android.ui.HorizontalListView.OnScrollListener;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.PlayerService.FileNameSelector;
import children.lemoon.music.PlayerService.MyReceiver;
import children.lemoon.music.util.MediaUtil;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.myrespone.UrlInfoEntry;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ExtraEntry;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.entry.ResponsePager;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.reqbased.utils.MyUtil;
import children.lemoon.ui.loading.CustomProgressDialog;
import children.lemoon.ui.view.BatteryImgView;
import children.lemoon.ui.view.BatteryRcvBindView;
import children.lemoon.utils.Logger;
 



import android.app.Activity;
import android.app.Dialog;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Player extends BaseReqActivity  {
	private MySurfaceView mSurfaceView;
	//private SurfaceHolder mHolder;
	HashMap<String, String> mUaMap = new HashMap<String,String>();
	
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

	final int CMD_REFRESH_SEEKBAR = 2001;
 
	private boolean isIdle = true;
	
	int mDuration;
	RelativeLayout mHeaderContainer;
	RelativeLayout mCtrlContainer, mLayerTop;
	TextView mMvTitle;
	ImageView mImgPauseFlag;
	//View mFill1, mFill2;
	TextView mTvCurtm, mTvDur;
	
	int mRunMode = Configer.RunMode.MODE_NETWORK; 
	
	private LinkedList<PlayItemEntity> mData = new LinkedList<PlayItemEntity>();
	public HorizontalListView mHListView, mSetsHList;
	private HorizontalScrollViewAdapter mAdapter;

	public final int PAGE_SIZE = 50;
	private int mCurPg = 0;   //用于去网络数据
	
	private int mCurSetsGrpIdx = 0;// 用于切换集组
	private int mTotalPage = 0; //总页数
	private SparseIntArray mHadDataGetArr = new SparseIntArray(); //获得数据的页面会被记录下来，用于判断是否再通过网络获取数据。
	
	
	private SetsHListAdapter mSetsAdapter;
	
	CustomProgressDialog mLoading;
	private boolean mBCtrlbarShow = false;
	
	SeekBar sBar;
	public Handler mHandler = new Handler(/*getMainLooper()*/) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

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
	String mCurCateId;
	int mReqType = 4;

 	final String SP_CUR_PAGE = "page"; 
	final String SP_CUR_CLICKID = "clickid"; 	
 
	
	private void saveState(final List<PlayItemEntity> data){ }
	private void recoverState(){ queryPlayList(mCurPg+1); }
	
	private void pauseBgMusic(){
	    long eventtime = SystemClock.uptimeMillis(); 
	    Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null); 
	    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, 
	    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0); 
	    downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent); 
	    sendOrderedBroadcast(downIntent, null); 

	    Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null); 
	    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, 
	    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0); 
	    upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent); 
	    sendOrderedBroadcast(upIntent, null); 
	}
	
	public int getCurSetGrpIdx(){
		return mCurSetsGrpIdx;
	}
	
	public void setCurSetGrpIdx(int idx){
		mCurSetsGrpIdx = idx;
//			arg1.requestFocus();
		mSetsHList.setSelection(idx);
//		mSetsAdapter.notifyDataSetChanged();
		
		//访问数据
		int pgidx = idx+1;
		if(pgidx == mCurPg)
			return;
		
		if(mHadDataGetArr.indexOfKey(pgidx) >= 0){
			mHListView.setSelection(idx*PAGE_SIZE);
		}
		else{
			queryPlayList(pgidx);
		}
	}

	
	//禁止锁屏方法二   
//    @Override  
//    protected void onResume() {  
//        super.onResume();  
//        pManager = ((PowerManager) getSystemService(POWER_SERVICE));  
//        mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK  
//                | PowerManager.ON_AFTER_RELEASE, TAG);  
//        mWakeLock.acquire();  
//    }  
//      
//    @Override  
//    protected void onPause() {  
//        super.onPause();  
//          
//        if(null != mWakeLock){  
//            mWakeLock.release();  
//        }  
//    }  
	
	
	
	private BatteryRcvBindView batteryReceiver;
	
	
	//showDialog(DLG_ITEM1);
	static final int DLG_ITEM1 = 1;
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id){
		case DLG_ITEM1:
			CustomProgressDialog dlg = CustomProgressDialog.createDialog(this);
			dlg.setMessage(getResources().getString(R.string.default_loading_txt));
			return dlg; 
		}
		return super.onCreateDialog(id);
	}

	@Override
	@Deprecated
	protected void onPrepareDialog(int id, Dialog dialog) {
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog);
		
	}
	
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//禁止锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		
		this.getWindowManager().getDefaultDisplay().getMetrics(mDm);
		setContentView(R.layout.mv_player);
		mContext = this;

		//停止音乐
		pauseBgMusic();
 
//		DatabaseManager.initializeInstance(this);
 
		initMyView();

		initPlayer();
		startProgressUpdate();
 
		
		mPlayPath = getIntent().getStringExtra("video_path");
		if(mPlayPath!=null && !mPlayPath.isEmpty()){
			mRunMode = Configer.RunMode.MODE_DIRECT;
			mMvTitle.setText(mPlayPath); 
			myPlay(mPlayPath, "");
			return;
		}
		
		//--------- 识别 运行模式： 本地 or 网络 , 分类标识 ---------------
		mCurCateId = getIntent().getStringExtra("cataId");
		mReqType = getIntent().getIntExtra("type", 4);
		
		
		String localpath  = getIntent().getStringExtra("localpath");
		if(localpath != null){
			if(localpath.isEmpty() || !new File(localpath).exists()){
				Toast.makeText(this, "路径非法："+ localpath, Toast.LENGTH_SHORT).show();
				onExitProc();
				return;
			}
			
			mCurLocalPath = localpath;
			mRunMode = Configer.RunMode.MODE_LOCAL;
		}
		else{
			//mCurCateId = getIntent().getIntExtra("cataId", -1);
			if(mCurCateId == null){
				Toast.makeText(this, "非法类别："+ mCurCateId, Toast.LENGTH_SHORT).show();
				onExitProc();
				return;
			}
			mRunMode = Configer.RunMode.MODE_NETWORK;
		}
		//-----------------------------------------------
		//加载数据,来源多种
		recoverState();
		
		// 模式是显示界面上的各控件的，几秒后消失。
		// 如果默认不显示，则在首次播放时，黑屏，不知道为什么
		ctrlShowMgr();
	}

	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Logger.LOGD("", "onstop");
		
		onExitProc();
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Logger.LOGD(TAG, "-=-=-=-onResume-=-=-=-=");
		
		registerMyRcv();

		
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
						//Logger.LOGD("", "seekbar...refresh...");

						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						Logger.LOGD("", "interrupt!");
					}
				}
			};
			mSeekThrd.start();
		}
	}
	
	//--------------------------------------------------
	void initMyView() {
 		//退出事件
		findViewById(R.id.go_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onExitProc();
			}
		}); 
		
		mLoading = CustomProgressDialog.createDialog(this);
		mLoading.setMessage(getResources().getString(R.string.default_loading_txt));
		mLoading.show();	
		
		//集组显示
		mSetsHList = (HorizontalListView) findViewById(R.id.id_sethlist);
		mSetsAdapter = new SetsHListAdapter(this);
		mSetsHList.setAdapter(mSetsAdapter);
		
		mSetsHList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				setCurSetGrpIdx(arg2);				
			}
		});

		
		mHListView = (HorizontalListView) findViewById(R.id.id_horizontalScrollView);
		mAdapter = new HorizontalScrollViewAdapter(this, mData);
		mHListView.setAdapter(mAdapter);
		
		mHListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Logger.LOGD("", "=================== onitemclick :"+ arg2);
 				((HorizontalScrollViewAdapter)arg0.getAdapter()).notifyDataSetChanged();
				myPlay(arg2);
			}
		});
 
 
		mHListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(View view, int scrollState) {
				// TODO Auto-generated method stub
			}
			
			//这里两个工作：1. 数据预请求；2.集组焦点的切换。
			@Override
			public void onScroll(View view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				Logger.LOGD("", "onScroll: "+ firstVisibleItem+","+visibleItemCount+","+totalItemCount);

				//对于请求的数据要先判断是否已经请求过了。
				if(firstVisibleItem+visibleItemCount+12 >= totalItemCount //注意next函数，尽可能一致
					&& 	mData.size()>visibleItemCount && mData.size()>=PAGE_SIZE){ //数据至少要满屏	
					//另外注意，这里的mCurPg，只有当请求成功，它的值才会变更。
					queryPlayList(mCurPg+1);
					return;
				}
				
				
				//1. totalItemCount >0
				//2. firstVisibleItem: idx for the first item of data
				//3. visibleItemCount 
				if(totalItemCount > 0){
					float end = firstVisibleItem+visibleItemCount;
					int setgrpidx = (int) (Math.ceil(end/PAGE_SIZE)-1);
					if(setgrpidx >= 0 && setgrpidx!=mSetsHList.getSelectPosition()){
						Logger.LOGD("", "############ Alter setgroup :"+setgrpidx);
						mCurSetsGrpIdx = setgrpidx;
						mSetsHList.setSelection(setgrpidx);
					}
				}
			}
		});
		
		initSurfaceView();
		
		mImgPauseFlag = (ImageView)findViewById(R.id.img_pause);
		
		mBtnPlay = (Button)findViewById(R.id.btn_play);
		mBtnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isPlaying()){
					Logger.LOGD("", "btn: isplaying!!, begin to pause");
					do_pause();
					//mBtnPlay.setBackgroundResource(R.drawable.mv_btn_play);
				}
				else if(mbPause){
					Logger.LOGD("", "btn: ispaused,begin to resmue!!");
					//mBtnPlay.setBackgroundResource(R.drawable.mv_btn_pause);
					do_resume();
				}
				
				ctrlShowMgr();
			}
		});
		mHeaderContainer = (RelativeLayout) findViewById(R.id.header_container);
		mCtrlContainer = (RelativeLayout) findViewById(R.id.ctrlbar_container);
		mMvTitle = (TextView) findViewById(R.id.mv_title);
		mMvTitle.setText("                       ");

		mLayerTop = (RelativeLayout)findViewById(R.id.layer_top);
		
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
				Logger.LOGD("", "seekbar.......onStartTrackingTouch");
				//ctrlShowMgr();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int dest = seekBar.getProgress();
				int sMax = sBar.getMax();
				mPlayer.seekTo(mDuration * dest / sMax);
				
				Logger.LOGD("", "seekbar.......onStopTrackingTouch");
				//ctrlShowMgr();
			}
		});
	}

	
	private void prev(boolean bplay){
		if(bplay){
			int pos = mHListView.getClickPos();
			if(pos <= 0 || mAdapter==null)
				return;
			mHListView.setClickPos(pos-1);
			
			
			int setgrppos = (int) (Math.ceil((pos*1f)/PAGE_SIZE)-1);
			if(setgrppos >= 0){
				mCurSetsGrpIdx = setgrppos;
				mSetsHList.setSelection(mCurSetsGrpIdx);
			}
			
			myPlay(pos-1);
		}
	}
	
	private void next(boolean bplay){
		if(mRunMode == Configer.RunMode.MODE_NETWORK){
			if (mAdapter.getCount() - mHListView.getClickPos()  < 12) {  //这儿与onscroll事件是有些不一样的哦
				Logger.LOGD("", "!!!!!! begin to load pg: " + (mCurPg + 1));
				queryPlayList(mCurPg + 1);
			}
		}
		
		if(bplay){
			int pos = mHListView.getClickPos() + 1; 
			if(pos >= mData.size() || mAdapter==null)
				return;
			mHListView.setClickPos(pos);
 
			int setgrppos = (int) (Math.ceil((pos*1f)/PAGE_SIZE)-1);
			if(setgrppos >= 0){
				mCurSetsGrpIdx = setgrppos;
				mSetsHList.setSelection(mCurSetsGrpIdx);
			}
 
			//仅当隐藏时，其才将当前播放的滚动到开头，避免用户在选择影片时，其突然跳到正在播放的条目
			if(/*mFill1*/mLayerTop.getVisibility() == View.INVISIBLE)
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
			Logger.LOGD("", "do_pause");
			mBtnPlay.setBackgroundResource(R.drawable.mv_btn_play);
			mPlayer.pause();
			mbPause = true;
			
			mImgPauseFlag.setVisibility(View.VISIBLE);
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
		Logger.LOGD("", "Log");
		if(mPlayer!=null && mbPause){
			Logger.LOGD("", "do_resume");
			mBtnPlay.setBackgroundResource(R.drawable.mv_btn_pause);
			mPlayer.start();
			mbPause = false;
			mImgPauseFlag.setVisibility(View.GONE);
		}
	}


	private boolean isPlaying(){
		if(mPlayer!=null && mPlayer.isPlaying() && !mbPause)
			return true;
		return false;
	}

	
	//按键处理
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch(event.getKeyCode()){
			case KeyEvent.KEYCODE_BACK:
				Logger.LOGE("======return");
				onExitProc();
				return true;
			case 87:	//next
				Logger.LOGE("======next");
				next(true);
				return true;
			case 88:	//prev
				Logger.LOGE("======prev");
				prev(true);
				return true;
			case 85: //play/pause
				Logger.LOGE("======play/pause");
				togglePlay();
				return true;
			}
		}
 
		return super.dispatchKeyEvent(event);
	}

	
	
	private void onExitProc(){
//		if (mPlayer != null) {
//			mPlayer.release();
//			mPlayer = null;
//		}

		releasePlayer();

		unregisterMyRcv();
		System.exit(0);
	}

//	public class FileNameSelector implements FilenameFilter{
//		@Override
//		public boolean accept(File arg0, String arg1) {
//			// TODO Auto-generated method stub
//			arg1 = arg1.toLowerCase();
//			
//			
//			if(arg1.endsWith("mp3")
//				|| arg1.endsWith("wma")
//				|| arg1.endsWith("wav")
//				|| arg1.endsWith("mod")
//				|| arg1.endsWith("cd")
//				|| arg1.endsWith("md")
//				|| arg1.endsWith("asf")
//				|| arg1.endsWith("aac")
//				|| arg1.endsWith("mp3pro")
//				|| arg1.endsWith("vqf")
//				|| arg1.endsWith("flac")
//				|| arg1.endsWith("ape")
//				|| arg1.endsWith("mid")
//				|| arg1.endsWith("ogg")
//				|| arg1.endsWith("m4a")
//				|| arg1.endsWith("aac+")
//				|| arg1.endsWith("aiff")
//				|| arg1.endsWith("vqf"))
//				return true;
//			return false;
//		}
//	}
	
	
	boolean mBfirstData = true;
	boolean mBReqing = false;
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
				
					HashMap<String, Integer> extSets = new HashMap<String, Integer>();
					extSets.put("mpeg", 1);			extSets.put("mpg", 1);			extSets.put("dat", 1);
					extSets.put("ra", 1);			extSets.put("rm", 1);			extSets.put("rmvb", 1);
					extSets.put("mp4", 1);			extSets.put("flv", 1);			extSets.put("mov", 1);
					extSets.put("qt", 1);			extSets.put("asf", 1);			extSets.put("wmv", 1);
					extSets.put("avi", 1);			extSets.put("3gp", 1);			extSets.put("mkv", 1);
					extSets.put("f4v", 1);			extSets.put("m4v", 1);			extSets.put("m4p", 1);
					extSets.put("m2v", 1);			extSets.put("dat", 1);			extSets.put("xvid", 1);
					extSets.put("divx", 1);			extSets.put("vob", 1);			extSets.put("mpv", 1);
					extSets.put("mpeg4", 1);		extSets.put("mpe", 1);			extSets.put("mlv", 1);
					extSets.put("ogm", 1);			extSets.put("m2ts", 1);			extSets.put("mts", 1);
					extSets.put("ask", 1);			extSets.put("trp", 1);			extSets.put("tp", 1);
					extSets.put("ts", 1);
					
					File f = new File(mCurLocalPath);
					File[] l = f.listFiles();			//f.list(new FileNameSelector());
					if(l == null){
						//Toast.makeText(this, mCurLocalPath+ " 目录下没有内容", Toast.LENGTH_SHORT).show();
						onExitProc();
						return ;
					}
					
					if(!mCurLocalPath.endsWith("/"))
						mCurLocalPath += "/";
					
					for(File file : l){
						//Logger.LOGD("", mCurLocalPath+file);
						if(file.isDirectory())
							continue;
						
						String name = file.getName();
						int pos = name.lastIndexOf(".");
						if(pos < 0 || (pos>=(name.length()-1)))
							continue;
						 
						String ext = name.substring(pos+1).toLowerCase();
						if(extSets.get(ext) == null)
							continue;
					 
						
						PlayItemEntity pie = new PlayItemEntity();
						pie.setName(name);
						
						List<UrlInfoEntry> ulist = new ArrayList<UrlInfoEntry>();
						UrlInfoEntry u = new UrlInfoEntry();
						u.setUrl(mCurLocalPath+name);
						ulist.add(u);
						pie.setUrlList(ulist);
						//pie.setDownUrl(mCurLocalPath+name);
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
			if(mBReqing)
				return true;
			
			//如果已经请求过了，则不再请求。
			if(mHadDataGetArr.indexOfKey(pgIdx) >= 0)
				return true;
			
			mBReqing = true;
			if(!mLoading.isShowing())
				mLoading.show();
			
			HashMap<String, Object> bodyRequest = new HashMap<String, Object>();
			bodyRequest.put("id", mCurCateId);
			bodyRequest.put("pageindex", pgIdx);
			bodyRequest.put("pagesize", PAGE_SIZE);
			bodyRequest.put("type", mReqType);
			HttpManger http = new HttpManger(this, bHandler, this);
			return http.httpRequest(Configer.REQ_VIDEO_PLAYLIST, bodyRequest, false, ResponePList.class, false, false, true);
		}
	}

	@Override
	protected void onPostHandle(int requestType, Object data, boolean status, int paramInt2, Object paramObject2, Object paramObject3) {
		// TODO Auto-generated method stub
		super.onPostHandle(requestType, data, status, paramInt2, paramObject2, paramObject3);
		if (data == null) {
			mLoading.cancel();
			mBReqing = false;
			return;
		}
 
		ResHeadAndBody rslt = (ResHeadAndBody) data;
		
		if(mUaMap.size() == 0){
			List<ExtraEntry> extra = rslt.getHeader().getExtra();
			if(extra != null && extra.size()>0){
				for(ExtraEntry en : extra){
					Logger.LOGD("++extra: "+ en.getKey()+", "+en.getValue());
					mUaMap.put(en.getKey(), en.getValue());
				}
			}
		}

		// gson 将请求的数据，转为了ResponePList数据类型
		ResponePList plist = (ResponePList) rslt.getBody();
		List<PlayItemEntity> pList = plist.getpList();
		//如果数据空，则页面相关信息不会被变更
		if (pList == null || pList.isEmpty()){
			mLoading.cancel();
			mBReqing = false;
			return;
		}
 
		ResponsePager pg = (ResponsePager)rslt.getPage();
		if(pg != null){
			if(mTotalPage == 0){
				mTotalPage = pg.getPageCount();
				
				//更新 集数分组
				mSetsAdapter.setSetCnt(pg.getCount());//总集数
				mSetsAdapter.notifyDataSetChanged();
				mSetsHList.setVisibility(View.VISIBLE);
			}
			
			mCurPg = pg.getPageIndex();
			if(mHadDataGetArr.indexOfKey(mCurPg)<0){
				mHadDataGetArr.put(mCurPg, 1);	//入库，即存在

				ArrayList<Integer> keys = new ArrayList<Integer>();
				for(int i=0; i < mHadDataGetArr.size(); i++){
					keys.add(mHadDataGetArr.keyAt(i));
				}
				
				//计算 insertIdx，根据带插入的列表的集码对应的位置，进行计算在mData中的插入位置
				Collections.sort(keys);
				int curIdx = keys.indexOf(mCurPg);
				int insertIdx = PAGE_SIZE*curIdx; //[insertIdx, --) 

				//数据入库
				mData.addAll(insertIdx, plist.getpList());  //在mData中insertIdx之前插入 plist.getpList()
				
				if(!mBfirstData)
					mHListView.setSelection(insertIdx);
			}
		}
 
		if(	mBfirstData){
			mHListView.setClickPos(0);
			mBfirstData = false;
			myPlay(mHListView.getClickPos());
		}
 
		/////////// 放在底部  每次请求要保存状态，保存状态  /////////////
		saveState(plist.getpList());
		
		mLoading.cancel();
		mBReqing = false;

	}
 
	private boolean inRangeOfView(View view, MotionEvent ev) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		if (ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight())) {
			return false;
		}
		return true;
	}
	
	
	void showCtrlbar(){
		mBCtrlbarShow = false;
		toggleClick();
	}
	
	void hideCtrlbar(){
		mBCtrlbarShow = true;
		toggleClick();
	}
	
	
	long mLasttm = 0;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		int action = ev.getAction();

		switch(action){
		case MotionEvent.ACTION_DOWN:
			if(!mBCtrlbarShow){
				showCtrlbar();
				return true;
			}
			
			if(inRangeOfView(mHListView, ev) 
					|| inRangeOfView(mHeaderContainer, ev)
					|| inRangeOfView(mCtrlContainer, ev)
					|| inRangeOfView(mSetsHList, ev)){
 
				Logger.LOGD("", "down on mHListView");
				if(System.currentTimeMillis()-mLasttm > 1500){
					mLasttm = System.currentTimeMillis();
					ctrlShowMgr();
				}
			}
			else {
				Logger.LOGD("", "down on Fill area");
				hideCtrlbar();
				return true;
			}
			break;
			
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			
			break;
		}
		
		return super.dispatchTouchEvent(ev);
	}
 
	
	
	
	
	// ------------------- player control ---------------------
	int mPlayingIdx = 0;
	public void myPlay(int curClickPos){
		if(curClickPos == -1)
			curClickPos = 0;
		if(mData==null || curClickPos>=mData.size())
			return;
		
		
		String purl = null;
		String src ="";
		PlayItemEntity pie = mData.get(curClickPos);
		if(mRunMode == Configer.RunMode.MODE_LOCAL){
			//purl = pie.getDownUrl();
			try{
				purl = pie.getUrlList().get(0).getUrl();
			}
			catch(Exception e){
				purl = "";
			}
//			if(purl != null)
//				purl = URLEncoder.encode(purl);
		}
		else{
			long ts = System.currentTimeMillis();
			UrlInfoEntry u = pie.getUrlList().get(mPlayingIdx);
			src = URLEncoder.encode(u.getSrc());
			String idx = u.getUrl();
			
			purl = Configer.initUrl(Configer.REQ_PLAYURL)+ pie.getIds()+"/"+src+"/"+idx+"/"+ts+"/"+ MyUtil.getSign(ts) ;
		}

		if (purl == null ||  (mPlayPath != null && mPlayPath.compareTo(purl) == 0) )
			return;
		
		
		mPlayPath = purl;
		myPlay(mPlayPath, src);
	}
	
	public void myPlay(String url, String src) {
		if (url == null || url.length() == 0) {
			Toast.makeText(mContext, "无播放地址", Toast.LENGTH_SHORT).show();
			return;
		}
 
		if(!mLoading.isShowing())
			mLoading.show();
		mPlayPath = url;
		Logger.LOGD(TAG, "begin to play:" + mPlayPath + ", "+ src);

		if (mPlayer == null) {
			Logger.LOGD(TAG, "mPlayer is null!!");
			initPlayer();
		}
		try {
			Logger.LOGD(TAG, "-=-=-=-=-=-= -=-=-reset-=--= -=-==-");
			mPlayer.reset();
			mPlayer.stop();
			
			Logger.LOGD("mUaMap.size= "+ mUaMap.size() +", value: "+ mUaMap.get(src));
			if(mUaMap.get(src)!= null){
				Logger.LOGD("=== had  headers ====");
				Map<String, String> headers = new HashMap<String,String>(); 
				
				String headinfo = mUaMap.get(src);
				String[] grp = headinfo.split("\\$\\$");
				if(grp!=null){
					Logger.LOGD("grp size:"+grp.length);
					for(String items:grp){
						String[] item = items.split("\\:");
						Logger.LOGD("item size:"+item.length);
						
						if(item!=null && item.length==2){
							Logger.LOGD("Add header: "+ item[0]+"="+item[1]);
							headers.put(item[0], item[1]);
						}
					}
				}
				
				mPlayer.setDataSource(Player.this, Uri.parse(url), headers);
			}
			else{
				mPlayer.setDataSource(url);
			}
			mPlayer.prepareAsync();
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
					if(!mLoading.isShowing())
						mLoading.show();
				}
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				if (needResume) {
					player.start();
					needResume = false;
					Logger.LOGD(TAG, "resume to play!!!! MEDIA_INFO_BUFFERING_END");
					mLoading.cancel();
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
			if(mRunMode == Configer.RunMode.MODE_DIRECT && whatError!=-38){
				onExitProc();
				return true;
			}
			
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
			if(mRunMode != Configer.RunMode.MODE_DIRECT){
				next(true);
			}
			else{
				onExitProc();
			}
		}
	};
	MediaPlayer.OnPreparedListener mPrepareListener = new MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mPlayer) {
			Logger.LOGD(TAG, "onPrepared called");
			
			if(mRunMode != Configer.RunMode.MODE_DIRECT){
				int pos = mHListView.getClickPos()==-1?0:mHListView.getClickPos();
				mMvTitle.setText(mData.get(pos).getName());
			}
			mDuration = mPlayer.getDuration();
			if(mDuration == 0)
				mDuration = 1200;
			mTvDur.setText(MediaUtil.formatTime(mDuration));
			mBtnPlay.setBackgroundResource(R.drawable.mv_btn_pause);
			mPlayer.start();
			
			mLoading.cancel();
		}
	};
	//----------------------------------------------------------
	
	

	//---------------- 控件显示/隐藏 控制 ----------------
//	class FillClickEvent implements View.OnClickListener{
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			Logger.LOGD("", "fillevent, to hide it");
//			mBCtrlbarShow = true;
//			toggleClick();
//		}
//	}
	//调用这个函数按理可以直接隐藏/显示控件的，在视频未播放时没有问题
	//播放时只能调用显示，却无隐藏效果，在其它平台没有问题， 后来将seekbar的刷新事件屏蔽或将 刷新变慢，就可以了。
	// 感觉是，只要seekbar上还有将要刷新的dongzuo
	void toggleClick() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mBCtrlbarShow) {
					Logger.LOGD("", "toggleClick begin to hide");
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
 
					mLayerTop.setVisibility(View.INVISIBLE);
					mSurfaceView.requestFocus();
				} else {
					Logger.LOGD("", "toggleClick begin to show");
					mBCtrlbarShow = true;
					mSeekThrd.setPause(false);
 
					mLayerTop.setVisibility(View.VISIBLE);
					//解决初始控件设为invisible，在视频播放后，通过setvisible无法使控件显示的问题。
					//mFill1.getParent().requestTransparentRegion(mSurfaceView);
					mLayerTop.requestTransparentRegion(mSurfaceView);
					ctrlShowMgr();
				}
			}
		});

	}
	private Runnable ctrBarHidding = new Runnable() {
		public void run() {
			//Logger.LOGD("", "ctrBarHidding...");
			mBCtrlbarShow = true;
			toggleClick();
			mHandler.removeCallbacks(ctrBarHidding); //执行一次就取消

		}
	};

	void ctrlShowMgr() {
		Logger.LOGD("", "---ctrlShowMgr");
		mHandler.removeCallbacks(ctrBarHidding); // 停止Timer
		mHandler.postDelayed(ctrBarHidding, 4000); // 开始Timer
	}
	//--------------------------------------------------------------


	// ------------------ surface relative-------------------
	void initSurfaceView() {
		mSurfaceView = (MySurfaceView) this.findViewById(R.id.surface_view);
		mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){
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
			}
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Logger.LOGD("Surface Destory:::", "surfaceDestroyed called");
			}
		});
	}
	//------------------------------------------------------------
	
	
	//------------------------ receiver -------------------------
	MyReceiver myReceiver;
	private void registerMyRcv(){
		Logger.LOGD(TAG, "Service registerMyRcv");
		
		if(batteryReceiver == null){
			batteryReceiver = new BatteryRcvBindView((BatteryImgView)findViewById(R.id.battery));
			registerReceiver(batteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED")); 
		}

		if(myReceiver == null){
			myReceiver = new MyReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Configer.Action.MV_CTL_PLAY);
			filter.addAction(Configer.Action.MV_CTL_PAUSE);
			filter.addAction(Configer.Action.MV_CTL_NEXT);
			filter.addAction(Configer.Action.MV_CTL_PREV);
			filter.addAction(Configer.Action.MV_CTL_PLAY_PAUSE);
			
			//exit
			filter.addAction(Configer.Action.ACT_EXIT);
			
			//锁(关)屏状态
		     filter.addAction(Intent.ACTION_SCREEN_ON);  
		     filter.addAction(Intent.ACTION_SCREEN_OFF);  
			
			registerReceiver(myReceiver, filter);
		}
	}
	
	private void unregisterMyRcv(){
		if(myReceiver != null){
			unregisterReceiver(myReceiver);
			myReceiver = null;
		}
		if(batteryReceiver != null){
			unregisterReceiver(batteryReceiver);
			batteryReceiver = null;
		}
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
			else if(Intent.ACTION_SCREEN_ON.equals(action)){  
                //mScreenStateListener.onScreenOn();  
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){  
                //mScreenStateListener.onScreenOff();
            	onExitProc();
            } 
            else if(action.equals(Configer.Action.ACT_EXIT)){
            	onExitProc();
            }
		}
	}
	//-----------------------------------------------------------------
}
