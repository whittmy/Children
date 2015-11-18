package children.lemoon.reqbased.entry;

public class ExtraEntry  extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3040477576236279213L;
	private String key;
	private String value;
	
	public String getKey(){
		return key;
	}
	
	public void setKey(String k){
		key = k;
	}
	
	public String getValue(){
		return value;
	}
	public void setValue(String v){
		value = v;
	}
}
