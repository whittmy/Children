package children.lemoon.reqbased.db.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import children.lemoon.reqbased.db.DownloadStatusBean;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.io.PrintStream;
import java.sql.SQLException;

import logger.lemoon.Logger;

public class DownloadStatusHelper extends OrmLiteSqliteOpenHelper {
	private static final String DATABASE_NAME = "downloadStatus.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "DownloadDbHelper";
	private Dao<DownloadStatusBean, Integer> downloadDao = null;

	public DownloadStatusHelper(Context paramContext) {
		super(paramContext, "downloadStatus.db", null, 1);
	}

	public Dao<DownloadStatusBean, Integer> getDownloadStatusDao() throws SQLException {
		if (this.downloadDao == null) {
			this.downloadDao = getDao(DownloadStatusBean.class);
		}
		return this.downloadDao;
	}

	public void onCreate(SQLiteDatabase paramSQLiteDatabase, ConnectionSource paramConnectionSource) {
		Logger.LOGD("db onCreate");
		try {
			TableUtils.createTable(paramConnectionSource, DownloadStatusBean.class);
			Logger.LOGD("DownloadDbHelper", "下载标识状态数据库已创建成功");
			return;
		} catch (SQLException localSQLException) {
			Logger.LOGD("DownloadDbHelper", "下载标识状态数据库创建失败");
			localSQLException.printStackTrace();
		}
	}

	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, ConnectionSource paramConnectionSource, int paramInt1, int paramInt2) {
		try {
			TableUtils.dropTable(paramConnectionSource, DownloadStatusBean.class, true);
			onCreate(paramSQLiteDatabase, paramConnectionSource);
			Logger.LOGD("DownloadDbHelper", "下载标识状态数据库 v" + paramInt1 + "--->v" + paramInt2 + "已升级完成");
			return;
		} catch (SQLException localSQLException) {
			localSQLException.printStackTrace();
			Logger.LOGD("DownloadDbHelper", "下载标识状态数据库 v" + paramInt1 + "--->v" + paramInt2 + "升级失败");
		}
	}
}

/*
 * Location:
 * C:\Users\Administrator\Desktop\2.开发相关\3D波波\V2.1.10.10\classes-dex2jar.jar
 * 
 * Qualified Name: com.bobo.splayer.db.orm.DownloadStatusHelper
 * 
 * JD-Core Version: 0.7.0.1
 */