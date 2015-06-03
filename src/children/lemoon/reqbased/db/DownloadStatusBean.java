package children.lemoon.reqbased.db;

import com.j256.ormlite.field.DatabaseField;
import java.io.Serializable;

public class DownloadStatusBean implements Serializable {
	private static final long serialVersionUID = 6147652026177435702L;
	@DatabaseField(id = true)
	public int gameId;
	@DatabaseField
	public int status;

	public DownloadStatusBean() {
	}

	public DownloadStatusBean(int paramInt1, int paramInt2) {
		this.gameId = paramInt1;
		this.status = paramInt2;
	}

	public int getGameId() {
		return this.gameId;
	}

	public int getStatus() {
		return this.status;
	}

	public void setGameId(int paramInt) {
		this.gameId = paramInt;
	}

	public void setStatus(int paramInt) {
		this.status = paramInt;
	}

	public String toString() {
		return "DownloadStatusBean [gameId=" + this.gameId + ", status=" + this.status + "]";
	}
}

/*
 * Location:
 * C:\Users\Administrator\Desktop\2.开发相关\3D波波\V2.1.10.10\classes-dex2jar.jar
 * 
 * Qualified Name: com.bobo.splayer.entity.VR.DownloadStatusBean
 * 
 * JD-Core Version: 0.7.0.1
 */