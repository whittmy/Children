package children.lemoon.player.org;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.util.MediaUtil;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponePList;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.ui.HorizontalScrollViewAdapter;
import children.lemoon.ui.MyHorizontalScrollView;
import children.lemoon.ui.MyHorizontalScrollView.CurrentImageChangeListener;
import children.lemoon.ui.MyHorizontalScrollView.OnItemClickListener;
import children.lemoon.ui.MyHorizontalScrollView.TouchListener;
import children.lemoon.utils.MyImageLoadTask;

import logger.lemoon.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Player extends BaseReqActivity implements SurfaceHolder.Callback {
	private MySurfaceView mSurfaceView;
	private SurfaceHolder mHolder;
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

	private int mVideoWidth;
	private int mVideoHeight;

	private int mSurfaceWidth;
	private int mSurfaceHeight;

	int mDuration;
	LinearLayout mHeaderContainer;
	RelativeLayout mCtrlContainer;
	TextView mMvTitle;
	ImageView mBattery;
	View mFill1, mFill2;
	TextView mTvCurtm, mTvDur;

	private LinkedList<PlayItemEntity> mData = new LinkedList<PlayItemEntity>();
	private MyHorizontalScrollView mHorizontalScrollView;
	private HorizontalScrollViewAdapter mAdapter;
	private int mCurPg = 0;
	private boolean mBCtrlbarShow = false;
	SeekBar sBar;

	public void fillEvent(View view) {
		toggleClick();
	}

	public class DelayThread extends Thread {
		int milliseconds;

		public DelayThread(int i) {
			milliseconds = i;
		}

		public void run() {
			while (true) {
				try {
					sleep(milliseconds);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				mHandler.sendEmptyMessage(CMD_REFRESH_SEEKBAR);
			}
		}
	}

	public void startProgressUpdate() {
		// 开辟Thread 用于定期刷新SeekBar
		DelayThread dThread = new DelayThread(100);
		dThread.start();
	}

	void initMyView() {
		mHeaderContainer = (LinearLayout) findViewById(R.id.header_container);
		mCtrlContainer = (RelativeLayout) findViewById(R.id.ctrlbar_container);
		mMvTitle = (TextView) findViewById(R.id.mv_title);
		mBattery = (ImageView) findViewById(R.id.battery);
		mFill1 = findViewById(R.id.fill1);
		mFill2 = findViewById(R.id.fill2);

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
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int dest = seekBar.getProgress();
				int sMax = sBar.getMax();
				mPlayer.seekTo(mDuration * dest / sMax);
			}
		});
	}

	private Runnable ctrBarHidding = new Runnable() {
		public void run() {
			mBCtrlbarShow = true;
			toggleClick();

			mHandler.removeCallbacks(ctrBarHidding); //执行一次就取消
		}
	};

	void ctrlShowMgr() {
		mHandler.removeCallbacks(ctrBarHidding); // 停止Timer
		mHandler.postDelayed(ctrBarHidding, 4000); // 开始Timer
	}

	void toggleClick() {
		if (mBCtrlbarShow) {
			// hide
			mBCtrlbarShow = false;

			mFill1.setVisibility(View.GONE);
			mFill2.setVisibility(View.GONE);
			mHeaderContainer.setVisibility(View.GONE);
			mCtrlContainer.setVisibility(View.GONE);
			mHorizontalScrollView.setVisibility(View.GONE);
			mHandler.removeCallbacks(ctrBarHidding);

		} else {
			mBCtrlbarShow = true;
			mFill1.setVisibility(View.VISIBLE);
			mFill2.setVisibility(View.VISIBLE);
			mHeaderContainer.setVisibility(View.VISIBLE);
			mCtrlContainer.setVisibility(View.VISIBLE);
			mHorizontalScrollView.setVisibility(View.VISIBLE);
			
			ctrlShowMgr();
		}
	}

	// ------------------ surface callback -------------------
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Logger.LOGD(TAG, "Surface Change:::");
		mSurfaceWidth = w;
		mSurfaceHeight = h;
		Logger.LOGD(TAG, "surfaceChanged video, w=" + mVideoWidth + ", h=" + mVideoHeight);
		Logger.LOGD(TAG, "surfaceChanged surfacesize,w=" + mSurfaceWidth + ",h=" + mSurfaceHeight);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Logger.LOGD(TAG, "surfaceCreated");

		mSurfaceView.setY(0);
		mHolder.setFixedSize(mDm.widthPixels, mDm.heightPixels);
		mSurfaceView.mIsFullScreen = false;
		if (mPlayer != null)
			mPlayer.setDisplay(mHolder);

		initPlayer();
		myPlay(mPlayPath);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Logger.LOGD("Surface Destory:::", "surfaceDestroyed called");
		mHolder = null;
		releasePlayer();

	}

	private void releasePlayer() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	public Handler mHandler;

	// based on surfaceview, then init other view or resource in function
	// surfacecreated
	void initSurfaceView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mSurfaceWidth = 0;
		mSurfaceHeight = 0;

		mSurfaceView = (MySurfaceView) this.findViewById(R.id.surface_view);

		mSurfaceView.setFocusable(true);
		mSurfaceView.setFocusableInTouchMode(true);
		mSurfaceView.requestFocus();

		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mContext = this;
		this.getWindowManager().getDefaultDisplay().getMetrics(mDm);
		setContentView(R.layout.mv_player);
		// getWindow().getDecorView().setSystemUiVisibility(8);

		initMyView();
		startProgressUpdate();

		Intent it = getIntent();
		String str = it.getStringExtra("video_name");

		mPlayPath = it.getStringExtra("video_path");

		mHandler = new Handler(getMainLooper()) {
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
					int sMax = sBar.getMax();
					sBar.setProgress(position * sMax / mDuration);
					break;

				}

				removeMessages(msg.what);
			};
		};

		initSurfaceView();

		// =====================
		mHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.id_horizontalScrollView);
		mAdapter = new HorizontalScrollViewAdapter(this, mHorizontalScrollView, mData);

		
		//添加触摸回调，
		mHorizontalScrollView.setTouchListener(new TouchListener() {
			@Override
			public void onHadToched() {
				// TODO Auto-generated method stub
				ctrlShowMgr();
			}
		});
		
		// 添加滚动回调
		mHorizontalScrollView.setCurrentImageChangeListener(new CurrentImageChangeListener() {
			public void onCurrentImgChanged(int position, View viewIndicator) {
				Log.e("", "!!!!!!!total=" + mAdapter.getCount() + ", position=" + position);
				next(false);
			}
		});

		// 添加点击回调
		mHorizontalScrollView.setOnItemClickListener(new OnItemClickListener() {
			public void onClick(View view, int position) {
				view.setBackgroundColor(Color.parseColor("#AA024DA4"));
				myPlay(position);
				
			}
		});

		queryPlayList(mCurPg+1);
	}

	
	
	private void next(boolean bplay){
		if (mAdapter.getCount() - mHorizontalScrollView.getItemCntConst() - (mHorizontalScrollView.getScrollPos() + 1) < 4) {
			Log.e("", "!!!!!! begin to load pg: " + (mCurPg + 1));
			queryPlayList(mCurPg + 1);
		}
		
		if(bplay){
			if(mHorizontalScrollView.getClickPos()+1 >= mData.size())
				return;
			mHorizontalScrollView.next();
			myPlay(mHorizontalScrollView.getClickPos());
		}
	}
	
	// AutoScrollTextView mTopNotice;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// getWindow().getDecorView().setSystemUiVisibility(8);
		Logger.LOGD(TAG, "-=-=-=-onResume-=-=-=-=");

		if (mPlayer != null) {
			myPlay(mPlayPath);
		}
	}

	private void initPlayer() {
		Logger.LOGD(TAG, "begint to new InitPlayer");
		if (mPlayer != null) {
			mPlayer.release();
			mSurfaceView.setVisibility(View.GONE);
		}

		mPlayer = new MediaPlayer();
		mSurfaceView.setVisibility(View.VISIBLE);
		if (mHolder != null)
			mPlayer.setDisplay(mHolder);
		mPlayer.setOnVideoSizeChangedListener(mVideoSizeListener);
		mPlayer.setOnPreparedListener(mPrepareListener);
		mPlayer.setOnCompletionListener(mCompleteListener);
		mPlayer.setOnErrorListener(mErrorListener);
		mPlayer.setOnInfoListener(mInfoListener);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mPlayer.reset();

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	boolean ispsrc(String url) {
		// String a = url.substring(0, 1);
		byte[] a = url.getBytes();
		if (a[0] == 0x70) {
			return true;
		} else {
			return false;
		}
	}

	
	public void myPlay(int curClickPos){
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
		
		//if(!url.isEmpty())
		//	return;
		
		
		Logger.LOGD(TAG, "myPlay:" + url);

		mPlayPath = url;
		Logger.LOGD(TAG, "begin to play:" + mPlayPath);

		if (mPlayer == null) {
			Logger.LOGD(TAG, "mPlayer is null!!");
			initPlayer();
		}

		try {
			Logger.LOGD(TAG, "-=-=-=-=-=-= -=-=-reset-=--= -=-==-");
			mPlayer.reset();
			mPlayer.setDataSource(url);
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

		setViewFocus();
	}

	// for controling focus, when prepare to play
	private void setViewFocus() {
		mSurfaceView.requestFocus();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean ret = false;
		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			Logger.LOGD(TAG, "exit.....");

			if (mPlayer != null) {
				// mPlayer.stop();
				// mPlayer.reset();
				mPlayer.release();
				mPlayer = null;
			}

			finish();

			ret = true;
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			Logger.LOGD(TAG, "onKeyDown event.getRepeatCount() " + event.getRepeatCount());

			if (event.getRepeatCount() == 0) {
				event.startTracking();
				return true;
			}

			break;
		default:
			Logger.LOGD(TAG, "onkeydown:" + keyCode);
			break;
		}
		Logger.LOGD(TAG, "global key down: ret=" + ret);

		if (!ret)
			ret = super.onKeyDown(keyCode, event);
		return ret;

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Logger.LOGD(TAG, "onStop");

		// LeService.stop();

		if (mPlayer != null) {
			mPlayer.reset();
		}

	}

	// ------------------- play listener ---------------------
	MediaPlayer.OnVideoSizeChangedListener mVideoSizeListener = new MediaPlayer.OnVideoSizeChangedListener() {
		@Override
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			// TODO Auto-generated method stub
			Logger.LOGD("Video Size Change", "onVideoSizeChanged called, w=" + width + ", h=" + height);

			Logger.LOGD(TAG, "surfaceChanged video, w=" + mVideoWidth + ", h=" + mVideoHeight);
			Logger.LOGD(TAG, "surfaceChanged surfacesize,w=" + mSurfaceWidth + ",h=" + mSurfaceHeight);
			/*
			 * if (mVideoWidth != 0 && mVideoHeight != 0)
			 * setVideoLayout(mVideoLayout, mVideoAspectRatio);
			 */
		}
	};

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
			Logger.LOGD(TAG, "onPrepare video, w=" + mVideoWidth + ", h=" + mVideoHeight);
			Logger.LOGD(TAG, "onPrepare surfacesize,w=" + mSurfaceWidth + ",h=" + mSurfaceHeight);

			Logger.LOGD(TAG, "dur=" + mPlayer.getDuration() + ", pos=" + mPlayer.getCurrentPosition());

			mMvTitle.setText(mData.get(mHorizontalScrollView.getClickPos()).getName());
			mDuration = mPlayer.getDuration();
			
			mTvDur.setText(MediaUtil.formatTime(mDuration));
			mPlayer.start();
		}
	};

	private boolean queryPlayList(int pgIdx/* , int pgSize */) {
		HashMap<String, Object> bodyRequest = new HashMap<String, Object>();
		// String timeStamp = TimeUtil.getTimeStamp();
		// bodyRequest.put("classid", gameTypeId);
		bodyRequest.put("pageindex", pgIdx);
		// bodyRequest.put("pagesize", pgSize);

		HttpManger http = new HttpManger(this, bHandler, this);
		return http.httpRequest(Configer.TYPE_QUICK_ENTRY_TEST, bodyRequest, false, ResponePList.class, false, false, true);
	}

	@Override
	protected void onPostHandle(int requestType, Object data, boolean status, int paramInt2, Object paramObject2, Object paramObject3) {
		// TODO Auto-generated method stub
		super.onPostHandle(requestType, data, status, paramInt2, paramObject2, paramObject3);
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
		mHorizontalScrollView.initDatas(mAdapter);
		// mHorizontalScrollView.update();
		mCurPg++;

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//Log.e("", "!!!!!!!!!!!!!!!!!!!!!down");
			toggleClick();
			return true;
		}
		return super.onTouchEvent(event);
	}

}
