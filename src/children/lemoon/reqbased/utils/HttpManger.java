package children.lemoon.reqbased.utils;

//ok
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import children.lemoon.Configer;
import children.lemoon.reqbased.db.DbUtil;
import children.lemoon.reqbased.entry.BasePaserMessageUtil;
import children.lemoon.reqbased.entry.ControlcurrentThread;
import children.lemoon.utils.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;



public class HttpManger {
	static int lastRequestType = -1;
	private ControlcurrentThread controlcurrentThread;
	private HttpTask httpTask;
	private Context mContext;
	private Handler mHandler;
	private BasePaserMessageUtil paseUtil;

	public HttpManger(Context paramContext, Handler paramHandler) {
		this.mContext = paramContext;
		this.mHandler = paramHandler;
	}

	public HttpManger(Context paramContext, Handler paramHandler, ControlcurrentThread paramControlcurrentThread) {
		this.mContext = paramContext;
		this.mHandler = paramHandler;
		this.controlcurrentThread = paramControlcurrentThread;
	}

	public boolean httpRequest(final int requestType, final Map<String, Object> bodyRequest, boolean bcache, final Class<?> class1, boolean paramBoolean2,
			boolean bzip, boolean showLoading) {
		if ((OnClickUtil.isMostPost()) && (lastRequestType == requestType)) {
			return false;
		}
		lastRequestType = requestType;
		Logger.LOGD("httpRequest", "httpRequest发送");
		this.paseUtil = new BasePaserMessageUtil();
		final String url = Configer.initUrl(requestType);

		// rocking cut!!!!
		// if (bcache) {
		// new Thread(new Runnable() {
		// public void run() {
		// try {
		// DbUtil localDbUtil = new DbUtil(HttpManger.this.mContext, class1);
		// localDbUtil.startWritableDatabase(false);
		// String str = PlayerApplication.getInstance().getUserId();
		// List<Object> localList = localDbUtil.queryList("_key=?", new String[]
		// { Md5Util.MD5(str + str + new Gson().toJson(bodyRequest)) });
		// localDbUtil.closeDatabase(false);
		// Logger.LOGD("", "list.size()=" + localList.size());
		// if ((localList.size() > 0) && (!HttpManger.this.paseUtil.isComeDb()))
		// {
		// HttpManger.this.mHandler.obtainMessage(requestType, 0, 99,
		// localList.get(0)).sendToTarget();
		// }
		// return;
		// } catch (Exception localException) {
		// localException.printStackTrace();
		// }
		// }
		// }).start();
		// }
		this.httpTask = new HttpTask(requestType, this.mHandler, url, this.mContext, bodyRequest, bcache, (Class<Object>) class1, this.paseUtil, bzip,
				showLoading);

		Executor localExecutor = AsyncTask.THREAD_POOL_EXECUTOR;
		RequestMethod[] arrayOfRequestMethod = { RequestMethod.GET };
		this.httpTask.executeOnExecutor(localExecutor, arrayOfRequestMethod);
		if (this.controlcurrentThread != null) {
			this.controlcurrentThread.getControlcurrentThread(this.httpTask);
		}
		return true;
	}
}
