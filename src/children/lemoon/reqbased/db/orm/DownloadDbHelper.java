package children.lemoon.reqbased.db.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import children.lemoon.reqbased.db.DownloadItem;
import children.lemoon.utils.Logger;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.io.PrintStream;
import java.sql.SQLException;



public class DownloadDbHelper extends OrmLiteSqliteOpenHelper {
	private static final String DATABASE_NAME = "download2.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "DownloadDbHelper";
	private Dao<DownloadItem, Integer> downloadDao = null;

	public DownloadDbHelper(Context paramContext) {
		super(paramContext, "download2.db", null, 1);
	}

	public Dao<DownloadItem, Integer> getDownloadDao() throws SQLException {
		if (this.downloadDao == null) {
			this.downloadDao = getDao(DownloadItem.class);
		}
		return this.downloadDao;
	}

	public void onCreate(SQLiteDatabase paramSQLiteDatabase, ConnectionSource paramConnectionSource) {
		Logger.LOGD("db onCreate");
		try {
			TableUtils.createTable(paramConnectionSource, DownloadItem.class);
			Logger.LOGD("DownloadDbHelper", "下载标识数据库已创建成功");
			return;
		} catch (SQLException localSQLException) {
			Logger.LOGD("DownloadDbHelper", "下载标识数据库创建失败");
			localSQLException.printStackTrace();
		}
	}

	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, ConnectionSource paramConnectionSource, int paramInt1, int paramInt2) {
		try {
			TableUtils.dropTable(paramConnectionSource, DownloadItem.class, true);
			onCreate(paramSQLiteDatabase, paramConnectionSource);
			Logger.LOGD("DownloadDbHelper", "下载标识数据库 v" + paramInt1 + "--->v" + paramInt2 + "已升级完成");
			return;
		} catch (SQLException localSQLException) {
			localSQLException.printStackTrace();
			Logger.LOGD("DownloadDbHelper", "下载标识数据库 v" + paramInt1 + "--->v" + paramInt2 + "升级失败");
		}
	}
}

/*
 * Location:
 * C:\Users\Administrator\Desktop\2.开发相关\3D波波\V2.1.10.10\classes-dex2jar.jar
 * 
 * Qualified Name: com.bobo.splayer.db.orm.DownloadDbHelper
 * 
 * JD-Core Version: 0.7.0.1
 */