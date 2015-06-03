package children.lemoon.reqbased.entry;

//ok
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.gson.Gson;
import java.util.Map;

import logger.lemoon.Logger;

import org.json.JSONObject;

public class BasePaserMessageUtil {
	ResHeadAndBody bodyAndHead = null;
	private boolean comeDb = false;
	private boolean isFromNet = false;
	Message message;
	protected Object responseBody;
	ResponseHeader responseHeader;
	protected ResponsePager responsePage;
	private Object result;

	private void getMessageInfo(Context paramContext, Class<Object> paramClass, int requestType, String url, Object responseResult, Handler paramHandler,
			Map<String, Object> bodyRequest, boolean bcache) {

		if (responseResult == null) {
			this.message = paramHandler.obtainMessage(requestType, 2, 0, null);
			this.message.sendToTarget();
			return;
		}

		try {
			parseJson((String) responseResult, paramClass, paramContext, bcache, bodyRequest, url);

			this.bodyAndHead = new ResHeadAndBody();
			this.bodyAndHead.setHeader(this.responseHeader);
			if (this.responsePage != null) {
				this.bodyAndHead.setPage(this.responsePage);
			}
			if (this.responseBody != null) {
				this.bodyAndHead.setBody(this.responseBody);
			}
			Logger.LOGD("paserJoson", "mHandler " + paramHandler);

			this.message = paramHandler.obtainMessage(requestType, this.bodyAndHead);
			this.comeDb = true;
			if ((!this.isFromNet) && ((this.comeDb) || (this.isFromNet))) {
				return;
			}
			Logger.LOGD("paserJoson", "mHandler send msg");
			this.message.sendToTarget();
			return;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private void parseJson(String responseResult, Class<Object> paramClass, Context paramContext, boolean bcache, Map<String, Object> bodyRequest, String url)
			throws Exception {

		JSONObject jobj1 = new JSONObject(responseResult);
		// header section
		this.responseHeader = ResponseHeader.parse(jobj1.optJSONObject("header").toString());

		// page section
		JSONObject jobj2 = jobj1.optJSONObject("page");
		if (jobj2 != null) {
			this.responsePage = ResponsePager.parse(jobj2.toString());
		}

		// body section
		String str = jobj1.optString("body");
		if ((paramClass != null) && (str != null) && (!str.equals("")) && (!str.equals("null"))) {
			Logger.LOGD("paserJoson", "strBody = " + str);

			// 将JSON数据转换为paramClass对象
			this.responseBody = new Gson().fromJson(str, paramClass);
			Logger.LOGD("paserJoson", "pase result = " + this.responseBody);
			if (bcache) {
				new SaveDbThread(paramContext, bodyRequest, url, paramClass).start();
			}
		}
	}

	public boolean isComeDb() {
		return this.comeDb;
	}

	public boolean isFromNet() {
		return this.isFromNet;
	}

	public void parse(Class<Object> paramClass, String url, int requestType, Object responseResult, Handler paramHandler, Context paramContext, boolean bcache,
			Map<String, Object> bodyRequest, boolean bcomeDb) {
		try {
			this.result = responseResult;
			if (bcomeDb) {
				this.comeDb = true;
			}
			this.isFromNet = bcomeDb;
			getMessageInfo(paramContext, paramClass, requestType, url, this.result, paramHandler, bodyRequest, bcache);
			return;
		} finally {
		}
	}

	public void setComeDb(boolean bComeDb) {
		this.comeDb = bComeDb;
	}

	public void setFromNet(boolean bFromNet) {
		this.isFromNet = bFromNet;
	}

	class SaveDbThread extends Thread {
		Map<String, Object> bodyRequest;
		Context context;
		Class<Object> paserClass;
		String url;

		public SaveDbThread(Context paramContext, Map<String, Object> paramMap, String paramString, Class<Object> paramClass) {
			this.context = paramContext;
			this.bodyRequest = paramMap;
			this.url = paramString;
			this.paserClass = paramClass;
		}

		public void run() {
		}
	}
}
