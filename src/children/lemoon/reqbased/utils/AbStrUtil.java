package children.lemoon.reqbased.utils;

//ok
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logger.lemoon.Logger;

public final class AbStrUtil {
	public static int chineseLength(String paramString) {
		boolean bool = isEmpty(paramString);
		int i = 0;
		if (!bool) {
			for (int j = 0; j < paramString.length(); j++) {
				if (paramString.substring(j, j + 1).matches("[Α-￥]")) { // "[\u0391-\uFFE5]";
					i += 2;
				}
			}
		}
		return i;
	}

	public static String convertStreamToString(InputStream paramInputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(paramInputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			// 最后一个\n删除
			if (sb.indexOf("\n") != -1 && sb.lastIndexOf("\n") == sb.length() - 1) {
				sb.delete(sb.lastIndexOf("\n"), sb.lastIndexOf("\n") + 1);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				paramInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String cutString(String paramString, int paramInt) {
		return cutString(paramString, paramInt, "");
	}

	public static String cutString(String str, int length, String dot) {
		int strBLen = strlen(str, "GBK");
		if (strBLen <= length) {
			return str;
		}
		int temp = 0;
		StringBuffer sb = new StringBuffer(length);
		char[] ch = str.toCharArray();
		for (char c : ch) {
			sb.append(c);
			if (c > 256) {
				temp += 2;
			} else {
				temp += 1;
			}
			if (temp >= length) {
				if (dot != null) {
					sb.append(dot);
				}
				break;
			}
		}
		return sb.toString();
	}

	public static String cutStringFromChar(String paramString1, String paramString2, int paramInt) {
		if (isEmpty(paramString1)) {
			return "";
		}
		int i = paramString1.indexOf(paramString2);
		if ((i != -1) && (paramString1.length() > i + paramInt)) {
			return paramString1.substring(i + paramInt);
		}
		return "";
	}

	public static String dateTimeFormat(String dateTime) {
		StringBuilder sb = new StringBuilder();
		try {
			if (isEmpty(dateTime)) {
				return null;
			}
			String[] dateAndTime = dateTime.split(" ");
			if (dateAndTime.length > 0) {
				for (String str : dateAndTime) {
					if (str.indexOf("-") != -1) {
						String[] date = str.split("-");
						for (int i = 0; i < date.length; i++) {
							String str1 = date[i];
							sb.append(strFormat2(str1));
							if (i < date.length - 1) {
								sb.append("-");
							}
						}
					} else if (str.indexOf(":") != -1) {
						sb.append(" ");
						String[] date = str.split(":");
						for (int i = 0; i < date.length; i++) {
							String str1 = date[i];
							sb.append(strFormat2(str1));
							if (i < date.length - 1) {
								sb.append(":");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	public static String getSizeDesc(long paramLong) {
		String str = "B";
		if (paramLong >= 1024L) {
			str = "K";
			paramLong >>= 10;
			if (paramLong >= 1024L) {
				str = "M";
				paramLong >>= 10;
				if (paramLong >= 1024L) {
					str = "G";
					paramLong >>= 10;
				}
			}
		}
		return paramLong + str;
	}

	public static long ip2int(String paramString) {
		String[] arrayOfString = paramString.replace(".", ",").split(",");
		return Long.valueOf(arrayOfString[0]).longValue() << 24 | Long.valueOf(arrayOfString[1]).longValue() << 16
				| Long.valueOf(arrayOfString[2]).longValue() << 8 | Long.valueOf(arrayOfString[3]).longValue();
	}

	public static Boolean isChinese(String paramString) {
		Boolean localBoolean = true;
		if (!isEmpty(paramString)) {
			for (int i = 0; i < paramString.length(); i++) {
				if (!paramString.substring(i, i + 1).matches("[Α-￥]")) {// "[\u0391-\uFFE5]";
					localBoolean = false;
				}
			}
		}

		return localBoolean;
	}

	public static Boolean isContainChinese(String paramString) {
		Boolean localBoolean = false;
		if (!isEmpty(paramString)) {
			for (int i = 0; i < paramString.length(); i++) {
				if (paramString.substring(i, i + 1).matches("[Α-￥]")) {
					localBoolean = true;
				}
			}
		}
		return localBoolean;
	}

	public static Boolean isEmail(String paramString) {
		Boolean localBoolean = false;
		if (paramString.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")) {
			localBoolean = Boolean.valueOf(true);
		}
		return localBoolean;
	}

	public static boolean isEmpty(String paramString) {
		return (paramString == null) || (paramString.trim().length() == 0) || (paramString.equals("null"));
	}

	public static Boolean isLetter(String paramString) {
		Boolean localBoolean = false;
		if (paramString.matches("^[A-Za-z]+$")) {
			localBoolean = Boolean.valueOf(true);
		}
		return localBoolean;
	}

	public static Boolean isMobileNo(String paramString) {
		Boolean localBoolean1 = false;
		try {
			Boolean localBoolean2 = Boolean.valueOf(Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$").matcher(paramString).matches());
			return localBoolean2;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return localBoolean1;
	}

	public static Boolean isNumber(String paramString) {
		Boolean localBoolean = false;
		if (paramString.matches("^[0-9]+$")) {
			localBoolean = Boolean.valueOf(true);
		}
		return localBoolean;
	}

	public static Boolean isNumberLetter(String paramString) {
		Boolean localBoolean = false;
		if (paramString.matches("^[A-Za-z0-9]+$")) {
			localBoolean = Boolean.valueOf(true);
		}
		return localBoolean;
	}

	public static void main(String[] paramArrayOfString) {
		Logger.LOGD(dateTimeFormat("2012-3-2 12:2:20"));
	}

	public static String parseEmpty(String paramString) {
		if ((paramString == null) || ("null".equals(paramString.trim()))) {
			paramString = "";
		}
		return paramString.trim();
	}

	public static String sectionShow(String paramString) {
		String str1 = paramString.substring(0, 3);
		String str2 = paramString.substring(3, 7);
		String str3 = paramString.substring(7, 11);
		return str1 + " " + str2 + " " + str3;
	}

	public static String strFormat2(String paramString) {
		try {
			if (paramString.length() <= 1) {
				String str = "0" + paramString;
				paramString = str;
			}
			return paramString;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return paramString;
	}

	public static int strLength(String str) {
		int valueLength = 0;
		String chinese = "[Α-￥]"; // "[\u0391-\uFFE5]"
		if (!isEmpty(str)) {
			// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
			for (int i = 0; i < str.length(); i++) {
				// 获取一个字符
				String temp = str.substring(i, i + 1);
				// 判断是否为中文字符
				if (temp.matches(chinese)) {
					// 中文字符长度为2
					valueLength += 2;
				} else {
					// 其他字符长度为1
					valueLength += 1;
				}
			}
		}
		return valueLength;
	}

	public static int strlen(String paramString1, String paramString2) {
		if ((paramString1 == null) || (paramString1.length() == 0)) {
			return 0;
		}
		try {
			int i = paramString1.getBytes(paramString2).length;
			return i;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return 0;
	}

	public static int subStringLength(String str, int maxL) {
		int currentIndex = 0;
		int valueLength = 0;
		String chinese = "[Α-￥]"; // "[\u0391-\uFFE5]";
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < str.length(); i++) {
			// 获取一个字符
			String temp = str.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为2
				valueLength += 2;
			} else {
				// 其他字符长度为1
				valueLength += 1;
			}
			if (valueLength >= maxL) {
				currentIndex = i;
				break;
			}
		}
		return currentIndex;
	}
}