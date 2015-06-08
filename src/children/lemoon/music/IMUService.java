package children.lemoon.music;

import java.util.LinkedList;

import children.lemoon.Configer;
import children.lemoon.myrespone.PlayItemEntity;

//音乐播放服务开放接口
public interface IMUService {
	public int getCurPos();
	
	public void setCurPos(int pos);
	public LinkedList<PlayItemEntity> getDatas();
	public int getCurTm();
	public int getDuration();
	public boolean isPlaying();
	
	public String getCurTitle();
	
	public int getCurRunMode();
}
