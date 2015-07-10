package children.lemoon.ui.loading;

 
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;


import org.apache.http.Header;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

 
public class LoadDataSrv extends Service {
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte	
    SharedPreferences mPref;
    
    public interface IF_UpdateProgress{
    	public void onProgressUpdate(final int cur, final int total);
    }	
    
    @Override
    public void onCreate() {
    	// TODO Auto-generated method stub
    	super.onCreate();
    	
    	mPref = getSharedPreferences("loading", 0);
    }
    
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	String path;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent != null){
			path = getCacheDir()+"/loadings/";
			File file = new File(path+"up.zip");
			if(file.exists()){
				Long loadingDate = Long.valueOf(file.lastModified());
				if (System.currentTimeMillis() - loadingDate.longValue() < 8*3600000) {
					
					Log.e("", "################earlyer！！！");
					
					return super.onStartCommand(intent, flags, startId);
				}
				
				File f = new File(path);
				DeleteFile(f);
				f.mkdirs();
			}
			 
			//2小时,如果下载失败则20分钟取一次
			if(System.currentTimeMillis() - mPref.getLong("lasttm", 0) < 7200000){ //7200*1000
				return super.onStartCommand(intent, flags, startId);
			}
			
			AsyncHttpClient client = new AsyncHttpClient();
			//zip中的内容
			//正常名字形式如： _xxxx_01.png, _xxxx_02.png
			// 或  xxxx_01.png, xxxx_02.png
			client.get("http://www.nybgjd.com/erge/api2/cfgLoading", new FileAsyncHttpResponseHandler(file) {
				@Override
				public void onSuccess(int statusCode, Header[] headers, File file) {
					// TODO Auto-generated method stub
					Log.e("", "$$$$$$$$$$$$$$$$$$$$$$ finishied");
					try {
						unzip(file.getAbsolutePath(), getCacheDir()+"/loadings/");
						 long currentTime = System.currentTimeMillis();
						 file.setLastModified(currentTime);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					mPref.edit().putLong("lasttm", System.currentTimeMillis()).commit();
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
					// TODO Auto-generated method stub
					mPref.edit().putLong("lasttm", System.currentTimeMillis()+7198800).commit();	//20分钟 1200秒
				}
			});
		}
		return super.onStartCommand(intent, flags, startId);
	}

	
	
	/**
     * 递归删除文件和文件夹
     * 
     * @param file
     *            要删除的根目录
     */ 
    public void DeleteFile(File file) { 
        if (file.exists() == false) { 
            return; 
        } else { 
            if (file.isFile()) { 
                file.delete(); 
                return; 
            } 
            if (file.isDirectory()) { 
                File[] childFile = file.listFiles(); 
                if (childFile == null || childFile.length == 0) { 
                    file.delete(); 
                    return; 
                } 
                for (File f : childFile) { 
                    DeleteFile(f); 
                } 
                file.delete(); 
            } 
        } 
    } 

	
    
	public void unzip(String zipFilePath, String targetPath)
			throws IOException {
		
		
		OutputStream os = null;
		InputStream is = null;
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilePath,"GBK");//要指定GBK编码，否则为乱码
			String directoryPath = "";
			if (null == targetPath || "".equals(targetPath)) {
				directoryPath = zipFilePath.substring(0,
						zipFilePath.lastIndexOf("."));
			} else {
				directoryPath = targetPath;
			}
			Enumeration entryEnum = zipFile.getEntries();
			if (null != entryEnum) {
				ZipEntry zipEntry = null;
				while (entryEnum.hasMoreElements()) {
					zipEntry = (ZipEntry) entryEnum.nextElement();
					if (zipEntry.isDirectory()) {
						directoryPath = directoryPath + File.separator
								+ zipEntry.getName();
						System.out.println(directoryPath);
						continue;
					}
					if (zipEntry.getSize() > 0) {
						// 文件
						File targetFile = new File(directoryPath+ File.separator + zipEntry.getName());
						//Log.d(TAG, new String(zipEntry.getName().getBytes(),"UTF-8"));
						os = new BufferedOutputStream(new FileOutputStream(targetFile));
						is = zipFile.getInputStream(zipEntry);
						byte[] buffer = new byte[4096];
						int readLen = 0;
						while ((readLen = is.read(buffer, 0, 1024)) >= 0) {
							os.write(buffer, 0, readLen);
						}

						os.flush();
						os.close();
					}
				}
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (null != zipFile) {
				zipFile = null;
			}
			if (null != is) {
				is.close();
			}
			if (null != os) {
				os.close();
			}
		}
	}
    
    
	   /**
     * 解压缩一个文件
     *
     * @param zipFile 压缩文件
     * @param folderPath 解压缩的目标目录
     * @throws IOException 当解压缩过程出错时抛出
     */
    
//    public void upZipFile(File zipFile, String folderPath, IF_UpdateProgress prog ) throws ZipException, IOException {
//        File desDir = new File(folderPath);
//        if (!desDir.exists()) {
//            desDir.mkdirs();
//        }
//        ZipFile zf = new ZipFile(zipFile);
//        int SumCnt = 0;
//        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
//        	entries.nextElement();
//        	SumCnt ++;
//        }
//
//        int curCnt = 0;
//        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
//        	if(prog != null)
//        		prog.onProgressUpdate(curCnt, SumCnt);
//        	
//            ZipEntry entry = ((ZipEntry)entries.nextElement());
//            InputStream in = zf.getInputStream(entry);
//            //Log.e("", entry.getName());
//            String str = folderPath + File.separator + entry.getName();
//            str = new String(str.getBytes("8859_1"), "GB2312");
//
//            
//            File desFile = new File(str);
//            
//            if(str.endsWith("/")){
//            	if(!desFile.exists()){
//            		desFile.mkdirs();
//            		continue;
//            	}
//            	else{
//            		chmod("777", str);
//                   	in.close();
//                	curCnt ++;
//            		continue;
//            	}
//            }
//            
//            if (!desFile.exists()) {
//      
//            	
//                File fileParentDir = desFile.getParentFile();
//                if (!fileParentDir.exists()) {
//                    fileParentDir.mkdirs();
//                }
////                desFile.createNewFile();
//            }
//            else if(desFile.isDirectory()){
//            	in.close();
//            	curCnt ++;
//            	continue;
//            }
//            
//            OutputStream out = new FileOutputStream(desFile);
//            byte buffer[] = new byte[BUFF_SIZE];
//            int realLength;
//            while ((realLength = in.read(buffer)) > 0) {
//                out.write(buffer, 0, realLength);
//            }
//            in.close();
//            out.close();
//            curCnt ++;
//        }
//    }
	 public void chmod(String type, String path) {
		String command 	= "chmod " + type + " " + path;
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command);
			process.waitFor();
			//Log.v(TAG, "change priv "+ type + " for " + path);
		} catch (Exception e) {
		}
	}	
}
