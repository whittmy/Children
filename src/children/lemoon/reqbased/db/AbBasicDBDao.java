package children.lemoon.reqbased.db;

//ok
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AbBasicDBDao {
	public void closeCursor(Cursor paramCursor) {
		if (paramCursor != null) {
			paramCursor.close();
		}
	}

	public void closeDB(Cursor paramCursor, SQLiteDatabase paramSQLiteDatabase) {
		if (paramCursor != null) {
			paramCursor.close();
		}
		if ((paramSQLiteDatabase != null) && (paramSQLiteDatabase.isOpen())) {
			paramSQLiteDatabase.close();
		}
	}

	public int getIntColumnValue(String paramString, Cursor paramCursor) {
		return paramCursor.getInt(paramCursor.getColumnIndex(paramString));
	}

	public String getStringColumnValue(String paramString, Cursor paramCursor) {
		return paramCursor.getString(paramCursor.getColumnIndex(paramString));
	}
}
