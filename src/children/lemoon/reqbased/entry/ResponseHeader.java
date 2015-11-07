package children.lemoon.reqbased.entry;

//ok

import java.util.List;

import com.google.gson.Gson;

public class ResponseHeader {
	private String accessToken;
	private String appId;
	private String appVersion;
	private String devId;
	private String devType;
	private String funcId;
	private InterceptTime interceptTime;
	private String osVersion;
	private String retMessage;
	private int retStatus;
	private String userId;
	private String userType;
	private List<ExtraEntry> extra;
	
	int flag;

	
	public List<ExtraEntry> getExtra(){
		return extra;
	}
	public void setExtra(List<ExtraEntry> a){
		extra = a;
	}
	
	
	public void setFlag(int paramInt) {
		this.flag = paramInt;
	}

	public int getFlag() {
		return this.flag;
	}

	public static ResponseHeader parse(String paramString) throws Exception {
		Gson localGson = new Gson();
		try {
			ResponseHeader localResponseHeader = (ResponseHeader) localGson.fromJson(paramString, ResponseHeader.class);
			return localResponseHeader;
		} catch (Exception localException) {
			throw new Exception();
		}
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public String getAppId() {
		return this.appId;
	}

	public String getAppVersion() {
		return this.appVersion;
	}

	public String getDevId() {
		return this.devId;
	}

	public String getDevType() {
		return this.devType;
	}

	public String getFuncId() {
		return this.funcId;
	}

	public InterceptTime getInterceptTime() {
		return this.interceptTime;
	}

	public String getOsVersion() {
		return this.osVersion;
	}

	public String getRetMessage() {
		return this.retMessage;
	}

	public int getRetStatus() {
		return this.retStatus;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getUserType() {
		return this.userType;
	}

	public void setAccessToken(String paramString) {
		this.accessToken = paramString;
	}

	public void setAppId(String paramString) {
		this.appId = paramString;
	}

	public void setAppVersion(String paramString) {
		this.appVersion = paramString;
	}

	public void setDevId(String paramString) {
		this.devId = paramString;
	}

	public void setDevType(String paramString) {
		this.devType = paramString;
	}

	public void setFuncId(String paramString) {
		this.funcId = paramString;
	}

	public void setInterceptTime(InterceptTime paramInterceptTime) {
		this.interceptTime = paramInterceptTime;
	}

	public void setOsVersion(String paramString) {
		this.osVersion = paramString;
	}

	public void setRetMessage(String paramString) {
		this.retMessage = paramString;
	}

	public void setRetStatus(int paramInt) {
		this.retStatus = paramInt;
	}

	public void setUserId(String paramString) {
		this.userId = paramString;
	}

	public void setUserType(String paramString) {
		this.userType = paramString;
	}
}
