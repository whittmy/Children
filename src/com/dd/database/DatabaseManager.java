package com.dd.database;


import com.dd.utils.L;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;
/*********************
用法
	DatabaseManager.initializeInstance(this);
	
	DatabaseManager.getInstance().executeQueryTask(new QueryExecutor() {
	    @Override
	    public void run(SQLiteDatabase database) {
	    	CacheMgrDAO udao = new CacheMgrDAO(database, CacheMgr.this); // your class
	    	Logger.LOGD("", "insert.."+idx+", "+name);
	    	udao.insert(idx, name, false);
	    }
	});

public class CacheMgrDAO {
	SQLiteDatabase mDb;
	Context mCx;
	private static final String TB_NAME = "rmmgr";
	public static final String COLUMNS_ID = "id";
	public static final String COLUMNS_INDEX = "indexed";
	public static final String COLUMNS_NAME = "name";
	public static final String COLUMNS_CHECK = "checked";

	public CacheMgrDAO(SQLiteDatabase db, Context cx) {
		mDb = db;
		mCx = cx;
	}
	
	public Cursor selectAll() {
		Cursor cursor = mDb.query(TB_NAME, null, null, null, null, null, "id desc");
		return cursor;
	}	
	public long insert(int index, String name, boolean checked) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMNS_INDEX, index);
		cv.put(COLUMNS_NAME, name);
		cv.put(COLUMNS_CHECK, checked);
		long row = mDb.insert(TB_NAME, null, cv);
		return row;

	}
	
	.....
*/
public class DatabaseManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DatabaseManager instance;
    private SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    private DatabaseManager(SQLiteOpenHelper helper) {
        mDatabaseHelper = helper;
    }
    
    public static synchronized void initializeInstance(Context cx) {
        if (instance == null) {
            instance = new DatabaseManager(new DatabaseHelper(cx));
        }
    }
    
    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager(helper);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    private synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        L.d("Database open counter: " + mOpenCounter.get());
        return mDatabase;
    }

    private synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();

        }
        L.d("Database open counter: " + mOpenCounter.get());
    }

    public void executeQuery(QueryExecutor executor) {
        SQLiteDatabase database = openDatabase();
        executor.run(database);
        closeDatabase();
    }

    public void executeQueryTask(final QueryExecutor executor) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = openDatabase();
                executor.run(database);
                closeDatabase();
            }
        }).start();
    }
}
