package com.dd.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	final static int VER = 2;
	
	private static final String DB_NAME = "cmgr";
	private static final String TB_MV_NAME = "mv_mgr";
	private static final String TB_MU_NAME = "mu_mgr";
	private static final String COLUMNS_ID = "id";
	private static final String COLUMNS_MID = "mvid";
	private static final String COLUMNS_NAME = "name";
	private static final String COLUMNS_FSIZE = "size";
	private static final String COLUMNS_CNT = "cnt";	
	private static final String COLUMNS_PIC = "pic";		
	private static final String COLUMNS_URL = "url";		
	public static final String COLUMNS_CID = "cateid";	
	
	
	private static final String TB_CATE_COURSE_NAME = "cate_course";
	private static final String COLUMNS_CSID = "courseid";
	private static final String COLUMNS_DWID = "downloadid";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, VER);
	}

	//oncreate仅在db被create时才被回调
	//这里是手动调用它的，通过版本提升的方式
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Attention:注意SQL语法，每个变量后需要有空格，否则不认识。
		String sql = "CREATE TABLE IF NOT EXISTS " + TB_MV_NAME + " ("
				+ COLUMNS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMNS_MID + " INTEGER,"
				+ COLUMNS_NAME + " TEXT,"
				+ COLUMNS_FSIZE + " INTEGER," 
				+ COLUMNS_CNT + " INTEGER," 
				+ COLUMNS_PIC + " TEXT," 
				+ COLUMNS_URL + " TEXT,"
				+ COLUMNS_CID + " INTEGER)";
		
		String sql2 = "CREATE TABLE IF NOT EXISTS " + TB_MU_NAME + " ("
				+ COLUMNS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMNS_MID + " INTEGER,"
				+ COLUMNS_NAME + " TEXT,"
				+ COLUMNS_FSIZE + " INTEGER," 
				+ COLUMNS_CNT + " INTEGER," 
				+ COLUMNS_PIC + " TEXT," 
				+ COLUMNS_URL + " TEXT,"
				+ COLUMNS_CID + " INTEGER)";
		
		
		String sql3 = "CREATE TABLE IF NOT EXISTS " + TB_CATE_COURSE_NAME + " ("
				+ COLUMNS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMNS_CSID + " TEXT,"
				+ COLUMNS_DWID + " INTEGER)";
		
		db.execSQL(sql);
		db.execSQL(sql2);
		db.execSQL(sql3);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TB_MV_NAME);
		onCreate(db);
	}

}