package com.thepegeekapps.easypd.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "easy_pd";
	public static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_RECORDS = "records";
	
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_LOCATION = "location";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_START_DATE = "start_date";
	public static final String FIELD_HOURS = "hours";
	public static final String FIELD_MINUTES = "minutes";
	public static final String FIELD_EXPLANATION = "explanation";
	public static final String FIELD_IMAGE_URL = "image_url";
	
	public static final String CREATE_TABLE_RECORDS =
			"create table if not exists " + TABLE_RECORDS + " (" +
			FIELD_ID + " integer primary key autoincrement, " +
			FIELD_NAME + " text, " +
			FIELD_LOCATION + " text, " +
			FIELD_TYPE + " integer, " +
			FIELD_START_DATE + " numeric, " +
			FIELD_HOURS + " integer, " +
			FIELD_MINUTES + " integer, " +
			FIELD_EXPLANATION + " text, " + 
			FIELD_IMAGE_URL + " text);";
	
	public static final String DROP_TABLE_RECORDS =
			"drop table if exists " + TABLE_RECORDS;
	
	
	
	protected Context context;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
		onCreate(db);
	}
	
	protected void createTables(SQLiteDatabase db) {
		if (db != null) {
			db.execSQL(CREATE_TABLE_RECORDS);
		}
	}
	
	protected void dropTables(SQLiteDatabase db) {
		if (db != null) {
			db.execSQL(DROP_TABLE_RECORDS);
		}
	}

}
