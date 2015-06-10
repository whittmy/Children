package children.lemoon.music;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
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
import children.lemoon.reqbased.utils.HttpManger;

 
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
	int mpgSize = 15;
	
	int mRunMode = Configer.RunMode.MODE_NETWORK; 
	int mCataId = -1;
	
	private int mPlayMode = Configer.PlayMode.MODE_LIST;			//播放状态，默认为顺序播放
	private MyReceiver myReceiver;	//自定义广播接收器
	private int currentTime;		//当前播放进度
	private int duration;			//播放长度
	private LrcProcess mLrcProcess;	//歌词处理
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象
	private int index = 0;			//歌词检索值
	
	//实现activity访问service数据
    private MyBind myBind=new MyBind(); 
 
	
	/**
	 * handler用来接收消息，来发送广播更新播放时间
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 1:
				if(mediaPlayer != null) {
					currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
//					Intent intent = new Intent();
//					intent.setAction(Configer.Action.ACT_MUSIC_CURRENT);
//					//intent.putExtra("currentTime", currentTime);
//					sendBroadcast(intent); // 给PlayerActivity发送广播
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
	
 	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "Service onCreate");
		mediaPlayer = new MediaPlayer();
		//mp3Infos = MediaUtil.getMp3Infos(PlayerService.this);

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
				next();
			}
		});

		registerMyRcv();
	}
 	
	// bindService会调用该函数
	//这里，我们通过bindService，方便activity获取service数据。
	@Override
	public IBinder onBind(Intent arg0) {
		Log.e(TAG, "Service onBind");
		return myBind;
	}

	// 每次startService走这步，(如果之前未运行，则先oncreate，否则直接该函数)
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent == null || mData==null)
			return;
		Log.e(TAG, "Service onStart, mData.size="+mData.size());
		
		String localpath = null;

		int mode = intent.getIntExtra("runmode", -1);
		int cataId = intent.getIntExtra("cataId", -1);
		if(mode != -1 && cataId!=-1){
			if(mode==Configer.RunMode.MODE_LOCAL){
				localpath = intent.getStringExtra("localpath");
			}
			
			//如果模式不一致，则先清除数据
			//若 分类不一样，则也清除数据
			if(mRunMode != mode || cataId != mCataId){
				mData.clear();
				mCurPg = 0;
			}
			
			mCataId = cataId;
			mRunMode = mode;
		}
 
		
		if(mRunMode == Configer.RunMode.MODE_LOCAL){
			//本地
			File f = new File(localpath);
			if(!f.exists()){
				Toast.makeText(this, localpath+ " 不存在", Toast.LENGTH_SHORT).show();
				return;
			}
			
			String[] l = f.list();
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
			
			//return;
			
			
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
	
	
	
	@Override
	public void onDestroy() {
		Log.e(TAG, "Service onDestroy");
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		mHandler.removeCallbacks(mRunnable);
		
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
			MuPlayer.mlrcView.setIndex(lrcIndex());
			MuPlayer.mlrcView.invalidate();
			mHandler.postDelayed(mRunnable, 10000);			// 歌词刷新频率，  原来100
		}
	};
	
	/**
	 * 根据时间获取歌词显示的索引值
	 * @return
	 */
	public int lrcIndex() {
		Log.e(TAG, "Service lrcIndex");
		if(mediaPlayer.isPlaying()) {
			currentTime = mediaPlayer.getCurrentPosition();
			duration = mediaPlayer.getDuration();
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
			initLrc();
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setDataSource(path);
			//mediaPlayer.prepare(); // 进行缓冲
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new PreparedListener());// 注册一个监听器
			mHandler.sendEmptyMessage(1);
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
		}
	}
	/**
	 * 停止音乐
	 */
	private void stop() {
		Log.e(TAG, "Service stop, mCurrent="+mCurrent);
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			try {
				mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			isPlaying = false;
		}
	}

	
	private void resume() {
		Log.e(TAG, "Service resume, mCurrent="+mCurrent);
		if (!isPlaying) {
			mediaPlayer.start();
			isPlaying = true;
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
		Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_ACTION, null);
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
		if(pg==(mCurPg-1) && idx >10){
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

			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_ACTION, null);
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
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_ACTION, null);
			
			//path = mp3Infos.get(mCurrent).getUrl();
			path = mData.get(mCurrent).getDownUrl();
			play(0);
		}
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
			duration = mediaPlayer.getDuration();
			Configer.sendNotice(PlayerService.this, Configer.Action.SVR_CTL_ACTION, new String[]{"MSG", String.format("%d", Configer.PlayerMsg.PLAYING_MSG)});	//触发更新播放中的进度更新的持续刷新机制。
			Configer.sendNotice(PlayerService.this, Configer.Action.ACT_MUSIC_DURATION, null);
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
				if(msg!=-1 && mCurrent!=-1){
					//  mCurrent = cur;
					path = mData.get(mCurrent).getDownUrl();
					switch(msg){
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
						Log.e(TAG, "Service MyReceiver PROGRESS_CHANGE mCurrent="+mCurrent);
						Message msg = new Message();
						msg.what = Configer.PlayerMsg.PROGRESS_CHANGE;
						msg.arg1 = intent.getIntExtra("progress", -1);
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
			if(action.equals(Configer.Action.SVR_SHOW_LRC)){
				// 3. 歌词
				Log.e(TAG, "Service MyReceiver SVR_SHOW_LRC mCurrent="+mCurrent);
				//mCurrent = intent.getIntExtra("listPosition", -1);
				initLrc();
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
			Log.e(TAG, "Service queryPlayList pgIdx="+pgIdx);
			HashMap<String, Object> bodyRequest = new HashMap<String, Object>();
			//bodyRequest.put("classid", mCataId);
			bodyRequest.put("pageindex", pgIdx);
			// bodyRequest.put("pagesize", pgSize);

			HttpManger http = new HttpManger(this, bHandler, this);
			return http.httpRequest(Configer.TYPE_QUICK_ENTRY_TESTA, bodyRequest, false, ResponePList.class, false, false, true);
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
		if (pList == null || pList.isEmpty())
			return;

		
		mData.addAll(plist.getpList());
		mCurPg++;
		
		Log.e(TAG, "Service onPostHandle success, curpg:"+mCurPg);
		
		Configer.sendNotice(PlayerService.this, Configer.Action.ACT_UPDATE_PlAYLIST, null);
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
 
    	
    	
    }  
}
