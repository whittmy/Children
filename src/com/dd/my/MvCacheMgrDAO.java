package com.dd.my;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MvCacheMgrDAO {
	SQLiteDatabase mDb;
	Context mCx;
	
	private static final String TB_NAME = "mv_mgr";	
	public static final String COLUMNS_MID = "mvid";
	public static final String COLUMNS_NAME = "name";
	public static final String COLUMNS_FSIZE = "size";
	public static final String COLUMNS_CNT = "cnt";	
	public static final String COLUMNS_PIC = "pic";		
	public static final String COLUMNS_URL = "url";	
	public static final String COLUMNS_CID = "cateid";	
	
	public MvCacheMgrDAO(SQLiteDatabase db, Context cx) {
		mDb = db;
		mCx = cx;
	}	
	
	public Cursor selectAll(int cateid) {
		Cursor cursor = mDb.query(TB_NAME, null, COLUMNS_CID+"="+cateid, null, null, null, null);
		return cursor;
	}	
	
	
	public long insert(int mid, String name, long size, int cnt, String pic, String url, int cid) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMNS_MID, mid);
		cv.put(COLUMNS_NAME, name);
		cv.put(COLUMNS_FSIZE, size);
		cv.put(COLUMNS_CNT, cnt);		
		cv.put(COLUMNS_PIC, pic);		
		cv.put(COLUMNS_URL, url);	
		cv.put(COLUMNS_CID, cid);
		long row = mDb.insert(TB_NAME, null, cv);
		return row;
	}
	
	public void removeAll(){
		mDb.delete(TB_NAME, null, null);
	}
	
}
