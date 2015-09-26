package dodola.example.android.bitmapfun.util;
//ok
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import children.lemoon.utils.Logger;

 
public class ImageFetcher
  extends ImageResizer
{
  public static final String HTTP_CACHE_DIR = "http";
  private static final int HTTP_CACHE_SIZE = 10485760;
  private static final String TAG = "ImageFetcher";
  
  public ImageFetcher(Context paramContext, int paramInt)
  {
    super(paramContext, paramInt);
    init(paramContext);
  }
  
  public ImageFetcher(Context paramContext, int paramInt1, int paramInt2)
  {
    super(paramContext, paramInt1, paramInt2);
    init(paramContext);
  }
  
  private void checkConnection(Context paramContext)
  {
    NetworkInfo localNetworkInfo = ((ConnectivityManager)paramContext.getSystemService("connectivity")).getActiveNetworkInfo();
    if ((localNetworkInfo == null) || (!localNetworkInfo.isConnectedOrConnecting()))
    {
      Toast.makeText(paramContext, "没有网络", 1).show();
      Logger.LOGE("ImageFetcher", "checkConnection - no connection found");
    }
  }
  
  /* Error */
  public static File downloadBitmap(Context context, String urlString)
  {
	  File cacheDir = DiskLruCache.getDiskCacheDir(context, "http");	//v3
	  DiskLruCache cache = DiskLruCache.openCache(context, cacheDir, 10485760);	//v2
	  
	  File cacheFile = new File(cache.createFilePath(urlString));	//v4
	  if(cache.containsKey(urlString)){
		  //cond0/goto0
		  return cacheFile;
	  }
	  
	  //cond1
	  Utils.disableConnectionReuseIfNecessary();
	  HttpURLConnection urlConnection = null;	//v10
	  BufferedOutputStream out = null;	//v8
	  BufferedInputStream in = null;	//v6
	  try{
		  URL url = new URL(urlString);
		  urlConnection = (HttpURLConnection)url.openConnection();	//v10
		  in = new BufferedInputStream(urlConnection.getInputStream(), 8192);//v6
		  out = new BufferedOutputStream(new FileOutputStream(cacheFile), 8192);
		  
		  int b = -1;	//v1
		  //:goto_1
		  while((b = in.read()) != -1){
			  //cond3
			  out.write(b);
			  //go goto1
		  }
 
		  if(urlConnection != null)
			  urlConnection.disconnect();
		  //cond2
		  if(out != null){
			  out.close();
		  }
		  
	  }
	  catch (IOException e) {
		// TODO: handle exception
		  if(urlConnection != null)
			  urlConnection.disconnect();
		  //cond2
		  if(out != null){
			  try {
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				try {
					throw e1;
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
		  }
	  }
	  
	  return cacheFile;
	  
  }
  
  private void init(Context paramContext)
  {
    checkConnection(paramContext);
  }
  
  private Bitmap processBitmap(String paramString)
  {
    File localFile = downloadBitmap(this.mContext, paramString);
    if (localFile != null) {
      return decodeSampledBitmapFromFile(localFile.toString(), this.mImageWidth, this.mImageHeight);
    }
    return null;
  }
  
  @Override
protected Bitmap processBitmap(Object paramObject)
  {
    return processBitmap(String.valueOf(paramObject));
  }
}

