package children.lemoon.myrespone;

import java.util.List;

import children.lemoon.reqbased.entry.BaseEntity;

public class ResponePList extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6595738111097808694L;

	private List<PlayItemEntity> pList;

	public List<PlayItemEntity> getpList() {
		return this.pList;
	}

	public void setpList(List<PlayItemEntity> paramList) {
		this.pList = paramList;
	}
}
