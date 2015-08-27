package children.lemoon.player.duplay.vermgr;

 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;



import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import children.lemoon.utils.Logger;

import com.baidu.cyberplayer.utils.VersionManager.CPU_TYPE;
import com.baidu.cyberplayer.utils.VersionManager.RequestCpuTypeAndFeatureCallback;
import com.baidu.cyberplayer.utils.VersionManager.RequestDownloadUrlForCurrentVersionCallback;
import com.baidu.cyberplayer.utils.ZipUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.util.Log;

public class PlayLibsVerMgr {
	com.baidu.cyberplayer.utils.VersionManager mVM;
	// 您的ak
	private String AK = "Ar4jru4mrAoHI8nTudiBCAfa";
	// 您的sk的前16位
	private String SK = "S2xkwtQZe0dOG8UhrbTbO18TzRHCnZOO";

	public static String CFG_NAME = "plyverctrl";
	public static String DU_VAL_CURVER = "du_curDuVer";
	public static String DU_VAL_HADLOAD = "du_hadload";

	SharedPreferences mPref;
	Context mCx;

	public PlayLibsVerMgr(Context cx) {
		mPref = cx.getSharedPreferences(CFG_NAME, Context.MODE_MULTI_PROCESS);
		mCx = cx;
	}

	public void Init() {
		vtChk();
		bdChk();
	}

	public void vtChk() {
//		// 实际应该检查，是否有新版本的so文件，不过目前没有维护了，就不检查更新
//		if (Vitamio.isInitialized(mCx))
//			return;
//
//		int type = Vitamio.getVitamioType();
//		final String soUrl = "http://www.nybgjd.com/3dclub/vermgr/vtsoupgrade/" + type;
//
//		AsyncHttpClient client = new AsyncHttpClient();
//		client.get(soUrl, new FileAsyncHttpResponseHandler(mCx) {
//			@Override
//			public void onSuccess(int arg0, Header[] arg1, File arg2) {
//				// TODO Auto-generated method stub
//				if (arg2 == null || arg2.length() < 15) {
//					Logger.LOGD("vtso has not get successful");
//					return;
//				}
//
//				// String md5 = null;
//				// for(Header h : arg1){
//				// if(h.getName().equals("Content-MD5")){
//				// md5 = h.getValue();
//				// break;
//				// }
//				// }
//				//
//				// if(md5 == null)
//				// return;
//
//				try {
//					String dlfileMd5 = getMd5ByFile(arg2);
//					// if (!md5.equalsIgnoreCase(dlfileMd5))
//					// return;
//
//					// 准备loadlibrary
//					if (Vitamio.initialize(mCx, arg2)) {
//						Logger.LOGE("", "load vitamio successfull");
//					}
//
//				} catch (FileNotFoundException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void onFailure(int arg0, Header[] arg1, Throwable arg2, File arg3) {
//				// TODO Auto-generated method stub
//
//			}
//		});

	}

	public static boolean vmoLibsHadOk(Context cx) {
		// return Vitamio.isInitialized(cx);
		return false;
	}

	public static boolean t5LibsHadOk(Context cx) {
		SharedPreferences pref = cx.getSharedPreferences(CFG_NAME, Context.MODE_MULTI_PROCESS);
		boolean s = pref.getBoolean(DU_VAL_HADLOAD, false);
		return s;
	}

	public void bdChk() {
		final String curDuMd5 = mPref.getString(DU_VAL_CURVER, null);
		// note: 不用再判断， 再其它位置 直接根据 getDuHadOk状态判断
		// if (curDuMd5 != null) {
		// // 已经存在，则可以先使用之前的，当前继续检查、下载、更换，下次生效
		// // ..... 可以直接使用
		// }

		mVM = new com.baidu.cyberplayer.utils.VersionManager();
		mVM.getCurrentSystemCpuTypeAndFeature(30000, AK, SK, new RequestCpuTypeAndFeatureCallback() {
			@Override
			public void onComplete(CPU_TYPE arg0, int arg1) {
				// TODO Auto-generated method stub
				mVM.getDownloadUrlForCurrentVersion(3000000, arg0, AK, SK, new RequestDownloadUrlForCurrentVersionCallback() {
					@Override
					public void onComplete(String arg0, int arg1) {
						// TODO Auto-generated method stub
						// arg0: url,
						// 1. 下载zip包， “Content-MD5”头字段用于校验
						// 2. 解压zip包，获取 libcyberplayer.so 和
						// libcyberplayer-core.so
						// 3. so文件拷贝到 /data/data/your_app_package_name/files
						// 4. 播放之前 BVideoView.setNativeLibsDirectory()设置 so
						// 所在的目录路径
						Logger.LOGD("onComplete", "arg0=" + arg0 + ", arg1=" + arg1);
						final String url = arg0;
						if (arg0 == null)
							return;

						// final String destmd5 = null;
						SyncNetDIYClient client = SyncNetDIYClient.getInstance(mCx);
						client.get(url, null, null);
						HttpResponse respone = client.getRespone();
						if (respone == null)
							return;
						Header[] hs = respone.getHeaders("Content-MD5");
						if (hs == null || hs.length == 0) {
							return;
						}

						final String newMd5 = hs[0].getValue();
						if (curDuMd5 != null && curDuMd5.equalsIgnoreCase(newMd5)) {
							// have no update, but file is exist???
							String path = getSoFileDir(mCx);
							if (new File(path + "libcyberplayer.so").exists() && new File(path + "libcyberplayer-core.so").exists())
								return;
						}

						// have updated!!!! download here
						((Activity) mCx).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								AsyncHttpClient client = new AsyncHttpClient();
								client.get(url, new FileAsyncHttpResponseHandler(mCx) {
									@Override
									public void onSuccess(int arg0, Header[] arg1, File arg2) {
										// TODO Auto-generated method stub
										try {
											String dlfileMd5 = getMd5ByFile(arg2);
											if (!newMd5.equalsIgnoreCase(dlfileMd5))
												return;

											ZipUtils z = ZipUtils.getInstance();
											z.unZip(mCx, arg2.getAbsolutePath(), getSoFileDir(mCx));

											// 提交
											mPref.edit().putString(DU_VAL_CURVER, newMd5).commit();

											// 至此，方可使用...................
											mPref.edit().putBoolean(DU_VAL_HADLOAD, true).commit();
											// ////////// ok //////////////////

										} catch (FileNotFoundException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}

									@Override
									public void onFailure(int arg0, Header[] arg1, Throwable arg2, File arg3) {
										// TODO Auto-generated method stub

									}
								});
							}
						});
					}
				});
			}
		});
	}

	// /////////////////////////
	public static String getSoFileDir(Context context) {
		String filepath = null;

		// if
		// (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
		// || !Environment.isExternalStorageRemovable()) {
		// filepath = context.getExternalFilesDir(null).getPath();
		// } else {
		// filepath = context.getFilesDir().getPath();
		// }

		PackageManager pm = context.getPackageManager();
		try {
			filepath = pm.getApplicationInfo(context.getPackageName(), 0).dataDir;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			filepath = context.getFilesDir().getPath();
		}

		if (filepath != null && !filepath.endsWith("/"))
			filepath += "/";
		return filepath;
	}

	private String getMd5ByFile(File file) throws FileNotFoundException {
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

}
