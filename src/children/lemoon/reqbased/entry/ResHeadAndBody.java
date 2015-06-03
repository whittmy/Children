package children.lemoon.reqbased.entry;

//ok
public class ResHeadAndBody {
	private Object body;
	private ResponseHeader header;
	private Object page;

	public Object getBody() {
		return this.body;
	}

	public ResponseHeader getHeader() {
		return this.header;
	}

	public Object getPage() {
		return this.page;
	}

	public void setBody(Object paramObject) {
		this.body = paramObject;
	}

	public void setHeader(ResponseHeader paramResponseHeader) {
		this.header = paramResponseHeader;
	}

	public void setPage(Object paramObject) {
		this.page = paramObject;
	}
}
