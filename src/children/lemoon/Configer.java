package children.lemoon;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import logger.lemoon.Logger;

public class Configer {
	public static final int OEM_ID = 2; //lemoon-wifi
	
	public static class Res{
		private static final int default_icon_for_categrid1 = R.drawable.p;
		private static final int default_icon_for_categrid2 = R.drawable.p;
		
		private static final int default_icon_for_player1 = R.drawable.mv_bg_default;
		private static final int default_icon_for_player2 = R.drawable.mv_bg_default;
		
		public static int get_icon_for_categrid(){
			switch(OEM_ID){
			case 2:
				return default_icon_for_categrid1;
			}
			return default_icon_for_categrid2;
		}
		
		public static int get_icon_for_player(){
			switch(OEM_ID){
			case 2:
				return default_icon_for_player1;
			}
			return default_icon_for_player2;
		}
		
		
		public static Drawable[] getLoading(Context context){
			Drawable[] imgs = new Drawable[3];
			imgs[0] = context.getResources().getDrawable(R.drawable.ic_launcher);
			imgs[1] = context.getResources().getDrawable(R.drawable.ic_launcher);
			imgs[2] = context.getResources().getDrawable(R.drawable.ic_launcher);
			return imgs;
		}
	}
	
	
	public static final int REQ_TYPE_CATEINFO = 100;
	
	
	public static final int TYPE_QUICK_ENTRY_TEST = 1001;
	public static final int TYPE_QUICK_ENTRY_TESTA = 1002;
	// rocking debug !!!!!!!!!!!
	public static String initUrl(int paramInt) {
		String prefix = "http://www.nybgjd.com/3dclub/api4/";
		prefix = "";
		String rt = null;
		switch (paramInt) // ok
		{
		default:
			Logger.LOGE("", "++++++++++++++++++++++++invlide request:" + paramInt);
			break;

		case 20:
			rt = "http://www.nybgjd.com/misc/livesrc/childcatedata.json";
			break;

		case REQ_TYPE_CATEINFO:
			rt = "http://www.nybgjd.com/erge/api2/getresList/";
			break;
		case TYPE_QUICK_ENTRY_TEST:
			rt = "http://10.0.2.2/ci/erge.php/erge/api/getPL/";
			rt = "http://www.nybgjd.com/erge/api2/getPL/";
			
			break;
		case TYPE_QUICK_ENTRY_TESTA:
			rt = "http://10.0.2.2/ci/erge.php/erge/api/getPL/a/";
			rt = "http://www.nybgjd.com/erge/api/getPL/a/";
			break;			
		}
		return rt;

	}
	
	
	
	
	
	
	
	
	public class PlayerMsg {
		public static final int PLAY_MSG = 601;		//播放
		public static final int PAUSE_MSG = 602;		//暂停
		public static final int STOP_MSG = 603;		//停止
		public static final int CONTINUE_MSG = 604;	//继续
		public static final int PRIVIOUS_MSG = 605;	//上一首
		public static final int NEXT_MSG = 606;		//下一首
		public static final int PROGRESS_CHANGE = 607;//进度改变
		public static final int PLAYING_MSG = 608;	//正在播放
	}
	
	
	public class PlayMode{
		public static final int MODE_REPEAT1 = 501;		//单曲循环
		public static final int MODE_REPEATALL = 502;		//全部循环
		public static final int MODE_LIST = 503;		//顺序播放
		public static final int MODE_RANDOM = 504;		//随机播放
	}
	
	
	//服务要发送的一些Action
	public class Action{
		//Activity
		public static final String ACT_MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT";	//当前音乐播放时间更新动作
		public static final String ACT_UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION";	//更新动作
		public static final String ACT_MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";//新音乐长度更新动作
		public static final String ACT_UPDATE_PlAYLIST = "com.wwj.action.PlAYLIST_UPDATE"; //刷新播放列表	
		public static final String ACT_CUR_FINISHED = "com.wwj.action.CUR_FINISHED";	//当前播放完成时触发的消息

		//Service
		public static final String SVR_CTL_ACTION = "com.wwj.action.CTL_ACTION";		//控制动作 参数：listPosition, MSG, progress;
		public static final String SVR_SHOW_LRC = "com.wwj.action.SHOW_LRC";			//通知显示歌词
		public static final String SVR_GET_NEWPG = "com.wwj.action.GET_NEWPG";		//取新页面		
		
		//Video
		public static final String MV_CTL_PLAY = "com.wwj.action.MV_CTL_PLAY";	 
		public static final String MV_CTL_PAUSE = "com.wwj.action.MV_CTL_PAUSE";	 
		public static final String MV_CTL_NEXT = "com.wwj.action.MV_CTL_NEXT";		 
		public static final String MV_CTL_PREV = "com.wwj.action.MV_CTL_PREV";		 
		public static final String MV_CTL_PLAY_PAUSE = "com.wwj.action.MV_CTL_PLAY_PAUSE";	 		
		
	}

	
	public class RunMode{
		public static final int MODE_NETWORK = 50;	
		public static final int MODE_LOCAL = 51;	
	}
	
	
	static public void sendNotice(Context cx, String action, String[]args){
		if(action == null || action.isEmpty())
			return;
		
		Intent intent = new Intent();
		intent.setAction(action);
		
		if(args!=null){
			int size = args.length;
			for(int i=0; i<(size-1); i=i+2){
				intent.putExtra(args[i], args[i+1]);
			}
		}
		
		cx.sendBroadcast(intent);
	}
}
