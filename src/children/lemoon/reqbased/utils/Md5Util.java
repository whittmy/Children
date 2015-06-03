package children.lemoon.reqbased.utils;

//ok
import java.security.MessageDigest;

import logger.lemoon.Logger;

import android.util.Log;

public class Md5Util {
	// 这里的md5函数是为了和php内置md5函数功能保持一致的
	public static final String MD5(String paramString) {
		// 0-f
		char[] arrayOfChar1 = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
		try {
			byte[] arrayOfByte1 = paramString.getBytes();
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			localMessageDigest.update(arrayOfByte1);
			byte[] arrayOfByte2 = localMessageDigest.digest();
			char[] arrayOfChar2 = new char[32];
			int i = 0;
			int j = 0;

			// goto0
			while (i < 16) {
				// cond0
				int k = arrayOfByte2[i];
				int m = j + 1;
				arrayOfChar2[j] = arrayOfChar1[(0xF & k >>> 4)];
				j = m + 1;
				arrayOfChar2[m] = arrayOfChar1[(k & 0xF)];
				i++;
				// go goto0
			}

			return new String(arrayOfChar2).toUpperCase();
		} catch (Exception localException) {
		}
		return null;
	}

	// ver3 ==============
	private static String S1 = "";
	private static String S2 = "";
	private static String S3 = "";

	public static String getS1() {
		S1 = "wd!%s1";

		Logger.LOGE("test", "get s1:" + S1); // / E/test(29497): get:wd!%s1
		return S1;
	}

	public static String getS2() {
		S2 = "wd!%s1";

		Logger.LOGE("test", "get s2:" + S2);
		return S2;
	}

	public static String getS3() {

		S3 = "wd!%s1";
		Logger.LOGE("test", "get s3:" + S3);
		return S3;
	}

}
