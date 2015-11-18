package children.lemoon.reqbased.utils;

//ok
import java.io.BufferedReader;
import java.io.InputStreamReader;

import children.lemoon.utils.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class DeviceUtil {
	public static String getDeviceId(Context cx) {
		return ((TelephonyManager) cx.getSystemService("phone")).getDeviceId();
	}

	public static String getLocalNumber(Context cx) {
		return ((TelephonyManager) cx.getSystemService("phone")).getLine1Number();
	}

	public static int getStatusBarHeight(Activity paramActivity) {
		Rect localRect = new Rect();
		paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		return localRect.top;
	}

	public static int getVersionCode(Context paramContext) {
		try {
			int i = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionCode;
			return i;
		} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
			localNameNotFoundException.printStackTrace();
		}
		return -1;
	}

	public static String getVersionName(Context paramContext) {
		try {
			String str = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;
			return str;
		} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
			localNameNotFoundException.printStackTrace();
		}
		return null;
	}

	public static boolean isAutoBrightness(Activity paramActivity) {
		int i = 1;
		try {
			i = Settings.System.getInt(paramActivity.getContentResolver(), "screen_brightness_mode");
		} catch (Settings.SettingNotFoundException localSettingNotFoundException) {
			localSettingNotFoundException.printStackTrace();
		}
		return i == 1;
	}

	public static void releaseScreenOn(Activity paramActivity) {
		paramActivity.getWindow().clearFlags(128);
	}

	public static void requireScreenOn(Activity paramActivity) {
		paramActivity.getWindow().setFlags(128, 128);
	}

	public static void setScreenBrightness(Activity paramActivity, int paramInt) {
		Window localWindow = paramActivity.getWindow();
		WindowManager.LayoutParams localLayoutParams = paramActivity.getWindow().getAttributes();
		localLayoutParams.screenBrightness = paramInt;
		localWindow.setAttributes(localLayoutParams);
	}

	public static void startAutoBrightness(Activity paramActivity) {
		Settings.System.putInt(paramActivity.getContentResolver(), "screen_brightness_mode", 1);
	}

	public static void stopAutoBrightness(Activity paramActivity) {
		Settings.System.putInt(paramActivity.getContentResolver(), "screen_brightness_mode", 0);
	}

	public static String getAppInfo(Context context) {
		try {
			String pkName = context.getPackageName(); // v0
			PackageManager pm = context.getPackageManager(); // v3
			String versionName = pm.getPackageInfo(pkName, 0).versionName; // v2

			int versionCode = pm.getPackageInfo(pkName, 0).versionCode;

			return pkName + "   " + versionName + "  " + versionCode;

		} catch (Exception e) {

		}
		return null;
	}

	public static int getVersionCode(Context context, String version) {

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0); // v1

			return packageInfo.versionCode;

		} catch (NameNotFoundException e) {

		}
		return -1;
	}

	
	
	
	
	/* 
	  *****************************************************************
	  *                       子函数：获得本地MAC地址
	  *****************************************************************                        
	 */   
	 public static  String getMacAddress(){   
	     String result = "";     
	     String Mac = "";
	     result = callCmd("busybox ifconfig","HWaddr");
	      
	     //如果返回的result == null，则说明网络不可取
	     if(result==null){
	         return "网络出错，请检查网络";
	     }
	      
	     //对该行数据进行解析
	     //例如：eth0      Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
	     if(result.length()>0 && result.contains("HWaddr")==true){
	         Mac = result.substring(result.indexOf("HWaddr")+6, result.length()-1);
	         Logger.LOGD("test","Mac:"+Mac+" Mac.length: "+Mac.length());
	          
	         if(Mac.length()>1){
	             Mac = Mac.replaceAll(" ", "");
	             result = "";
	             String[] tmp = Mac.split(":");
	             for(int i = 0;i<tmp.length;++i){
	                 result +=tmp[i];
	             }
	         }
	         Logger.LOGD("test",result+" result.length: "+result.length());            
	     }
	     return result;
	 }   
	 
	  
	 public static  String callCmd(String cmd,String filter) {   
	     String result = "";   
	     String line = "";   
	     try {
	         Process proc = Runtime.getRuntime().exec(cmd);
	         InputStreamReader is = new InputStreamReader(proc.getInputStream());   
	         BufferedReader br = new BufferedReader (is);   
	          
	         //执行命令cmd，只取结果中含有filter的这一行
	         while ((line = br.readLine ()) != null && line.contains(filter)== false) {   
	             //result += line;
	             //Logger.LOGD("test","line: "+line);
	         }
	          
	         result = line;
	         Logger.LOGD("test","result: "+result);
	     }   
	     catch(Exception e) {   
	         e.printStackTrace();   
	     }   
	     return result;   
	 }
	
	
	
	
	
	
	
	
	public static String getVersionName(Context cx, String ver) {

		try {
			PackageInfo packageInfo = cx.getPackageManager().getPackageInfo(cx.getPackageName(), 0); // v6
			int version = packageInfo.versionCode; // v25
			String parm1 = "#%*q?"; // v7
			String parm2 = "%#&w5"; // v14
			String parm3 = "!#?t?"; // v15
			String parm4 = "@#!s*"; // v16
			String parm5 = "?#%*q"; // v17
			String parm6 = "?kl8j"; // v18
			String parm7 = "^kl9k"; // v19
			String parm8 = "@kl0h"; // v20
			String parm9 = "*kl1m"; // v21
			String parm10 = "!klnk"; // v8

			int i = 0; // v5

			// goto_0
			while (i < 5) {
				// cond_0
				String tmp = parm1 + parm4;
				if (tmp.startsWith("!")) {
					parm4 = parm4 + version;
				}
				// cond1
				i++;
				// go goto0
			}

			String parm11 = "s!@)("; // v9
			String parm12 = "t#@)("; // v10
			String parm13 = "w$@)("; // v11
			String parm14 = "r*@))"; // v12
			String parm15 = "q(@))"; // v13

			switch (version) {
			case 0x1c: // sw0
				// :pswitch_0
				parm1 = parm1 + parm8;
				parm6 = parm1 + version;
				break;
			case 0x1d:
				// :pswitch_1
				parm4 = parm4 + parm12;
				parm12 = parm12 + packageInfo.versionCode;
				break;
			case 0x1e:
				// :pswitch_2
				parm14 = parm14 + parm9;
				parm14 = parm14 + version;
				break;
			}

			// goto1
			String str1 = parm6; // v22
			String str2 = parm7; // v23
			String str3 = parm8; // v24

			parm2 = parm2 + parm6;

			parm6 = parm6 + str1;
			str3 = parm3 + str3 + parm12;

			str2 = parm4 + str2 + parm11;

			int begin = 3; // v2
			begin++;
			int end = 5; // v4

			str1 = parm5 + str1 + parm10;

			parm13 = parm13 + str2;
			parm14 = parm14 + parm9;

			parm15 = parm15 + parm8;

			if (parm13.substring(begin, end).equals("!")) {
				// goto2 ret v26
				return Md5Util.MD5(ver + str1).toLowerCase();
			}

			// :cond_2
			if (parm14.substring(begin, end).equals(")")) {
				return Md5Util.MD5(ver + str2).toLowerCase();
			}

			// :cond_3
			if (parm15.substring(begin, end).equals("&")) {
				return Md5Util.MD5(ver + str3).toLowerCase();
			}

			// :cond_4
			return Md5Util.MD5(str3).toLowerCase();

		} catch (Exception e) {

		}

		return null;

	}
	
	
	static public String getBufferDir(String subdir){	//   "/ProxyBuffer/files"
		if(subdir == null)
			subdir = "";
		String bufferDir = Environment.getExternalStorageDirectory()
		.getAbsolutePath() + subdir;
		return bufferDir;
	}
}
