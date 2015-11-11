package children.lemoon.myrespone;

import java.util.List;

import children.lemoon.reqbased.entry.BaseEntity;

public class ResponeResList extends BaseEntity {
 
	private static final long serialVersionUID = -8493796516134096810L;
	
	private List<PlayItemEntity> resList;
	private List<HeaderItemEntity> headerList;
	private String title;
	
	public void setTitle(String s){
		title = s;
	}
	
	public String getTitle(){
		return title;
	}
	
	
	public List<PlayItemEntity> getResList() {
		return this.resList;
	}

	public void setResList(List<PlayItemEntity> paramList) {
		this.resList = paramList;
	}
	
	
	public List<HeaderItemEntity> getHeaderList() {
		return this.headerList;
	}

	public void setHeaderList(List<HeaderItemEntity> paramList) {
		this.headerList = paramList;
	}
}
