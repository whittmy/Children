package children.lemoon.myrespone;

import java.util.List;

import children.lemoon.reqbased.entry.BaseEntity;

public class ResponeChildClassList extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6595738111097808694L;

	private List<RecommendEntity> topcateList;

	public List<RecommendEntity> getChildTopCateList() {
		return this.topcateList;
	}

	public void setChildTopCateList(List<RecommendEntity> paramList) {
		this.topcateList = paramList;
	}
}
