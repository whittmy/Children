package children.lemoon.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
 
import android.util.Log;

import com.loopj.android.http.RequestParams;

public class HttpUtil {
	static public String doRequest(String paramString) {
		try {
			String str = EntityUtils.toString(new DefaultHttpClient().execute(new HttpGet(paramString)).getEntity());
			return str;
		} catch (ClientProtocolException localClientProtocolException) {
			localClientProtocolException.printStackTrace();
			return "";
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return "";
	}

	/** 
     * 如果服务器不支持中文路径的情况下需要转换url的编码。 
     * @param string 
     * @return 
     */  
    static public String encodeGB(String string)  
    {  
        //转换中文编码  
        String split[] = string.split("/");  
        for (int i = 1; i < split.length; i++) {  
            try {  
                split[i] = URLEncoder.encode(split[i], "GB2312");  
            } catch (UnsupportedEncodingException e) {  
                e.printStackTrace();  
            }  
            split[0] = split[0]+"/"+split[i];  
        }  
        split[0] = split[0].replaceAll("\\+", "%20");//处理空格  
        return split[0];  
    }  
		
	
	/**
	 * 获取重定向之后的网址信息
	 * 
	 * @see HttpClient缺省会自动处理客户端重定向
	 * @see 即访问网页A后,假设被重定向到了B网页,那么HttpClient将自动返回B网页的内容
	 * @see 若想取得B网页的地址
	 *      ,就需要借助HttpContext对象,HttpContext实际上是客户端用来在多次请求响应的交互中,保持状态信息的
	 * @see 我们自己也可以利用HttpContext来存放一些我们需要的信息,以便下次请求的时候能够取出这些信息来使用
	 */
	
	static int time = 0;
	public static String getRedirectUrl(String url, Header[] headers, RequestParams params, final int cnt) {
		//这句很重要，否则 https会出现  javax.net.ssl.SSLException: hostname in certificate didn't match:
		SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());  
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		//rocking resolved : Invalid cookie header: "set-cookie: 
		httpClient.getCookieSpecs().register("lenient", new CookieSpecFactory() {
	        public CookieSpec newInstance(HttpParams params) {
	            return new LenientCookieSpec();
	        }
	    });
		HttpClientParams.setCookiePolicy(httpClient.getParams(), "lenient");
        //
		
		
		time = 0;
		httpClient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 3);
		httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		
		httpClient.setRedirectHandler(new DefaultRedirectHandler() {
			@Override
			public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
				// TODO Auto-generated method stub
				int statusCode = response.getStatusLine().getStatusCode();
				Logger.LOGE("setEnableRedirects", "code:" + statusCode);
				if(time == cnt){
					return false;
				}
				
				if (statusCode == 301 || statusCode == 302) {
					Logger.LOGE("setEnableRedirects", "enableRedirects: true");
					time ++;
					return true;
				}
				return false;
			}
			
			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
				// TODO Auto-generated method stub
				if (response == null) {
					throw new IllegalArgumentException("HTTP response may not be null");
				}
				// get the location header to find out where to redirect to
				Header locationHeader = response.getFirstHeader("location");
				if (locationHeader == null) {
					// got a redirect response, but no location header
					throw new ProtocolException("Received redirect response " + response.getStatusLine() + " but no location header");
				}
				// HERE IS THE MODIFIED LINE OF CODE
				String location = locationHeader.getValue().replaceAll(" ", "%20");

				URI uri;
				try {
					uri = new URI(location);
				} catch (URISyntaxException ex) {
					throw new ProtocolException("Invalid redirect URI: " + location, ex);
				}

				HttpParams params = response.getParams();
				// rfc2616 demands the location value be a complete URI Location = "Location" ":" absoluteURI
				if (!uri.isAbsolute()) {
					if (params.isParameterTrue(ClientPNames.REJECT_RELATIVE_REDIRECT)) {
						throw new ProtocolException("Relative redirect location '" + uri + "' not allowed");
					}
					// Adjust location URI
					HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					if (target == null) {
						throw new IllegalStateException("Target host not available " + "in the HTTP context");
					}

					HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
					try {
						URI requestURI = new URI(request.getRequestLine().getUri());
						URI absoluteRequestURI = URIUtils.rewriteURI(requestURI, target, true);
						uri = URIUtils.resolve(absoluteRequestURI, uri);
					} catch (URISyntaxException ex) {
						throw new ProtocolException(ex.getMessage(), ex);
					}
				}

				if (params.isParameterFalse(ClientPNames.ALLOW_CIRCULAR_REDIRECTS)) {
					RedirectLocations redirectLocations = (RedirectLocations) context.getAttribute("http.protocol.redirect-locations");
					if (redirectLocations == null) {
						redirectLocations = new RedirectLocations();
						context.setAttribute("http.protocol.redirect-locations", redirectLocations);
					}

					URI redirectURI;
					if (uri.getFragment() != null) {
						try {
							HttpHost target = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
							redirectURI = URIUtils.rewriteURI(uri, target, true);
						} catch (URISyntaxException ex) {
							throw new ProtocolException(ex.getMessage(), ex);
						}
					} else {
						redirectURI = uri;
					}

					if (redirectLocations.contains(redirectURI)) {
						throw new CircularRedirectException("Circular redirect to '" + redirectURI + "'");
					} else {
						redirectLocations.add(redirectURI);
					}
				}
				return uri;
			}
			
		});

		HttpContext httpContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(url);
		try {
			// 将HttpContext对象作为参数传给execute()方法,则HttpClient会把请求响应交互过程中的状态信息存储在HttpContext中
			HttpResponse response = httpClient.execute(httpGet, httpContext);

			// 获取重定向之后的主机地址信息,即"http://127.0.0.1:8088"
			HttpHost targetHost = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			// 获取实际的请求对象的URI,即重定向之后的"/blog/admin/login.jsp"
			HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
			// System.out.println("主机地址:" + targetHost);
			// System.out.println("URI信息:" + realRequest.getURI());
			/*
			 * HttpEntity entity = response.getEntity(); if(null != entity){
			 * //System.out.println("响应内容:" + EntityUtils.toString(entity,
			 * ContentType.getOrDefault(entity).getCharset()));
			 * //EntityUtils.consume(entity); System.out.println("响应内容:" +
			 * entity.toString()); entity.consumeContent(); }
			 */
			return encodeGB(targetHost.toString() + realRequest.getURI());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return "";
	}
}
