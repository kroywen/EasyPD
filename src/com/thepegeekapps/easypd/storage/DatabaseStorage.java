package com.thepegeekapps.easypd.storage;

import java.util.LinkedList;
import java.util.List;

import com.thepegeekapps.easypd.model.Record;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseStorage {
	
	protected static DatabaseStorage instance;
	protected Context context;
	protected DatabaseHelper dbHelper;
	protected SQLiteDatabase db;
	
	protected DatabaseStorage(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	public static DatabaseStorage getInstance(Context context) {
		if (instance == null)
			instance = new DatabaseStorage(context);
		return instance;
	}
	
	/************************************************************************************************************************
	 **************************************************** R E C O R D S ***************************************************** 
	 ************************************************************************************************************************/
	
	public List<Record> getRecords() {
		List<Record> records = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_RECORDS,
					null, null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				records = new LinkedList<Record>();
				do {
					Record record = new Record(
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ID)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_LOCATION)),
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_TYPE)),
						c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_START_DATE)),
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_HOURS)),
						c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_MINUTES)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_EXPLANATION)),
						c.getString(c.getColumnIndex(DatabaseHelper.FIELD_IMAGE_URL))
					);
					records.add(record);
				} while (c.moveToNext());
			}
			if (c != null && !c.isClosed())
				c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return records;
	}
	
	public Record getRecordById(int id) {
		Record record = null;
		try {
			Cursor c = db.query(DatabaseHelper.TABLE_RECORDS,
					null, DatabaseHelper.FIELD_ID+"="+id, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				record = new Record(
					id,
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NAME)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_LOCATION)),
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_TYPE)),
					c.getLong(c.getColumnIndex(DatabaseHelper.FIELD_START_DATE)),
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_HOURS)),
					c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_MINUTES)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_EXPLANATION)),
					c.getString(c.getColumnIndex(DatabaseHelper.FIELD_IMAGE_URL))
				);
			}
			if (c != null && !c.isClosed())
				c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return record;
	}
	
	public boolean addRecord(Record record) {
		if (record == null)
			return false;
		ContentValues values = prepareContentValues(record);
		long insertedId = db.insert(DatabaseHelper.TABLE_RECORDS, null, values);
		return insertedId != -1;
	}
	
	public boolean updateRecord(Record record) {
		if (record == null)
			return false;
		ContentValues values = prepareContentValues(record);
		int rowsAffected = db.update(DatabaseHelper.TABLE_RECORDS, values, DatabaseHelper.FIELD_ID+"="+record.getId(), null);
		return rowsAffected == 1;
	}
	
	protected ContentValues prepareContentValues(Record record) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FIELD_NAME, record.getName());
		values.put(DatabaseHelper.FIELD_LOCATION, record.getLocation());
		values.put(DatabaseHelper.FIELD_TYPE, record.getType());
		values.put(DatabaseHelper.FIELD_START_DATE, record.getStartDate());
		values.put(DatabaseHelper.FIELD_HOURS, record.getHours());
		values.put(DatabaseHelper.FIELD_MINUTES, record.getMinutes());
		values.put(DatabaseHelper.FIELD_EXPLANATION, record.getExplanation());
		values.put(DatabaseHelper.FIELD_IMAGE_URL, record.getImageUrl());
		return values;
	}
	
	public boolean deleteRecord(Record Record) {
		if (Record == null)
			return false;
		int rowsAffected = db.delete(DatabaseHelper.TABLE_RECORDS, DatabaseHelper.FIELD_ID+"="+Record.getId(), null);
		return rowsAffected == 1;
	}

}
