package children.lemoon.reqbased.db.orm;

//ok
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import java.io.File;
import java.io.IOException;

import children.lemoon.reqbased.utils.AbStrUtil;

public abstract class AbSDSQLiteOpenHelper extends SQLiteOpenHelper {
	private static final String TAG = "SDSQLiteOpenHelper";
	private final Context mContext;
	private SQLiteDatabase mDatabase = null;
	private final SQLiteDatabase.CursorFactory mFactory;
	private boolean mIsInitializing = false;
	private final String mName;
	private final int mNewVersion;
	private final String mPath;

	public AbSDSQLiteOpenHelper(Context paramContext, String paramString1, String paramString2, SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt) {
		super(paramContext, paramString2, paramCursorFactory, paramInt);
		if (paramInt < 1) {
			throw new IllegalArgumentException("Version must be >= 1, was " + paramInt);
		}
		mContext = paramContext;
		mPath = paramString1;
		mName = paramString2;
		mFactory = paramCursorFactory;
		mNewVersion = paramInt;
	}

	public synchronized void close() {

		if (mIsInitializing) {
			throw new IllegalStateException("Closed during initialization");
		}

		if ((mDatabase != null) && (mDatabase.isOpen())) {
			mDatabase.close();
			mDatabase = null;
		}
	}

	public File getDatabasePath(String paramString1, String paramString2) {
		String str = AbStrUtil.parseEmpty(paramString1);
		File localFile1 = new File(Environment.getExternalStorageDirectory() + "/" + str);
		File localFile2 = new File(localFile1.getPath(), paramString2);
		if (!localFile1.exists()) {
			localFile1.mkdirs();
		}
		if (!localFile2.exists()) {
			try {
				localFile2.createNewFile();
				return localFile2;
			} catch (IOException localIOException) {
				localIOException.printStackTrace();
			}

		}

		return localFile2;
	}

	public synchronized SQLiteDatabase getReadableDatabase() {

		if ((mDatabase != null) && (mDatabase.isOpen())) {
			// goto0/cond0
			return mDatabase; // return v3
		}

		// cond1
		if (mIsInitializing) {
			throw new IllegalStateException("数据库已被占用getReadableDatabase()");
		}

		// cond2
		SQLiteDatabase localSQLiteDatabase1 = null;
		try {
			localSQLiteDatabase1 = getWritableDatabase();
			mDatabase = localSQLiteDatabase1;

			// cond3/goto1
			// go goto0
			return mDatabase;
		} catch (Exception localException) {
			try {
				mIsInitializing = true;
				String str = getDatabasePath(mPath, mName).getPath();
				localSQLiteDatabase1 = SQLiteDatabase.openDatabase(str, mFactory, 1);
				if (localSQLiteDatabase1.getVersion() != mNewVersion) {
					throw new SQLiteException("不能更新只读数据库的版本 from version " + localSQLiteDatabase1.getVersion() + " to " + mNewVersion + ": " + str);
				}
				onOpen(localSQLiteDatabase1);
				mDatabase = localSQLiteDatabase1;
				return mDatabase;
			} catch (SQLiteException localSQLiteException) {
				mIsInitializing = false;
				if ((localSQLiteDatabase1 != null) && (localSQLiteDatabase1 != mDatabase)) {
					localSQLiteDatabase1.close();
				}
			} finally {
				mIsInitializing = false;
				if ((localSQLiteDatabase1 != null) && (localSQLiteDatabase1 != mDatabase)) {
					localSQLiteDatabase1.close();
				}
			}
		}
		return mDatabase;
	}

	public synchronized SQLiteDatabase getWritableDatabase() {
		if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
			// cond0/goto0
			return mDatabase;
		}

		// cond1
		if (mIsInitializing) {
			throw new IllegalStateException("数据库已被占用getWritableDatabase()");
		}

		// cond2
		boolean success = false;
		SQLiteDatabase db = null;

		try {
			mIsInitializing = true;
			if (mName != null) {
				// cond5
				String path = getDatabasePath(mPath, mName).getPath();
				db = SQLiteDatabase.openOrCreateDatabase(path, mFactory);
			} else {
				db = SQLiteDatabase.create(null);
			}

			// goto1
			int version = db.getVersion();
			if (version != mNewVersion) {
				db.beginTransaction();
				try {
					if (version != 0) {
						// cond6
						onUpgrade(db, version, mNewVersion);
					} else {
						onCreate(db);
					}

					// goto2
					db.setVersion(mNewVersion);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
			// cond3
			onOpen(db);
			success = true;
			return db;
		} finally {
			mIsInitializing = false;
			if (!success) {
				// cond9
				if (db != null) {
					db.close();
				}
			} else {
				if (mDatabase != null) {
					try {
						mDatabase.close();
					} catch (Exception e) {
					}
				}
				// cond4/goto3
				mDatabase = db;
			}
		}
	}

	public abstract void onCreate(SQLiteDatabase paramSQLiteDatabase);

	public void onOpen(SQLiteDatabase paramSQLiteDatabase) {
	}

	public abstract void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2);
}
