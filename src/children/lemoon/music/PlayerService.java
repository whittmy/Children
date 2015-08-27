package children.lemoon.music;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ResponseHandler;

 
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import children.lemoon.Configer;
import children.lemoon.Configer.RunMode;
import children.lemoon.R;
import children.lemoon.music.lrc.LrcContent;
import children.lemoon.music.lrc.LrcProcess;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.reqbased.BaseReqService;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.entry.ResponsePager;
import children.lemoon.reqbased.utils.DeviceUtil;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.utils.Logger;

 
/***
 * 2013/5/25
 * @author rocking
 *  播放控制等操作 全部通过广播进行
 * 音乐播放服务
 */
@SuppressLint("NewApi")
public class PlayerService extends BaseReqService {
	private final String TAG = "PlayerService";
 
	
	private MediaPlayer mediaPlayer; // 媒体播放器对象
	private String path; 			// 音乐文件路径
	private int msg;				//播放信息
	private boolean isPlaying; 		// 暂停状态
	
	private int mCurrent = 0; 		// 记录当前正在播放的音乐
	private LinkedList<PlayItemEntity> mData = new LinkedList<PlayItemEntity>();
	int mCurPg = 0;
	int mpgSize = 20;
	int mTotalPage =9999;
	
	int mRunMode = Configer.RunMode.MODE_NETWORK; 
	String mCataId = null; //用于网络
	String mCataName = null; //用于本地判断类别是否改变，不可为空
	int mReqType = 0;
	
	private int mPlayMode = Configer.PlayMode.MODE_REPEATALL;     //Configer.PlayMode.MODE_LIST;			//播放状态，默认为顺序播放
	private MyReceiver myReceiver;	//自定义广播接收器
	private int currentTime;		//当前播放进度
	private int duration;			//播放长度
	private LrcProcess mLrcProcess;	//歌词处理
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象
	private int index = 0;			//歌词检索值
	
	//实现activity访问service数据
    private MyBind myBind=new MyBind(); 
 
    
    
	static private final int PREBUFFER_SIZE= 4*1024*1024;
	static private final String CACHE_PATH = "/ProxyBuffer/files";
	private String id=null;
	

	
	/**
	 * handler用来接收消息，来发送广播更新播放时间
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			//first clear
			mHandler.removeMessages(msg.what);
			
			switch(msg.what){
			case 1:
				if(mediaPlayer != null && mediaPlayer.isPlaying()) {
					currentTime = (int)mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
					Configer.sendNotice(PlayerService.this, Configer.Action.ACT_MUSIC_CURRENT, null);
					mHandler.sendEmptyMessageDelayed(1, 1000);
				}
				break;
			case Configer.PlayerMsg.PLAY_MSG: //直接播放音乐
				Log.e(TAG, "Service handleMsg: PLAY_MSG, mCurrent:"+mCurrent);
				path = mData.get(mCurrent).getDownUrl();
				play(0);
				break;
			case Configer.PlayerMsg.PAUSE_MSG://暂停
				Log.e(TAG, "Service handleMsg: PAUSE_MSG, mCurrent:"+mCurrent);
				pause();
				break;
			case Configer.PlayerMsg.STOP_MSG://停止
				Log.e(TAG, "Service handleMsg: STOP_MSG, mCurrent:"+mCurrent);
				stop();
				break;
			case Configer.PlayerMsg.CONTINUE_MSG://继续播放
				Log.e(TAG, "Service handleMsg: CONTINUE_MSG, mCurrent:"+mCurrent);
				resume();	
				break;
			case Configer.PlayerMsg.PRIVIOUS_MSG:	//上一首
				Log.e(TAG, "Service handleMsg: PRIVIOUS_MSG, mCurrent:"+mCurrent);
				previous();
				break;
			case Configer.PlayerMsg.NEXT_MSG://下一首
				Log.e(TAG, "Service handleMsg: NEXT_MSG, mCurrent:"+mCurrent);
				next();
				break;
			case Configer.PlayerMsg.PROGRESS_CHANGE://进度更新
				Log.e(TAG, "Service handleMsg: PROGRESS_CHANGE, mCurrent:"+mCurrent);
				currentTime = msg.arg1;
				play(currentTime);
				break;	
			case Configer.PlayerMsg.TOGGLEPAUSE_MSG:
				if(isPlaying){
					pause();
				}
				else{
					resume();
				}
				break;
			}
		};
	};
 
	private void registerMyRcv(){
		Log.e(TAG, "Service registerMyRcv");
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Configer.Action.SVR_CTL_ACTION);
		filter.addAction(Configer.Action.SVR_SHOW_LRC);
		filter.addAction(Configer.Action.SVR_GET_NEWPG);
		registerReceiver(myReceiver, filter);
	}
	
	private void unregisterMyRcv(){
		Log.e(TAG, "Service unregisterMyRcv");
		unregisterReceiver(myReceiver);
	}
	
 
	
	AudioManager mAudioManager;
	//RemoteControlClient mRemoteControlClient;
	 private ComponentName mRemoteControlResponder;
 	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "Service onCreate");
		
		mediaPlayer = new MediaPlayer();
		 
		/**
		 * 设置音乐播放完成时的监听器
		 */
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.e(TAG, "Service MediaPlayer onCompletion!");
				Configer.sendNotice(PlayerService.this, Configer.Action.ACT_CUR_FINISHED, null);

				//先清除 当前播放时间的反复刷新机制。
				mHandler.removeMessages(1);
				
				//doNext();  //这个不翻页，所以要改成下面的
				if(!mbUserStop){
					next();
				}
			}
		});
		
		mediaPlayer.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				
				
				mediaPlayer.reset();
				
				return false;
			}
		});
		//=============================
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mRemoteControlResponder = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);

		registerMyRcv();
		
		mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
	}
 	
	// bindService会调用该函数
	//这里，我们通过bindService，方便activity获取service数据。
	@Override
	public IBinder onBind(Intent arg0) {
		Log.e(TAG, "Service onBind");
		return myBind;
	}

	
	boolean bNetFirst = true;
	// 每次startService走这步，(如果之前未运行，则先oncreate，否则直接该函数)
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent == null)
			return;

		
		if(intent.getBooleanExtra("longpress", false)){
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_NEW_CATE_DATA, null);
			return;
		}
 
		Log.e(TAG, "Service onStart, mData.size="+mData.size());

		String localpath = null;
		mReqType = intent.getIntExtra("type", 0);
		int mode = intent.getIntExtra("runmode", -1);
		String cataid = intent.getStringExtra("cataId");  //本想这用于本地的也用cataid，发现无法使用，
		String cataName = intent.getStringExtra("cataName");

		if(mode != -1){
			if(mode==Configer.RunMode.MODE_LOCAL && cataName!=null){
				localpath = intent.getStringExtra("localpath");
			}
			
			if(mCataName!=null&& cataName!=null){
				if(mRunMode == Configer.RunMode.MODE_LOCAL){
					if(mCataName.equals(cataName) && mRunMode == mode){
						Configer.sendNotice(PlayerService.this, Configer.Action.ACT_NEW_CATE_DATA, null);
						return;
					}
				}
				else{
					if(mCataId!=null && cataid!=null){
						if(mCataName.equals(cataName) && mCataId.equals(cataid) && mRunMode == mode){
							Configer.sendNotice(PlayerService.this, Configer.Action.ACT_NEW_CATE_DATA, null);
							return;
						}
					}
				}

			}

			//如果模式不一致，则先清除数据
			//若 分类不一样，则也清除数据
			if(mRunMode != mode || cataName != mCataName || cataid!=mCataId){
				mData.clear();
				mCurrent = 0;
				mCurPg = 0;
				bNetFirst = true;
				
				mHandler.sendEmptyMessage(Configer.PlayerMsg.STOP_MSG);
			}
 
			
			
			mCataName = cataName;
			mCataId = cataid;
			mRunMode = mode;
		}
 
		
		if(mRunMode == Configer.RunMode.MODE_LOCAL){
			//本地
			File f = new File(localpath);
			if(!f.exists()){
				Toast.makeText(this, localpath+ " 不存在", Toast.LENGTH_SHORT).show();
				return;
			}
			
			String[] l = f.list(new FileNameSelector());
			if(l == null){
				Toast.makeText(this, localpath+ " 目录下没有内容", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(!localpath.endsWith("/"))
				localpath += "/";
			
			for(String file : l){
				//Log.e("", localpath+file);
				PlayItemEntity pie = new PlayItemEntity();
				pie.setName(file);
				pie.setDownUrl(localpath+file);
				mData.add(pie);
			}
			
			//播放
			if(mData.size() >0){
				mHandler.sendEmptyMessage(Configer.PlayerMsg.PLAY_MSG);
				Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_PlAYLIST, null);
			}
 
		}
		else if(mRunMode == Configer.RunMode.MODE_NETWORK){
			//网络
			if(mData.isEmpty()){
				queryPlayList(mCurPg+1);
				return;
			}
		}

		super.onStart(intent, startId);
	}
	
	
	public class FileNameSelector implements FilenameFilter{
		@Override
		public boolean accept(File arg0, String arg1) {
			// TODO Auto-generated method stub
			arg1 = arg1.toLowerCase();
			if(arg1.endsWith("mp3")
				|| arg1.endsWith("wma")
				|| arg1.endsWith("wav")
				|| arg1.endsWith("mod")
				|| arg1.endsWith("cd")
				|| arg1.endsWith("md")
				|| arg1.endsWith("asf")
				|| arg1.endsWith("aac")
				|| arg1.endsWith("mp3pro")
				|| arg1.endsWith("vqf")
				|| arg1.endsWith("flac")
				|| arg1.endsWith("ape")
				|| arg1.endsWith("mid")
				|| arg1.endsWith("ogg")
				|| arg1.endsWith("m4a")
				|| arg1.endsWith("aac+")
				|| arg1.endsWith("aiff")
				|| arg1.endsWith("vqf"))
				return true;
			return false;
		}
	}
	
	
	@Override
	public void onDestroy() {
		Log.e(TAG, "Service onDestroy");
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		mHandler.removeCallbacks(mRunnable);
		mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
		unregisterMyRcv();
	}


	/*********************************************************************************
	 * 						初始化歌词配置
	 ***********************************************************************************/
	public void initLrc(){
		Log.e(TAG, "Service initLrc");
		mHandler.removeCallbacks(mRunnable);
		mLrcProcess = new LrcProcess();
		//读取歌词文件
		//mLrcProcess.readLRC(mp3Infos.get(mCurrent).getUrl());

		mLrcProcess.readLRC(mData.get(mCurrent).getDownUrl());

		//传回处理后的歌词文件
		lrcList = mLrcProcess.getLrcList();
		MuPlayer.mlrcView.setmLrcList(lrcList);
		//切换带动画显示歌词
		MuPlayer.mlrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha_z));
		mHandler.post(mRunnable);
	}
	
	//每100毫秒刷新一次 歌词View
	Runnable mRunnable = new Runnable() {		
		@Override
		public void run() {
			// rocking 由于我们这版本不需要支持字幕，所以先屏蔽掉
//			MuPlayer.mlrcView.setIndex(lrcIndex());
//			MuPlayer.mlrcView.invalidate();
//			mHandler.postDelayed(mRunnable, 10000);			// 歌词刷新频率，  原来100
		}
	};
	
	/**
	 * 根据时间获取歌词显示的索引值
	 * @return
	 */
	public int lrcIndex() {
		Log.e(TAG, "Service lrcIndex");
		if(mediaPlayer.isPlaying()) {
			currentTime = (int)mediaPlayer.getCurrentPosition();
			duration = (int)mediaPlayer.getDuration();
		}
		if(currentTime < duration) {
			for (int i = 0; i < lrcList.size(); i++) {
				if (i < lrcList.size() - 1) {
					if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
						index = i;
					}
					if (currentTime > lrcList.get(i).getLrcTime()
							&& currentTime < lrcList.get(i + 1).getLrcTime()) {
						index = i;
					}
				}
				if (i == lrcList.size() - 1
						&& currentTime > lrcList.get(i).getLrcTime()) {
					index = i;
				}
			}
		}
		return index;
	}
	
	
	
	/***********************************************************
	  *  						播放控制
	 * ********************************************************/
	/**
	 * 获取随机位置
	 * @param end
	 * @return
	 */
	protected int getRandomIndex(int end) {
		Log.e(TAG, "Service getRandomIndex");
		int index = (int) (Math.random() * end);
		return index;
	}
	 
	
	/**
	 * 播放音乐
	 * 
	 * @param position: 播放位置
	 */
	private void play(int currentTime) {
		Log.e(TAG, "Service play arg1:"+currentTime+", mCurrent="+mCurrent+",path="+path);
		try {
			if(currentTime>5000 && mediaPlayer!=null && mediaPlayer.getDuration()>0){
				mediaPlayer.seekTo(currentTime);
				mediaPlayer.start();
				return;
			}
			
			resetStatus();
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_SHOW_LOADING, null);

			//initLrc();
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			
			//path = "http://quku.cn010w.com/qkca1116sp/upload_quku8/20078311111257.mp3";
			mediaPlayer.setDataSource(path);
			
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new PreparedListener());// 注册一个监听器

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	
	/**
	 * 暂停音乐
	 */
	private void pause() {
		Log.e(TAG, "Service pause, mCurrent="+mCurrent);
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			isPlaying = false;
			mbUserStop = true;
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_ACTION, null);
		}
	}
	/**
	 * 停止音乐
	 */
	private boolean mbUserStop = false;
	private void stop() {
		Log.e(TAG, "Service stop, mCurrent="+mCurrent);
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			//我屏蔽下面了，另外注意stop后，会执行 onComplet回调，会跳转到下一曲，所以要区分认为终止还是自然终止
//			try {
//				mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数 
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			isPlaying = false;
			mbUserStop = true;
			
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_ACTION, null);
		}
	}

	
	private void resume() {
		Log.e(TAG, "Service resume, mCurrent="+mCurrent);
		if (!isPlaying && mediaPlayer!=null) {
			mediaPlayer.start();
			isPlaying = true;
			mHandler.sendEmptyMessage(1);
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_ACTION, null);
		}
	}

	/**
	 * 上一首 不涉及 播放模式
	 */
	private void previous() {
		Log.e(TAG, "Service previous, mCurrent="+mCurrent);
		//rocking
		mCurrent--;
		if(mCurrent < 0){
			mCurrent = 0;
		}
		path = mData.get(mCurrent).getDownUrl();
		
		
//		Intent sendIntent = new Intent(Configer.Action.ACT_UPDATE_ACTION);
//		//sendIntent.putExtra("mCurrent", mCurrent);
//		// 发送广播，将被Activity组件中的BroadcastReceiver接收到
//		sendBroadcast(sendIntent);
		
		//resetStatus();
		play(0);
	}

	/**
	 * 下一首  注意要涉及播放模式
	 */
	private void next() {
		Log.e(TAG, "Service next, mCurrent="+mCurrent);
		//判断是否要取数据
		int pg = mCurrent/mpgSize;
		int idx = mCurrent%mpgSize;
		if(mRunMode==Configer.RunMode.MODE_NETWORK && pg==(mCurPg-1) && idx >10){
			Log.e("", "begin to load page:"+(mCurPg+1)+", mCurrent="+mCurrent+", mcurpage="+mCurPg);
			queryPlayList(mCurPg+1);
		}

		doNext();
		
//		mCurrent++;
//		if (mCurrent >= mData.size()) {
//			mCurrent = 0;
//		}
//		path = mData.get(mCurrent).getDownUrl();
//		
//		Intent sendIntent = new Intent(Configer.Action.ACT_UPDATE_ACTION);
//		sendIntent.putExtra("mCurrent", mCurrent);
//		// 发送广播，将被Activity组件中的BroadcastReceiver接收到
//		sendBroadcast(sendIntent);
//		play(0);
	}

	//跳下一曲，考虑 播放模式
	private void doNext(){
		Log.e(TAG, "Service doNext, mCurrent="+mCurrent);
		int rt = mCurrent;
		
		switch(mRunMode){
		case Configer.RunMode.MODE_LOCAL:
			if (mPlayMode == Configer.PlayMode.MODE_REPEAT1) { // 单曲循环
				rt = -2;
			} 
			else if (mPlayMode == Configer.PlayMode.MODE_REPEATALL) { // 全部循环
				rt++;
				if(rt >= mData.size()) {	//变为第一首的位置继续播放
					rt = 0;
				}
			} 
			else if (mPlayMode == Configer.PlayMode.MODE_LIST) { // 顺序播放
				rt++;	//下一首位置
				if (rt >= mData.size()) {
					rt = -1;
				}
			} 
			else if(mPlayMode == Configer.PlayMode.MODE_RANDOM) {	//随机播放
				rt = getRandomIndex(mData.size() - 1);
			}			
			break;
		case Configer.RunMode.MODE_NETWORK:
			rt ++;
			if(rt >= mData.size()) {	//变为第一首的位置继续播放, 即便是是网络，也有尽头
				rt = 0;
			}			
			break;
		}
 

		if(rt == -1){ 
			mediaPlayer.seekTo(0);
			mCurrent = 0;    

			resetStatus();
		}
		else if(rt == -2){
			mediaPlayer.start();
		}
		else{
			mCurrent = rt;
//			Intent sendIntent = new Intent(Configer.Action.ACT_UPDATE_ACTION);
//			//sendIntent.putExtra("mCurrent", mCurrent);
//			// 发送广播，将被Activity组件中的BroadcastReceiver接收到
//			sendBroadcast(sendIntent);
			
			//resetStatus();
			
			//path = mp3Infos.get(mCurrent).getUrl();
			path = mData.get(mCurrent).getDownUrl();
			play(0);
		}
	}
	

	void resetStatus(){
		isPlaying = false ; 	 

		currentTime = 0;		//当前播放进度
		duration = 0;			//播放长度

		Configer.sendNotice(PlayerService.this, Configer.Action.ACT_STATUS_RESET, null);
	}
	
	/**
	 * 
	 * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
	 * 
	 */
	private final class PreparedListener implements OnPreparedListener {
		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.e(TAG, "Service onPrepared, mCurrent="+mCurrent);
			mediaPlayer.start(); // 开始播放
			if (currentTime > 0) { // 如果音乐不是从头播放
				mediaPlayer.seekTo(currentTime);
			}
//			Intent intent = new Intent();
//			intent.setAction(Configer.Action.ACT_MUSIC_DURATION);
//			duration = mediaPlayer.getDuration();
//			intent.putExtra("duration", duration);	//通过Intent来传递歌曲的总长度
//			sendBroadcast(intent);
			
			isPlaying = true;
			mbUserStop = false;
			duration = (int)mediaPlayer.getDuration();
			Configer.sendNotice(PlayerService.this, Configer.Action.SVR_CTL_ACTION, new String[]{"MSG", String.format("%d", Configer.PlayerMsg.PLAYING_MSG)});	//触发更新播放中的进度更新的持续刷新机制。
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_MUSIC_DURATION, null);
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_HIDE_LOADING, null);
		}
	}

	//接受的参数：(1)listPosition, MSG, progress; (2)control; (3)
	
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Configer.Action.SVR_CTL_ACTION)){
				//1. 处理播放控制消息  耗时操作已移植handler 
				msg = Integer.valueOf(intent.getStringExtra("MSG")) ;			//播放信息
				//           int cur = intent.getIntExtra("listPosition", -1);	//当前播放歌曲的在mp3Infos的位置
				if(msg!=-1 && mCurrent!=-1 && mCurrent<mData.size()){
					//  mCurrent = cur;
					path = mData.get(mCurrent).getDownUrl();
					switch(msg){
					case Configer.PlayerMsg.TOGGLEPAUSE_MSG:
						mHandler.sendEmptyMessage(Configer.PlayerMsg.TOGGLEPAUSE_MSG);
						break;
					case Configer.PlayerMsg.PLAY_MSG: //直接播放音乐
						Log.e(TAG, "Service MyReceiver msg:PLAY_MSG mCurrent="+mCurrent);
						mHandler.sendEmptyMessage(Configer.PlayerMsg.PLAY_MSG);
						break;
					case Configer.PlayerMsg.PAUSE_MSG://暂停
						Log.e(TAG, "Service MyReceiver PAUSE_MSG mCurrent="+mCurrent);
						mHandler.sendEmptyMessage(Configer.PlayerMsg.PAUSE_MSG);
						break;
					case Configer.PlayerMsg.STOP_MSG://停止
						Log.e(TAG, "Service MyReceiver STOP_MSG mCurrent="+mCurrent);
						mHandler.sendEmptyMessage(Configer.PlayerMsg.STOP_MSG);
						break;
					case Configer.PlayerMsg.CONTINUE_MSG://继续播放
						Log.e(TAG, "Service MyReceiver CONTINUE_MSG mCurrent="+mCurrent);
						mHandler.sendEmptyMessage(Configer.PlayerMsg.CONTINUE_MSG);
						break;
					case Configer.PlayerMsg.PRIVIOUS_MSG:	//上一首
						Log.e(TAG, "Service MyReceiver PRIVIOUS_MSG mCurrent="+mCurrent);
						mHandler.sendEmptyMessage(Configer.PlayerMsg.PRIVIOUS_MSG);
						break;
					case Configer.PlayerMsg.NEXT_MSG://下一首
						Log.e(TAG, "Service MyReceiver NEXT_MSG mCurrent="+mCurrent);
						mHandler.sendEmptyMessage(Configer.PlayerMsg.NEXT_MSG);
						break;
					case Configer.PlayerMsg.PROGRESS_CHANGE://进度更新
						Log.e(TAG, "Service MyReceiver PROGRESS_CHANGE mCurrent="+mCurrent+",porgress:"+intent.getStringExtra("progress"));
						Message msg = new Message();
						msg.what = Configer.PlayerMsg.PROGRESS_CHANGE;
						msg.arg1 = Integer.valueOf(intent.getStringExtra("progress"));
						mHandler.sendMessage(msg);
						break;															
					case Configer.PlayerMsg.PLAYING_MSG:
						//Log.e(TAG, "Service MyReceiver PLAYING_MSG mCurrent="+mCurrent);
						mHandler.sendEmptyMessage(1);
						break;					
					}
				}
	 
				
				//2. 处理播放模式
				int control = intent.getIntExtra("control", -1);
				if(control != -1){
					switch (control) {
					case 1:
						mPlayMode = Configer.PlayMode.MODE_REPEAT1; // 将播放状态置为1表示：单曲循环
						break;
					case 2:
						mPlayMode = Configer.PlayMode.MODE_REPEATALL;	//将播放状态置为2表示：全部循环
						break;
					case 3:
						mPlayMode = Configer.PlayMode.MODE_LIST;	//将播放状态置为3表示：顺序播放
						break;
					case 4:
						mPlayMode = Configer.PlayMode.MODE_RANDOM;	//将播放状态置为4表示：随机播放
						break;
					}
				}
				
				return;
			}
			else if(action.equals(Configer.Action.SVR_SHOW_LRC)){
				// 3. 歌词
				Log.e(TAG, "Service MyReceiver SVR_SHOW_LRC mCurrent="+mCurrent);
				//mCurrent = intent.getIntExtra("listPosition", -1);
				//initLrc();
				return;
			}
			else if(action.equals(Configer.Action.SVR_GET_NEWPG)){
				//4. 加载新页面
				Log.e(TAG, "Service MyReceiver SVR_GET_NEWPG mCurrent="+mCurrent);
				//boolean bNext = intent.getBooleanExtra("bnextpg", false);
				//if(bNext){
					queryPlayList(mCurPg+1);
					return;
				//}
			}
		}
	}

	
	private boolean queryPlayList(int pgIdx/* , int pgSize */) {
		if(mRunMode == Configer.RunMode.MODE_NETWORK){
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_SHOW_LOADING, null);
			
			Log.e(TAG, "Service queryPlayList pgIdx="+pgIdx);
			HashMap<String, Object> bodyRequest = new HashMap<String, Object>();
			bodyRequest.put("id", mCataId);
			bodyRequest.put("pageindex", pgIdx);
			bodyRequest.put("pagesize", mpgSize);
			bodyRequest.put("type", mReqType);

			HttpManger http = new HttpManger(this, bHandler, this);
			return http.httpRequest(Configer.REQ_AUDIO_PLAYLIST, bodyRequest, false, ResponePList.class, false, false, true);
		}
		else{
			return true;
		}
	}
	
	protected void onPostHandle(int requestType, Object data, boolean status, int paramInt2, Object paramObject2, Object paramObject3) {
		if (data == null) {
			return;
		}

		// 请求成功，并返回游戏列表
		// if (requestType == 31) { }

		ResHeadAndBody rslt = (ResHeadAndBody) data;
		
		// gson 将请求的数据，转为了ResponePList数据类型
		ResponePList plist = (ResponePList) rslt.getBody();
		List<PlayItemEntity> pList = plist.getpList();
		if (pList == null || pList.isEmpty()){
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_HIDE_LOADING, null);
			
			if(mData.size() == 0){
				Toast.makeText(PlayerService.this, "获取数据失败，请重试！", Toast.LENGTH_SHORT).show();
			}
			else{
				//未必最后页
			}
			return;
		}
		
		ResponsePager pg = (ResponsePager)rslt.getPage();
		if(pg != null){
			if(mTotalPage == 0){
				mTotalPage = pg.getPageCount();
			}
			mCurPg = pg.getPageIndex();
		}
		
		mData.addAll(plist.getpList());
		//mCurPg++;
		
		Log.e(TAG, "Service onPostHandle success, curpg:"+mCurPg);
		
		if(bNetFirst){
			mHandler.sendEmptyMessage(Configer.PlayerMsg.PLAY_MSG);
			bNetFirst = false;
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_NEW_CATE_DATA, null);
		}
		Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_PlAYLIST, null);
		Configer.sendNotice(PlayerService.this, Configer.Action.ACT_HIDE_LOADING, null);
	};
	
	
	
	
   public class MyBind extends Binder implements IMUService{
    	public int getCurPos(){
    		return mCurrent;
    	}
    	
    	public LinkedList<PlayItemEntity> getDatas(){
    		return mData;
    	}

		@Override
		public int getCurTm() {
			// TODO Auto-generated method stub
			return currentTime;
		}

		@Override
		public int getDuration() {
			// TODO Auto-generated method stub
			return duration;
		}

		@Override
		public boolean isPlaying() {
			// TODO Auto-generated method stub
			return isPlaying;
		}

		@Override
		public String getCurTitle() {
			// TODO Auto-generated method stub
			if(mData!=null && mCurrent<mData.size())
				return mData.get(mCurrent).getName();
			else
				return "";
		}

		@Override
		public void setCurPos(int pos) {
			// TODO Auto-generated method stub
			if(mData!=null && pos<mData.size() && pos>=0)
				mCurrent = pos;
		}

		@Override
		public int getCurRunMode() {
			// TODO Auto-generated method stub
			return mRunMode;
		}
 
		public String getCurCateName(){
			return mCataName;
		}
    	
    }  
}
