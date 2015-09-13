package com.dd.my;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CateCourseMgrDAO {
	SQLiteDatabase mDb;
	Context mCx;
	
	private static final String TB_NAME = "cate_course";
	public static final String COLUMNS_CSID = "courseid";
	public static final String COLUMNS_DWID = "downloadid";
	
	public CateCourseMgrDAO(SQLiteDatabase db, Context cx) {
		mDb = db;
		mCx = cx;
	}	
	
	public Cursor selectAll() {
		Cursor cursor = mDb.query(TB_NAME, null, null, null, null, null, null);
		return cursor;
	}	
	
	
	public long insert(String courseid,  long downid) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMNS_CSID, courseid);
		cv.put(COLUMNS_DWID, downid);
		long row = mDb.insert(TB_NAME, null, cv);
		return row;
	}
	
	public void deleteByDownId(long downid){
		mDb.delete(TB_NAME, COLUMNS_DWID+"="+downid, null);
	}
	
	public void removeAll(){
		mDb.delete(TB_NAME, null, null);
	}
	
}
