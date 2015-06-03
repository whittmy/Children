package children.lemoon.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;

public class VolleyImageCache implements ImageLoader.ImageCache {
	private static VolleyImageCache myImageCache;
	private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>((int) Runtime.getRuntime().maxMemory() / 8) {
		protected int sizeOf(String paramAnonymousString, Bitmap paramAnonymousBitmap) {
			return paramAnonymousBitmap.getHeight() * paramAnonymousBitmap.getRowBytes();
		}
	};

	public static VolleyImageCache getInstance() {
		if (myImageCache == null) {
			myImageCache = new VolleyImageCache();
		}
		return myImageCache;
	}

	public Bitmap getBitmap(String paramString) {
		return (Bitmap) this.mLruCache.get(paramString);
	}

	public void putBitmap(String paramString, Bitmap paramBitmap) {
		this.mLruCache.put(paramString, paramBitmap);
	}
}
