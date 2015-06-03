package children.lemoon.reqbased.db.orm;

//ok
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class AbSDDBHelper extends AbSDSQLiteOpenHelper {
	private Class<?>[] modelClasses;

	public AbSDDBHelper(Context paramContext, String paramString1, String paramString2, SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt,
			Class<?>[] paramArrayOfClass) {
		super(paramContext, paramString1, paramString2, null, paramInt);
		this.modelClasses = paramArrayOfClass;
	}

	public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
		AbTableHelper.createTablesByClasses(paramSQLiteDatabase, this.modelClasses);
	}

	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
		AbTableHelper.dropTablesByClasses(paramSQLiteDatabase, this.modelClasses);
		onCreate(paramSQLiteDatabase);
	}
}
