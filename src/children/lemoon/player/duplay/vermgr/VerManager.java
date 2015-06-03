package children.lemoon.player.duplay.vermgr;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.HandlerThread;

public class VerManager {
	private static VerManager mInstance = null;
	private String updateApi = "http://cybertran.baidu.com/mediasdk/video?method=sdkupdate";
	public static final int RET_SUCCESS = 0;
	public static final int RET_ERROR_NETWORK = 1;
	public static final int RET_ERROR_AKSK = 2;
	public static final int RET_ERROR_NOTFOUND = 3;

	public static VerManager getInstance() {
		if (mInstance == null) {
			mInstance = new VerManager();
		}
		return mInstance;
	}

	public String getCurrentVersion() {
		return "baidu_developer_1_7_0_2";
	}

	public void getCurrentSystemCpuTypeAndFeature(final int timeout, final String ak, final String sk, final RequestCpuTypeAndFeatureCallback callback) {
		final HandlerThread thrd = new HandlerThread("getCurrentSystemCpuTypeAndFeature");
		thrd.start();

		Handler handler = new Handler(thrd.getLooper());
		handler.post(new Runnable() {
			public void run() {
				CPUINFO cinfo = getCpuInfoFromSvr(timeout, /* CPU_TYPE.UNKNOWN */null, ak, sk);
				if (CPUINFO.getLastStatus(cinfo) != 0) {
					CPUINFO.setCpuType(cinfo, getCpuTypeByName(CpuUtils.getCpuFullType()));
					CPUINFO.setLastStatus(cinfo, 0);
				}
				if (callback != null)
					callback.onComplete(CPUINFO.getCpuType(cinfo), CPUINFO.getLastStatus(cinfo));
				thrd.quit();
			}
		});
	}

	public void getDownloadUrlForCurrentVersion(final int timeout, final CPU_TYPE cpuType, final String ak, final String sk,
			final RequestDownloadUrlForCurrentVersionCallback callback) {
		final HandlerThread thrd = new HandlerThread("getDownUrlForCurrentVersion");
		thrd.start();
		Handler localHandler = new Handler(thrd.getLooper());
		localHandler.post(new Runnable() {
			public void run() {
				CPUINFO locala = getCpuInfoFromSvr(timeout, cpuType, ak, sk);
				callback.onComplete(CPUINFO.getDLUrl(locala), CPUINFO.getLastStatus(locala));
				thrd.quit();
			}
		});
	}

	private CPUINFO getCpuInfoFromSvr(int paramInt, CPU_TYPE paramCPU_TYPE, String ak, String sk) {
		CPUINFO cinfo = new CPUINFO();
		long tm = System.currentTimeMillis();
		String url = this.updateApi + "&ak=" + ak + "&time=" + tm + "&sign=" + CpuUtils.getMd5(URLEncoder.encode(tm + "req_videotran" + sk.substring(0, 16)))
				+ "&platform=android";
		if (paramCPU_TYPE != null) {
			url = url + "&getso=1&version=" + "baidu_developer_1_7_0_2".replace(".", "_") + "&cputype=" + getCpuNameByType(paramCPU_TYPE);
		}

		HttpPost post = new HttpPost(url);
		String str3 = null;
		try {
			StringEntity ent = new StringEntity(CpuUtils.getCpuType(), "utf8");
			post.setEntity((HttpEntity) ent);

			HttpParams param = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(param, paramInt);
			HttpConnectionParams.setSoTimeout(param, paramInt);
			DefaultHttpClient client = new DefaultHttpClient(param);
			HttpResponse resp = client.execute((HttpUriRequest) post);
			if (resp.getStatusLine().getStatusCode() == 200) {
				str3 = EntityUtils.toString(resp.getEntity(), "UTF-8");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (str3 != null) {
			// Object localObject3;
			try {
				JSONObject jobj = new JSONObject(str3);
				String err = jobj.optString("errno");
				if (paramCPU_TYPE == null) {
					if ((err.equals("200")) || (err.equals("404"))) {
						String cputype = jobj.optString("cputype");
						CPUINFO.setCpuType(cinfo, getCpuTypeByName(cputype));
						CPUINFO.setLastStatus(cinfo, 0);
					} else if (err.equals("403")) {
						CPUINFO.setLastStatus(cinfo, 2);
					}
				} else if (err.equals("200")) {
					String gurl = jobj.optString("geturl");
					CPUINFO.setDLUrl(cinfo, gurl);
					CPUINFO.setLastStatus(cinfo, 0);
				} else if (err.equals("403")) {
					CPUINFO.setLastStatus(cinfo, 2);
				} else {
					CPUINFO.setLastStatus(cinfo, 3);
				}
			} catch (JSONException localJSONException) {
				localJSONException.printStackTrace();
				CPUINFO.setLastStatus(cinfo, 3);
			}
		} else {
			CPUINFO.setLastStatus(cinfo, 1);
		}
		return cinfo;
	}

	private static CPU_TYPE getCpuTypeByName(String cpuName) {
		if (cpuName == null) {
			return CPU_TYPE.UNKNOWN;
		}
		if (cpuName.equals("armv5_none")) {
			return CPU_TYPE.ARMV5_NORMAL;
		}
		if (cpuName.equals("armv5_vfp")) {
			return CPU_TYPE.ARMV5_VFP;
		}
		if (cpuName.equals("armv6_none")) {
			return CPU_TYPE.ARMV6_NORMAL;
		}
		if (cpuName.equals("armv6_vfp")) {
			return CPU_TYPE.ARMV6_VFP;
		}
		if (cpuName.equals("armv7_vfp")) {
			return CPU_TYPE.ARMV7_VFP;
		}
		if (cpuName.equals("armv7_vfpv3")) {
			return CPU_TYPE.ARMV7_VFPV3;
		}
		if (cpuName.equals("armv7_neon")) {
			return CPU_TYPE.ARMV7_NEON;
		}
		if (cpuName.equals("intel_none")) {
			return CPU_TYPE.X86_NORMAL;
		}
		return CPU_TYPE.UNKNOWN;
	}

	private static String getCpuNameByType(CPU_TYPE paramCPU_TYPE) {
		if (paramCPU_TYPE == CPU_TYPE.ARMV5_NORMAL) {
			return "armv5_none";
		}
		if (paramCPU_TYPE == CPU_TYPE.ARMV5_VFP) {
			return "armv5_vfp";
		}
		if (paramCPU_TYPE == CPU_TYPE.ARMV6_NORMAL) {
			return "armv6_none";
		}
		if (paramCPU_TYPE == CPU_TYPE.ARMV6_VFP) {
			return "armv6_vfp";
		}
		if (paramCPU_TYPE == CPU_TYPE.ARMV7_VFP) {
			return "armv7_vfp";
		}
		if (paramCPU_TYPE == CPU_TYPE.ARMV7_VFPV3) {
			return "armv7_vfpv3";
		}
		if (paramCPU_TYPE == CPU_TYPE.ARMV7_NEON) {
			return "armv7_neon";
		}
		if (paramCPU_TYPE == CPU_TYPE.X86_NORMAL) {
			return "intel_none";
		}
		return "unknown";
	}

	static private class CPUINFO {
		private CPU_TYPE mCpuType = CPU_TYPE.UNKNOWN;
		private String mDlUrl = null;
		private int mLastStatus = 0;

		public static int getLastStatus(CPUINFO d) {
			return d.mLastStatus;
		}

		public static int setLastStatus(CPUINFO d, int c) {
			d.mLastStatus = c;
			return c;
		}

		public static CPU_TYPE getCpuType(CPUINFO d) {
			return d.mCpuType;
		}

		public static CPU_TYPE setCpuType(CPUINFO d, CPU_TYPE c) {
			d.mCpuType = c;
			return c;
		}

		public static String getDLUrl(CPUINFO d) {
			return d.mDlUrl;
		}

		public static String setDLUrl(CPUINFO d, String c) {
			d.mDlUrl = c;
			return c;
		}

	}

	public static abstract interface RequestDownloadUrlForCurrentVersionCallback {
		public abstract void onComplete(String paramString, int paramInt);
	}

	public static abstract interface RequestCpuTypeAndFeatureCallback {
		public abstract void onComplete(CPU_TYPE paramCPU_TYPE, int paramInt);
	}

	public static enum CPU_TYPE {
		UNKNOWN, ARMV5_NORMAL, ARMV5_VFP, ARMV6_NORMAL, ARMV6_VFP, ARMV7_VFP, ARMV7_VFPV3, ARMV7_NEON, X86_NORMAL

	}
}
