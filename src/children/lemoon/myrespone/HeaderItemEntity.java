package children.lemoon.myrespone;

//ok
import java.net.URLEncoder;
import java.util.List;

import children.lemoon.reqbased.entry.BaseEntity;

public class HeaderItemEntity extends BaseEntity {
 
	private static final long serialVersionUID = 5191812891641741379L;
	
	private String title;
	private int id;
	private List<PlayItemEntity> childs;
	
	
	public void setTitle(String t){
		title = t;
	}
	
	public String getTitle(){
		return title;
	}
	
	
	public int getId(){
		return id;
	}
	public void setId(int i){
		id = i;
	}
	
	
	
	public List<PlayItemEntity> getChildList() {
		return this.childs;
	}

	public void setChildList(List<PlayItemEntity> paramList) {
		this.childs = paramList;
	}
	
}
