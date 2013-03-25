package com.thepegeekapps.easypd.model;

public class Sorting {
	
	public static final int SORT_ACTIVITY = 0;
	public static final int SORT_DATE = 1;
	public static final int SORT_LOCATION = 2;
	public static final int SORT_DURATION = 3;
	public static final int SORT_TYPE = 4;
	
	public static final int SORT_DEFAULT = SORT_ACTIVITY;
	
	public static final int ORDER_ASC = 0;
	public static final int ORDER_DESC = 1;
	
	public static final int ORDER_DEFAULT = ORDER_ASC;
	
	protected int sortMode;
	protected int orderMode;
	
	public Sorting() {
		setSortMode(SORT_DEFAULT);
		setOrderMode(ORDER_DEFAULT);
	}
	
	public Sorting(int sortMode, int orderMode) {
		this.sortMode = sortMode;
		this.orderMode = orderMode;
	}
	
	public int getSortMode() {
		return sortMode;
	}
	
	public void setSortMode(int sortMode) {
		this.sortMode = sortMode;
	}
	
	public int getOrderMode() {
		return orderMode;
	}
	
	public void setOrderMode(int orderMode) {
		this.orderMode = orderMode;
	}
	
	public void toogleOrderMode() {
		orderMode = (orderMode == ORDER_ASC) ? ORDER_DESC : ORDER_ASC; 
	}

}
