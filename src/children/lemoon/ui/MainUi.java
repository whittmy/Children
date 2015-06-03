package children.lemoon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import plugs.parser.Parser;

import logger.lemoon.Logger;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import children.lemoon.R;
import children.lemoon.R.id;
import children.lemoon.R.layout;
import children.lemoon.myrespone.RecommendEntity;
import children.lemoon.myrespone.ResponeChildClassList;
import children.lemoon.player.duplay.PlayingActivity;
import children.lemoon.player.duplay.vermgr.PlayLibsVerMgr;
import children.lemoon.player.org.Player;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ControlcurrentThread;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.reqbased.utils.TimeUtil;
import children.lemoon.utils.NetworkUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class MainUi extends BaseReqActivity implements AdapterView.OnItemClickListener {
	private GridView childcateGrid;
	private ChildCateAdapter adapter;
	private LinkedList<RecommendEntity> mData = new LinkedList<RecommendEntity>();
	PlayLibsVerMgr mVerMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// getWindow().getDecorView().setSystemUiVisibility(0x00000008);
		setContentView(R.layout.mainui);

		mVerMgr = new PlayLibsVerMgr(this);
		mVerMgr.Init();

		childcateGrid = ((GridView) findViewById(R.id.gv_childcate_list));
		adapter = new ChildCateAdapter(this, mData);
		childcateGrid.setAdapter(adapter);
		childcateGrid.setOnItemClickListener(this);

		queryChildrenCata();
	}

	private boolean queryChildrenCata() {
		// String timeStamp = TimeUtil.getTimeStamp();
		HashMap<String, Object> bodyRequest = new HashMap<String, Object>();

		HttpManger http = new HttpManger(this, bHandler, this);
		return http.httpRequest(20, bodyRequest, false, ResponeChildClassList.class, false, false, true);
	}

	@Override
	protected void onPostHandle(int requestType, Object data, boolean status, int paramInt2, Object paramObject2, Object paramObject3) {
		// TODO Auto-generated method stub
		super.onPostHandle(requestType, data, status, paramInt2, paramObject2, paramObject3);
		if (data == null) {
			return;
		}

		if (requestType == 20) {
			ResHeadAndBody localResHeadAndBody = (ResHeadAndBody) data;
			ResponeChildClassList res = (ResponeChildClassList) localResHeadAndBody.getBody();
			mData.addAll(res.getChildTopCateList());
			adapter.notifyDataSetChanged();

			Logger.LOGD("+++children-cata-list++" + res.getChildTopCateList() + "++++++");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		RecommendEntity re = (RecommendEntity) ((Adapter) arg0.getAdapter()).getItem(arg2);
		if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前网络不可用", Toast.LENGTH_SHORT).show();
			return;
		}

		if (NetworkUtils.is3G(getApplicationContext())) {
			return;
		}

		String tmp = null;
		switch (arg2) {
		case 0:
			tmp = "http://v.youku.com/v_show/id_XNTk4ODU3Mjc2.html?from=s1.8-1-1.1";
			tmp = "http://bbhlt.shoujiduoduo.com/bb/video/202259301/329236100.mp4";
			break;
		case 1:
			tmp = "http://v.youku.com/v_show/id_XNTAzNjQ2OTky.html?from=s1.8-1-1.1";
			tmp = "http://bbhlt.shoujiduoduo.com/bb/video/202259301/329220600.mp4";
			break;
		case 2:
			tmp = "http://v.youku.com/v_show/id_XNTk4ODU0NDAw.html?from=s1.8-1-1.1";
			tmp = "http://bbhlt.shoujiduoduo.com/bb/video/202259301/329231700.mp4";
			break;
		case 3:
			tmp = "http://v.youku.com/v_show/id_XNTk4ODg4MzA0.html?from=s1.8-1-1.1";
			tmp = "http://bbhlt.shoujiduoduo.com/bb/video/202259301/329249700.mp4";
			break;
		case 4:
			tmp = "http://v.youku.com/v_show/id_XNTk4ODg4ODM2.html?from=s1.8-1-1.1";
			tmp = "http://bbhlt.shoujiduoduo.com/bb/video/202259301/329241700.mp4";
			break;
		case 5:
			tmp = "http://v.youku.com/v_show/id_XODc4MDI2Nzg0.html?from=s1.8-1-1.1";
			tmp = "http://bbhlt.shoujiduoduo.com/bb/video/202259301/329238900.mp4";
			break;
		case 6:
			tmp = "http://v.youku.com/v_show/id_XMzQxNTE2MjAw.html?from=s1.8-1-1.1";
			tmp = "http://bbhlt.shoujiduoduo.com/bb/video/202259301/329247400.mp4";
			break;
		}
		final String url = tmp;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// ArrayList<String> arr = Parser.test(MainUi.this, url);
				if (/* arr != null && !arr.isEmpty() */true) {
					// Intent it = new Intent(MainUi.this,
					// PlayingActivity.class);
					Intent it = new Intent(MainUi.this, /* Player.class */PlayingActivity.class);
					it.putExtra("video_path", /* arr.get(0) */url);
					it.putExtra("video_name", "测试视频");
					MainUi.this.startActivity(it);
					return;
				} else {
					MainUi.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(MainUi.this, "获取地址失败，请重试", Toast.LENGTH_SHORT).show();
						}
					});

					return;
				}
			}
		}).start();

	}

}
