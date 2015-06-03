package children.lemoon.utils;

//ok
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class NetworkUtils {
	public static boolean checkNetwork(Context paramContext) {
		try {
			ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext.getSystemService("connectivity");
			boolean bool1 = false;
			if (localConnectivityManager != null) {
				NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
				bool1 = false;
				if (localNetworkInfo != null) {
					boolean bool2 = localNetworkInfo.isConnected();
					bool1 = false;
					if (bool2) {
						NetworkInfo.State localState1 = localNetworkInfo.getState();
						NetworkInfo.State localState2 = NetworkInfo.State.CONNECTED;
						bool1 = false;
						if (localState1 == localState2) {
							bool1 = true;
						}
					}
				}
			}
			return bool1;
		} catch (Exception localException) {
		}
		return false;
	}

	public static String getWifiSSID(Context paramContext) {
		return ((WifiManager) paramContext.getSystemService("wifi")).getConnectionInfo().getSSID();
	}

	public static boolean is3G(Context paramContext) {
		NetworkInfo localNetworkInfo = ((ConnectivityManager) paramContext.getSystemService("connectivity")).getActiveNetworkInfo();
		return (localNetworkInfo != null) && (localNetworkInfo.getType() == 0);
	}

	public static boolean isNetworkAvailable(Context paramContext) {
		ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext.getSystemService("connectivity");
		if (localConnectivityManager == null) {
			// cond0 -> goto1
			return false;
		}

		NetworkInfo[] arrayOfNetworkInfo = localConnectivityManager.getAllNetworkInfo();
		if ((arrayOfNetworkInfo == null) || (arrayOfNetworkInfo.length == 0)) {
			// cond0 -> goto1
			return false;
		}

		int i = 0;
		// goto0
		while (i < arrayOfNetworkInfo.length) {
			// cond1
			if (arrayOfNetworkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
				return true;
			}
			// cond2
			i++;
			// go goto0
		}

		// cond0 -> goto1
		return false;
	}

	public static boolean isWifi(Context paramContext) {
		NetworkInfo localNetworkInfo = ((ConnectivityManager) paramContext.getSystemService("connectivity")).getActiveNetworkInfo();
		return (localNetworkInfo != null) && (localNetworkInfo.getType() == 1);
	}

	public static boolean isWifiEnabled(Context paramContext) {
		ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext.getSystemService("connectivity");
		TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
		return ((localConnectivityManager.getActiveNetworkInfo() != null) && (localConnectivityManager.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED))
				|| (localTelephonyManager.getNetworkType() == 3);
	}

	public static void showNetWorkError(Context paramContext) {
	}
}
