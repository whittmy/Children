package children.lemoon.myrespone;

//ok
import java.net.URLEncoder;

import children.lemoon.reqbased.entry.BaseEntity;

public class RecommendEntity extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8292061455810672268L;
	private String images;
	private int id;
	private String title;

	private String chinesename;
	private String classname;
	private String createtime;
	private String datatype;
	private long downloadId = -1L;
	private float downloadProgress;
	private Object downloadTag;
	private int downloadnum;
	private String file;
	private long fileSize;

	private boolean isStartDownload = false;
	private String localApk;
	private String movie_definition;
	private String movieplace;
	private String packagename;
	private String publishdate;
	private String recommend;
	private String size;

	private int type;
	private String version;
	private String web_url;

	// ver3
	private String crossimages;
	private String pptvmovieid;
	private String url;

	private int checkcnt = 0;

	public void setCheckCnt(int cnt) {
		checkcnt = cnt;
	}

	public int getCheckCnt() {
		return checkcnt;
	}

	private String ext;

	public void setExt(String e) {
		ext = e;
	}

	public String getExt() {
		return ext;
	}

	public String getCrossimages() {
		return crossimages;
	}

	public String getPptvmovieid() {
		return pptvmovieid;
	}

	public String getUrl() {
		return url;
	}

	public void setCrossimages(String crossimages) {
		this.crossimages = crossimages;
	}

	public void setPptvmovieid(String pptvmovieid) {
		this.pptvmovieid = pptvmovieid;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	// ////////////////

	public String getChinesename() {
		if (chinesename != null && chinesename.contains("播播")) {
			chinesename = chinesename.replace("播播", "部落");
		}
		return this.chinesename;
	}

	public String getClassname() {
		if (classname != null && classname.contains("播播")) {
			classname = classname.replace("播播", "部落");
		}
		return this.classname;
	}

	public String getCreatetime() {
		return this.createtime;
	}

	public String getDatatype() {
		return this.datatype;
	}

	public long getDownloadId() {
		return this.downloadId;
	}

	public float getDownloadProgress() {
		return this.downloadProgress;
	}

	public Object getDownloadTag() {
		return this.downloadTag;
	}

	public int getDownloadnum() {
		return this.downloadnum;
	}

	public String getFile() {
		return this.file;
	}

	public long getFileSize() {
		return this.fileSize;
	}

	public int getId() {
		return this.id;
	}

	public String getImages() {
		return images;
	}

	public String getLocalApk() {
		return this.localApk;
	}

	public String getMovie_definition() {
		return this.movie_definition;
	}

	public String getMovieplace() {
		return this.movieplace;
	}

	public String getPackagename() {
		return this.packagename;
	}

	public String getPublishdate() {
		return this.publishdate;
	}

	public String getRecommend() {
		if (recommend != null && recommend.contains("播播"))
			recommend = recommend.replace("播播", "部落");
		return this.recommend;
	}

	public String getSize() {
		return this.size;
	}

	public String getTitle() {
		if (title != null && title.contains("播播"))
			title = title.replace("播播", "部落");
		return this.title;
	}

	public int getType() {
		return this.type;
	}

	public String getVersion() {
		return this.version;
	}

	public String getWeb_url() {
		return this.web_url;
	}

	public boolean isStartDownload() {
		return this.isStartDownload;
	}

	public void setChinesename(String paramString) {
		this.chinesename = paramString;
	}

	public void setClassname(String paramString) {
		this.classname = paramString;
	}

	public void setCreatetime(String paramString) {
		this.createtime = paramString;
	}

	public void setDatatype(String paramString) {
		this.datatype = paramString;
	}

	public void setDownloadId(long paramLong) {
		this.downloadId = paramLong;
	}

	public void setDownloadProgress(float paramFloat) {
		this.downloadProgress = paramFloat;
	}

	public void setDownloadTag(Object paramObject) {
		this.downloadTag = paramObject;
	}

	public void setDownloadnum(int paramInt) {
		this.downloadnum = paramInt;
	}

	public void setFile(String paramString) {
		this.file = paramString;
	}

	public void setFileSize(long paramLong) {
		this.fileSize = paramLong;
	}

	public void setId(int paramInt) {
		this.id = paramInt;
	}

	public void setImages(String paramString) {
		this.images = paramString;
	}

	public void setLocalApk(String paramString) {
		this.localApk = paramString;
	}

	public void setMovie_definition(String paramString) {
		this.movie_definition = paramString;
	}

	public void setMovieplace(String paramString) {
		this.movieplace = paramString;
	}

	public void setPackagename(String paramString) {
		this.packagename = paramString;
	}

	public void setPublishdate(String paramString) {
		this.publishdate = paramString;
	}

	public void setRecommend(String paramString) {
		this.recommend = paramString;
	}

	public void setSize(String paramString) {
		this.size = paramString;
	}

	public void setStartDownload(boolean paramBoolean) {
		this.isStartDownload = paramBoolean;
	}

	public void setTitle(String paramString) {
		this.title = paramString;
	}

	public void setType(int paramInt) {
		this.type = paramInt;
	}

	public void setVersion(String paramString) {
		this.version = paramString;
	}

	public void setWeb_url(String paramString) {
		this.web_url = paramString;
	}

	public String toString() {
		return "RecommendEntity [id=" + this.id + ", title=" + this.title + ", chinesename=" + this.chinesename + ", datatype=" + this.datatype
				+ ", recommend=" + this.recommend + ", type=" + this.type + ", classname=" + this.classname + ", downloadProgress=" + this.downloadProgress
				+ ", localApk=" + this.localApk + "]";
	}
}
