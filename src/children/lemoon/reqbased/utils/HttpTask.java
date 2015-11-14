package children.lemoon.reqbased.utils;

//ok
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import children.lemoon.Configer;
import children.lemoon.reqbased.entry.BasePaserMessageUtil;
import children.lemoon.utils.Logger;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

//import lib.runningman.RunningMan;


public class HttpTask extends AsyncTask<RequestMethod, Integer, Message> {
	String TAG = "HttpTask";
	Map<String, Object> bodyRequest;
	private boolean cache;
	private Context context;
	private boolean isZip;
	private String jsonObjectBody;
	private String jsonObjectHeader;
	private Handler mHandler;
	private Message message;
	BasePaserMessageUtil paseUtil;
	private int requestType;
	Class<Object> responseClass;
	Object responseResult = null;
	private String url = "";
	private HttpURLConnection urlConnDownLoad;

	private static int[] request_method = null;
	private boolean bshowLoading;

	static int[] request_method() {
		if (request_method != null) {
			return request_method;
		}

		request_method = new int[RequestMethod.values().length];

		request_method[RequestMethod.GET.ordinal()] = 1;
		request_method[RequestMethod.POST.ordinal()] = 2;

		return request_method;
	}

	public HttpTask(int requestType, Handler handler, String url, Context context, Map<String, Object> bodyRequest, boolean bcache, Class<Object> paramClass,
			BasePaserMessageUtil paseUtil, boolean bzip, boolean showLoading) {
		this.mHandler = handler;
		this.context = context;
		this.requestType = requestType;
		this.url = url;
		this.bodyRequest = bodyRequest;
		this.cache = bcache;
		this.responseClass = paramClass;
		this.paseUtil = paseUtil;
		this.isZip = bzip;

		bshowLoading = showLoading;
	}

	/*
	 * 两种请求方式： get/post
	 */
	/*
	protected Message doInBackground(RequestMethod... param) {
		URL ur = null;
		String resp = ""; 
		
		long bgtm = System.currentTimeMillis();
		try {
			ur = new URL(url);
			int s = request_method()[param[0].ordinal()];
			switch (s) {
			case 1: 
				BufferedReader reader = null; 
				// + retry
				for(int retries=0; retries < 3; retries++){
					boolean bok = false;
					try {
						String requestParam = "?header=" + URLEncoder.encode(jsonObjectHeader, "utf-8"); // v9
						requestParam += "&body=" + URLEncoder.encode(jsonObjectBody, "utf-8");

						URL getRequestUrl = new URL(url + requestParam);
						Logger.LOGE("Request", getRequestUrl.toString());
						urlConnDownLoad = (HttpURLConnection) getRequestUrl.openConnection();
						urlConnDownLoad.setConnectTimeout(3000);	//连接时间尽可能短，其仅仅代表ip通与不通的问题，没必要在其上面浪费时间
						urlConnDownLoad.setReadTimeout(15000);
						urlConnDownLoad.setInstanceFollowRedirects(true);
						urlConnDownLoad.connect();
						reader = new BufferedReader(new InputStreamReader(urlConnDownLoad.getInputStream()));// v8

						String str = null; 
						while ((str = reader.readLine()) != null) {
							resp += str;
						}
						responseResult = resp;
						
						reader.close(); reader = null;
						urlConnDownLoad.disconnect(); urlConnDownLoad = null;	
						bok = true;
					}
					catch (MalformedURLException e) {
				        // TODO Auto-generated catch block
				        e.printStackTrace();
					}
					catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}	
					finally{
				        if(urlConnDownLoad != null){
				        	urlConnDownLoad.disconnect();
				        }
				        
				        if(reader != null){
				        	try {
								reader.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        }
				    }
					
					if(bok){
						Logger.LOGD("", "#################delt="+(System.currentTimeMillis()-bgtm));
						break;
					}
					
					try{
						Thread.sleep(200);
					}
					catch(Exception e){}
				}
				return null;

			case 2: // swich1 post
				try {
					urlConnDownLoad = (HttpURLConnection) ur.openConnection();
					urlConnDownLoad.setDoOutput(true);
					urlConnDownLoad.setDoInput(true);
					urlConnDownLoad.setRequestMethod("POST");
					urlConnDownLoad.setUseCaches(false);
					urlConnDownLoad.setConnectTimeout(40000);
					urlConnDownLoad.setReadTimeout(40000);
					urlConnDownLoad.setInstanceFollowRedirects(true);
					urlConnDownLoad.setRequestProperty("Content-type", "text/html;charset=UTF-8");
					urlConnDownLoad.connect();

					DataOutputStream outDown = new DataOutputStream(urlConnDownLoad.getOutputStream()); // v6
					if (isZip) {
						ZipOutputStream zipOut = new ZipOutputStream(outDown); // v14
						zipOut.close();
					}

					outDown.flush();
					outDown.close();

					InputStream inputStream = urlConnDownLoad.getInputStream(); // v5
					int code = urlConnDownLoad.getResponseCode();
					if (code != 0xc8) {
						return null;
					}

					BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream));
					String str = null;
					while ((str = breader.readLine()) != null) {
						resp += str;
					}

					responseResult = resp;
					Logger.LOGD("jsonObject", "responseResult=" + responseResult);

				} catch (UnsupportedEncodingException e1) {
					mHandler.obtainMessage(requestType, 4, 0, responseResult).sendToTarget();
					return null;
				} catch (ProtocolException e) {
					e.printStackTrace();
					mHandler.obtainMessage(requestType, 4, 0, responseResult).sendToTarget();
					return null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e) {

		}
		return null;
	}
	*/

	///>>>>>>>>>>
	private boolean mIsCancle = false;
	private HttpGet mGet;
	private HttpClient mHttp;
	
	protected Message doInBackground(RequestMethod... param) {
		// 请求数据
		String resp = ""; 
		
		int s = request_method()[param[0].ordinal()];
		switch (s) {
		case 1: //GET
			String requestParam = null;
			try {
				requestParam = "?header=" + URLEncoder.encode(jsonObjectHeader, "utf-8") + "&body=" + URLEncoder.encode(jsonObjectBody, "utf-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}  
			
			BufferedReader reader = null;
			Logger.LOGD(TAG, url + requestParam);
			mGet = initHttpGet(url + requestParam);
			mHttp = initHttp();
			try {
				HttpResponse response = mHttp.execute(mGet);
				if (mIsCancle) {
					return null;
				}
				if (response != null) {
					if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						Logger.LOGD(TAG, "the code is :" + response.getStatusLine().getStatusCode());
						return null;
					}
					
					//String strResult = EntityUtils.toString(response.getEntity());
					InputStream is = response.getEntity().getContent();  
			        Header contentEncoding = response.getFirstHeader("Content-Encoding");  
		            if (contentEncoding != null  
		                    && contentEncoding.getValue().equalsIgnoreCase("gzip")) {  
		                is = new GZIPInputStream(new BufferedInputStream(is));  
		            }  
		            
					reader = new BufferedReader(new InputStreamReader(is));

					String str = null; 
					while ((str = reader.readLine()) != null) {
						resp += str;
					}
					responseResult = resp;
					
				}
			} catch (ConnectTimeoutException e) {
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if(reader != null){
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					reader = null;
				}
				mHttp.getConnectionManager().shutdown();
			}			
			
			break;
		}
		
		return null;
	}

	private HttpClient initHttp() {
		HttpClient client = new DefaultHttpClient();
		client.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 30000); // 超时设置
		client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 15000);// 连接超时
		return client;
	}

	private HttpGet initHttpGet(String mUrl) {
		HttpGet get = new HttpGet(mUrl);
		//initHeader(get);
		return get;
	}

	public boolean tryCancel() {
		Logger.LOGD(TAG, "tryCanle is working");
		mGet.abort();
		mIsCancle = true;
		mHttp.getConnectionManager().shutdown();
		return true;
	}
	//<<<<<<<
	protected void onPostExecute(Message paramMessage) {
		super.onPostExecute(paramMessage);
		this.paseUtil.parse(this.responseClass, this.url, this.requestType, this.responseResult, this.mHandler, this.context, this.cache, this.bodyRequest,
				true);
	}

	protected void onPreExecute() {
		super.onPreExecute();
		Gson localGson = new Gson();
		this.jsonObjectHeader = localGson.toJson(wrapHeader());
		this.jsonObjectBody = localGson.toJson(this.bodyRequest);
 
	}

	public Map<String, String> wrapHeader() {
		HashMap<String, String> headers = new HashMap<String, String>();
 		 int versionCode = DeviceUtil.getVersionCode(context);
 		 String timeStamp = TimeUtil.getTimeStamp();
 		 
 		 
 		String firmware = "";
        try {
        	Context otherAppsContext = context.createPackageContext("adjusttime.lemoon", 0);
            SharedPreferences  sp = otherAppsContext.getSharedPreferences("firm_info", Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
            
	        firmware = sp.getString("ota_ver", "");
	   		if(firmware.isEmpty()){
	   			firmware = sp.getString("firm_ver","");
	   		}
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        }
        
		

		 headers.put("vercode", String.valueOf(versionCode));
  		 headers.put("reqtime", timeStamp);
		 headers.put("sign", MyUtil.getSign(Long.valueOf(timeStamp)));
		 headers.put("mac", DeviceUtil.getMacAddress());
		 headers.put("firmware", firmware);
		return headers;
	}
}
