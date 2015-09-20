package children.lemoon;

import children.lemoon.utils.Logger;
import children.lemoon.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class Configer {
	public static  String OEM_NAME = "piapia_v2"; 
	public static boolean _DEBUG = false;
	public static String COM_KEY = "@#xpia&*1452";
	public static String IMG_URL_PRE = "http://www.nybgjd.com/misc/story_res/images/";
	
	public static class Res{
		private static final int default_icon_for_categrid1 = R.drawable.mmh_p;
		private static final int default_icon_for_categrid2 = R.drawable.p;
		
		private static final int default_icon_for_player1 = R.drawable.mv_bg_default_mmh;
		private static final int default_icon_for_player2 = R.drawable.mv_bg_default;
		
		public static int get_icon_for_categrid(){
			if(OEM_NAME.contains("piapia")){
				return default_icon_for_categrid1;
			}
			return default_icon_for_categrid2;
		}
		
		public static int get_icon_for_player(){
			if(OEM_NAME.contains("piapia")){
				return default_icon_for_player1;
			}
			return default_icon_for_player2;
		}
		
	 
		
		public static Drawable[] getLoading(Context context){
			Drawable[] imgs;
			if(OEM_NAME.contains("piapia")){
 
				imgs = new Drawable[7];
				imgs[0 ] = context.getResources().getDrawable(R.drawable.mloading1 );
				imgs[1 ] = context.getResources().getDrawable(R.drawable.mloading2 );
                imgs[2 ] = context.getResources().getDrawable(R.drawable.mloading3  );
				imgs[3 ] = context.getResources().getDrawable(R.drawable.mloading4  );
                imgs[4 ] = context.getResources().getDrawable(R.drawable.mloading5  );
				imgs[5 ] = context.getResources().getDrawable(R.drawable.mloading6  );
                imgs[6 ] = context.getResources().getDrawable(R.drawable.mloading7  );
			}	 
			else{
				imgs = new Drawable[12];
				imgs[0 ] = context.getResources().getDrawable(R.drawable.loading1 );
				imgs[1 ] = context.getResources().getDrawable(R.drawable.loading2 );
				imgs[2 ] = context.getResources().getDrawable(R.drawable.loading3 );
				imgs[3 ] = context.getResources().getDrawable(R.drawable.loading4 );
				imgs[4 ] = context.getResources().getDrawable(R.drawable.loading5 );
				imgs[5 ] = context.getResources().getDrawable(R.drawable.loading6 );
				imgs[6 ] = context.getResources().getDrawable(R.drawable.loading7 );
				imgs[7 ] = context.getResources().getDrawable(R.drawable.loading8 );
				imgs[8 ] = context.getResources().getDrawable(R.drawable.loading9 );
				imgs[9 ] = context.getResources().getDrawable(R.drawable.loading10);
				imgs[10] = context.getResources().getDrawable(R.drawable.loading11);
				imgs[11] = context.getResources().getDrawable(R.drawable.loading12);
			}
			return imgs;
		}
	}
	
	
	public static final int REQ_TYPE_CATEINFO = 100;
	public static final int REQ_TYPE_HLIST = 101;	
	public static final int REQ_PLAYURL = 300;
	
	public static final int REQ_VIDEO_PLAYLIST = 1001;
	public static final int REQ_AUDIO_PLAYLIST = 1002;
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

		case REQ_TYPE_HLIST:
			rt = "http://www.nybgjd.com/erge/"+OEM_NAME+"/getHResList/";
			break;
		case REQ_TYPE_CATEINFO:
			rt = "http://www.nybgjd.com/erge/"+OEM_NAME+"/getresList/";
			break;
		case REQ_PLAYURL:
			rt = "http://www.nybgjd.com/erge/"+OEM_NAME+"/playurl/";
			break;
			
		case 20:
			rt = "http://www.nybgjd.com/misc/livesrc/childcatedata.json";
			break;			
		case REQ_VIDEO_PLAYLIST:
			//rt = "http://10.0.2.2/ci/erge.php/erge/api/getPL/";
			rt = "http://www.nybgjd.com/erge/"+OEM_NAME+"/getPL/";
			break;
		case REQ_AUDIO_PLAYLIST:
			//rt = "http://10.0.2.2/ci/erge.php/erge/api/getPL/a/";
			rt = "http://www.nybgjd.com/erge/"+OEM_NAME+"/getPL/";
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
		public static final int TOGGLEPAUSE_MSG = 609;	//播放/暂停
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
		public static final String ACT_MUSIC_CURRENT = "lemoon.action.MUSIC_CURRENT";	//当前音乐播放时间更新动作
		public static final String ACT_UPDATE_ACTION = "lemoon.action.UPDATE_ACTION";	//更新动作
		public static final String ACT_MUSIC_DURATION = "lemoon.action.MUSIC_DURATION";//新音乐长度更新动作
		public static final String ACT_UPDATE_PlAYLIST = "lemoon.action.PlAYLIST_UPDATE"; //刷新播放列表	
		public static final String ACT_CUR_FINISHED = "lemoon.action.CUR_FINISHED";	//当前播放完成时触发的消息
		public static final String ACT_STATUS_RESET = "lemoon.action.STATUS_RESET";
		public static final String ACT_NEW_CATE_DATA = "lemoon.action.ACT_NEW_CATE_DATA";
		public static final String ACT_SHOW_LOADING = "lemoon.action.ACT_SHOW_LOADING";
		public static final String ACT_HIDE_LOADING = "lemoon.action.ACT_HIDE_LOADING";
		public static final String ACT_MUPLAYER_LAUCHED = "lemoon.action.muplayer.lauched";

		//Service
		public static final String SVR_CTL_ACTION = "lemoon.action.CTL_ACTION";		//控制动作 参数：listPosition, MSG, progress;
		public static final String SVR_SHOW_LRC = "lemoon.action.SHOW_LRC";			//通知显示歌词
		public static final String SVR_GET_NEWPG = "lemoon.action.GET_NEWPG";		//取新页面		
		
		//Video
		public static final String MV_CTL_PLAY = "lemoon.action.MV_CTL_PLAY";	 
		public static final String MV_CTL_PAUSE = "lemoon.action.MV_CTL_PAUSE";	 
		public static final String MV_CTL_NEXT = "lemoon.action.MV_CTL_NEXT";		 
		public static final String MV_CTL_PREV = "lemoon.action.MV_CTL_PREV";		 
		public static final String MV_CTL_PLAY_PAUSE = "lemoon.action.MV_CTL_PLAY_PAUSE";	 	
		

		//退出
		public static final String ACT_EXIT = "lemoon.action.exit";
		
		
		//系统的音乐播放器相关Action
		public static final String SERVICECMD = "com.android.music.musicservicecommand";
		public static final String CMDNAME = "command";
		public static final String CMDTOGGLEPAUSE = "togglepause";
		public static final String CMDSTOP = "stop";
		public static final String CMDPAUSE = "pause";
		public static final String CMDPREVIOUS = "previous";
		public static final String CMDNEXT = "next";
		
		public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
		public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
		public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
		public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
	}

	
	public class RunMode{
		public static final int MODE_NETWORK = 50;	
		public static final int MODE_LOCAL = 51;	
		public static final int MODE_DIRECT = 52;
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
