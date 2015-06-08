package children.lemoon.music;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;



import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.lrc.LrcView;
import children.lemoon.music.util.MediaUtil;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.reqbased.utils.OnClickUtil;

public class MuPlayer extends Activity{
	ListView mVSongList;
	SongListAdapter mAdapter;
	
	public static LrcView mlrcView; // 自定义歌词视图, 由service更新
	ImageView mImgDisc; 
	//Animation mAnimDisc;
	//private ObjectAnimator anim;
	
	
	TextView mTvmuName, mTvCate;
	Button mPrev,mPlay,mNext,mFav;
	CtrlBtnClickListener mCtrlListener = new CtrlBtnClickListener();
	
	
	
	private PlayerReceiver playerReceiver;
	public static final String MUSIC_PLAYING = "com.wwj.action.MUSIC_PLAYING"; // 音乐正在播放动作
	public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION"; // 音乐重复播放动作
	public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION";// 音乐随机播放动作
	

	private SeekBar mSeekBar; // 歌曲进度
	private TextView mTvCurTm; // 当前进度消耗的时间
	private TextView mTvDur; // 歌曲时间

	SeekBarChangeListener mSeekBarListener = new SeekBarChangeListener();
 
	private void registerReceiver() {
		//定义和注册广播接收器
		playerReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Configer.Action.ACT_UPDATE_ACTION);
		filter.addAction(Configer.Action.ACT_MUSIC_CURRENT);
		filter.addAction(Configer.Action.ACT_MUSIC_DURATION);
		filter.addAction(Configer.Action.ACT_UPDATE_PlAYLIST);
		filter.addAction(Configer.Action.ACT_CUR_FINISHED);
		
		registerReceiver(playerReceiver, filter);
	}
	
	
	void initView(){
		mTvmuName = (TextView)findViewById(R.id.mu_title);
		mTvCate = (TextView)findViewById(R.id.mu_cata);
		
		mPrev = (Button)findViewById(R.id.btn_prev);
		mPlay = (Button)findViewById(R.id.btn_ok);
		mNext = (Button)findViewById(R.id.btn_next);
		mFav = (Button)findViewById(R.id.btn_fav);
		mPrev.setOnClickListener(mCtrlListener);
		mPlay.setOnClickListener(mCtrlListener);
		mNext.setOnClickListener(mCtrlListener);
		mFav.setOnClickListener(mCtrlListener);
		mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);

		mlrcView = (LrcView)findViewById(R.id.lrcShowView);
		mSeekBar = (SeekBar)findViewById(R.id.seekb);
		mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
		mTvCurTm = (TextView)findViewById(R.id.curtime);
		mTvDur = (TextView)findViewById(R.id.dur);

		//唱片动画
		mImgDisc = (ImageView)findViewById(R.id.mu_disc);  

//		mAnimDisc = AnimationUtils.loadAnimation(this, R.anim.disc);  
//		LinearInterpolator lin = new LinearInterpolator();  
//		mAnimDisc.setInterpolator(lin);  
		
//		anim = ObjectAnimator.ofFloat(mImgDisc, "rotation", 0, 360);
//		anim.setDuration(1000);
//		anim.setRepeatCount(ValueAnimator.INFINITE);
//		anim.setRepeatMode(ObjectAnimator.RESTART);
		
		
		animator = ObjectAnimator.ofFloat(mImgDisc, "rotation", 0, 360);
		animator.setRepeatCount(ValueAnimator.INFINITE);
		animator.setDuration(20000);
		animator.setRepeatMode(ObjectAnimator.RESTART);
		//为了增加
		animator.addUpdateListener(updateListener);
		
		
		//播放列表
		mVSongList = (ListView)findViewById(R.id.mu_songlist);
		mVSongList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Log.e("", "onItemClick");
				
				if(mIService!=null && mIService.getDatas()!=null && arg2<mIService.getDatas().size()){
					mTvmuName.setText(mIService.getDatas().get(arg2).getName());
					play(arg2) ;	
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
		            	//Log.v("正在滚动：SCROLL_STATE_TOUCH_SCROLL");
		                break;
		            }
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				//本地模式不涉及翻页
				if(mIService != null && mIService.getCurRunMode()==Configer.RunMode.MODE_LOCAL)
					return;
				
				//判断是否滚到最后一行, 最后三行
				if (firstVisibleItem + visibleItemCount+3 >= totalItemCount && totalItemCount > 0) {
					Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_GET_NEWPG, null);
				}
			}
		});
		
		

		//stop
		//mImgDisc.clearAnimation();  
	}
	
	private IMUService mIService=null;
	ServiceConnection mConnection=new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
        	//Log.d(TAG,"DisConnection");
            //System.out.println("DisConnection!!!");
        	mIService = null;
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub 
        	//Log.d(TAG,"Connection");
           	mIService = (IMUService)service;

           	mAdapter = new SongListAdapter(MuPlayer.this, mIService.getDatas());
			mVSongList.setAdapter(mAdapter);
			
			
			if(mIService.isPlaying()){
				mPlay.setBackgroundResource(R.drawable.mu_pausebtn_selector);
			}
			else{
				mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
			}
			mTvmuName.setText(mIService.getCurTitle());
        }
    }; 	
	
	public int getCurPos(){
		if(mIService == null)
			return -1;
		return mIService.getCurPos();
	}		
    
    private void bindMySvr(){
		Intent intent=new Intent(MuPlayer.this,PlayerService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE); 
    }
    
    private void unbindMySvr(){
    	 unbindService(mConnection);
    }
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mu_player);

		initView();
		
	}
	
	
	
	//String mCurCataName;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Intent it = getIntent();
		String cataName = it.getStringExtra("curCata");
		String localpath  = it.getStringExtra("localpath");
		cataName = cataName==null?"":cataName;
		localpath = localpath==null?"":localpath;
		
		//根据参数 localpath 来判断模式
		int runmode = Configer.RunMode.MODE_NETWORK;
		if(localpath.length() > 0 ){
			runmode = Configer.RunMode.MODE_LOCAL;
		}
 
		//获取分类id，这个决定数据，直接传入服务
		int cataid = it.getIntExtra("cataId", -1);
		if(cataid == -1){
			Toast.makeText(this, "invalid cata", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		//设置分类名
		if(mTvCate.getText() != cataName){
			if(!cataName.isEmpty()){
				mTvCate.setText(cataName);
			}
		}
 
		
		//启动服务器
		it = new Intent(MuPlayer.this, PlayerService.class);
		it.putExtra("runmode", runmode);
		it.putExtra("cataId", cataid);
		if(runmode == Configer.RunMode.MODE_LOCAL)
			it.putExtra("localpath", localpath);
		startService(it);
		bindMySvr();
	}
	
	///////////////////// Receiver 控制  ///////////////////////
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		registerReceiver();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(playerReceiver != null)
			unregisterReceiver(playerReceiver);
		if(mConnection !=null)
			unbindService(mConnection);
	}
	/////////////////////////////////////////////////////////////

	 class CtrlBtnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			switch(v.getId()){
			case R.id.btn_prev:
				previous_music();
				break;
			case R.id.btn_ok:
				String[] param = new String[2];
				if (mIService.isPlaying()) {
					mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
					//暂停
					updateListener.pause();
					
					param[0] = "MSG"; param[1] = String.format("%d", Configer.PlayerMsg.PAUSE_MSG) ;
				}
				else{
					mPlay.setBackgroundResource(R.drawable.mu_pausebtn_selector);
//					if (mAnimDisc != null) {  
//						mImgDisc.startAnimation(mAnimDisc);  
//					}  
					//如果已经暂停，是继续播放
					if(updateListener.isPause)
						updateListener.play();
					else //否则就是从头开始播放
						animator.start();
					
					param[0] = "MSG"; param[1] = String.format("%d", Configer.PlayerMsg.CONTINUE_MSG) ;
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
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
					if (fromUser) {
						audioTrackChange(progress); // 用户控制进度的改变
					}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		}	

		
		/**
		 * 播放进度改变
		 * @param progress
		 */
		public void audioTrackChange(int progress) {
			String []args = new String[4];
			//args[0] = "listPosition"; args[1] = String.format("%d", mIService.getCurPos());
			args[0] = "MSG"; args[1]= String.format("%d", Configer.PlayerMsg.PROGRESS_CHANGE);
			args[2]="progress"; args[3]=String.format("%d", progress);
			Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);
		}
		

		/**
		 * 播放音乐
		 */
		public void play(int myPos) {
			// 开始播放的时候为顺序播放
			//repeat_none();
			mIService.setCurPos(myPos);
			
			String []args = new String[2];
			//args[0] = "listPosition"; args[1] = String.format("%d", mIService.getCurPos());
			args[0] = "MSG"; args[1]= String.format("%d", Configer.PlayerMsg.PLAY_MSG);
			Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);
		}
		
		
		/**
		 * 上一首
		 */
		public void previous_music() {
			mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
			
			updateListener.play();
			animator.end();
			animator.start();
			updateListener.pause();
			
			String []args = new String[2];
			//args[0] = "listPosition"; args[1] = String.format("%d", mIService.getCurPos());
			args[0] = "MSG"; args[1]= String.format("%d", Configer.PlayerMsg.PRIVIOUS_MSG);
			Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);
		}

		/**
		 * 下一首
		 */
		public void next_music() {
			mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
			
			updateListener.play();
			animator.end();
			animator.start();
			updateListener.pause();
			
			String []args = new String[2];
			//args[0] = "listPosition"; args[1] = String.format("%d", mIService.getCurPos());
			args[0] = "MSG"; args[1]= String.format("%d", Configer.PlayerMsg.NEXT_MSG);
			Configer.sendNotice(MuPlayer.this, Configer.Action.SVR_CTL_ACTION, args);			
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
			if (action.equals(Configer.Action.ACT_MUSIC_CURRENT)) {
				//int currentTime = intent.getIntExtra("currentTime", -1);
				int currentTime = mIService.getCurTm();
				mTvCurTm.setText(MediaUtil.formatTime(currentTime));
				mSeekBar.setProgress(currentTime);
			} else if (action.equals(Configer.Action.ACT_MUSIC_DURATION)) {
				//更新总时间，可以认为是首次播放
				mPlay.setBackgroundResource(R.drawable.mu_pausebtn_selector);
				
				int dur = mIService.getDuration()<=0?300:mIService.getDuration();
				mSeekBar.setMax(dur);
				mTvDur.setText(MediaUtil.formatTime(dur));
				
				
				mVSongList.setSelection(mIService.getCurPos());
				mAdapter.notifyDataSetChanged();	//为了让选中项文字颜色改变
				mVSongList.setSelection(mIService.getCurPos());  //为了使选中项条目滚动到可见区域  
				mVSongList.requestFocus();
				
				//start，启动播放动画
//				if (mAnimDisc != null) {  
//					mImgDisc.startAnimation(mAnimDisc);  
//				}  

				
				//如果已经暂停，是继续播放

				if(updateListener.isPause)
					updateListener.play();
				else //否则就是从头开始播放
					animator.start();	
				
			} else if (action.equals(Configer.Action.ACT_UPDATE_ACTION)) {
					//播放操作部分的界面更新部分放在这儿
					if (mIService.getCurPos() >= 0) {
						mTvmuName.setText(mIService.getCurTitle());
						//musicArtist.setText(mp3Infos.get(listPosition).getArtist());
					}
					mTvDur.setText(MediaUtil.formatTime(mIService.getDuration()));
					
					if(mIService.isPlaying()){
						mPlay.setBackgroundResource(R.drawable.mu_pausebtn_selector);
//						if (mAnimDisc != null) {  
//							//stop
//							mImgDisc.clearAnimation();  
//						}  
						
						updateListener.pause();
					}
					else{
						mPlay.setBackgroundResource(R.drawable.mu_playbtn_selector);
//						if (mAnimDisc != null) {  
//							mImgDisc.startAnimation(mAnimDisc);  
//						}  
						
						//如果已经暂停，是继续播放
						if(updateListener.isPause)
							updateListener.play();
						else{ //否则就是从头开始播放
							animator.start();
						}
					}
			}
			else if(action.equals(Configer.Action.ACT_UPDATE_PlAYLIST)){
				mAdapter.notifyDataSetChanged();
			}
			else if(action.equals(Configer.Action.ACT_CUR_FINISHED)){
				updateListener.play();
				animator.end();
				animator.start();
				updateListener.pause();
			}
		}
	}	
	

	
	
	
	
	//碟片选择控制
//	switch(id){
//	case ID_BTN_PLAY:
//		//如果已经暂停，是继续播放
//		if(updateListener.isPause)updateListener.play();
//		//否则就是从头开始播放
//		else animator.start();
//		break;
//	case ID_BTN_STOP:
//		//如果点击停止，那么我们还需要将暂停的动画重新设置一下
//		updateListener.play();
//		animator.end();
//		break;
//	case ID_BTN_PAUSE:
//		updateListener.pause();
//		break;
//	}
	ObjectAnimator animator;
	MyAnimatorUpdateListener updateListener = new MyAnimatorUpdateListener();
	class MyAnimatorUpdateListener implements AnimatorUpdateListener{
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
		 * @return
		 */
		public boolean isPause(){
			return isPause;
		}
		
		/**
		 * 停止方法，只是设置标志位，剩余的工作会根据状态位置在onAnimationUpdate进行操作
		 */
		public void pause(){
			isPause = true;
		}
		public void play(){
			isPause = false;
			isPaused = false;
		}
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			/**
			 * 如果是暂停则将状态保持下来，并每个刷新动画的时间了；来设置当前时间，让动画
			 * 在时间上处于暂停状态，同时要设置一个静止的时间加速器，来保证动画不会抖动
			 */
			if(isPause){
				if(!isPaused){
					mCurrentPlayTime = animation.getCurrentPlayTime();
					fraction = animation.getAnimatedFraction();
					animation.setInterpolator(new TimeInterpolator() {
						@Override
						public float getInterpolation(float input) {
							return fraction;
						}
					});
					isPaused =  true;
				}
				//每隔动画播放的时间，我们都会将播放时间往回调整，以便重新播放的时候接着使用这个时间,同时也为了让整个动画不结束
				new CountDownTimer(ValueAnimator.getFrameDelay(), ValueAnimator.getFrameDelay()){

					@Override
					public void onTick(long millisUntilFinished) {
					}

					@Override
					public void onFinish() {
						animator.setCurrentPlayTime(mCurrentPlayTime);
					}
				}.start();
			}else{
				//将时间拦截器恢复成线性的，如果您有自己的，也可以在这里进行恢复
				animation.setInterpolator(null);
			}
		}
		
	}
	
	
	
	
}
