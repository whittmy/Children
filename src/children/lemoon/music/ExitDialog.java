package children.lemoon.music;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.ui.loading.CustomProgressDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.View;

public class ExitDialog  extends Dialog {
	Context context;
	//ClickEvent mClick;
	//View.OnClickListener mClick;
	static ExitDialog dlg;
	
	public ExitDialog(Context context){
		super(context);
		this.context = context;
		init();
	}
	
	public ExitDialog(Context context, int theme) {
        super(context, theme);
		this.context = context;
		init();
    }
	
	public void setMyOnClickListener(View.OnClickListener click){
		if(click != null){
			findViewById(R.id.btn_close_music).setOnClickListener(click);
			findViewById(R.id.btn_bg_music).setOnClickListener(click);
		}
	}
	
//	class ClickEvent implements View.OnClickListener{
//		@Override
//		public void onClick(View arg0) {
//			// TODO Auto-generated method stub
//			switch(arg0.getId()){
//			case R.id.ok:
//				Log.e("", "============ ok");
//				Configer.sendNotice(context, Configer.Action.SVR_CTL_ACTION, new String[]{"MSG", String.format("%d", Configer.PlayerMsg.STOP_MSG)});	 
//				
//				
//				break;
//				
//			case R.id.cancle:
//				Log.e("", "============ cancel");
//				break;
//			}
//			
//			dlg.dismiss();
//		}
//	}
	
	private void init(){
 		
		setContentView(R.layout.exitdlg);
		

	}
	
	public static ExitDialog createDialog(Context context){
		dlg = new ExitDialog(context,R.style.CustomProgressDialog);
		//dlg.setCanceledOnTouchOutside(false);
		return dlg;
	}
 
}
