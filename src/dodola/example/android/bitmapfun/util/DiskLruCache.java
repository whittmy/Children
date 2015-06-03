package dodola.example.android.bitmapfun.util;

//ok
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logger.lemoon.Logger;

public class DiskLruCache {
	private static final String CACHE_FILENAME_PREFIX = "cache_";
	private static final int INITIAL_CAPACITY = 32;
	private static final float LOAD_FACTOR = 0.75F;
	private static final int MAX_REMOVALS = 4;
	private static final String TAG = "DiskLruCache";
	private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File paramAnonymousFile, String paramAnonymousString) {
			return paramAnonymousString.startsWith("cache_");
		}
	};
	private int cacheByteSize = 0;
	private int cacheSize = 0;
	private final File mCacheDir;
	private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
	private int mCompressQuality = 70;
	private final Map<String, String> mLinkedHashMap = Collections.synchronizedMap(new LinkedHashMap(32, 0.75F, true));
	private long maxCacheByteSize = 5242880L;
	private final int maxCacheItemSize = 64;

	private DiskLruCache(File paramFile, long paramLong) {
		this.mCacheDir = paramFile;
		this.maxCacheByteSize = paramLong;
	}

	public static void clearCache(Context paramContext, String paramString) {
		clearCache(getDiskCacheDir(paramContext, paramString));
	}

	private static void clearCache(File paramFile) {
		File[] arrayOfFile = paramFile.listFiles(cacheFileFilter);
		for (int i = 0; i < arrayOfFile.length; i++) {
			arrayOfFile[i].delete();
		}
	}

	public static String createFilePath(File paramFile, String paramString) {
		try {
			String str = paramFile.getAbsolutePath() + File.separator + "cache_" + URLEncoder.encode(paramString.replace("*", ""), "UTF-8");
			return str;
		} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
			Logger.LOGE("DiskLruCache", "createFilePath - " + localUnsupportedEncodingException);
		}
		return null;
	}

	private void flushCache() {
		for (int i = 0; ((i < 4) && ((this.cacheSize > 64) || (this.cacheByteSize > this.maxCacheByteSize))); i++) {
			Map.Entry<String, String> localEntry = (Map.Entry<String, String>) this.mLinkedHashMap.entrySet().iterator().next();
			File localFile = new File((String) localEntry.getValue());
			long l = localFile.length();
			this.mLinkedHashMap.remove(localEntry.getKey());
			localFile.delete();
			this.cacheSize = this.mLinkedHashMap.size();
			this.cacheByteSize = ((int) (this.cacheByteSize - l));
		}
	}

	public static File getDiskCacheDir(Context paramContext, String paramString) {
		String str = null;
		if ((Environment.getExternalStorageState() == "mounted") || (!Utils.isExternalStorageRemovable())) {
			str = Utils.getExternalCacheDir(paramContext).getPath();
		} else {
			str = paramContext.getCacheDir().getPath();

		}
		return new File(str + File.separator + paramString);
	}

	public static DiskLruCache openCache(Context paramContext, File paramFile, long paramLong) {
		if (!paramFile.exists()) {
			paramFile.mkdir();
		}
		if ((paramFile.isDirectory()) && (paramFile.canWrite()) && (Utils.getUsableSpace(paramFile) > paramLong)) {
			return new DiskLruCache(paramFile, paramLong);
		}
		return null;
	}

	private void put(String paramString1, String paramString2) {
		this.mLinkedHashMap.put(paramString1, paramString2);
		this.cacheSize = this.mLinkedHashMap.size();
		this.cacheByteSize = ((int) (this.cacheByteSize + new File(paramString2).length()));
	}

	private boolean writeBitmapToFile(Bitmap paramBitmap, String paramString) throws IOException, FileNotFoundException {
		BufferedOutputStream localBufferedOutputStream = null;
		try {
			localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(paramString), 8192);

			boolean bool = paramBitmap.compress(this.mCompressFormat, this.mCompressQuality, localBufferedOutputStream);
			if (localBufferedOutputStream != null) {
				localBufferedOutputStream.close();
			}

			return bool;
		} finally {
			if (localBufferedOutputStream == null) {
				throw new IOException();
			}
		}

	}

	public void clearCache() {
		clearCache(this.mCacheDir);
	}

	public boolean containsKey(String paramString) {
		if (this.mLinkedHashMap.containsKey(paramString)) {
			return true;
		}
		String str = createFilePath(this.mCacheDir, paramString);
		if (new File(str).exists()) {
			put(paramString, str);
			return true;
		}
		return false;
	}

	public String createFilePath(String paramString) {
		return createFilePath(this.mCacheDir, paramString);
	}

	public Bitmap get(String paramString) {
		synchronized (this.mLinkedHashMap) {
			String str1 = (String) this.mLinkedHashMap.get(paramString);
			if (str1 != null) {
				Bitmap localBitmap1 = BitmapFactory.decodeFile(str1);
				return localBitmap1;
			}
			String str2 = createFilePath(this.mCacheDir, paramString);
			if (new File(str2).exists()) {
				put(paramString, str2);
				Bitmap localBitmap2 = BitmapFactory.decodeFile(str2);
				return localBitmap2;
			}
		}
		return null;
	}

	public void put(String paramString, Bitmap paramBitmap) {
		synchronized (this.mLinkedHashMap) {
			Object localObject2 = this.mLinkedHashMap.get(paramString);
			if (localObject2 != null) {
				return;
			}
		}
		try {
			String str = createFilePath(this.mCacheDir, paramString);
			if (writeBitmapToFile(paramBitmap, str)) {
				put(paramString, str);
				flushCache();
			}
			return;
		} catch (FileNotFoundException localFileNotFoundException) {

			Logger.LOGE("DiskLruCache", "Error in put: " + localFileNotFoundException.getMessage());
			// localObject1 = finally;
			// throw localObject1;
		} catch (IOException localIOException) {
			Logger.LOGE("DiskLruCache", "Error in put: " + localIOException.getMessage());
		}
	}

	public void setCompressParams(Bitmap.CompressFormat paramCompressFormat, int paramInt) {
		this.mCompressFormat = paramCompressFormat;
		this.mCompressQuality = paramInt;
	}
}
