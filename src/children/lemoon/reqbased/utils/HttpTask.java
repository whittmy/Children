package children.lemoon.reqbased.utils;

//ok
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import children.lemoon.Configer;
import children.lemoon.reqbased.entry.BasePaserMessageUtil;
import children.lemoon.utils.Logger;

import com.google.gson.Gson;

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
import java.util.zip.ZipOutputStream;

//import lib.runningman.RunningMan;


public class HttpTask extends AsyncTask<RequestMethod, Integer, Message> {
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

//	private RunningMan mRunMan;
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
//		if (context != null)
//			mRunMan = new RunningMan(context);
	}

	/*
	 * 两种请求方式： get/post
	 */
	protected Message doInBackground(RequestMethod... param) {
		URL ur = null; // v12
		String resp = ""; // v10
		
		long bgtm = System.currentTimeMillis();
		try {
			ur = new URL(url);

			// goto0
			int s = request_method()[param[0].ordinal()];
			switch (s) {
			case 1: // switch0 get
				BufferedReader reader = null; // reader
				
				// + retry
				for(int retries=0; retries < 3; retries++){
					boolean bok = false;
					try {
						String requestParam = "?header=" + URLEncoder.encode(jsonObjectHeader, "utf-8"); // v9
						requestParam += "&body=" + URLEncoder.encode(jsonObjectBody, "utf-8");

						URL getRequestUrl = new URL(url + requestParam);
						Logger.LOGE("Request", getRequestUrl.toString());
						urlConnDownLoad = (HttpURLConnection) getRequestUrl.openConnection();
						urlConnDownLoad.setConnectTimeout(15000);
						urlConnDownLoad.setReadTimeout(15000);
						urlConnDownLoad.setInstanceFollowRedirects(true);
						urlConnDownLoad.connect();
						reader = new BufferedReader(new InputStreamReader(urlConnDownLoad.getInputStream()));// v8

						String str = null; // v11
						// goto2
						while ((str = reader.readLine()) != null) {
							// cond1
							resp += str;
						}
						responseResult = resp;
						
//						reader.close();
//						urlConnDownLoad.disconnect();	
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
						Log.e("", "#################delt="+(System.currentTimeMillis()-bgtm));
						break;
					}
					
					try{
						Thread.sleep(200);
					}
					catch(Exception e){}
				}
				// go goto1
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

					// cond2 / goto4
					outDown.flush();
					outDown.close();

					InputStream inputStream = urlConnDownLoad.getInputStream(); // v5
					int code = urlConnDownLoad.getResponseCode();
					if (code != 0xc8) {
						// cond0
						return null;
					}

					BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream));
					String str = null;
					// goto_5
					while ((str = breader.readLine()) != null) {
						// cond3
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
		// cond0/goto1
		return null;
	}

	protected void onPostExecute(Message paramMessage) {
		super.onPostExecute(paramMessage);
		this.paseUtil.parse(this.responseClass, this.url, this.requestType, this.responseResult, this.mHandler, this.context, this.cache, this.bodyRequest,
				true);
//		if (mRunMan != null) {
//			mRunMan.hide();
//			mRunMan.close();
//		}
	}

	protected void onPreExecute() {
		super.onPreExecute();
		Gson localGson = new Gson();
		// HashMap<Object, Object> localHashMap = new HashMap<Object, Object>();
		// localHashMap.put("header", localGson.toJson(wrapHeader()));
		// localHashMap.put("body", localGson.toJson(this.bodyRequest));
		this.jsonObjectHeader = localGson.toJson(wrapHeader());
		this.jsonObjectBody = localGson.toJson(this.bodyRequest);
 
	}

	public Map<String, String> wrapHeader() {
		HashMap<String, String> localHashMap = new HashMap<String, String>();
 		 int versionCode = DeviceUtil.getVersionCode(context);
 		 String timeStamp = TimeUtil.getTimeStamp();
 		 
		 localHashMap.put("vercode", String.valueOf(versionCode));
  		 localHashMap.put("reqtime", timeStamp);
		 localHashMap.put("sign", MyUtil.getSign(Long.valueOf(timeStamp)));
		 localHashMap.put("mac", DeviceUtil.getMacAddress());
 
		return localHashMap;
	}
}
