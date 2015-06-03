package children.lemoon.reqbased.entry;

//ok

import java.io.Serializable;

import children.lemoon.reqbased.db.orm.annotation.Column;
import children.lemoon.reqbased.db.orm.annotation.Id;

public class BaseEntity implements Serializable {

	private static final long serialVersionUID = 2050938986801469798L;

	@Column(name = "_baseUserId")
	private String _baseUserId = "test";
	@Column(name = "_id")
	@Id
	private int _id;
	private String _key;

	public int getId() {
		return this._id;
	}

	public String get_baseUserId() {
		return this._baseUserId;
	}

	public String get_key() {
		return this._key;
	}

	public void setId(int paramInt) {
		this._id = paramInt;
	}

	public void set_baseUserId(String paramString) {
		this._baseUserId = paramString;
	}

	public void set_key(String paramString) {
		this._key = paramString;
	}
}
