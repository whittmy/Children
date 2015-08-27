package children.lemoon.player.duplay.vermgr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;



import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultRedirectHandler;

import android.content.Context;
import android.util.Log;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

import children.lemoon.utils.Logger;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

public class SyncNetDIYClient {
	private SyncHttpClient client = null;
	private Context mcx;
	static private SyncNetDIYClient mInst;
	private PersistentCookieStore myCookieStore = null;

	HttpResponse mRespone;
	AsyncHttpResponseHandler mhandler = new AsyncHttpResponseHandler() {
		public boolean onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
			mRespone = response;

			if (mRespone != null) {
				Header[] headers = mRespone.getAllHeaders();
				if (headers != null) {
					// 获取cookie的第一种方式
					for (Header header : headers) {
						Logger.LOGD("LOGIN", header.getName() + "=" + header.getValue());
						if ("Set-Cookie".equalsIgnoreCase(header.getName())) {
							// Set-Cookie:
							// BAIDUID=F9A74ECEFE85E641598F8FAACC4C09CE:FG=1;
							// expires=Sat, 23-Apr-16 08:06:04 GMT;
							// max-age=31536000; path=/; domain=.baidu.com;
							// version=1
							String[] items = header.getValue().split("; ");
							if (items != null) {
								BasicClientCookie newCookie = null;
								for (String item : items) {
									String[] info = item.split("=");
									if (info[0].equals(ClientCookie.COMMENT_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
											// newCookie.setComment(info[1]);
										}
									} else if (info[0].equals(ClientCookie.COMMENTURL_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
										}
									} else if (info[0].equals(ClientCookie.DISCARD_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
										}
									} else if (info[0].equals(ClientCookie.DOMAIN_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
											newCookie.setDomain(info[1]);
										}
									} else if (info[0].equals(ClientCookie.EXPIRES_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
											SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH);
											try {
												Date s = sdf.parse(info[1]);
												newCookie.setExpiryDate(s);
											} catch (ParseException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}
									} else if (info[0].equals(ClientCookie.MAX_AGE_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
										}
									} else if (info[0].equals(ClientCookie.PATH_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
											newCookie.setPath(info[1]);
										}
									} else if (info[0].equals(ClientCookie.PORT_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
										}
									} else if (info[0].equals(ClientCookie.SECURE_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
										}
									} else if (info[0].equals(ClientCookie.VERSION_ATTR)) {
										if (newCookie != null) {
											newCookie.setAttribute(info[0], info[1]);
											newCookie.setVersion(Integer.valueOf(info[1]));
										}
									} else {
										newCookie = new BasicClientCookie(info[0], info[1]);
									}
								}
								if (newCookie != null)
									myCookieStore.addCookie(newCookie);
							}
						}
					}
				}
			}
			return true;
		};

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
		}

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
		}

	};

	private SyncNetDIYClient(Context cx) {
		mcx = cx;
		client = new SyncHttpClient();

		// 只有如下设置，方可启用 访问带cookie(读取、存储)
		myCookieStore = new PersistentCookieStore(mcx);

		client.setCookieStore(myCookieStore);
		client.setTimeout(300000);
		client.setConnectTimeout(30000);
		client.setMaxConnections(30);
	}

	public static SyncNetDIYClient getInstance(Context cx) {
		if (mInst == null) {
			mInst = new SyncNetDIYClient(cx);
		}
		return mInst;
	}

	public HttpResponse getRespone() {
		return mRespone;
	}

	public void get(String url, Header[] headers, RequestParams params) {
		Logger.LOGD("SyncNetDIYClient", "get url=" + url);
		client.get(mcx, url, headers, params, mhandler);
	}

	public void post(String url, Header[] headers, String contentType, RequestParams params) {
		Logger.LOGD("SyncNetDIYClient", "POST url=" + url);
		client.post(mcx, url, headers, params, contentType, mhandler);
	}

	public void addCookie(Cookie cookie) {
		myCookieStore.addCookie(cookie);
	}

	public CookieStore getAllCookies() {
		HttpContext httpContext = client.getHttpContext();
		return (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
	}

	public List<String> getAllCookieList() {
		List<String> cookielist = new ArrayList<String>();
		HttpContext httpContext = client.getHttpContext();
		CookieStore cookies = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		for (Cookie c : cookies.getCookies()) {
			cookielist.add(c.getName() + "=" + c.getValue());
		}

		return cookielist;
	}

	public List<String> getCookieList(String url) {
		URL aURL;
		List<String> cookielist = new ArrayList<String>();
		try {
			aURL = new URL(url);
			System.out.println("protocol = " + aURL.getProtocol());
			System.out.println("authority = " + aURL.getAuthority());
			System.out.println("host = " + aURL.getHost());
			System.out.println("port = " + aURL.getPort());
			System.out.println("path = " + aURL.getPath());
			System.out.println("query = " + aURL.getQuery());
			System.out.println("filename = " + aURL.getFile());
			System.out.println("ref = " + aURL.getRef());

			CookieStore cookies = getAllCookies();
			for (Cookie c : cookies.getCookies()) {
				if (aURL.getHost().contains(c.getDomain())) {
					cookielist.add(c.getName() + "=" + c.getValue());
				}
			}

			return cookielist;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
