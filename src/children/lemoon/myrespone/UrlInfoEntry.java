package children.lemoon.myrespone;

import children.lemoon.reqbased.entry.BaseEntity;

public class UrlInfoEntry  extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5223840817568713466L;
	private String s;
	private String u;
	
	public String getSrc(){
		return s;
	}
	
	public void setSrc(String k){
		s = k;
	}
	
	public String getUrl(){
		return u;
	}
	public void setUrl(String v){
		u = v;
	}
}
