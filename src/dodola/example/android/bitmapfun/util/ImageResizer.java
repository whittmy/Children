package dodola.example.android.bitmapfun.util;
//ok
 import children.lemoon.utils.Logger;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class ImageResizer
  extends ImageWorker
{
  private static final String TAG = "ImageWorker";
  protected int mImageHeight;
  protected int mImageWidth;
  
  public ImageResizer(Context paramContext, int paramInt)
  {
    super(paramContext);
    setImageSize(paramInt);
  }
  
  public ImageResizer(Context paramContext, int paramInt1, int paramInt2)
  {
    super(paramContext);
    setImageSize(paramInt1, paramInt2);
  }
  
  public static int calculateInSampleSize(BitmapFactory.Options paramOptions, int paramInt1, int paramInt2)
  {
    int i = paramOptions.outHeight;
    int j = paramOptions.outWidth;
    int k = 1;
    float f1;
    float f2;
    if ((i > paramInt2) || (j > paramInt1))
    {
    	//cond0
      if (j <= i) {
    	  //cond_2
    	  k = Math.round(j / paramInt1);
      }
      else{
    	  k = Math.round(i / paramInt2);
      }
      //goto0
      f1 = j * i;
      f2 = 2 * (paramInt1 * paramInt2);
      
      //goto1
      while (f1 / (k * k) > f2)
      {
    	  //cond3
    	  k++;
      }
    }

    //cond1
    return k;  
  }
  
  public static Bitmap decodeSampledBitmapFromFile(String paramString, int paramInt1, int paramInt2)
  {
    try
    {
      BitmapFactory.Options localOptions = new BitmapFactory.Options();
      localOptions.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(paramString, localOptions);
      localOptions.inSampleSize = calculateInSampleSize(localOptions, paramInt1, paramInt2);
      localOptions.inJustDecodeBounds = false;
      Bitmap localBitmap = BitmapFactory.decodeFile(paramString, localOptions);
      return localBitmap;
    }
    finally
    {
      //localObject = finally;
      //throw localObject;
    }
  }
  
  public static Bitmap decodeSampledBitmapFromResource(Resources paramResources, int paramInt1, int paramInt2, int paramInt3)
  {
    BitmapFactory.Options localOptions = new BitmapFactory.Options();
    localOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(paramResources, paramInt1, localOptions);
    localOptions.inSampleSize = calculateInSampleSize(localOptions, paramInt2, paramInt3);
    localOptions.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(paramResources, paramInt1, localOptions);
  }
  
  private Bitmap processBitmap(int paramInt)
  {
    Logger.LOGD("ImageWorker", "processBitmap - " + paramInt);
    return decodeSampledBitmapFromResource(this.mContext.getResources(), paramInt, this.mImageWidth, this.mImageHeight);
  }
  
  @Override
protected Bitmap processBitmap(Object paramObject)
  {
    return processBitmap(Integer.parseInt(String.valueOf(paramObject)));
  }
  
  public void setImageSize(int paramInt)
  {
    setImageSize(paramInt, paramInt);
  }
  
  public void setImageSize(int paramInt1, int paramInt2)
  {
    this.mImageWidth = paramInt1;
    this.mImageHeight = paramInt2;
  }
}

