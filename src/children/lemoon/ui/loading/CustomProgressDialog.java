package children.lemoon.ui.loading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
 
import children.lemoon.Configer;
import children.lemoon.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


/********************************************************************
 * [Summary]
 *       TODO 请在此处简要描述此类所实现的功能。因为这项注释主要是为了在IDE环境中生成tip帮助，务必简明扼要
 * [Remarks]
 *       TODO 请在此处详细描述类的功能、调用方法、注意事项、以及与其它类的关系.
 *******************************************************************/

public class CustomProgressDialog extends Dialog {
	private Context context = null;
	private static CustomProgressDialog customProgressDialog = null;
	private String mPath = null;
	private AnimationDrawable frameAnim;
 
	TextView tvMsg;
	private Drawable getDrawable(String path){
		//File f = new File(path);
		InputStream is = null;
		try {
			is = new FileInputStream(path);
			Drawable da = Drawable.createFromStream(is, null);
			return da;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		finally{
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	
	private class ComparatorByName implements Comparator<String> {
	    @Override
	    public int compare(String lhs, String rhs) {
	    	return lhs.compareTo(rhs);
	    }
	}
 
	
	private void init(){
		setContentView(R.layout.customprogressdialog);
		getWindow().getAttributes().gravity = Gravity.CENTER;
		
		//2015.8.12 解决loading独占焦点，导致底下activity无法响应事件的问题。
		// 关键在于让dialog无法响应焦点便可，如下：
		getWindow().getAttributes().flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		getWindow().getAttributes().flags |=  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;
		
		tvMsg = (TextView)findViewById(R.id.id_tv_loadingmsg);

		mPath = context.getCacheDir()+"/loadings/";
		File f = new File(mPath);
		if(!f.exists()){
			f.mkdirs();
		}
		

		String [] files = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				if(filename.contains(".png"))
					return true;
				return false;
			}
		});		
 
		
		Drawable[] imgs;
		if(files == null || files.length<2){
			//内置
			imgs = Configer.Res.getLoading(context);
		}
		else{
			Arrays.sort(files, new ComparatorByName());
			imgs = new Drawable[files.length];
			for(int i=0; i<files.length; i++){
				Drawable d = getDrawable(mPath+files[i]);
				if(d == null){
					imgs[0] = null;
					break;
				}
				imgs[i] = d;
			}
			 
			if(imgs[0] == null){
				//内置
				imgs = Configer.Res.getLoading(context);
			}
			else{
				//设置标题, 当以_开头的文件名就不用了
				//正常名字形式如： _xxxx_01.png, _xxxx_02.png
				// 或  xxxx_01.png, xxxx_02.png
				
				String s = files[0];
				if(!s.startsWith("_")){
					String[] info = s.split("_");
					if(info!=null && info.length>0){
						setMessage(info[0]);
					}
				}
			}
		}
		
		//开始检测
		context.startService(new Intent(context, LoadDataSrv.class));

		frameAnim =new AnimationDrawable();
		// 为AnimationDrawable添加动画帧
		for(Drawable d2 :imgs){
			frameAnim.addFrame(d2, 150);
		}
		frameAnim.setOneShot(false);
		

		 // 设置ImageView的背景为AnimationDrawable
		findViewById(R.id.loadingImageView).setBackgroundDrawable(frameAnim);
		
		frameAnim.start();
		
	}
	
	public CustomProgressDialog(Context context){
		super(context);
		this.context = context;
		init();
	}
	
	public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        init();
    }
	

	
	public static CustomProgressDialog createDialog(Context context){
		customProgressDialog = new CustomProgressDialog(context,R.style.CustomProgressDialog);
		customProgressDialog.setCanceledOnTouchOutside(false);
		return customProgressDialog;
	}
 
    public void onWindowFocusChanged(boolean hasFocus){
    	if (customProgressDialog == null){
    		return;
    	}
    	
        ImageView imageView = (ImageView) customProgressDialog.findViewById(R.id.loadingImageView);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();
    }
 
 
    
    /**
     * 
     * [Summary]
     *       setMessage 提示内容
     * @param strMessage
     * @return
     *
     */
    public void setMessage(String strMessage){    	
    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    	}
    }
    
    public void setCurPackage(String packname){
    	mCurPack = packname;
    }
    public void setCurStatus(String status){
    	mCurStatus = status;
    }
    
    private String mCurPack = "", mCurStatus="";
    public static String ACTION = "loading.CustomProgressDialog";
    
    
    //该加载框与其所属的Activity就是不相干的关系，加载中取消如何实现？
    //如下
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	// TODO Auto-generated method stub
    	if(event.getAction() == KeyEvent.ACTION_DOWN){
    		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
    			Intent intent = new Intent();
    			intent.setAction(ACTION);
    			intent.putExtra("curPackage", mCurPack);
    			intent.putExtra("curStatus", mCurStatus);
    			context.sendBroadcast(intent);
    			return super.dispatchKeyEvent(event);
    		}
    	}
    	return super.dispatchKeyEvent(event);
    }
 
}