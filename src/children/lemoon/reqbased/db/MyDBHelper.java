package children.lemoon.reqbased.db;

//ok
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
	private static final String DBNAME = "wemaxplayer.db";
	private static final int VERSION = 1;

	public MyDBHelper(Context paramContext) {
		super(paramContext, "wemaxplayer.db", null, 1);
	}

	public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
		paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS T_SearchHistory (_ID INTEGER PRIMARY KEY AUTOINCREMENT,KeyWord TEXT)");
	}

	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
		paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS T_SearchHistory");
		onCreate(paramSQLiteDatabase);
	}
}
