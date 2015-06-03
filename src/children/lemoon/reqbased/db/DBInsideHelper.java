package children.lemoon.reqbased.db;

//ok
import children.lemoon.reqbased.db.orm.AbDBHelper;
import android.content.Context;

public class DBInsideHelper extends AbDBHelper {
	private static final String DBNAME = "wemaxplayer.db";
	private static final int DBVERSION = 1;
	private static final Class<?>[] clazz = new Class[0];
	public static final String password = "";

	public DBInsideHelper(Context paramContext) {
		super(paramContext, "wemaxplayer.db", null, 1, clazz);
	}
}
