package com.thepegeekapps.easypd.model;

import java.util.Date;

import com.thepegeekapps.easypd.R;

import android.content.Context;
import android.text.TextUtils;

public class Record {
	
	public static final int TYPE_INTERNAL = 0;
	public static final int TYPE_EXTERNAL = 1;
	
	public static final int TYPE_DEFAULT = TYPE_EXTERNAL;
	protected long START_DATE_DEFAULT = new Date(System.currentTimeMillis()).getTime();
	
	protected int id;
	protected String name;
	protected String location;
	protected int type;
	protected long startDate;
	protected int hours;
	protected int minutes;
	protected String explanation;
	protected String imageUrl;
	
	public Record(int id, String name, String location, int type, long startDate,
			int hours, int minutes, String explanation, String imageUrl) 
	{
		this.id = id;
		this.name = name;
		this.location = location;
		this.type = type;
		this.startDate = startDate;
		this.hours = hours;
		this.minutes = minutes;
		this.explanation = explanation;
		this.imageUrl = imageUrl;
	}
	
	public Record() {
		setType(TYPE_DEFAULT);
		setStartDate(START_DATE_DEFAULT);
		setHours(0);
		setMinutes(1);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}
	
	public int getMinutes() {
		return minutes;
	}
	
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	
	public long getDurationInMillis() {
		return (getHours() * 60 + getMinutes()) * 60000;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public boolean hasImage() {
		return !TextUtils.isEmpty(imageUrl);
	}
	
	public String getTypeName(Context context) {
		return (type == TYPE_INTERNAL) ? context.getString(R.string.internal_training) :
			context.getString(R.string.external_training);
	}

}
