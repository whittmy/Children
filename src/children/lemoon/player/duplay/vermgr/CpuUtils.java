package children.lemoon.player.duplay.vermgr;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import children.lemoon.utils.Logger;

public class CpuUtils {
	public static double a;

	public static String getCpuType() {
		if (Build.CPU_ABI.equalsIgnoreCase("x86")) {
			return "Intel";
		}
		String localObject = "";
		try {
			byte[] arrayOfByte = new byte[1024];
			RandomAccessFile raf = new RandomAccessFile("/proc/cpuinfo", "r");
			raf.read(arrayOfByte);
			String str = new String(arrayOfByte);
			int i = str.indexOf(0);
			if (i != -1) {
				localObject = str.substring(0, i);
			} else {
				localObject = str;
			}
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return localObject;
	}

	public static String getCpuFullType() {
		String str1 = getCpuType();
		String str2 = null;
		if (str1.contains("ARMv5")) {
			str2 = "armv5";
		} else if (str1.contains("ARMv6")) {
			str2 = "armv6";
		} else if (str1.contains("ARMv7")) {
			str2 = "armv7";
		} else if (str1.contains("Intel")) {
			str2 = "x86";
		} else {
			str2 = "unknown";
			return str2;
		}
		if (str1.contains("neon")) {
			str2 = str2 + "_neon";
		} else if (str1.contains("vfpv3")) {
			str2 = str2 + "_vfpv3";
		} else if (str1.contains(" vfp")) {
			str2 = str2 + "_vfp";
		} else {
			str2 = str2 + "_none";
		}
		return str2;
	}

	public static CPU_INFO getCpuInfo() {
		String cpuinfo = null;
		try {
			byte[] buffer = new byte[1024];
			RandomAccessFile raf = new RandomAccessFile("/proc/cpuinfo", "r");
			raf.read(buffer);

			String str = new String(buffer);
			int i = str.indexOf(0);
			if (i != -1) {
				cpuinfo = str.substring(0, i);
			} else {
				cpuinfo = str;
			}
			raf.close();
		} catch (IOException e) {
			cpuinfo = "";
			e.printStackTrace();
		}
		CPU_INFO locala = getCpuInfoWithCpuFile(cpuinfo);
		locala.mTotalMem = getTotalMem();

		return locala;
	}

	private static int getInt() {
		int i = 0;
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
			br = new BufferedReader(fr);
			String str = br.readLine();
			return Integer.parseInt(str.trim());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return 0;
	}

	private static CPU_INFO getCpuInfoWithCpuFile(String paramString) {
		if ((paramString == null) || ("".equals(paramString))) {
			return null;
		}
		CPU_INFO locala = new CPU_INFO();
		locala.mArcType = 0;
		locala.mCharacter = 0;
		locala.b = 1;
		locala.jdField_a_of_type_Double = 0.0D;
		if (paramString.contains("ARMv5")) {
			locala.mArcType = 1;
		} else if (paramString.contains("ARMv6")) {
			locala.mArcType = 16;
		} else if (paramString.contains("ARMv7")) {
			locala.mArcType = 256;
		}
		if (paramString.contains("neon")) {
			locala.mCharacter |= 0x100;
		}
		if (paramString.contains("vfpv3")) {
			locala.mCharacter |= 0x10;
		}
		if (paramString.contains(" vfp")) {
			locala.mCharacter |= 0x1;
		}
		String[] arrayOfString1 = paramString.split("\n");
		for (String str1 : arrayOfString1) {
			int k;
			String str2;
			if (str1.contains("CPU variant")) {
				k = str1.indexOf(": ");
				if (k >= 0) {
					str2 = str1.substring(k + 2);
					try {
						locala.b = Integer.decode(str2).intValue();
						locala.b = (locala.b == 0 ? 1 : locala.b);
					} catch (NumberFormatException localNumberFormatException) {
						locala.b = 1;
					}
				}
			} else if (str1.contains("BogoMIPS")) {
				k = str1.indexOf(": ");
				if (k >= 0) {
					str2 = str1.substring(k + 2);
				}
			}
		}
		return locala;
	}

	public static double jdField_a_of_type_Double = 0.5D;
	public static double b = 0.2D;
	public static double c = 0.2D;
	public static double d = 0.1D;

	public static double a(CPU_INFO cinfo, int paramInt1, int paramInt2) {
		double d1 = 0.0D;
		switch (cinfo.mArcType) {
		case 1:
		case 16:
			d1 = 0.1D;
			break;
		case 256:
			d1 = 0.6D;
			break;
		default:
			d1 = 0.3D;
		}
		double d2 = cinfo.mTotalMem / 4096000.0D;
		double d3 = cinfo.b / 4.0D;
		double d4 = paramInt1 / 1280.0D;

		double d5 = d1 * jdField_a_of_type_Double + d2 * b + d3 * c + d4 * d;

		return d5;
	}

	public static String getMd5(String paramString) {
		if (paramString == null) {
			return null;
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(paramString.getBytes());
			return a(md.digest(), "");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String a(byte[] arr, String paramString) {
		StringBuilder sb = new StringBuilder();

		for (int k : arr) {
			if ((k & 0xf0) <= 0) {
				sb.append("0").append(Integer.toHexString(0xFF & k));
			} else {
				sb.append(Integer.toHexString(0xFF & k)).append(paramString);
			}
		}
		return sb.toString();
	}

	public static String getStringByStreamWithBuf(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		for (String str = br.readLine(); str != null; str = br.readLine()) {
			sb.append(str);
		}
		br.close();
		return sb.toString();
	}

	public static String getStrByStream(InputStream in) {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String str = null;
		try {
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}

		} catch (IOException e2) {
			e2.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e4) {
				e4.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static boolean bStartSplash(String paramString) {
		if (null == paramString) {
			return false;
		}
		if (paramString.startsWith("/")) {
			return false;
		}
		return true;
	}

	public static long getTotalMem() {
		String str1 = "/proc/meminfo";

		long l = 0L;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader br = new BufferedReader(fr, 8192);
			String str2 = br.readLine();
			String[] arr = str2.split("\\s+");
			l = Integer.valueOf(arr[1]).intValue() / 1024;
			br.close();
			return l;
		} catch (IOException e) {
		}
		return -1L;
	}

	public static int[] getDisplayInfo(Context cx) {
		int[] scrInfo = new int[] { 0, 0, 0 };
		WindowManager wm = (WindowManager) cx.getSystemService("window");
		if (null == wm) {
			return scrInfo;
		}
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int w = dm.widthPixels;
		int h = dm.heightPixels;
		if (w < h) {
			int k = w;
			w = h;
			h = k;
		}
		scrInfo[0] = w;
		scrInfo[1] = h;
		scrInfo[2] = dm.densityDpi;
		return scrInfo;
	}

	public static String getCpuArc1() {
		String str = "";

		CPU_INFO locala = getCpuInfo();
		if ((locala.mArcType & 0x1) == 1) {
			str = "armv5";
		} else if ((locala.mArcType & 0x10) == 16) {
			str = "armv6";
		} else if ((locala.mArcType & 0x100) == 256) {
			str = "armv7";
		} else {
			str = "unknown";
		}
		return str;
	}

	public static String getCpuArc2() {
		String str = "";

		CPU_INFO locala = getCpuInfo();
		if ((locala.mCharacter & 0x100) == 256) {
			str = "neon";
		} else if ((locala.mCharacter & 0x1) == 1) {
			str = "vfp";
		} else if ((locala.mCharacter & 0x10) == 16) {
			str = "vfpv3";
		} else {
			str = "unknown";
		}
		return str;
	}

	public static boolean isExistFile(String path) {
		File f = new File(path);
		if (f.exists()) {
			Logger.LOGD("CommonUtils", path + " exists.");
			return true;
		}
		Logger.LOGD("CommonUtils", path + " can't be found.");

		return false;
	}

	public static class CPU_INFO {
		public int mArcType;
		public int b;
		public int mCharacter;
		public double jdField_a_of_type_Double;
		public long mTotalMem;
	}
}