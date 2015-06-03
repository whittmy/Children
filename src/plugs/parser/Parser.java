package plugs.parser;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.util.Log;

public class Parser {

	static public ArrayList<String> test(Context cx, String urlString) {
		SyncNetStringClient.init(cx);

		// http://www.flvcd.com/parse.php?kw=http%3A%2F%2Fyuntv.letv.com%2Fbcloud.html%3Fuu%3Da04808d307%26vu%3D8f54aa905c&flag=one&format=real
		// http://www.flvcd.com/parse.php?kw=http%3A%2F%2Fyuntv.letv.com%2Fbcloud.html%3Fuu%3Da04808d307%26vu%3D8f54aa905c&flag=one&format=normal
		// format=high 高清
		// format=super 超清版
		// http://www.flvcd.com/parse.php?kw=http://v.youku.com/v_show/id_XMzI5MzQwOA==.html;
		// String urlString = "http://172.16.2.4/urlparser/data.txt";
		// urlString =
		// "http://www.flvcd.com/parse.php?kw=http%3A%2F%2Fyuntv.letv.com%2Fbcloud.html%3Fuu%3Da04808d307%26vu%3D51250315d3&flag=one&format=real";
		// urlString =
		// "http://www.flvcd.com/parse.php?kw=http%3A%2F%2Fv.youku.com%2Fv_show%2Fid_XODIxNDA3Nzk2.html&flag=one&format=high";
		// urlString =
		// "http://www.flvcd.com/parse.php?kw=http%3A%2F%2Fv.pptv.com%2Fshow%2F1XFb2UGnF1W4Np4.html&flag=one&format=super";

		// urlString = "http://v.youku.com/v_show/id_XODUyMTkwMTY4.html";
		if (urlString.indexOf("letv") != -1) {
			urlString = String.format("http://www.flvcd.com/parse.php?kw=%s&flag=one&format=real", URLEncoder.encode(urlString));
		} else {
			urlString = String.format("http://www.flvcd.com/parse.php?kw=%s&flag=one&format=super", URLEncoder.encode(urlString));
		}

		BasicHeader[] headers = new BasicHeader[4];
		headers[0] = new BasicHeader("Accept-Encoding", "gzip,deflate,sdch");
		headers[1] = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36");
		headers[2] = new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers[3] = new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8");

		SyncNetStringClient.get(urlString, headers, null);
		if (SyncNetStringClient.isOk()) {
			String rslt = SyncNetStringClient.getContent();

			// <form action="get_m3u.php" method="post" name="m3uForm">
			// Pattern pattern =
			// Pattern.compile("<form action=\"(.+php)\" method=\"post\" name=\"m3u.*\"");
			// Matcher matcher = pattern.matcher(rslt);
			// if(matcher.find()){
			// String requrl = "http://www.flvcd.com/" + matcher.group(1);
			// pattern =
			// Pattern.compile("<input type=\"hidden\" name=\"(.+)\" value=\"(http.*)\"\\W*/>");
			// matcher = pattern.matcher(rslt);
			// if(matcher.find()){
			// RequestParams para = new RequestParams();
			// para.put(matcher.group(1), matcher.group(2));
			// SyncNetStringClient.post(requrl, headers, null, para);
			// if (SyncNetStringClient.isOk()) {
			// Header[] header = SyncNetStringClient.getHeader();
			// String m3u8 = SyncNetStringClient.getContent();
			// Log.e("", header.toString());
			// Log.e("", m3u8);
			// }
			//
			// }
			// return null;
			// }

			String target = null;
			String method = null;
			Pattern pattern = Pattern.compile("name=\"mform\".*\"(.+?)\".*\"(.+?)\"");
			Matcher matcher = pattern.matcher(rslt);
			if (matcher.find()) {
				target = matcher.group(1);
				method = matcher.group(2);
			}

			pattern = Pattern.compile("<input type=\"hidden\" name=\"([^\"]*)\".*value=\"([^\"]*)\">");
			matcher = pattern.matcher(rslt);

			Map<String, String> param = new HashMap<String, String>();
			while (matcher.find()) {
				param.put(matcher.group(1), matcher.group(2));
			}

			if (method == null || method.isEmpty())
				return null;

			if (method.equals("get")) {
				String getargs = "";
				Iterator<String> iter = param.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					String val = param.get(key);
					getargs = getargs + "&" + key + "=" + URLEncoder.encode(val);
				}
				if (getargs.startsWith("&"))
					getargs = getargs.replaceFirst("&", "?");
				if (getargs.endsWith("&"))
					getargs = getargs.substring(0, getargs.length() - 2);

				String returl = target + getargs;
				// Log.e("", returl);

				headers = new BasicHeader[5];
				headers[0] = new BasicHeader("Accept-Encoding", "gzip,deflate,sdch");
				headers[1] = new BasicHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36");
				headers[2] = new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				headers[3] = new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
				headers[4] = new BasicHeader("Referer", urlString);
				SyncNetStringClient.get(returl, headers, null);
				if (SyncNetStringClient.isOk()) {
					String ret = SyncNetStringClient.getContent();
					// Log.e("", ret);

					pattern = Pattern.compile("id=(.+?)\"");
					matcher = pattern.matcher(ret);
					if (matcher.find()) {
						String id = matcher.group(1);
						String url = "http://www.flvcd.com/diy/diy00" + id + ".htm";
						// Log.e("", url);
						headers = new BasicHeader[2];
						headers[0] = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101");
						headers[1] = new BasicHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");

						SyncNetStringClient.get(url, headers, null);
						if (SyncNetStringClient.isOk()) {
							ret = SyncNetStringClient.getContent();
							// Log.e("", ret);

							pattern = Pattern.compile("<U>(http.+)");// <U>(http.+)\\s<C>(.+)\\s<US>(.+)
							matcher = pattern.matcher(ret);
							ArrayList<String> retUrls = new ArrayList<String>();
							while (matcher.find()) {
								String rslturl = matcher.group(1); // +
																	// matcher.group(2)
																	// +
																	// matcher.group(3);
								retUrls.add(rslturl);
								// Log.e("rslturl", rslturl);
							}
							return retUrls;
						}
					}
				}
			} else {
				// post
			}

		}
		return null;
	}
}
