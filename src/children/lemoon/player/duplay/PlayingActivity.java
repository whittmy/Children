package children.lemoon.player.duplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;

import children.lemoon.MyApp;
import children.lemoon.R;
import children.lemoon.player.duplay.utils.VerticalSeekBar;
import children.lemoon.player.duplay.utils.VideoGestureDetector;
import children.lemoon.player.duplay.utils.VideoGestureDetector.VideoGestureDetectListenner;
import children.lemoon.player.duplay.vermgr.PlayLibsVerMgr;

import com.baidu.cyberplayer.core.BVideoView;
import com.baidu.cyberplayer.utils.VersionManager;
import com.baidu.cyberplayer.utils.VersionManager.CPU_TYPE;
import com.baidu.cyberplayer.utils.VersionManager.RequestCpuTypeAndFeatureCallback;
import com.baidu.cyberplayer.utils.VersionManager.RequestDownloadUrlForCurrentVersionCallback;
import com.baidu.cyberplayer.utils.ZipUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

//import lib.runningman.RunningMan;
import logger.lemoon.Logger;

import org.apache.http.Header;

public class PlayingActivity extends Activity implements VideoGestureDetectListenner, View.OnClickListener, BVideoView.OnPreparedListener,
		BVideoView.OnErrorListener, BVideoView.OnInfoListener, BVideoView.OnCompletionListener, SensorEventListener {
	private static final int PLAY_END = 101;
	private static final String POWER_LOCK = "PlayingActivity";
	private static final String TAG = "PlayingActivity";
	private static final int USER_EIXT = 102;
	private static final String mAK = "Ar4jru4mrAoHI8nTudiBCAfa";
	private static final String mSK = "S2xkwtQZe0dOG8UhrbTbO18TzRHCnZOO";
	public static String strVideoPath;
	private final int CMD_CLEAR_CONTROLHINT = 5;
	private final int CMD_CLEAR_CONTROLHINT_DELAY = 6;
	private final int CMD_CLEAR_POPOUT = 7;
	private final int CMD_CLEAR_POPOUT_DELAY = 8;
	private final int CMD_EXIT = 9;
	private final int CMD_UPDATE_CURRPOSITION = 4;
	private final int GET_RENDER_FAIL = 1002;
	private final int GET_RENDER_SUC = 1001;
	private final int LINK_FAIL = 1004;
	private final int LINK_SUC = 1003;
	private final int REFRESH_COUNT = 30;
	private int REFRESH_DELAY = 200;
	private final int STATUS_CHANGED = 0;
	private ProgressDialog alertPorgress;
	private boolean barShow = true;
	private long control_hint_time = -1L;
	private LinearLayout controllerBar;
	private TextView currTextView;
	private TextView durTextView;
	private int heightPix = -1;
	private boolean isFullScreen = true;
	private boolean isMove;
	private ImageButton lastButton;
	private VerticalSeekBar lightBar;
	private LinearLayout lightBarLayout;
	private ImageButton lightButton;
	private int lightness = -1;
	private TextView mControlHint;
	PLAYER_STATUS mCurPlayerStatus;
	private boolean mExitActivity = true;
	private int mLastPos;
	private int mPrePosition;
	int mRefreshCount = 0;
	private long mTouchTime;
	private VideoGestureDetector mVideoGestureDetector;
	private int mVolume = -1;
	private PowerManager.WakeLock mWakeLock = null;
	private WindowManager mWindowManager = null;
	private int maxProgress;
	private ImageButton playButton;
	private long popuout_time = -1L;
	private ImageButton preButton;
	private SeekBar progressSeekBar;
	List<String> renderList;
	private ImageButton returnButton;
	private AlertDialog selectDialog;
	private int startLightness;
	private int startVolume;
	private float startX;
	private RelativeLayout titleBar;

	private boolean bExit = false;
	private final int CMD_SRC_PREPARED = 10001;
//	private RunningMan mRunner;

	private Handler uiHandler = new Handler() {
		public void handleMessage(Message paramAnonymousMessage) {
			switch (paramAnonymousMessage.what) {
			case STATUS_CHANGED:// sswitch_0
				updateControls(mCurPlayerStatus);
				if ((mCurPlayerStatus != PlayingActivity.PLAYER_STATUS.PLAYER_IDLE) && (mCurPlayerStatus == PlayingActivity.PLAYER_STATUS.PLAYER_PREPARED)) {
					uiHandler.removeMessages(CMD_UPDATE_CURRPOSITION);
					uiHandler.sendEmptyMessage(CMD_UPDATE_CURRPOSITION);

//					mRunner.hide();
				}
				return;
			case CMD_UPDATE_CURRPOSITION:// sswitch_1
				if (videoView != null) {
					mRefreshCount++;
					if (barShow) {
						mRefreshCount++;

						if (mRefreshCount > REFRESH_COUNT) {
							updateControlBar(false);
							return;
						}
					}

					if (videoView.isPlaying()) {
						refreshControlBar();
					} else {
						uiHandler.sendEmptyMessageDelayed(CMD_UPDATE_CURRPOSITION, REFRESH_DELAY);
					}
					return;
				}
				return;
			case CMD_CLEAR_CONTROLHINT:// sswitch_2
				control_hint_time = java.lang.System.currentTimeMillis();
				uiHandler.sendEmptyMessageDelayed(CMD_CLEAR_CONTROLHINT_DELAY, 3000L);
				return;
			case CMD_CLEAR_CONTROLHINT_DELAY:// sswitch_3
				if (java.lang.System.currentTimeMillis() - control_hint_time > 2999L)
					mControlHint.setVisibility(View.INVISIBLE);
				return;
			case CMD_CLEAR_POPOUT:// sswitch_4
				popuout_time = java.lang.System.currentTimeMillis();
				uiHandler.sendEmptyMessageDelayed(CMD_CLEAR_POPOUT_DELAY, 3000L);
				return;
			case CMD_CLEAR_POPOUT_DELAY:// sswitch_5
				if ((java.lang.System.currentTimeMillis() - popuout_time > 2999L) && (zoomPopup != null))
					zoomPopup.setVisibility(View.INVISIBLE);
				return;
			case CMD_EXIT:// sswitch_6
				finish();

			case GET_RENDER_SUC:// sswitch_7
				if ((alertPorgress != null) && (alertPorgress.isShowing())) {
					alertPorgress.dismiss();
				}
				alertPorgress = null;
				showRenderList();
				return;

			case GET_RENDER_FAIL:// sswitch_8
				if ((alertPorgress != null) && (alertPorgress.isShowing()))
					alertPorgress.dismiss();
				return;
			case LINK_SUC:// sswitch_9
				if ((alertPorgress != null) && (alertPorgress.isShowing()))
					alertPorgress.dismiss();
				return;

			case LINK_FAIL:// sswitch_a
				if ((alertPorgress != null) && (alertPorgress.isShowing()))
					alertPorgress.dismiss();
				return;

				// rocking
			case CMD_SRC_PREPARED:
				BVideoView.setNativeLibsDirectory(PlayLibsVerMgr.getSoFileDir(PlayingActivity.this));
				videoView.setVideoPath(strVideoPath);
				videoView.start();
				return;
			}
			return;
		}
	};
	private TextView videoTitle;
	BVideoView videoView = null;
	private RelativeLayout video_root;
	private ImageButton volumeButton;
	private int volumeHint = 0;
	private SeekBar volumeSeekBar;
	private int widthPix = -1;
	private LinearLayout zoomPopup;
	private TextView zoomTextView;

	PlayLibsVerMgr mVerMgr;

	private void soloading() {
		if (mVerMgr == null) {
			mVerMgr = new PlayLibsVerMgr(this);
			mVerMgr.Init();
		}
	}

	Runnable srcPreparing = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				if (bExit)
					return;
				Logger.LOGE("....srcPreparing......");
				if (strVideoPath != null && !strVideoPath.isEmpty()) {
					if (PlayLibsVerMgr.t5LibsHadOk(PlayingActivity.this)) {
						uiHandler.sendEmptyMessage(CMD_SRC_PREPARED);
						return;
					} else {
						soloading();
					}
				}

				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	enum PLAYER_STATUS {
		PLAYER_IDLE, PLAYER_INIT, PLAYER_PREPARED
	}

	private void adjustLightness(int paramInt) {
		setLightness(paramInt + startLightness);
	}

	private void dlnaSelect(String paramString) {
		alertPorgress = new ProgressDialog(this);
		alertPorgress.setTitle("Linking device");
		alertPorgress.show();
	}

	private boolean isMove(MotionEvent paramMotionEvent) {
		isMove = true;
		if (!barShow) {
			return isMove;
		}
		int[] arrayOfInt = new int[2];
		titleBar.getLocationOnScreen(arrayOfInt);
		int i = arrayOfInt[0];
		int j = arrayOfInt[1];
		if ((paramMotionEvent.getX() > i) && (paramMotionEvent.getX() < i + titleBar.getWidth()) && (paramMotionEvent.getY() > j)
				&& (paramMotionEvent.getY() < j + titleBar.getHeight())) {
			isMove = false;
			return isMove;
		}
		controllerBar.getLocationOnScreen(arrayOfInt);
		int k = arrayOfInt[0];
		int m = arrayOfInt[1];
		if ((paramMotionEvent.getX() > k) && (paramMotionEvent.getX() < k + controllerBar.getWidth()) && (paramMotionEvent.getY() > m)
				&& (paramMotionEvent.getY() < m + controllerBar.getHeight())) {
			isMove = false;
			return isMove;
		}
		return isMove;
	}

	private void refreshControlBar() {
		updateTextViewWithTimeFormat(durTextView, videoView.getDuration());
		if (videoView.getDuration() != progressSeekBar.getMax()) {
			progressSeekBar.setMax(videoView.getDuration());
		}
		progressSeekBar.setProgress(videoView.getCurrentPosition());
		uiHandler.sendEmptyMessageDelayed(CMD_UPDATE_CURRPOSITION, REFRESH_DELAY);
	}

	private void showControlHint(String paramString) {
		mControlHint.setText(paramString);
		mControlHint.setVisibility(View.VISIBLE);
		uiHandler.sendEmptyMessage(CMD_CLEAR_CONTROLHINT);
	}

	private void showRenderList() {
		if ((renderList == null) || (renderList.size() == 0)) {
			return;
		}
		ListView lv = new ListView(this);
		lv.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, renderList));
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong) {
				dlnaSelect((String) renderList.get(paramAnonymousInt));
				String str = (String) renderList.get(paramAnonymousInt);
				selectDialog.dismiss();
				selectDialog.cancel();
				playButton.setImageResource(R.drawable._4ui_duplayer_engine_play_media_style);
				videoView.pause();

				/*
				 * Intent localIntent = new Intent(PlayingActivity.this,
				 * DLNAControlActivity.class); localIntent.putExtra("device",
				 * str); startActivity(localIntent);
				 */
			}
		});
		selectDialog = new AlertDialog.Builder(this).setView(lv).setCancelable(true).create();
		selectDialog.show();
	}

	private void showZoomSelect() {
		if (zoomPopup == null) {
			zoomPopup = ((LinearLayout) findViewById(R.id.zoom_popup));
			((TextView) zoomPopup.findViewById(R.id.engine_zoom_original)).setOnClickListener(this);
			((TextView) zoomPopup.findViewById(R.id.engine_zoom_full)).setOnClickListener(this);
		}
		if (zoomPopup.getVisibility() != 0) {
			zoomPopup.setVisibility(View.VISIBLE);
			uiHandler.sendEmptyMessage(CMD_CLEAR_POPOUT);
			return;
		}
		zoomPopup.setVisibility(View.INVISIBLE);
	}

	private void toogleFullScreen() {
		if (isFullScreen) {
			zoomTextView.setText(R.string.zoom_original);
			RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
			params2.width = videoView.getVideoWidth();
			params2.height = videoView.getVideoHeight();
			videoView.setLayoutParams(params2);
			isFullScreen = false;
		} else {
			zoomTextView.setText(R.string.zoom_full);
			RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
			params1.width = -1;
			params1.height = -1;
			videoView.setLayoutParams(params1);
			isFullScreen = true;
		}

		return;
	}

	private void updateControls(PLAYER_STATUS paramPLAYER_STATUS) {
		if (paramPLAYER_STATUS == PLAYER_STATUS.PLAYER_IDLE) {
			playButton.setEnabled(true);
			playButton.setImageResource(R.drawable._4ui_duplayer_engine_play_media_style);
			progressSeekBar.setEnabled(false);
			zoomTextView.setEnabled(true);
			updateTextViewWithTimeFormat(currTextView, 0);
			updateTextViewWithTimeFormat(durTextView, 0);
			progressSeekBar.setProgress(0);
		} else if (paramPLAYER_STATUS == PLAYER_STATUS.PLAYER_INIT) {
			playButton.setEnabled(false);
			playButton.setImageResource(R.drawable._4ui_duplayer_engine_stop_media_style);
			progressSeekBar.setEnabled(false);
			zoomTextView.setEnabled(false);
		} else if (paramPLAYER_STATUS == PLAYER_STATUS.PLAYER_PREPARED) {
			playButton.setEnabled(true);
			playButton.setImageResource(R.drawable._4ui_duplayer_engine_stop_media_style);
			progressSeekBar.setEnabled(true);
			zoomTextView.setEnabled(true);
		}

		return;
	}

	private void updateTextViewWithTimeFormat(TextView paramTextView, int paramInt) {
		int i = paramInt / 3600;
		int j = paramInt % 3600 / 60;
		int k = paramInt % 60;
		Object[] arrayOfObject = new Object[3];
		arrayOfObject[0] = Integer.valueOf(i);
		arrayOfObject[1] = Integer.valueOf(j);
		arrayOfObject[2] = Integer.valueOf(k);
		paramTextView.setText(String.format("%02d:%02d:%02d", arrayOfObject));
	}

	private void updateVideoTitle(String paramString) {
		if (paramString == null) {
			videoTitle.setText("");
			return;
		}
		int i = paramString.lastIndexOf('/');
		if (i != -1) {
			videoTitle.setText(paramString.substring(i + 1));
			return;
		}
		videoTitle.setText(paramString);
	}

	public void adjustVolume(int paramInt) {
		setVolume(paramInt + startVolume);
	}

	public int getLightness() {
		return 100 * Settings.System.getInt(getContentResolver(), "screen_brightness", 255) / 255;
	}

	public int getVolume() {
		AudioManager localAudioManager = (AudioManager) getSystemService("audio");
		int i = localAudioManager.getStreamMaxVolume(3);
		int j = localAudioManager.getStreamVolume(3);
		if (i != 0) {
			return j * 100 / i;
		}
		return 0;
	}

	void initUI() {
		initmQualityPopWin();

		mControlHint = ((TextView) findViewById(R.id.cyberplayer_control_hint));
		titleBar = ((RelativeLayout) findViewById(R.id.title_bar));
		titleBar.getBackground().setAlpha(90);
		videoTitle = ((TextView) findViewById(R.id.sdlvideotitle));
		returnButton = ((ImageButton) findViewById(R.id.cyberplayer_back));
		returnButton.setOnClickListener(this);
		controllerBar = ((LinearLayout) findViewById(R.id.controlbar));
		controllerBar.getBackground().setAlpha(90);
		progressSeekBar = ((SeekBar) findViewById(R.id.mediacontroller_progress));
		progressSeekBar.setEnabled(false);
		playButton = ((ImageButton) findViewById(R.id.pause));
		playButton.setEnabled(false);
		playButton.setOnClickListener(this);
		lastButton = ((ImageButton) findViewById(R.id.rew));
		lastButton.setOnClickListener(this);
		preButton = ((ImageButton) findViewById(R.id.ffwd));
		preButton.setOnClickListener(this);
		returnButton = ((ImageButton) findViewById(R.id.cyberplayer_back));
		returnButton.setOnClickListener(this);
		volumeButton = ((ImageButton) findViewById(R.id.volume));
		volumeButton.setOnClickListener(this);
		currTextView = ((TextView) findViewById(R.id.time_current));
		durTextView = ((TextView) findViewById(R.id.time));
		lightButton = ((ImageButton) findViewById(R.id.lightness));
		lightButton.setOnClickListener(this);
		zoomTextView = ((TextView) findViewById(R.id.zoom_select));
		zoomTextView.setEnabled(false);
		zoomTextView.setOnClickListener(this);
		volumeSeekBar = ((SeekBar) findViewById(R.id.volume_progress));
		volumeSeekBar.setMax(100);
		volumeSeekBar.setProgress(getVolume());
		if (volumeSeekBar.getProgress() == 0) {
			volumeButton.setImageResource(R.drawable._4ui_duplayer_engine_mute_style);
		} else {
			volumeButton.setImageResource(R.drawable._4ui_duplayer_engine_volume_btn_style);
		}

		volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar paramAnonymousSeekBar, int paramAnonymousInt, boolean paramAnonymousBoolean) {
				setVolume(paramAnonymousInt);
			}

			public void onStartTrackingTouch(SeekBar paramAnonymousSeekBar) {
			}

			public void onStopTrackingTouch(SeekBar paramAnonymousSeekBar) {
				if (mControlHint != null) {
					mControlHint.setVisibility(View.INVISIBLE);
				}
			}
		});

		progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar paramAnonymousSeekBar, int paramAnonymousInt, boolean paramAnonymousBoolean) {
				updateTextViewWithTimeFormat(currTextView, paramAnonymousInt);
			}

			public void onStartTrackingTouch(SeekBar paramAnonymousSeekBar) {
				uiHandler.removeMessages(CMD_UPDATE_CURRPOSITION);
			}

			public void onStopTrackingTouch(SeekBar paramAnonymousSeekBar) {
				int i = paramAnonymousSeekBar.getProgress();
				uiHandler.removeMessages(CMD_UPDATE_CURRPOSITION);
				videoView.seekTo(i);
				uiHandler.sendEmptyMessage(CMD_UPDATE_CURRPOSITION);
			}
		});
		progressSeekBar.setEnabled(false);
		lightBar = ((VerticalSeekBar) findViewById(R.id.light_seekbar));
		lightBarLayout = ((LinearLayout) findViewById(R.id.light_seekbar_layout));
		lightBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(VerticalSeekBar paramAnonymousVerticalSeekBar, int paramAnonymousInt, boolean paramAnonymousBoolean) {
				if (paramAnonymousInt != lightness) {
					setLightness(paramAnonymousInt);
				}
			}

			public void onStartTrackingTouch(VerticalSeekBar paramAnonymousVerticalSeekBar) {
			}

			public void onStopTrackingTouch(VerticalSeekBar paramAnonymousVerticalSeekBar) {
				if (mControlHint != null) {
					mControlHint.setVisibility(View.INVISIBLE);
				}
			}
		});
		lightBar.setEnabled(true);
		return;

	}

	public void onClick(View paramView) {
		mRefreshCount = 0;
		switch (paramView.getId()) {
		default:
			return;
		case R.id.pause: // s0
			if (videoView.isPlaying()) {
				playButton.setImageResource(R.drawable._4ui_duplayer_engine_play_media_style);
				videoView.pause();
				return;
			}
			playButton.setImageResource(R.drawable._4ui_duplayer_engine_stop_media_style);
			videoView.resume();
			return;
		case R.id.volume: // s1
			if (mVolume == 0) {
				if (volumeHint == 0) {
					volumeHint = 30;
				}
				volumeSeekBar.setProgress(volumeHint);
				return;
			}
			volumeHint = volumeSeekBar.getProgress();
			volumeSeekBar.setProgress(0);
			return;
		case R.id.cyberplayer_back: // s2
			setResult(USER_EIXT);
			finish();
			return;
		case R.id.lightness: // s3
			if (lightBarLayout.getVisibility() == 0) {
				lightBarLayout.setVisibility(View.GONE);
				return;
			}
			lightBar.setProgress(1 + getLightness());
			lightBarLayout.setVisibility(View.GONE);
			lightBarLayout.setVisibility(View.VISIBLE);
			return;
		case R.id.zoom_select: // s4
			toogleFullScreen();
			return;
		case R.id.engine_zoom_original: // s5
			zoomPopup.setVisibility(View.INVISIBLE);
			int i = videoView.getVideoWidth();
			int j = videoView.getVideoHeight();
			zoomTextView.setText(R.string.zoom_original);
			RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
			params2.width = i;
			params2.height = j;
			videoView.setLayoutParams(params2);
			return;
		case R.id.engine_zoom_full: // s6
			zoomPopup.setVisibility(View.INVISIBLE);
			zoomTextView.setText(R.string.zoom_full);
			RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
			params1.width = -1;
			params1.height = -1;
			videoView.setLayoutParams(params1);
			return;

			// quality popmenu
		case R.id.quality:
			showmQualityPopWin(mTVCurQuality);
			break;
		case R.id.tv_quality_org:
			mTVCurQuality.setText("原画");
			mQualityPopWin.dismiss();
			break;
		case R.id.tv_quality_smooth:
			mTVCurQuality.setText("流畅");
			mQualityPopWin.dismiss();
			break;
		}

	}

	public void onCompletion() {
		mCurPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
		uiHandler.sendEmptyMessage(STATUS_CHANGED);
		if (mExitActivity) {
			setResult(PLAY_END);
			uiHandler.sendEmptyMessage(CMD_EXIT);
		}
	}

	// com.baidu.cyberplayer.utils.VersionManager mVM;
	private SensorManager sensorManager;

	private void initSensor() {
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(4), 0x2);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(1), 0x2);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(0x8), 0x2);
		return;
	}

	public String getMd5ByFile(File file) throws FileNotFoundException {
		String value = null;
		FileInputStream in = new FileInputStream(file);
		try {
			MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}

	// 1.百度的链接 ： isdu、video_name
	// 2.普通的连接： video_path 、video_name
	// 3.第三方打开
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout._4ui_duplayer_playingactivity);
		mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
				POWER_LOCK);
		sensorManager = (SensorManager) getSystemService("sensor");

//		mRunner = new RunningMan(this);
//		mRunner.show("   ");

		Intent it = getIntent();
		String str = it.getStringExtra("video_name");

		strVideoPath = it.getStringExtra("video_path");
		// 若不是百度的，则需要考虑下该页面被直接调用还是被当作第三方调用的！！！
		boolean isdu = getIntent().getBooleanExtra("isdu", false);
		if (!isdu) {
			if (strVideoPath == null || strVideoPath.isEmpty()) {
				String dataStr = it.getDataString();// v11
				String schemaStr = it.getScheme();// v18
				if (dataStr != null && schemaStr != null) {
					if ("file".equals(schemaStr)) {
						try {
							strVideoPath = URLDecoder.decode(dataStr, "UTF-8");
							strVideoPath = strVideoPath.replace("file://", "");
						} catch (UnsupportedEncodingException e) {
						}
					} else {
						strVideoPath = dataStr;
					}
				}
			}
		}

		initUI();

		BVideoView.setAKSK(mAK, mSK);
		videoView = new BVideoView(this);
		// videoView.setUserAgent("erge_iphone_v2%20iOS/2.0.0.0 CFNetwork/672.0.8 Darwin/14.0.0");

		video_root = ((RelativeLayout) findViewById(R.id.video_root));
		video_root.setGravity(17);
		video_root.addView(videoView);
		videoView.setOnPreparedListener(this);
		videoView.setOnErrorListener(this);
		videoView.setOnInfoListener(this);
		videoView.setOnCompletionListener(this);
		videoView.setDecodeMode(it.getBooleanExtra("isHW", false) ? 0 : 1);
		videoView.showCacheInfo(false);

		if ((str != null) && (!str.equals(""))) {
			updateVideoTitle(str);
		}

		mVideoGestureDetector = new VideoGestureDetector(this);

		new Thread(srcPreparing).start();

		return;
	}

	public boolean onCreateOptionsMenu(Menu paramMenu) {
		return true;
	}

	public void onDestroy() {
		super.onDestroy();
		if (videoView != null) {
			videoView.stopPlayback();
		}
		uiHandler.removeMessages(CMD_UPDATE_CURRPOSITION);
		bExit = true;
//		mRunner.close();
		System.exit(0);
	}

	public boolean onError(int paramInt1, int paramInt2) {
		Logger.LOGE(TAG, "  onError: what=" + paramInt1 + " extra=" + paramInt2);
		mCurPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
		uiHandler.sendEmptyMessage(STATUS_CHANGED);
		setResult(paramInt1);
		uiHandler.sendEmptyMessage(CMD_EXIT);
		return true;
	}

	public void onGoing(int paramInt, MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2) {
		if (!isMove) {
			return;
		}

		switch (paramInt) {
		case 5: // s0
		default:
			return;
		case 3: // s1
			verticalAjustEvent(paramMotionEvent1, paramMotionEvent2);
			return;
		case 6: // s2
			int i = (int) (30.0F * (paramMotionEvent2.getX() - startX) / widthPix) + mPrePosition;
			if (i > maxProgress) {
				i = -5 + maxProgress;
			}
			if (i < 0) {
				i = 0;
			}
			showControlHint(getString(R.string.cyberplayer_seekto) + i / 60 + getString(R.string.cyberplayer_minute) + i % 60
					+ getString(R.string.cyberplayer_second));
			return;
		case 4: // s3
		case 7:
			if (mControlHint != null)
				mControlHint.setVisibility(View.INVISIBLE);
		}

	}

	public boolean onInfo(int paramInt1, int paramInt2) {
		return false;
	}

	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		switch (paramInt) {
		case 4: // s1
			setResult(USER_EIXT);
			bExit = true;

			finish();
			System.exit(0);
			break;
		case 0x18: // s0
		case 0x19:
			if (volumeSeekBar != null) {
				volumeSeekBar.setProgress(getVolume());
			}
			break;
		}

		return super.onKeyDown(paramInt, paramKeyEvent);

	}

	public void onPause() {
		Logger.LOGD("PlayingActivity", "onPause");
		super.onPause();

		if (sensorManager != null)
			sensorManager.unregisterListener(this);

		if (mCurPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
			mExitActivity = false;
			mLastPos = videoView.getCurrentPosition();
			videoView.stopPlayback();
			Logger.LOGD("PlayingActivity", "onPause:" + mLastPos);
		}
	}

	public void onPrepared() {
		mCurPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
		if (mLastPos != 0) {
			videoView.seekTo(mLastPos);
		}
		uiHandler.sendEmptyMessage(STATUS_CHANGED);
	}

	public void onResume() {
		Logger.LOGD("PlayingActivity", "onResume");
		super.onResume();

		initSensor();

		if ((mWakeLock != null) && (!mWakeLock.isHeld())) {
			mWakeLock.acquire();
		}

		mExitActivity = true;
		if (mLastPos > 0) {
			videoView.seekTo(mLastPos);
			mLastPos = 0;
			videoView.start();
		}

		// if(getIntent().getStringExtra("DuID") == null){
		// if(PlayLibsVerMgr.getDuHadOk(this)){
		// BVideoView.setNativeLibsDirectory(getSoFileDir(this));
		// videoView.setVideoPath(strVideoPath);
		// videoView.start();
		// }
		// }
	}

	public void onStart(int paramInt, MotionEvent paramMotionEvent) {
		if (!isMove(paramMotionEvent)) {
			return;
		}
		if ((widthPix < 0) || (heightPix < 0)) {
			if (mWindowManager == null) {
				mWindowManager = ((WindowManager) getSystemService("window"));
			}
			Display localDisplay = mWindowManager.getDefaultDisplay();
			DisplayMetrics localDisplayMetrics = new DisplayMetrics();
			localDisplay.getMetrics(localDisplayMetrics);
			widthPix = localDisplayMetrics.widthPixels;
			heightPix = localDisplayMetrics.heightPixels;
		}
		switch (paramInt) {
		case 11:
		default:
			return;
		case 2:
			mControlHint.setVisibility(View.VISIBLE);
			startVolume = getVolume();
			startLightness = getLightness();
			return;
		case 5:
			mControlHint.setVisibility(View.VISIBLE);
			startX = paramMotionEvent.getX();
			maxProgress = videoView.getDuration();
			mPrePosition = videoView.getCurrentPosition();
			return;
		}

	}

	public void onStop() {
		super.onStop();
		Logger.LOGD("PlayingActivity", "onStop");
	}

	public void onStop(int paramInt, MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2) {
		if (!isMove) {
			isMove = true;
			return;
		}

		switch (paramInt) {
		case 5:
		case 6:
		default:
			return;
		case 4: // s1
			mControlHint.setVisibility(View.INVISIBLE);
			return;
		case 7: // s2
			if (Math.abs(paramMotionEvent1.getX() - paramMotionEvent2.getX()) > 30.0F) {
				mControlHint.setVisibility(View.INVISIBLE);
				if (mWindowManager == null) {
					mWindowManager = ((WindowManager) getSystemService("window"));
				}

				Display localDisplay = mWindowManager.getDefaultDisplay();
				DisplayMetrics localDisplayMetrics = new DisplayMetrics();
				localDisplay.getMetrics(localDisplayMetrics);
				int i = (int) (30.0F * (paramMotionEvent2.getX() - startX - 30.0F) / localDisplayMetrics.widthPixels) + mPrePosition;
				if (i > maxProgress) {
					i = -5 + maxProgress;
				}
				if (i < 0) {
					i = 0;
				}
				uiHandler.removeMessages(CMD_UPDATE_CURRPOSITION);
				videoView.seekTo(i);
				uiHandler.sendEmptyMessage(CMD_UPDATE_CURRPOSITION);
			}
			return;
		}
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		if (paramMotionEvent.getAction() == 0) {
			mTouchTime = java.lang.System.currentTimeMillis();
		} else if ((paramMotionEvent.getAction() == 1) && (java.lang.System.currentTimeMillis() - mTouchTime < 400L)) {
			updateControlBar(!barShow);
		}

		mVideoGestureDetector.onTouchEvent(paramMotionEvent);
		return false;

	}

	public void setLightness(int paramInt) {
		int i = 100;
		if (paramInt <= i) {
			i = paramInt;
		}

		if (i < 0) {
			i = 0;
		}
		if (i == lightness) {
			showControlHint(getString(R.string.lightness_hint) + i);
			return;
		}
		Settings.System.putInt(getContentResolver(), "screen_brightness", i * 255 / 100);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		float f = i / 100.0F;
		if ((f > 0.0F) && (f <= 1.0F)) {
			params.screenBrightness = f;
		}
		getWindow().setAttributes(params);
		showControlHint(getString(R.string.lightness_hint) + i);
		lightness = i;
		lightBar.setProgress(lightness);
		return;
	}

	public void setVolume(int paramInt) {
		int i = 100;
		if (paramInt <= i) {
			i = paramInt;
		}

		int j = 0;
		if (i >= 0) {
			j = i;
		}

		// goto1
		if (j == 0) {
			volumeButton.setImageResource(R.drawable._4ui_duplayer_engine_mute_style);

		} else {
			volumeButton.setImageResource(R.drawable._4ui_duplayer_engine_volume_btn_style);
		}

		if (mVolume != j) {
			AudioManager localAudioManager = (AudioManager) getSystemService("audio");
			localAudioManager.setStreamVolume(3, j * localAudioManager.getStreamMaxVolume(3) / 100, 0);
			showControlHint(getString(R.string.volume_hint) + j);
			mVolume = j;
			if (volumeSeekBar.getProgress() != j)
				;
			volumeSeekBar.setProgress(j);
		} else {
			showControlHint(getString(R.string.volume_hint) + j);
		}
		return;

	}

	public void updateControlBar(boolean paramBoolean) {
		if (paramBoolean) {
			mRefreshCount = 0;
			uiHandler.removeMessages(CMD_UPDATE_CURRPOSITION);
			refreshControlBar();
			controllerBar.setVisibility(View.VISIBLE);
			titleBar.setVisibility(View.VISIBLE);
		} else {
			controllerBar.setVisibility(View.INVISIBLE);
			titleBar.setVisibility(View.INVISIBLE);
			mControlHint.setVisibility(View.INVISIBLE);
			lightBarLayout.setVisibility(View.INVISIBLE);
		}

		barShow = paramBoolean;
		return;
	}

	public void verticalAjustEvent(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2) {
		int i = (int) (80.0F * (paramMotionEvent1.getY() - paramMotionEvent2.getY()) / heightPix);
		if (paramMotionEvent1.getX() < widthPix / 2) {
			adjustLightness(i);
			return;
		}
		adjustVolume(i);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		switch (event.sensor.getType()) {
		case 1: // sw1
			if (getWindowManager().getDefaultDisplay().getRotation() != 3) {
				if (new BigDecimal(event.values[0]).compareTo(new BigDecimal(-0.875f)) < 0) {
					setRequestedOrientation(8);
					// ((MyApp)getApplication()).setScreenTurnRight(true);
					// curScreenOrientation = 8;
					// go goto0
					return;
				}
			}
			// cond2
			if (getWindowManager().getDefaultDisplay().getRotation() != 1) {
				if (new BigDecimal(event.values[0]).compareTo(new BigDecimal(5.0f)) > 0) {
					// ((MyApp)getApplication()).setScreenTurnRight(false);
					setRequestedOrientation(0);
					// curScreenOrientation = 0;
				}
			}
			return;
		}
	}

	// /////////// quality popmenu ////////////////
	private PopupWindow mQualityPopWin;
	private View mQualityPopWinView;
	private TextView mTVCurQuality;

	/** 显示mQualityPopWin */
	private void showmQualityPopWin(View parent) {
		if (!mQualityPopWin.isShowing()) {
			int popupWidth = mQualityPopWinView.getMeasuredWidth();
			int popupHeight = mQualityPopWinView.getMeasuredHeight();
			int[] location = new int[2];
			parent.getLocationOnScreen(location);
			mQualityPopWin.showAtLocation(parent, Gravity.NO_GRAVITY, (location[0] + parent.getWidth() / 2) - popupWidth / 2, location[1] - popupHeight - 12);
		} else {
			mQualityPopWin.dismiss();
		}
	}

	/**
	 * 初始化mQualityPopWin
	 */
	private void initmQualityPopWin() {
		mTVCurQuality = (TextView) findViewById(R.id.quality);
		mTVCurQuality.setOnClickListener(this);

		// 初始化mQualityPopWin，绑定显示view，设置该view的宽度/高度
		initQualityPopWinView();

		mQualityPopWin = new PopupWindow(mQualityPopWinView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mQualityPopWin.setFocusable(true);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景；使用该方法点击窗体之外，才可关闭窗体
		mQualityPopWin.setOutsideTouchable(true);

		// Background不能设置为null，dismiss会失效
		mQualityPopWin.setBackgroundDrawable(getResources().getDrawable(R.drawable._4ui_duplayer_video_pop_window_bg));
		// mQualityPopWin.setBackgroundDrawable(null);

		// 设置渐入、渐出动画效果
		// mQualityPopWin.setAnimationStyle(R.style.mQualityPopWin);
		mQualityPopWin.update();

		// mQualityPopWin调用dismiss时触发，设置了setOutsideTouchable(true)，点击view之外/按键back的地方也会触发
		mQualityPopWin.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				// showToast("关闭mQualityPopWin");
			}
		});
	}

	/**
	 * 初始化mQualityPopWinView,监听view中的textview点击事件
	 */
	private void initQualityPopWinView() {
		mQualityPopWinView = LayoutInflater.from(this).inflate(R.layout._4ui_duplayer_quality_popwin, null);
		TextView tv_org = (TextView) mQualityPopWinView.findViewById(R.id.tv_quality_org);
		tv_org.setOnClickListener(this);
		TextView tv_smooth = (TextView) mQualityPopWinView.findViewById(R.id.tv_quality_smooth);
		tv_smooth.setOnClickListener(this);

		mQualityPopWinView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
	}

}
