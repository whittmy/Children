package children.lemoon.utils;

//ok
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import children.lemoon.MyApp;
import children.lemoon.R;

import logger.lemoon.Logger;

import dodola.example.android.bitmapfun.util.DiskLruCache;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.LruCache;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.PorterDuff;
import android.graphics.Shader;

public class ImageLoader {
	protected static final String TAG = "ImageLoader";
	public static String debugTag;
	private static DiskLruCache diskCache;
	private static ImageLoader mImageLoader;
	private static LruCache<String, Bitmap> mMemoryCache;
	private static Paint mTextPaint = new Paint();

	private ImageLoader() {
		int i = (int) Runtime.getRuntime().maxMemory() / 8;
		mMemoryCache = new LruCache<String, Bitmap>(i) {
			protected void entryRemoved(boolean paramAnonymousBoolean, String paramAnonymousString, Bitmap paramAnonymousBitmap1, Bitmap paramAnonymousBitmap2) {
				super.entryRemoved(paramAnonymousBoolean, paramAnonymousString, paramAnonymousBitmap1, paramAnonymousBitmap2);
				if (paramAnonymousBoolean) {
					Logger.LOGD("移除====<" + ImageLoader.debugTag + ">  key=" + paramAnonymousString);
					if (ImageLoader.diskCache != null) {
						ImageLoader.diskCache.put(paramAnonymousString, paramAnonymousBitmap1);
					}
				}
			}

			protected int sizeOf(String paramAnonymousString, Bitmap paramAnonymousBitmap) {
				return paramAnonymousBitmap.getByteCount();
			}
		};
		long l1 = Runtime.getRuntime().maxMemory() / 3L;
		long l2 = getSDAvlidSpace();
		if (l1 >= l2) {
			l1 = l2 / 2L;
		}
		String str1 = Formatter.formatFileSize(MyApp.mContext, i);
		String str2 = Formatter.formatFileSize(MyApp.mContext, l1);
		Logger.LOGD("\n   内存缓存=" + str1 + "    磁盘缓存=" + str2);
		File localFile = MyApp.mContext.getExternalCacheDir();
		if (localFile != null) {
			diskCache = DiskLruCache.openCache(MyApp.mContext, localFile, l1);
		}
	}

	public static long getSDAvlidSpace() {
		if (!Environment.getExternalStorageState().equals("mounted")) {
			return -1L;
		}
		StatFs localStatFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
		long l = localStatFs.getBlockSize() * localStatFs.getAvailableBlocks();
		String str = Formatter.formatFileSize(MyApp.getInstance(), l);
		Log.v("VrDownloadUtil", "sd卡空间还剩有：" + l + " B==" + l + " M==" + str);
		return l;
	}

	public static int calculateInSampleSize(BitmapFactory.Options paramOptions, int paramInt) {
		int i = paramOptions.outWidth;
		int j = 1;
		if (i > paramInt) {
			j = Math.round(i / paramInt);
		}
		return j;
	}

	static int calculateInSampleSize(BitmapFactory.Options paramOptions, int paramInt1, int paramInt2) {
		int i = paramOptions.outHeight;
		int j = paramOptions.outWidth;
		int k = 1;
		int n;
		if ((i > paramInt2) || (j > paramInt1)) {
			int m = Math.round(i / paramInt2);
			n = Math.round(j / paramInt1);
			if (m < n) {
				k = m;
			}
		} else {
			return k;
		}
		return n;
	}

	public static Bitmap decodeSampledBitmapFromResource(String pathName) {
		Bitmap result = null; // v3
		FileInputStream is = null; // v1
		try {
			is = new FileInputStream(pathName);
			result = BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			result = null;
		} catch (Exception e) {
			// TODO: handle exception
			result = null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		// cond0/goto0
		if (result == null) {
			result = BitmapFactory.decodeStream(MyApp.getInstance().getResources().openRawResource(R.drawable.p));
		}
		// cond1
		return result;
	}

	public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth) {
		Bitmap result = null; // v3
		BitmapFactory.Options options = new BitmapFactory.Options(); // v2

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		options.inSampleSize = ImageLoader.calculateInSampleSize(options, reqWidth);
		options.inJustDecodeBounds = false;
		FileInputStream is = null; // v1
		try {
			is = new FileInputStream(pathName);
			result = BitmapFactory.decodeStream(is, null, options);
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			result = null;
		} catch (Exception e) {
			// TODO: handle exception
			result = null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		// cond0/goto0
		if (result == null) {
			options.inSampleSize = 2;
			result = BitmapFactory.decodeStream(MyApp.getInstance().getResources().openRawResource(R.drawable.p), null, options);
		}
		// cond1
		return result;
	}

	public static Bitmap decodeSampledBitmapFromResource(String path, int imgWidth, int imgHeight) {
		Bitmap result = null;
		BitmapFactory.Options options = new BitmapFactory.Options(); // v9
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = ImageLoader.calculateInSampleSize(options, imgWidth, imgHeight);
		options.inJustDecodeBounds = false;

		Bitmap srcBitmap = null; // v0
		FileInputStream is = null; // v8
		try {
			is = new FileInputStream(path);
			srcBitmap = BitmapFactory.decodeStream(is, null, options);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			srcBitmap = null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		// cond0/goto0
		if (srcBitmap == null) {
			options.inSampleSize = 2;
			srcBitmap = BitmapFactory.decodeStream(MyApp.getInstance().getResources().openRawResource(R.drawable.p), null, options);
		}

		// cond1
		if (imgWidth <= 0 || imgHeight <= 0) {
			// cond4
			result = srcBitmap;
			// go goto1
			return result;
		}

		int srcWidth = srcBitmap.getWidth(); // v3
		int srcHeight = srcBitmap.getHeight(); // v4

		float scaleWidth = (float) imgWidth / (float) srcWidth; // v12
		float scaleHeight = (float) imgHeight / (float) srcHeight; // v11
		Matrix matrix = new Matrix(); // v5
		matrix.setScale(scaleWidth, scaleHeight);

		result = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth, srcHeight, matrix, true);
		if (srcBitmap != null) {
			srcBitmap.recycle();
			srcBitmap = null;
		}
		// cond2/goto1
		return result;
	}

	public static Bitmap decodeSampledBitmapFromStream(InputStream inputstream) {
		Bitmap bitmap = null;
		if (inputstream != null) {
			bitmap = BitmapFactory.decodeStream(inputstream);

			try {
				inputstream.close();
			} catch (IOException ioexception2) {
				bitmap = null;
			}
		}

		if (bitmap == null) {
			Logger.LOGD("ImageLoader", "null^^^^null^^^^null^^^^null^^^^null^^^^null^^^^");
			bitmap = BitmapFactory.decodeStream(MyApp.getInstance().getResources().openRawResource(R.drawable.p));
		}
		return bitmap;
	}

	public static Bitmap decodeSampledBitmapFromStream(InputStream inputstream, int i, int j, android.graphics.BitmapFactory.Options options) {
		Bitmap bitmap = null;
		if (options != null) {
			if (inputstream != null)
				try {
					options.inSampleSize = calculateInSampleSize(options, i, j);
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeStream(inputstream, null, options);
					inputstream.close();
				} catch (IOException ioexception2) {
				}

			if (bitmap == null) {
				Logger.LOGD("ImageLoader", "null^^^^null^^^^null^^^^null^^^^null^^^^null^^^^");
				return null;
			}

			if (i > 0 && j > 0) {
				Matrix matrix = new Matrix();
				matrix.setScale((float) i / (float) bitmap.getWidth(), (float) j / (float) bitmap.getHeight());
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				if (bitmap != null) {
					bitmap.recycle();
					return bitmap;
				}
			} else {
				return bitmap;
			}
		} else {
			try {
				bitmap = decodeSampledBitmapFromStream(inputstream);
			} catch (Exception e) {
				// TODO: handle exception
				bitmap = null;
			}
		}
		return bitmap;
	}

	public static Bitmap decodeSampledBitmapFromStream(InputStream inputstream, int i, android.graphics.BitmapFactory.Options options) {
		Bitmap bitmap = null;
		try {
			if (options == null)
				return decodeSampledBitmapFromStream(inputstream);
			options.inSampleSize = calculateInSampleSize(options, i);
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeStream(inputstream, null, options);

			if (inputstream != null)
				try {
					inputstream.close();
				} catch (IOException ioexception2) {

				}

			if (bitmap == null) {
				options.inSampleSize = 2;
				bitmap = BitmapFactory.decodeStream(MyApp.getInstance().getResources().openRawResource(0x7f0200d9), null, options);
			}
			Logger.LOGD("opt", (new StringBuilder("result   ")).append(bitmap.getWidth()).append("---").append(bitmap.getHeight()).toString());
			return bitmap;

		} catch (Exception e) {
			// TODO: handle exception
			bitmap = null;
		}

		if (inputstream != null)
			try {
				inputstream.close();
			}
			// Misplaced declaration of an exception variable
			catch (IOException ioexception) {
			}
		return bitmap;
	}

	public static BitmapFactory.Options extractOpts(InputStream paramInputStream) {
		BitmapFactory.Options localOptions = new BitmapFactory.Options();
		localOptions.inInputShareable = true;
		localOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(paramInputStream, null, localOptions);
		if (paramInputStream != null) {
		}
		try {
			paramInputStream.close();
			return localOptions;
		} catch (Exception localException) {
		}
		return localOptions;
	}

	public static ImageLoader getInstance() {
		if (mImageLoader == null) {
		}
		try {
			if (mImageLoader == null) {
				mImageLoader = new ImageLoader();
			}
			return mImageLoader;
		} finally {
		}
	}

	public static Bitmap getReflectionBitmap(Bitmap paramBitmap) {
		int i = paramBitmap.getWidth();
		int j = paramBitmap.getHeight();
		Matrix localMatrix = new Matrix();
		localMatrix.setScale(1.0F, -1.0F);
		Bitmap localBitmap1 = Bitmap.createBitmap(paramBitmap, 0, j / 2, i, j / 2, localMatrix, false);
		Bitmap localBitmap2 = Bitmap.createBitmap(i, j + j / 2, Bitmap.Config.ARGB_8888);
		Canvas localCanvas = new Canvas(localBitmap2);
		localCanvas.drawBitmap(paramBitmap, 0.0F, 0.0F, null);
		localCanvas.drawBitmap(localBitmap1, 0.0F, j + 4, null);
		Paint localPaint = new Paint();
		localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		localPaint.setShader(new LinearGradient(0.0F, j, 0.0F, localBitmap2.getHeight(), 1879048192, 0, Shader.TileMode.CLAMP));
		localCanvas.drawRect(0.0F, j, localBitmap2.getWidth(), localBitmap2.getHeight(), localPaint);
		return localBitmap2;
	}

	public static Bitmap getReflectionBitmap(Bitmap paramBitmap, boolean paramBoolean) {
		int i = paramBitmap.getWidth();
		int j = paramBitmap.getHeight();
		Matrix localMatrix = new Matrix();
		localMatrix.setScale(1.0F, -1.0F);
		Bitmap localBitmap = Bitmap.createBitmap(paramBitmap, 0, j / 2, i, j / 2, localMatrix, false);
		Canvas localCanvas = new Canvas(localBitmap);
		localCanvas.drawBitmap(localBitmap, 0.0F, 0.0F, null);
		Paint localPaint = new Paint();
		localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		localPaint.setShader(new LinearGradient(0.0F, 0.0F, 0.0F, j / 2, 1879048192, 0, Shader.TileMode.CLAMP));
		localCanvas.drawRect(0.0F, 0.0F, localBitmap.getWidth(), j / 2, localPaint);
		return localBitmap;
	}

	public static Bitmap getSubReflectBmp(Bitmap paramBitmap) {
		int i = paramBitmap.getWidth();
		int j = paramBitmap.getHeight();
		Matrix localMatrix = new Matrix();
		localMatrix.setScale(1.0F, -1.0F);
		Bitmap localBitmap1 = Bitmap.createBitmap(paramBitmap, 0, j / 2, i, j / 2, localMatrix, false);
		Bitmap localBitmap2 = Bitmap.createBitmap(i, j / 2, Bitmap.Config.ARGB_8888);
		Canvas localCanvas = new Canvas(localBitmap2);
		localCanvas.drawBitmap(localBitmap1, 0.0F, 0.0F, null);
		Paint localPaint = new Paint();
		localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		localPaint.setShader(new LinearGradient(0.0F, 0.0F, 0.0F, j / 2, 1879048192, 0, Shader.TileMode.CLAMP));
		localCanvas.drawRect(0.0F, 0.0F, i, j, localPaint);
		return localBitmap2;
	}

	private boolean hasFreeMemory() {
		long l1 = Runtime.getRuntime().freeMemory();
		ActivityManager localActivityManager = (ActivityManager) MyApp.getInstance().getSystemService("activity");
		ActivityManager.MemoryInfo localMemoryInfo = new ActivityManager.MemoryInfo();
		localActivityManager.getMemoryInfo(localMemoryInfo);
		long l2 = localMemoryInfo.availMem;

		boolean bool;
		if ((l1 < 5242880L) || (l2 < 5242880L)) {
			bool = true;
		} else {
			bool = false;
		}

		String str1 = Formatter.formatFileSize(MyApp.mContext, l1);
		String str2 = Formatter.formatFileSize(MyApp.getInstance(), l2);
		if (bool) {
			Logger.LOGD("memory", "\n  当前剩余内存--》" + str1 + "    availMem=" + str2 + "  result=====" + bool);
		} else {
			Logger.LOGD("memory", "\n  当前剩余内存不足--》" + str1 + "    availMem=" + str2 + "  result=====" + bool);
		}

		return bool;
	}

	public static Bitmap resizeBitmap(Bitmap paramBitmap, int paramInt1, int paramInt2) {
		Bitmap localBitmap;
		if ((paramInt1 > 0) && (paramInt2 > 0)) {
			int i = paramBitmap.getWidth();
			int j = paramBitmap.getHeight();
			float f1 = paramInt1 / i;
			float f2 = paramInt2 / j;
			Matrix localMatrix = new Matrix();
			localMatrix.setScale(f1, f2);
			localBitmap = Bitmap.createBitmap(paramBitmap, 0, 0, i, j, localMatrix, true);
			if (paramBitmap != null) {
				paramBitmap.recycle();
			}
		} else {
			localBitmap = paramBitmap;
		}

		Logger.LOGD("result   " + localBitmap.getWidth() + "---" + localBitmap.getHeight());
		return localBitmap;

	}

	public void addBitmap2Cache(String paramString, Bitmap paramBitmap) {
		if ((paramString == null) || (paramBitmap == null)) {
			Logger.LOGE("\nkey=" + paramString + "---bitmap=" + paramBitmap);
			return;
		}

		addBitmapToMemoryCache(paramString, paramBitmap);
		if (diskCache != null)
			;
		diskCache.put(paramString, paramBitmap);
	}

	public void addBitmapToMemoryCache(String paramString, Bitmap paramBitmap) {
		if ((paramString == null) || (paramBitmap == null)) {

			return;
		}
		if (getBitmapFromMemoryCache(paramString) != null) {
			mMemoryCache.put(paramString, paramBitmap);
		}

	}

	public Bitmap getBitmapFromMemoryCache(String paramString) {
		Bitmap localBitmap1;
		if (TextUtils.isEmpty(paramString)) {
			localBitmap1 = null;
			return localBitmap1;
		}

		localBitmap1 = (Bitmap) mMemoryCache.get(paramString);
		if (localBitmap1 != null) {
			Logger.LOGD("\n  软缓存--软缓存--软缓存--软缓存--软缓存     <" + debugTag + ">");
			return localBitmap1;
		}

		if ((diskCache != null) && (hasFreeMemory())) {
			Bitmap localBitmap2 = diskCache.get(paramString);
			if (localBitmap2 != null) {
				Logger.LOGD("\n  硬缓中已存在--硬缓已存在      <" + debugTag + ">");
				return localBitmap2;
			}

			Logger.LOGE("\n  硬缓中还不存在--硬缓还不存在  <" + debugTag + ">");
			return localBitmap2;
		}

		return null;
	}

	public Bitmap getRoundedCornerBitmap(Bitmap paramBitmap, int paramInt) {
		Bitmap localBitmap = Bitmap.createBitmap(paramBitmap.getWidth(), paramBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas localCanvas = new Canvas(localBitmap);
		localCanvas.drawARGB(0, 0, 0, 0);
		Rect localRect = new Rect(10, 10, paramBitmap.getWidth(), paramBitmap.getHeight());
		RectF localRectF = new RectF(localRect);
		Paint localPaint = new Paint();
		localPaint.setAntiAlias(true);
		localPaint.setColor(-12434878);
		localCanvas.drawRoundRect(localRectF, paramInt, paramInt, localPaint);
		localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		localCanvas.drawBitmap(paramBitmap, localRect, localRect, localPaint);
		System.out.println("返回圆角");
		return localBitmap;
	}

	public void removeBitmapFromMemoryCache(String paramString) {
		mMemoryCache.remove(paramString);
	}
}