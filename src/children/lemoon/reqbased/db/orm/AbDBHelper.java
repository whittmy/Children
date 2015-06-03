package children.lemoon.reqbased.db.orm;

//ok
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AbDBHelper extends SQLiteOpenHelper {
	private Class<?>[] modelClasses;

	public AbDBHelper(Context paramContext, String paramString, SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt, Class<?>[] paramArrayOfClass) {
		super(paramContext, paramString, paramCursorFactory, paramInt);
		this.modelClasses = paramArrayOfClass;
	}

	public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
		AbTableHelper.createTablesByClasses(paramSQLiteDatabase, this.modelClasses);
	}

	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
	}
}
