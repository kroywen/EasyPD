package com.thepegeekapps.easypd.storage;

import com.thepegeekapps.easypd.model.Record;

public class RecordStorage {
	
	protected static RecordStorage instance;
	
	protected Record record;
	
	protected RecordStorage() {}
	
	public static RecordStorage getInstance() {
		if (instance == null) {
			instance = new RecordStorage();
		}
		return instance;
	}
	
	public Record getRecord() {
		return record;
	}
	
	public void setRecord(Record record) {
		this.record = record;
	}

}
