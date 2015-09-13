package children.lemoon.music;

import children.lemoon.Configer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {
    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static final int LONG_PRESS_DELAY = 1000;

    private static long mLastClickTime = 0;
    private static boolean mDown = false;
    private static boolean mLaunched = false;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LONGPRESS_TIMEOUT:
                    if (!mLaunched) {
                        Context context = (Context)msg.obj;
                        
                        Intent i = new Intent();
                        i.putExtra("longpress", true);
                        i.setClass(context, MuPlayer.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                        
                        
                        mLaunched = true;
                    }
                    break;
            }
        }
    };
    
    
    Context cx;
    int mCmd;
     @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        cx = context; 
        Log.e("", "RemoteControlReceiver action="+intentAction);
        
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
        	Configer.sendNotice(context, Configer.Action.SVR_CTL_ACTION, new String[]{"MSG", String.format("%d", Configer.PlayerMsg.PAUSE_MSG)});	 
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            
            if (event == null) {
                return;
            }

            int keycode = event.getKeyCode();
            int action = event.getAction();
            long eventtime = event.getEventTime();

            
            // single quick press: pause/resume. 
            // double press: next track
            // long press: start auto-shuffle mode.
            
            int command = -1;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = Configer.PlayerMsg.STOP_MSG;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    command = Configer.PlayerMsg.TOGGLEPAUSE_MSG;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = Configer.PlayerMsg.NEXT_MSG;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = Configer.PlayerMsg.PRIVIOUS_MSG;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    command = Configer.PlayerMsg.PAUSE_MSG;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = Configer.PlayerMsg.CONTINUE_MSG;
                    break;
            }

           
            Log.d("", "keycode="+keycode+", action="+action+", repeatcount="+event.getRepeatCount()+",eventtime-mLastClickTime="+(eventtime-mLastClickTime));
            if (command != -1) {
            	mCmd = command;
            	 
                if (action == KeyEvent.ACTION_DOWN) { 
                    if (mDown) {
                        if ((Configer.PlayerMsg.TOGGLEPAUSE_MSG == command  ||
                        		Configer.PlayerMsg.CONTINUE_MSG ==command)
                                && mLastClickTime != 0 
                                && eventtime - mLastClickTime > LONG_PRESS_DELAY) {
                            mHandler.sendMessage( mHandler.obtainMessage(MSG_LONGPRESS_TIMEOUT, context));
                        }
                    } else if (event.getRepeatCount() == 0) {
//                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK && eventtime - mLastClickTime < 300) {
//                            String[] param = new String[]{"MSG", String.format("%d", Configer.PlayerMsg.NEXT_MSG)};
//                            Configer.sendNotice(context, Configer.Action.SVR_CTL_ACTION, param);
//                            mLastClickTime = 0;
//                        }
//                        else if(eventtime-eventtime>300 && mLastClickTime !=0 ){
//    	                    String[] param = new String[]{"MSG", String.format("%d", command)};
//    	                    Configer.sendNotice(context, Configer.Action.SVR_CTL_ACTION, param);
//    	                    mLastClickTime = 0;
//                        }
//                        else{
//                        	mLastClickTime = eventtime;
//                        }
                    	
                    	mHandler.removeCallbacks(mRunner);
                    	mHandler.postDelayed(mRunner, 500);
                    	
                    	mLastClickTime = eventtime;
                    	
                        mLaunched = false;
                        mDown = true;
                    }
                    else{
                    	mLastClickTime = eventtime;
                    }
                } 
                else {
                    mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT);
                    mDown = false;
                }
                
                if (isOrderedBroadcast()) {
                    abortBroadcast();
                }
            }
        }
    }
     
     
    Runnable mRunner = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!mDown){
                 String[] param = new String[]{"MSG", String.format("%d", mCmd)};
                 Configer.sendNotice(cx, Configer.Action.SVR_CTL_ACTION, param);
				}
			}
		};

}
