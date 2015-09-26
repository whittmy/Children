package dodola.example.android.bitmapfun.util;
//ok
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import java.lang.ref.WeakReference;

import children.lemoon.utils.Logger;

 
public abstract class ImageWorker
{
  private static final int FADE_IN_TIME = 200;
  private static final String TAG = "ImageWorker";
  protected Context mContext;
  private boolean mExitTasksEarly = false;
  private boolean mFadeInBitmap = true;
  private ImageCache mImageCache;
  protected ImageWorkerAdapter mImageWorkerAdapter;
  private Bitmap mLoadingBitmap;
  
  protected ImageWorker(Context paramContext)
  {
    this.mContext = paramContext;
  }
  
  public static boolean cancelPotentialWork(Object paramObject, ImageView paramImageView)
  {
    BitmapWorkerTask localBitmapWorkerTask = getBitmapWorkerTask(paramImageView);
    if (localBitmapWorkerTask != null)
    {
      Object localObject = localBitmapWorkerTask.data;
      if ((localObject == null) || (!localObject.equals(paramObject)))
      {
        localBitmapWorkerTask.cancel(true);
        Logger.LOGD("ImageWorker", "cancelPotentialWork - cancelled work for " + paramObject);
      }
    }
    else
    {
      return true;
    }
    return false;
  }
  
  public static void cancelWork(ImageView paramImageView)
  {
    BitmapWorkerTask localBitmapWorkerTask = getBitmapWorkerTask(paramImageView);
    if (localBitmapWorkerTask != null)
    {
      localBitmapWorkerTask.cancel(true);
      Object localObject = localBitmapWorkerTask.data;
      Logger.LOGD("ImageWorker", "cancelWork - cancelled work for " + localObject);
    }
  }
  
  private static BitmapWorkerTask getBitmapWorkerTask(ImageView paramImageView)
  {
    if (paramImageView != null)
    {
      Drawable localDrawable = paramImageView.getDrawable();
      if ((localDrawable instanceof AsyncDrawable)) {
        return ((AsyncDrawable)localDrawable).getBitmapWorkerTask();
      }
    }
    return null;
  }
  
  private void setImageBitmap(ImageView paramImageView, Bitmap paramBitmap)
  {
    if (this.mFadeInBitmap)
    {
      Drawable[] arrayOfDrawable = new Drawable[2];
      arrayOfDrawable[0] = new ColorDrawable(17170445);
      arrayOfDrawable[1] = new BitmapDrawable(this.mContext.getResources(), paramBitmap);
      TransitionDrawable localTransitionDrawable = new TransitionDrawable(arrayOfDrawable);
      paramImageView.setBackgroundDrawable(new BitmapDrawable(this.mContext.getResources(), this.mLoadingBitmap));
      paramImageView.setImageDrawable(localTransitionDrawable);
      localTransitionDrawable.startTransition(200);
      return;
    }
    paramImageView.setImageBitmap(paramBitmap);
  }
  
  public ImageWorkerAdapter getAdapter()
  {
    return this.mImageWorkerAdapter;
  }
  
  public ImageCache getImageCache()
  {
    return this.mImageCache;
  }
  
  public void loadImage(int paramInt, ImageView paramImageView)
  {
    if (this.mImageWorkerAdapter != null)
    {
      loadImage(this.mImageWorkerAdapter.getItem(paramInt), paramImageView);
      return;
    }
    throw new NullPointerException("Data not set, must call setAdapter() first.");
  }
  
  public void loadImage(Object paramObject, ImageView paramImageView)
  {
    if ((paramObject == null) || (paramImageView == null)) {
    	return;
    }

      ImageCache localImageCache = this.mImageCache;
      Bitmap localBitmap = null;
      if (localImageCache != null) {
        localBitmap = this.mImageCache.getBitmapFromMemCache(String.valueOf(paramObject));
      }
      Logger.LOGD("xiongyun", "");
      if (localBitmap != null)
      {
        paramImageView.setImageBitmap(localBitmap);
        return;
      }
      
     if (cancelPotentialWork(paramObject, paramImageView)){
	    BitmapWorkerTask localBitmapWorkerTask = new BitmapWorkerTask(paramImageView);
	    paramImageView.setImageDrawable(new AsyncDrawable(this.mContext.getResources(), this.mLoadingBitmap, localBitmapWorkerTask));
	    localBitmapWorkerTask.execute(new Object[] { paramObject });
     }
  }
  
  protected abstract Bitmap processBitmap(Object paramObject);
  
  public void setAdapter(ImageWorkerAdapter paramImageWorkerAdapter)
  {
    this.mImageWorkerAdapter = paramImageWorkerAdapter;
  }
  
  public void setExitTasksEarly(boolean paramBoolean)
  {
    this.mExitTasksEarly = paramBoolean;
  }
  
  public void setImageCache(ImageCache paramImageCache)
  {
    this.mImageCache = paramImageCache;
  }
  
  public void setImageFadeIn(boolean paramBoolean)
  {
    this.mFadeInBitmap = paramBoolean;
  }
  
  public void setLoadingImage(int paramInt)
  {
    this.mLoadingBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), paramInt);
  }
  
  public void setLoadingImage(Bitmap paramBitmap)
  {
    this.mLoadingBitmap = paramBitmap;
  }
  
  private static class AsyncDrawable
    extends BitmapDrawable
  {
    private final WeakReference<ImageWorker.BitmapWorkerTask> bitmapWorkerTaskReference;
    
    public AsyncDrawable(Resources paramResources, Bitmap paramBitmap, ImageWorker.BitmapWorkerTask paramBitmapWorkerTask)
    {
      super(paramBitmap);
      this.bitmapWorkerTaskReference = new WeakReference(paramBitmapWorkerTask);
    }
    
    public ImageWorker.BitmapWorkerTask getBitmapWorkerTask()
    {
      return (ImageWorker.BitmapWorkerTask)this.bitmapWorkerTaskReference.get();
    }
  }
  
  private class BitmapWorkerTask
    extends AsyncTask<Object, Void, Bitmap>
  {
    private Object data;
    private final WeakReference<ImageView> imageViewReference;
    
    public BitmapWorkerTask(ImageView paramImageView)
    {
      this.imageViewReference = new WeakReference(paramImageView);
    }
    
    private ImageView getAttachedImageView()
    {
      ImageView localImageView = (ImageView)this.imageViewReference.get();
      if (this == ImageWorker.getBitmapWorkerTask(localImageView)) {
        return localImageView;
      }
      return null;
    }
    
    @Override
	protected Bitmap doInBackground(Object... paramVarArgs)
    {
      this.data = paramVarArgs[0];
      String str = String.valueOf(this.data);
      ImageCache localImageCache = ImageWorker.this.mImageCache;
      Bitmap localBitmap = null;
      if (localImageCache != null)
      {
        boolean bool1 = isCancelled();
        localBitmap = null;
        if (!bool1)
        {
          ImageView localImageView = getAttachedImageView();
          localBitmap = null;
          if (localImageView != null)
          {
            boolean bool2 = ImageWorker.this.mExitTasksEarly;
            localBitmap = null;
            if (!bool2) {
              localBitmap = ImageWorker.this.mImageCache.getBitmapFromDiskCache(str);
            }
          }
        }
      }
      if ((localBitmap == null) && (!isCancelled()) && (getAttachedImageView() != null) && (!ImageWorker.this.mExitTasksEarly)) {
        localBitmap = ImageWorker.this.processBitmap(paramVarArgs[0]);
      }
      if ((localBitmap != null) && (ImageWorker.this.mImageCache != null)) {
        ImageWorker.this.mImageCache.addBitmapToCache(str, localBitmap);
      }
      return localBitmap;
    }
    
    @Override
	protected void onPostExecute(Bitmap paramBitmap)
    {
      if ((isCancelled()) || (ImageWorker.this.mExitTasksEarly)) {
        paramBitmap = null;
      }
      ImageView localImageView = getAttachedImageView();
      if ((paramBitmap != null) && (localImageView != null)) {
        ImageWorker.this.setImageBitmap(localImageView, paramBitmap);
      }
    }
  }
  
  public static abstract class ImageWorkerAdapter
  {
    public abstract Object getItem(int paramInt);
    
    public abstract int getSize();
  }
}
