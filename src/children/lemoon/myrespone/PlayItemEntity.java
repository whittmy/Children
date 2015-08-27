package children.lemoon.myrespone;

//ok
import java.net.URLEncoder;

import children.lemoon.reqbased.entry.BaseEntity;

public class PlayItemEntity extends BaseEntity {
	private static final long serialVersionUID = 4848129606284363626L;

	private String id;
	private int fileSize;
	private int playcnt;

	private String downurl;
	private String pic;
	private String name;
	private String artist;

	private String hasseq;
	private String src;
	
	private int type;
	
	public String getIds() {
		return this.id;
	}

	public void setIds(String paramInt) {
		this.id = paramInt;
	}

	public int getType() {
		return this.type;
	}

	public void setType(int t) {
		this.type = t;
	}	
	
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public long getFileSize() {
		return this.fileSize;
	}

	public void setPlayCnt(int paramLong) {
		this.playcnt = paramLong;
	}

	public int getPlayCnt() {
		return this.playcnt;
	}

	public void setDownUrl(String url) {
		this.downurl = url;
	}

	public String getDownUrl() {
		return this.downurl;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getPic() {
		return this.pic;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getArtist() {
		return this.artist;
	}

	public void setSeq(String hasseq) {
		this.hasseq = hasseq;
	}

	public String getSeq() {
		return this.hasseq;
	}	
	
	
	public void setSrc(String s) {
		this.src = s;
	}

	public String getSrc() {
		return this.src;
	}		
	
	
	
	
	
	public String toString() {
		return "PlayItemEntity [id=" + this.id + ", name=" + this.name + ", fileSize=" + this.fileSize + ", playcnt=" + this.playcnt + ", downurl="
				+ this.downurl + ", pic=" + this.pic + ", artist=" + this.artist + ", hasseq=" + this.hasseq + "]";
	}
}
