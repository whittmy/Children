package children.lemoon.reqbased.db;

//ok
import java.lang.reflect.Type;

import children.lemoon.reqbased.db.orm.dao.AbDBDaoImpl;

import android.content.Context;

public class DbUtil<T> extends AbDBDaoImpl<T> {

	public DbUtil(Context paramContext, Class<T> paramClass) {
		super(new DBInsideHelper(paramContext), paramClass);
	}
}