package children.lemoon.reqbased.db;

import com.j256.ormlite.field.DatabaseField;
import java.io.Serializable;

public class DownloadItem implements Serializable {
	private static final long serialVersionUID = 4282278874812077767L;
	@DatabaseField
	public long downloadId;
	@DatabaseField(id = true)
	public int gameId;

	public DownloadItem() {
	}

	public DownloadItem(Integer paramInteger, Long paramLong) {
		this.gameId = paramInteger.intValue();
		this.downloadId = paramLong.longValue();
	}

	public Long getDownloadId() {
		return Long.valueOf(this.downloadId);
	}

	public int getGameId() {
		return this.gameId;
	}

	public void setDownloadId(Long paramLong) {
		this.downloadId = paramLong.longValue();
	}

	public void setGameId(Integer paramInteger) {
		this.gameId = paramInteger.intValue();
	}

	public String toString() {
		return "DownloadItemEntity [gameId=" + this.gameId + ", downloadId=" + this.downloadId + "]";
	}
}

/*
 * Location:
 * C:\Users\Administrator\Desktop\2.开发相关\3D波波\V2.1.10.10\classes-dex2jar.jar
 * 
 * Qualified Name: com.bobo.splayer.entity.VR.DownloadItem
 * 
 * JD-Core Version: 0.7.0.1
 */