package children.lemoon.reqbased.entry;

//ok
import com.google.gson.Gson;

public class ResponsePager {
	private int count;
	private int pagecount;
	private int pageindex;

	public static ResponsePager parse(String paramString) throws Exception {
		Gson localGson = new Gson();
		try {
			ResponsePager localResponsePager = (ResponsePager) localGson.fromJson(paramString, ResponsePager.class);
			return localResponsePager;
		} catch (Exception localException) {
			throw new Exception();
		}
	}
	//总集数
	public int getCount() {
		return this.count;
	}

	public int getPageCount() {
		return this.pagecount;
	}

	public int getPageIndex() {
		return this.pageindex;
	}

	public void setCount(int paramInt) {
		this.count = paramInt;
	}

	public void setPageCount(int paramInt) {
		this.pagecount = paramInt;
	}

	public void setPageIndex(int paramInt) {
		this.pageindex = paramInt;
	}
}
