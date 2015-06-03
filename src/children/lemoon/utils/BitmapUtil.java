package children.lemoon.utils;

//ok
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {
	public static Bitmap getBitMapFromFilePath(String paramString) {
		BitmapFactory.Options localOptions = new BitmapFactory.Options();
		localOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		localOptions.inSampleSize = 2;
		return BitmapFactory.decodeFile(paramString, localOptions);
	}

	public static Bitmap getBitMapFromResource(Context paramContext, int paramInt) {
		BitmapFactory.Options localOptions = new BitmapFactory.Options();
		localOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		localOptions.inPurgeable = true;
		localOptions.inInputShareable = true;
		InputStream localInputStream = paramContext.getResources().openRawResource(paramInt);
		Bitmap localBitmap = BitmapFactory.decodeStream(localInputStream, null, localOptions);
		if (localInputStream != null) {
		}
		try {
			localInputStream.close();
			return localBitmap;
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
		return localBitmap;
	}
}
