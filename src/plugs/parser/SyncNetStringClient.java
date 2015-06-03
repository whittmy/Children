package plugs.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultRedirectHandler;

import android.content.Context;
import android.util.Log;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.DataAsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

public class SyncNetStringClient {
	private static SyncHttpClient client = null;
	private static Context mcx;
	private static boolean isInit = false;

	public static void init(Context cx) {
		if (isInit) {
			return;
		}
		mcx = cx;
		isInit = true;
		client = new SyncHttpClient();
		myCookieStore = new PersistentCookieStore(mcx);
		client.setCookieStore(myCookieStore);
		client.setTimeout(30000);

	}

	static PersistentCookieStore myCookieStore = null;

	public static void get(String url, Header[] headers, RequestParams params) {
		// client.get(url, params, handler);
		client.get(mcx, url, headers, params, handler);
	}

	public static void post(String url, Header[] headers, String contentType, RequestParams params) {
		// client.post(url, params, handler);
		client.post(mcx, url, headers, params, contentType, handler);
	}

	// private static String getAbsoluteUrl(String relativeUrl) {
	// return BASE_URL + relativeUrl;
	// }

	static boolean mSuccess = false;
	static String mRetStr = "";
	static Header[] mRetHead = null;

	static TextHttpResponseHandler handler = new TextHttpResponseHandler() {
		@Override
		public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
			// TODO Auto-generated method stub
			mRetStr = "";
			mSuccess = false;
			mRetHead = null;
		}

		@Override
		public void onSuccess(int arg0, Header[] arg1, String arg2) {
			// TODO Auto-generated method stub
			mRetStr = arg2;
			mSuccess = true;
			mRetHead = arg1;
		}
	};

	static public boolean isOk() {
		return mSuccess;
	}

	static public String getContent() {
		return mRetStr;
	}

	static public Header[] getHeader() {
		return mRetHead;
	}

}
