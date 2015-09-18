package children.lemoon.music;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
 
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.PlayerService.MyBind;
import children.lemoon.music.lrc.LrcView;
import children.lemoon.music.util.MediaUtil;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.player.org.Player;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.reqbased.utils.OnClickUtil;
import children.lemoon.ui.loading.CustomProgressDialog;
import children.lemoon.ui.view.BatteryImgView;
import children.lemoon.ui.view.BatteryRcvBindView;
import children.lemoon.utils.Logger;

public class MuPlayer extends Activity {
	ListView mVSongList;
	SongListAdapter mAdapter;

	private int[] mBgImg = { R.drawable.mu_bg, R.drawable.mu_bg3 };/*, R.drawable.mu_bg2*/

	public static LrcView mlrcView; // 自定义歌词视图, 由service更新
	ImageView mImgDisc;
	// Animation mAnimDisc;
	// private ObjectAnimator anim;

	TextView mTvmuName, mTvCate;
	Button mPrev, mPlay, mNext, mFav;
	CtrlBtnClickListener mCtrlListener = new CtrlBtnClickListener();

	private PlayerReceiver playerReceiver;
	public static final String MUSIC_PLAYING = "lemoon.action.MUSIC_PLAYING"; // 音乐正在播放动作
	public static final String REPEAT_ACTION = "lemoon.action.REPEAT_ACTION"; // 音乐重复播放动作
	public static final String SHUFFLE_ACTION = "lemoon.action.SHUFFLE_ACTION";// 音乐随机播放动作

	private SeekBar mSeekBar; // 歌曲进度
	private TextView mTvCurTm; // 当前进度消耗的时间
	private TextView mTvDur; // 歌曲时间

	SeekBarChangeListener mSeekBarListener = new SeekBarChangeListener();

	CustomProgressDialog mLoading;
	ExitDialog mExitDlg;
	void initView() {
		
		mBatteryView = (BatteryImgView)findViewById(R.id.battery);
		RelativeLayout mubg = (RelativeLayout) findViewById(R.id.mubg);
		
		SimpleDateFormat formatter = new SimpleDateFormat("HH");       
		Date curDate    =   new Date(System.currentTimeMillis());//获取当前时间       
		String str = formatter.format(curDate);     
		if(str.compareTo("18") >= 0 || str.compareTo("06")<0){
			//night
			mubg.setBackgroundResource(R.drawable.mu_bg2);
		}
		else{
			mubg.setBackgroundResource(mBgImg[new Random().nextInt(mBgImg.length)]);
		}
 
		mTvmuName = (TextView) findViewById(R.id.mu_title);
		mTvCate = (TextView) findViewById(R.id.mu_cata);

		findViewById(R.id.go_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
    			mExitDlg.show();

				//finish();
			}
		});
		
		mPrev = (Button) findViewById(R.id.btn_prev);
		mPlay = (Button) findViewById(R.id.btn_ok);
		mNext = (Button) findViewById(R.id.btn_next);
		mFav = (Button) findViewById(R.id.btn_fav);
		mPrev.setOnClickListener(mCtrlListener);
		mPlay.setOnClickListener(mCtrlListener);
		mNext.setOnClickListener(mCtrlListener);
		mFav.setOnClickListener(mCtrlListener);
		mFav.setVisibility(View.INVISIBLE); // 先隐藏
		
		
		mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);

		mlrcView = (LrcView) findViewById(R.id.lrcShowView);
		mSeekBar = (SeekBar) findViewById(R.id.seekb);
		mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
		mTvCurTm = (TextView) findViewById(R.id.curtime);
		mTvDur = (TextView) findViewById(R.id.dur);

		// 唱片动画
		mImgDisc = (ImageView) findViewById(R.id.mu_disc);

		animator = ObjectAnimator.ofFloat(mImgDisc,  "rotation", 0, 360 );
		animator.setRepeatCount(ValueAnimator.INFINITE);
		animator.setDuration(15000);
		animator.setRepeatMode(ObjectAnimator.RESTART);
		// 为了增加
		animator.addUpdateListener(updateListener);
		
		// 播放列表
		mVSongList = (ListView) findViewById(R.id.mu_songlist);
		mVSongList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Logger.LOGD("", "onItemClick");

				if (mIService != null  && arg2 < mIService.getDatas().size()) {
					mTvmuName.setText(mIService.getDatas().get(arg2).getName());
					play(arg2);
				}
			}
		});
		mVSongList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					// Log.v("已经停止：SCROLL_STATE_IDLE");
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					// Log.v("开始滚动：SCROLL_STATE_FLING");
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					// Log.v("正在滚动：SCROLL_STATE_TOUCH_SCROLL");
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				mVisibleLastidx = firstVisibleItem + visibleItemCount;

				// 这儿会反复执行！！！！！！！！！！！！！， 会反复去取数据， 当播放的时候是这样，这个问题得解决
				Logger.LOGD("", "onScroll:" + firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount);
				// 本地模式不涉及翻页
				if (mIService != null && mIService.getCurRunMode() == Configer.RunMode.MODE_LOCAL)
					return;

				// 判断是否滚到最后一行, 最后5行, 总数至少要大于一页(13)
				if (firstVisibleItem + visibleItemCount + 5 >= totalItemCount && totalItemCount >= 13) {
					Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_GET_NEWPG, null);
				}
			}
		});
	}

	int mVisibleLastidx = 0;



	public int getCurPos() {
		if (mIService == null)
			return -1;
		return mIService.getCurPos();
	}



	private BatteryRcvBindView batteryReceiver;
	
	
	BatteryImgView mBatteryView;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mu_player);

		initView();

		//startMyService();

		//bindMySvr();
	}

	
	
	//>>>>>>>>>>>>>>>>>>>>>>>>>> Service >>>>>>>>>>>>>>>
	private IMUService mIService = null;

	//bind的目的是与service通信，获取service中的数据, 
	//先绑定，通信之后才知道service中到底有没有数据。
	
	private void bindMySvr() {
		if(mIService == null && mConnection == null){
			mConnection= new ServiceConnection() {
				public void onServiceDisconnected(ComponentName name) {
					 Logger.LOGD("","===============DisConnection");
					 System.out.println("DisConnection!!!");
					mIService = null;
					mConnection = null;
				}
	 
				public void onServiceConnected(ComponentName name, IBinder service) {
					// TODO Auto-generated method stub
					Logger.LOGD("","=============Connection");
					mIService = (IMUService) ((MyBind)service);
					if(mIService == null){
						return;
					}	
					
					startMyService();
				}
			};
			
			Intent it = buildSrvIntent();
 			bindService(it, mConnection, BIND_AUTO_CREATE);
		}
	}

	private void unbindMySvr() {
		Logger.LOGD("", "unbindMySvr?????????????");
		if (mIService != null){
			unbindService(mConnection);
			
			mIService = null ;
			mConnection = null;
		}
	}

	ServiceConnection mConnection;

	
	
	Intent buildSrvIntent(){
		Intent it = getIntent();
		boolean longPress = it.getBooleanExtra("longpress", false);
		String cataName = it.getStringExtra("curCata");
		String localpath = it.getStringExtra("localpath");
		int type = it.getIntExtra("type", 0);
		//cataName = cataName == null ? "" : cataName;
		//localpath = localpath == null ? "" : localpath;

		// 根据参数 localpath 来判断模式
		int runmode = Configer.RunMode.MODE_NETWORK;
		if (localpath/*.length() > 0*/ != null) {
			runmode = Configer.RunMode.MODE_LOCAL;
		}
 
		// 获取分类id，这个决定数据，直接传入服务
		String cataid = it.getStringExtra("cataId") ;
//		if (runmode==Configer.RunMode.MODE_NETWORK && cataid == null) {
//			Toast.makeText(this, "invalid cata", Toast.LENGTH_SHORT).show();
//			finish();
//			return null;
//		}			
 
//		// 设置分类名
//		if (mTvCate.getText() != cataName) {
//			if (!cataName.isEmpty()) {
//				mTvCate.setText(cataName);
//			}
//		}

		// 启动服务器
		it = new Intent(MuPlayer.this, PlayerService.class);
		it.putExtra("runmode", runmode);
		it.putExtra("cataId", cataid);
		it.putExtra("cataName", cataName);
		it.putExtra("type", type);
		it.putExtra("longpress", longPress);
		if (runmode == Configer.RunMode.MODE_LOCAL)
			it.putExtra("localpath", localpath);
		return it;
 
	}
	
	//startService的目的是传参到service，并让其执行相应的指令。
	void startMyService(){
		Logger.LOGD("", "===startMyService====");

		Intent it = buildSrvIntent();
		if(it!=null){
			startService(it);
		}
	}
	//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	 
	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Receiver 控制   >>>>>>>>>>>>>>>>>>>>>>
	private void registerMyReceiver() {
		//电量receiver
		if(batteryReceiver == null){
			batteryReceiver = new BatteryRcvBindView(mBatteryView);
			registerReceiver(batteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED")); 
		}

		// 定义和注册广播接收器
		if(playerReceiver == null){
			Logger.LOGD("","=========playerReceiver");
			playerReceiver = new PlayerReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Configer.Action.ACT_UPDATE_ACTION);
			filter.addAction(Configer.Action.ACT_MUSIC_CURRENT);
			filter.addAction(Configer.Action.ACT_MUSIC_DURATION);
			filter.addAction(Configer.Action.ACT_UPDATE_PlAYLIST);
			filter.addAction(Configer.Action.ACT_CUR_FINISHED);
			filter.addAction(Configer.Action.ACT_STATUS_RESET);
			filter.addAction(Configer.Action.ACT_NEW_CATE_DATA);
			
			filter.addAction(Configer.Action.ACT_SHOW_LOADING);
			filter.addAction(Configer.Action.ACT_HIDE_LOADING);
			
			
			//锁屏
		    filter.addAction(Intent.ACTION_SCREEN_ON);  
		    filter.addAction(Intent.ACTION_SCREEN_OFF);  
			
			registerReceiver(playerReceiver, filter);	
		}
	}
	
	void unregisterMyRcver(){
		if(batteryReceiver != null){
			unregisterReceiver(batteryReceiver);
			batteryReceiver = null;
		}
		if (playerReceiver != null){
			unregisterReceiver(playerReceiver);
			playerReceiver = null;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Logger.LOGD("", "============== onresume=============");
		
		if(mLoading == null)
			mLoading = CustomProgressDialog.createDialog(this);
		if(mExitDlg == null){
			mExitDlg = ExitDialog.createDialog(this);
			mExitDlg.setMyOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch(v.getId()){
					case R.id.btn_close_music:
						Logger.LOGD("", "============ ok");
						Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, new String[]{"MSG", String.format("%d", Configer.PlayerMsg.STOP_MSG)});	
						mExitDlg.dismiss();
						MuPlayer.this.finish();
						return;

					case R.id.btn_bg_music:
						Logger.LOGD("", "=========== cancel");
						mExitDlg.dismiss();
						MuPlayer.this.finish();
						return;
					}
				}
			});
		}
		bindMySvr();
		
		
		registerMyReceiver();
		
		
		Configer.sendNotice(this, Configer.Action.ACT_MUPLAYER_LAUCHED, null);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Logger.LOGD("", "==============onstop========");
		updateListener.destory();
		animator.cancel();
		
		if(mLoading != null){
			mLoading.dismiss();
			mLoading = null;
		}
		
		if(mExitDlg != null){
			mExitDlg.dismiss();
			mExitDlg = null;
		}

		unregisterMyRcver();
		
		unbindMySvr();
	}
	
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	// TODO Auto-generated method stub
    	if(event.getAction() == KeyEvent.ACTION_DOWN){
    		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
    			mExitDlg.show();
    			return super.dispatchKeyEvent(event);
    		}
    	}
    	return super.dispatchKeyEvent(event);
    }
	
	private void updateProgess(){
		int currentTime = mIService.getCurTm();
		mTvCurTm.setText(MediaUtil.formatTime(currentTime));
		mSeekBar.setProgress(currentTime);
	}
	
	private void updatePlayStatus(){
		if(mAdapter == null)
			return; 
		
		// 播放操作部分的界面更新部分放在这儿
		if (mIService.getCurPos() >= 0) {
			mTvmuName.setText(mIService.getCurTitle());
		}
		mTvDur.setText(MediaUtil.formatTime(mIService.getDuration()));

		int dur = mIService.getDuration() <= 0 ? 300 : mIService.getDuration();
		mSeekBar.setMax(dur);
		mTvDur.setText(MediaUtil.formatTime(dur));

		mVSongList.setSelection(mIService.getCurPos());
		mAdapter.notifyDataSetChanged(); // 为了让选中项文字颜色改变

		if (mIService.getCurPos() >= mVisibleLastidx) {
			mVSongList.setSelection(mIService.getCurPos()); // 为了使选中项条目滚动到可见区域,
															// 但是总是会出现在当期可视区域的第一个，就意味着点击一下，选择中的调到第一个了。
			mVSongList.requestFocus();
		}
		
		
		if (mIService.isPlaying()) {
			mPlay.setBackgroundResource(R.drawable.mu_pausebtn_selector);
			animCtrl(AnimAct.ANIM_PLAY);
			
		} else {
			mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
			animCtrl(AnimAct.ANIM_PAUSE);
		}
	}
	
	
	/**
	 * 用来接收从service传回来的广播的内部类
	 * 
	 * @author wwj
	 * 
	 */
	public class PlayerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(mIService == null){
				return;
			}
 
 			if (action.equals(Configer.Action.ACT_MUSIC_CURRENT)) {
 				updateProgess();
			} 
			else if (action.equals(Configer.Action.ACT_MUSIC_DURATION)
					|| action.equals(Configer.Action.ACT_UPDATE_ACTION)) {
				updatePlayStatus();
			}
			else if (action.equals(Configer.Action.ACT_UPDATE_PlAYLIST)) {
				if(mIService.getDatas().size()==0)
					return;
				initPlayInfo();
				
				Logger.LOGD("", "========Configer.Action.ACT_UPDATE_PlAYLIST");
				mAdapter.notifyDataSetChanged();
			}
			else if(Intent.ACTION_SCREEN_ON.equals(action)){  
                //mScreenStateListener.onScreenOn();  
				Logger.LOGD("", "-------------Screen--------on");
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){  
                //mScreenStateListener.onScreenOff();
            	Logger.LOGD("", "-------------Screen--------off");
            }  
            else if(action.equals(Configer.Action.ACT_STATUS_RESET)
            		||action.equals(Configer.Action.ACT_CUR_FINISHED)){
            	animCtrl(AnimAct.ANIM_STOP);
            	mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
            	mTvDur.setText("00:00");
            	mTvCurTm.setText("00:00");
            	mSeekBar.setProgress(0);
            	
            }
            else if(action.equals(Configer.Action.ACT_NEW_CATE_DATA)){
				if(mIService.getDatas().size()==0)
					return;
				
				initPlayInfo();
				return;
            }
            else if(action.equals(Configer.Action.ACT_SHOW_LOADING)){
            	if(mLoading != null)
            		mLoading.show();
            }
            else if(action.equals(Configer.Action.ACT_HIDE_LOADING)){
            	if(mLoading != null)
            		mLoading.hide();
            }
		}
	}	
	
	
	private void initPlayInfo(){
		//设置初始界面信息
		mAdapter = new SongListAdapter(MuPlayer.this, mIService.getDatas());
		mVSongList.setAdapter(mAdapter);

		if (mIService.isPlaying()) {
			animCtrl(AnimAct.ANIM_PLAY);
			mPlay.setBackgroundResource(R.drawable.mu_pausebtn_selector);
		} else {
			mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
		}
		mTvmuName.setText(mIService.getCurTitle());
		mTvCurTm.setText(MediaUtil.formatTime(mIService.getCurTm()));
		mTvDur.setText(MediaUtil.formatTime(mIService.getDuration()));
		mSeekBar.setMax(mIService.getDuration());
		mSeekBar.setProgress(mIService.getCurTm());
		mTvCate.setText(mIService.getCurCateName());
	}
	//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

 
	

	public enum AnimAct {
		ANIM_PLAY, ANIM_PAUSE, ANIM_REPLAY, ANIM_STOP
	};
	// flag:
	private void animCtrl(AnimAct flag) {
 
		switch (flag) {
		case ANIM_PLAY:// 开始
			// 如果已经暂停，是继续播放
			if (updateListener.isPause)
				updateListener.play();
			else
				// 否则就是从头开始播放
				animator.start();
			Logger.LOGD("", "#### animCtrl: play ###");
			break;
		case ANIM_PAUSE:// 暂停
			updateListener.pause();
			Logger.LOGD("", "#### animCtrl: pause ###");
			break;
		case ANIM_REPLAY:
			updateListener.play();
			animator.end();
			animator.start();
			updateListener.pause();
			Logger.LOGD("", "#### animCtrl: replay ###");
			break;
		case ANIM_STOP:
			animator.end();
			Logger.LOGD("", "#### animCtrl: stop ###");
			break;
		}
 
	}

	// ///////////////////////////////////////////////////////////
	class CtrlBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_prev:
				previous_music();
				break;
			case R.id.btn_ok:
				String[] param = null;
				if (mIService.isPlaying()) {
					mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
					//animCtrl(AnimAct.ANIM_PAUSE);
					param = new String[]{"MSG", String.format("%d", Configer.PlayerMsg.PAUSE_MSG)};
				} else {
					mPlay.setBackgroundResource(R.drawable.mu_pausebtn_selector);
					//animCtrl(AnimAct.ANIM_PLAY);
					param = new String[]{"MSG", String.format("%d", Configer.PlayerMsg.CONTINUE_MSG)};
				}
				Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, param);
				break;
			case R.id.btn_next:
				next_music();
				break;
			case R.id.btn_fav:
				break;
			}
		}
	}

	/**
	 * 实现监听Seekbar的类
	 * 
	 * @author wwj
	 * 
	 */
	private class SeekBarChangeListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				audioTrackChange(progress); // 用户控制进度的改变
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) { }

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) { }
	}

	/**
	 * 播放进度改变
	 * 
	 * @param progress
	 */
	public void audioTrackChange(int progress) {
		String[] args = { "MSG", String.format("%d", Configer.PlayerMsg.PROGRESS_CHANGE), "progress", String.format("%d", progress) };
		Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);
	}

	/**
	 * 播放音乐
	 */
	public void play(int myPos) {
		// 开始播放的时候为顺序播放
		// repeat_none();
		mIService.setCurPos(myPos);

		String[] args = { "MSG", String.format("%d", Configer.PlayerMsg.PLAY_MSG) };
		Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);
	}

	/**
	 * 上一首
	 */
	public void previous_music() {
		mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);

		animCtrl(AnimAct.ANIM_REPLAY);

		String[] args = { "MSG", String.format("%d", Configer.PlayerMsg.PRIVIOUS_MSG) };
		Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);
	}

	/**
	 * 下一首
	 */
	public void next_music() {
		mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);

		animCtrl(AnimAct.ANIM_REPLAY);

		String[] args = { "MSG", String.format("%d", Configer.PlayerMsg.NEXT_MSG) };
		Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);
	}



	ObjectAnimator animator;
	MyAnimatorUpdateListener updateListener = new MyAnimatorUpdateListener();

	class MyAnimatorUpdateListener implements AnimatorUpdateListener {
		/**
		 * 暂停状态
		 */
		private boolean isPause = false;
		/**
		 * 是否已经暂停，如果一已经暂停，那么就不需要再次设置停止的一些事件和监听器了
		 */
		private boolean isPaused = false;
		/**
		 * 当前的动画的播放位置
		 */
		private float fraction = 0.0f;
		/**
		 * 当前动画的播放运行时间
		 */
		private long mCurrentPlayTime = 0l;

		/**
		 * 是否是暂停状态
		 * 
		 * @return
		 */
		public boolean isPause() {
			return isPause;
		}

		/**
		 * 停止方法，只是设置标志位，剩余的工作会根据状态位置在onAnimationUpdate进行操作
		 */
		public void pause() {
			isPause = true;
		}

		public void play() {
			isPause = false;
			isPaused = false;

			if (mCd != null) {
				mCd.cancel();
				mCd = null;
			}
		}

		CountDownTimer mCd;
		public void destory(){
			if(mCd != null){
				mCd.cancel();
				mCd = null;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			/**
			 * 如果是暂停则将状态保持下来，并每个刷新动画的时间了；来设置当前时间，让动画
			 * 在时间上处于暂停状态，同时要设置一个静止的时间加速器，来保证动画不会抖动
			 */
			if (isPause) {
				if (!isPaused) {
					mCurrentPlayTime = animation.getCurrentPlayTime();
					fraction = animation.getAnimatedFraction();
					Interpolator it = new Interpolator() {
						@Override
						public float getInterpolation(float input) {
							return fraction;
						}
					};
					animation.setInterpolator(it);
					isPaused = true;
				}

				// rocking: avoid high-cpu
				if (mCd != null) {
					return;
				}

				Logger.LOGD("", "================ countdowntimer: getFrameDelay=" + ValueAnimator.getFrameDelay());
				// 每隔动画播放的时间，我们都会将播放时间往回调整，以便重新播放的时候接着使用这个时间,同时也为了让整个动画不结束
				long tm = ValueAnimator.getFrameDelay(); // 10ms, high-cpu

				// rocking: avoid high-cpu
				tm = 1000; // 0.5s
				mCd = new CountDownTimer(tm, tm) {
					@Override
					public void onTick(long millisUntilFinished) {
					}

					@Override
					public void onFinish() {
						if(mCd != null){
							mCd.cancel();
							mCd = null;
						}
						animator.setCurrentPlayTime(mCurrentPlayTime); // high-cpu
					}
				}.start();
			} else {
				// 将时间拦截器恢复成线性的，如果您有自己的，也可以在这里进行恢复
				animation.setInterpolator(null);
			}
		}
	}
}
