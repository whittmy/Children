package children.lemoon.player.duplay;

import java.io.File;
import java.io.FileFilter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import children.lemoon.R;
import children.lemoon.player.duplay.fileselect.FileSelectDialog;
import children.lemoon.player.duplay.fileselect.FileSelectedCallBackBundle;

import com.baidu.cyberplayer.subtitle.SubtitleManager;
import com.baidu.cyberplayer.subtitle.utils.DownFinishCallback;

/**
 * @Description: 字幕设置popupwindow
 * @author: wangyuhe@baidu.com
 * @date: 2013-11-29
 */
public class SrtSettingPopupWin extends PopupWindow implements OnClickListener {
	/** 每按一次调整delta */
	private static final int ADJUST_DELTA = 5;
	/** 单位 */
	private static final String UNITS = "秒";
	/** 3000ms */
	private static final int THREE_THOUSAND_MS = 3000;
	/** dismiss window 消息 */
	private static final int EVENT_DISMISS_WINDOW = 5001;

	/** popupWindown的根view */
	private View mMainView;
	/** 字幕开关 */
	private ToggleButton mSubtitleSwitch;
	/** 手动调整的viewGroup */
	private ViewGroup mAdjustGroup;
	/** 调整提示 */
	private TextView mAdjustTipsView;
	/** 快进按钮 */
	private Button mSpeedUpButton;
	/** 减慢按钮 */
	private Button mSlowDownButton;
	/** 字幕调整的秒数 */
	private Context mContext;
	private int mAdjustedTime = 0;
	private Button mSelectSubtitleButton;
	private EditText mSubtitleEdit;
	private Button mPlaySubtitleButton;
	private EditText mColorEdit;
	private EditText mSizeEdit;
	private Button mColorSetButton;
	private Button mSizeSetButton;
	private EditText mAlignEdit;
	private EditText mMarginEdit;
	private Button mMarginSetButton;
	private Dialog mFileSelectDialog;
	private Button mDownButton;
	private SubtitleManager mSubtitleManager;
	/** handler */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_DISMISS_WINDOW:
				// dismiss();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};

	/**
	 * @param context
	 *            {@link Context}
	 * @param subtitleManager
	 * @param presenter
	 *            VideoPlayerPresenter的实例
	 */
	public SrtSettingPopupWin(Context context, SubtitleManager subtitleManager) {
		super(context);
		mContext = context;
		mSubtitleManager = subtitleManager;
		initView(context);
	}

	/**
	 * @Description: 初始化View
	 * @param context
	 *            {@link Context}
	 */
	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMainView = inflater.inflate(R.layout._4ui_duplayer_subtitle_setting_layout, null);
		mSubtitleSwitch = (ToggleButton) mMainView.findViewById(R.id.subtitle_switch);
		mAdjustGroup = (ViewGroup) mMainView.findViewById(R.id.adjust_button_group);
		mSpeedUpButton = (Button) mMainView.findViewById(R.id.speed_up);
		mSlowDownButton = (Button) mMainView.findViewById(R.id.slow_down);
		mAdjustTipsView = (TextView) mMainView.findViewById(R.id.adjust_prompt);
		mSelectSubtitleButton = (Button) mMainView.findViewById(R.id.select_subtitle);
		mSubtitleEdit = (EditText) mMainView.findViewById(R.id.subtitle_path);
		mPlaySubtitleButton = (Button) mMainView.findViewById(R.id.select_play);
		mColorEdit = (EditText) mMainView.findViewById(R.id.color_string);
		mColorSetButton = (Button) mMainView.findViewById(R.id.set_color);
		mSizeEdit = (EditText) mMainView.findViewById(R.id.subttext_size);
		mSizeSetButton = (Button) mMainView.findViewById(R.id.set_size);
		mMarginEdit = (EditText) mMainView.findViewById(R.id.edit_text_margin);
		mMarginSetButton = (Button) mMainView.findViewById(R.id.set_margin);
		mAlignEdit = (EditText) mMainView.findViewById(R.id.edit_text_align);
		mDownButton = (Button) mMainView.findViewById(R.id.down_subtitle);
		updateTips();
		mSubtitleSwitch.setChecked(true);

		mAdjustGroup.setVisibility(View.VISIBLE);
		mSubtitleManager.setIsShowSubtitle(true);

		initListener();
		initPopupWindow(context);
	}

	/**
	 * @param context
	 *            {@link Context}
	 * @Description:初始化popupWindow
	 */
	private void initPopupWindow(Context context) {
		int windowWidth = (int) context.getResources().getDimension(R.dimen._4ui_duplayer_subtitle_setting_width);
		int windowHeight = (int) context.getResources().getDimension(R.dimen._4ui_duplayer_subtitle_setting_height);
		this.setContentView(mMainView);
		this.setWidth(windowWidth);
		this.setHeight(windowHeight);
		this.setFocusable(true);

	}

	/**
	 * @Description:初始化监听器
	 */
	private void initListener() {
		mSubtitleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateDismissMsg();
				if (isChecked) {
					mSubtitleManager.setIsShowSubtitle(true);
					mAdjustGroup.setVisibility(View.VISIBLE);
				} else {
					mSubtitleManager.setIsShowSubtitle(false);
					mAdjustGroup.setVisibility(View.GONE);
				}
			}
		});
		mSpeedUpButton.setOnClickListener(this);
		mSlowDownButton.setOnClickListener(this);
		mSelectSubtitleButton.setOnClickListener(this);
		mPlaySubtitleButton.setOnClickListener(this);
		mColorSetButton.setOnClickListener(this);
		mSizeSetButton.setOnClickListener(this);
		mMarginSetButton.setOnClickListener(this);
		mDownButton.setOnClickListener(this);
	}

	/**
	 * @Description:更新提示View的显示
	 */
	protected void updateTips() {
		String numberString = getNumberStrings();
		mAdjustTipsView.setText(numberString + UNITS);
	}

	/**
	 * @Description: 获取提示的String
	 * @return 提示String
	 */
	private String getNumberStrings() {
		if (mAdjustedTime == 0) {
			return "0";
		}
		int absValue = Math.abs(mAdjustedTime);
		int integerPart = absValue / 10; // SUPPRESS CHECKSTYLE
		int fractionalPart = absValue % 10; // SUPPRESS CHECKSTYLE

		StringBuilder sb = new StringBuilder();
		if (mAdjustedTime > 0) {
			sb.append("+");
		} else {
			sb.append("-");
		}

		sb.append(String.valueOf(integerPart));
		if (fractionalPart != 0) {
			sb.append(".");
			sb.append(String.valueOf(fractionalPart));
		}
		return sb.toString();
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		mHandler.sendEmptyMessageDelayed(EVENT_DISMISS_WINDOW, THREE_THOUSAND_MS);
	}

	/**
	 * @Description:更新dismiss消息
	 */
	private void updateDismissMsg() {
		mHandler.removeMessages(EVENT_DISMISS_WINDOW);
		mHandler.sendEmptyMessageDelayed(EVENT_DISMISS_WINDOW, THREE_THOUSAND_MS);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.speed_up:
			updateDismissMsg();
			mAdjustedTime += ADJUST_DELTA;
			updateTips();
			mSubtitleManager.manualSyncSubtitle(mAdjustedTime * 100);
			break;

		case R.id.slow_down:
			updateDismissMsg();
			mAdjustedTime -= ADJUST_DELTA;
			updateTips();
			mSubtitleManager.manualSyncSubtitle(mAdjustedTime * 100);
			break;

		case R.id.select_subtitle:
			createSelectSubtitleFileDialog();
			break;

		case R.id.select_play:
			startPlaySubtitle();
			this.dismiss();
			break;

		case R.id.set_color:
			setSubtitleColor();
			break;

		case R.id.set_size:
			setSubtitleSize();
			break;

		case R.id.set_margin:
			setSubtitleLocation();
			break;

		case R.id.down_subtitle:
			downSubtitle();
			this.dismiss();
			break;

		default:
			break;
		}

	}

	private void downSubtitle() {
		String url = "http://bcs.duapp.com/subtitle/34677636.srt?sign=MBO:2af85f6e5c42fbc804bd9eee337e652d:9rJd06c8jKHb63SKzdYp3PWQYe4%3D";
		String name = "baixingjiudian.srt";
		String dir = new File(mContext.getFilesDir(), "subtitle").getAbsolutePath();
		String fullPath = "/data/data/com.baidu.bvideoviewtest/files/subtitle/baixingjiudian.srt";
		DownFinishCallback callback = new DownFinishCallback() {

			@Override
			public void onDownFinished(boolean isSuccess, String msg) {
				Toast.makeText(mContext, "下载状态: " + isSuccess + ", msg: " + msg, Toast.LENGTH_LONG).show();
			}
		};
		mSubtitleManager.startSubtitle(url, dir, name, 0, callback);
		// mSubtitleManager.startSubtitle(url, fullPath, 0, callback);
	}

	private void setSubtitleLocation() {
		int align = 0;
		int padding = 10;
		try {
			align = Integer.valueOf(mAlignEdit.getText().toString());
			padding = Integer.valueOf(mMarginEdit.getText().toString());
		} catch (Exception e) {
			Log.w("test_subtitle", "location setting error");
			return;
		}

		mSubtitleManager.getSubtitleSettings().setDisplayLocation(align, padding);

	}

	private void setSubtitleSize() {
		float size = 0;
		try {
			size = Float.valueOf(mSizeEdit.getText().toString());
		} catch (Exception e) {
			Log.w("test_subtitle", "size setting error");
			return;
		}

		mSubtitleManager.getSubtitleSettings().setDisplayFontSize(size);
	}

	private void setSubtitleColor() {
		int color = 0;
		try {
			color = Color.parseColor(mColorEdit.getText().toString());
		} catch (Exception e) {
			Log.w("test_subtitle", "color setting error");
			return;
		}

		mSubtitleManager.getSubtitleSettings().setDisplayColor(Color.parseColor(mColorEdit.getText().toString()));
	}

	private void startPlaySubtitle() {
		String uriString = mSubtitleEdit.getText().toString();
		mSubtitleManager.startSubtitle(uriString);
	}

	private void createSelectSubtitleFileDialog() {
		if (mFileSelectDialog == null) {
			FileFilter fileFilter = new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory()) {
						return true;
					}

					String path = pathname.getName().toLowerCase();
					if ((path != null) && path.endsWith(".srt")) {
						return true;
					}
					return false;
				}

			};
			mFileSelectDialog = FileSelectDialog.createDialog(mContext, "选择字幕", fileFilter, new FileSelectedCallBackBundle() {

				@Override
				public void onFileSelected(Bundle bundle) {
					String uri = bundle.getString("uri");
					mSubtitleEdit.setText(uri);
					if (mFileSelectDialog != null && mFileSelectDialog.isShowing()) {
						mFileSelectDialog.dismiss();
					}
				}
			});
		}
		mFileSelectDialog.show();
	}

}
