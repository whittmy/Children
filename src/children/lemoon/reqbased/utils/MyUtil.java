package children.lemoon.reqbased.utils;

//ok
import java.security.MessageDigest;

import children.lemoon.Configer;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class MyUtil {
	public static int dip2px(Context paramContext, float paramFloat) {
		return (int) (0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density);
	}

	
	public static  String getSign(long ts){
		String org = ts+ Configer.COM_KEY;
		String ret  = Md5Util.MD5(org);
		return ret;
	}
	
	
	// ver3 ===============
	public static final String mySign(String p1, String p2, String p3) {

		char[] hexDigits = { 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, };

		String parm1 = "d!e@#"; // v9
		String parm2 = "q!q@#"; // v12
		String parm3 = "af!@#"; // v13
		String parm4 = "ds!@#"; // v14
		String parm5 = "q!a@#"; // v15
		String parm6 = "a!46"; // v16
		String parm7 = "b!68"; // v17
		String parm8 = "c!01"; // v18
		String parm9 = "d!23";// v19
		String parm10 = "e!45";// v10
		String parm11 = "f!67";// v11

		String str1 = parm1; // v20 <- v9
		String str2 = parm6; // v21 <- v6

		str1 = str1 + p2; // v20
		parm1 = parm1 + parm2 + parm5; // v9
		// ==>
		// str1 = str1 + parm2+parm5;

		// ???
		parm1 = parm2; // v9 <- v12
		parm2 = str1; // v12 <- v20

		str1 = str1 + parm4; // v20
		// ==>
		// parm2 = str1+parm4;

		// ????
		str1 = p2;
		parm1 = parm1 + str1;

		// ???
		parm10 = p1;
		str2 = str2 + parm7;
		parm7 = parm7 + parm1;

		// ??/
		parm7 = parm9;
		parm11 = p3;

		parm8 = parm8 + parm1 + parm11;
		parm10 = parm10 + parm11;
		str2 = parm1 + parm7;

		parm10 = parm10 + str2;

		// ???
		str2 = parm10;
		parm10 = parm10 + str2 + parm1;

		try {
			byte[] strTemp = str2.getBytes(); // v22
			MessageDigest mdTemp = MessageDigest.getInstance("MD5"); // V8
			mdTemp.update(strTemp);
			byte[] tmp = mdTemp.digest(); // v24

			char[] strs = new char[32]; // v23
			int k = 0; // v7
			int i = 0; // v5
			// goto0
			while (i < 16) {
				// cond0
				byte byte0 = tmp[i]; // v2

				strs[k] = hexDigits[(byte0 >>> 0x4) & 0xf];
				strs[k + 1] = hexDigits[byte0 & 0xf];

				k += 2;
				i++;
				// go goto0
			}

			// goto1
			return new String(strs).toLowerCase();
		} catch (Exception e) {

		}
		return null;

	}

}
