package children.lemoon.utils;

//ok
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff; //android.graphics.PorterDuff.Mode
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import children.lemoon.MyApp;
import children.lemoon.R;

import logger.lemoon.Logger;

public class MyImageLoadTask extends AsyncTask<String, Void, Bitmap> {
	private ImageLoader imageLoader;
	private int imgHeight;
	private int imgWidth;
	private String mImageUrl;
	private ImageView mImageView;
	private int roundPx = 0;

	public MyImageLoadTask(ImageView paramImageView) {
		this.mImageView = paramImageView;
		this.imageLoader = ImageLoader.getInstance();
	}

	public MyImageLoadTask(ImageView paramImageView, int paramInt1, int paramInt2) {
		this.mImageView = paramImageView;
		this.imgWidth = paramInt1;
		this.imgHeight = paramInt2;
		this.imageLoader = ImageLoader.getInstance();
	}

	/* Error */
	private void downloadImage(String imageUrl) {
		Logger.LOGD("downloadImage---imageUrl=" + imageUrl);
		HttpURLConnection con = null; // v8
		FileOutputStream fos = null;// v11
		BufferedOutputStream bos = null; // v7
		BufferedInputStream bis = null; // v4
		File imageFile = null; // v13
		Logger.LOGD("1c   downloadImage---imageUrl=" + imageUrl);

		URL url = null;// v15
		try {
			url = new URL(imageUrl);
			con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(0x1388);
			con.setReadTimeout(0x3a98);
			con.setDoInput(true);
			bis = new BufferedInputStream(con.getInputStream());
			imageFile = new File(getImagePath(imageUrl));
			fos = new FileOutputStream(imageFile);
			bos = new BufferedOutputStream(fos);
			byte[] b = new byte[0x400]; // v2

			// goto0
			int length = -1; // v14
			while ((length = bis.read(b)) != -1) {
				// cond4
				bos.write(b, 0, length);
				bos.flush();
				// go goto0
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// cond0:
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// cond1:
			if (con != null) {
				con.disconnect();
			}
		}

		// cond2/goto1
		if (imageFile != null) {
			// v5
			Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(imageFile.getPath(), imgWidth, imgHeight);
			imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
		}

		// cond3
		return;
	}

	private String getImagePath(String paramString) {
		String str1 = paramString.substring(1 + paramString.lastIndexOf("/"));
		String str2 = Environment.getExternalStorageDirectory().getPath() + "/.PhotoChildren/";
		File localFile = new File(str2);
		if (!localFile.exists()) {
			localFile.mkdirs();
		}
		return str2 + str1;
	}

	private Bitmap getRoundedCornerBitmap(Bitmap paramBitmap, int paramInt) {
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
		Logger.LOGD("返回圆角");
		return localBitmap;
	}

	private Bitmap loadImage(String paramString) {
		File localFile = new File(getImagePath(paramString));
		if (!localFile.exists()) {
			downloadImage(paramString);
		}
		Bitmap localBitmap = ImageLoader.decodeSampledBitmapFromResource(localFile.getPath(), this.imgWidth, this.imgHeight);
		if (localBitmap != null) {
			this.imageLoader.addBitmapToMemoryCache(paramString, localBitmap);
			return localBitmap;
		}

		boolean bool = localFile.delete();
		Logger.LOGD("MyImageLoadTask", "deleted=" + bool + "-该图片已损毁：" + paramString + "----" + getImagePath(paramString));
		return BitmapFactory.decodeStream(MyApp.getInstance().getResources().openRawResource(R.drawable.p));
	}

	protected Bitmap doInBackground(String... paramVarArgs) {
		this.mImageUrl = paramVarArgs[0];

		Bitmap localBitmap = this.imageLoader.getBitmapFromMemoryCache(this.mImageUrl);
		if (localBitmap == null) {
			localBitmap = loadImage(this.mImageUrl);
		}
		if (this.roundPx != 0) {
			localBitmap = getRoundedCornerBitmap(localBitmap, this.roundPx);
		}
		return localBitmap;
	}

	protected void onPostExecute(Bitmap paramBitmap) {
		super.onPostExecute(paramBitmap);
		if (paramBitmap != null) {
			this.mImageView.setImageBitmap(paramBitmap);
		}
	}
}
