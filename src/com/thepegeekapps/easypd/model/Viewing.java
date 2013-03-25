package com.thepegeekapps.easypd.model;

import java.util.Date;

public class Viewing {
	
	public static final int VIEW_INTERNAL = 0;
	public static final int VIEW_EXTERNAL = 1;
	public static final int VIEW_BETWEEN_DATES = 2;
	public static final int VIEW_ALL = 3;
	
	public static final int VIEW_DEFAULT = VIEW_ALL;
	
	protected int viewing;
	protected Date from;
	protected Date to;
	
	public Viewing() {
		setViewing(VIEW_DEFAULT);
	}
	
	public Viewing(int viewing, Date from, Date to) {
		this.viewing = viewing;
		this.from = from;
		this.to = to;
	}
	
	public int getViewing() {
		return viewing;
	}
	
	public void setViewing(int viewing) {
		this.viewing = viewing;
	}
	
	public Date getFrom() {
		if (from == null)
			from = new Date();
		return from;
	}
	
	public void setFrom(Date from) {
		this.from = from;
	}
	
	public Date getTo() {
		if (to == null)
			to = new Date();
		return to;
	}
	
	public void setTo(Date to) {
		this.to = to;
	}

}
