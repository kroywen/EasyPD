package com.thepegeekapps.easypd.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

import com.thepegeekapps.easypd.R;
import com.thepegeekapps.easypd.model.Record;

public class Utils {
	
	public static long start = 0;
	public static long end = 0;
	public static int count = 0;
	
	public static final int MILLISECONDS_IN_DAY = 86400000;
	public static final int START_YEAR = 1900;
	
	public static Date[] dates;
	public static String[] hours;
	public static String[] minutes;
	public static String[] ampmItems = {"AM", "PM"};
	public static String[] years;
	public static String[] days31;
	public static String[] days30;
	public static String[] days29;
	public static String[] days28;
	
	public static final String DATE_TIME_FORMAT = "M/d/y, h:mm a";
	public static SimpleDateFormat datetimeFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US);
	
	public static final String DATE_FORMAT = "E LLL d";
	public static SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
	
	public static final String RECORD_DATE_TIME_FORMAT = "dd LLL, yyyy, h:mm a";
	public static SimpleDateFormat recordDatetimeFormatter = new SimpleDateFormat(RECORD_DATE_TIME_FORMAT, Locale.US);
	
	public static final String RECORD_FILENAME_FORMAT = "yyyy-MM-dd hhmmss Z";
	public static SimpleDateFormat recordFilenameFormatter = new SimpleDateFormat(RECORD_FILENAME_FORMAT, Locale.US);
	
	public static String formatRecordFilename(long timestamp) {
		Date date = new Date(timestamp);
		return recordFilenameFormatter.format(date);
	}
	
	public static String formatDatetime(long timestamp) {
		Date date = new Date(timestamp);
		return datetimeFormatter.format(date);
	}
	
	public static String formatDate(long timestamp) {
		Date date = new Date(timestamp);
		return dateFormatter.format(date);
	}
	
	public static String formatDate(Date date) {
		return dateFormatter.format(date);
	}
	
	public static String formatRecordDatetime(long timestamp) {
		Date date = new Date(timestamp);
		return recordDatetimeFormatter.format(date);
	}
	
	public static Date getDate(String datetime) {
		Date date = null;
		try {
			date = datetimeFormatter.parse(datetime);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static String capitalize(String line) {
	  return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
	public static String quantityText(int quantity, String [] array) {
        return quantity + " " + quantityWord(quantity, array);
    }
	
	public static String quantityWord(int quantity, String [] array) {
        if (quantity > 10 && quantity < 20) {
            return array[2];
        }
        switch (quantity % 10) {
            case 0:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return array[2];
            case 1:
                return array[0];
            case 2:
            case 3:
            case 4:
                return array[1];
            default:
                return null;
        }
    }
	
	public static String[] getDateItems(long currentTimeMillis) {
		Calendar cal = Calendar.getInstance(Locale.US);
		if (dates == null) {
			cal.setTimeInMillis(currentTimeMillis);
			int year = cal.get(Calendar.YEAR);
			
			cal.set(year, 0, 1, 0, 0, 0);
			start = cal.getTimeInMillis();
			
			cal.set(year, 11, 31, 23, 59, 59);
			end = cal.getTimeInMillis();
			
			count = (int) Math.floor((end - start) / MILLISECONDS_IN_DAY);
			
			dates = new Date[count];
			cal.setTimeInMillis(start);
			for (int i = 0; i < count; i++) {
				dates[i] = new Date(cal.getTimeInMillis());
				cal.add(Calendar.MILLISECOND, MILLISECONDS_IN_DAY);
			}
		}
		cal.setTimeInMillis(start);
		String[] datesStr = new String[count];
		for (int i = 0; i < count; i++) {
			datesStr[i] = formatDate(dates[i]);
			cal.add(Calendar.MILLISECOND, MILLISECONDS_IN_DAY);
		}
		
		return datesStr;
	}
	
	@SuppressWarnings("deprecation")
	public static int getDatePosition(long timestamp) {
		if (dates != null) {
			Date curDate = new Date(timestamp);
			for (int i=0; i<dates.length; i++) {
				if (dates[i].getYear() == curDate.getYear() && 
					dates[i].getMonth() == curDate.getMonth() &&
					dates[i].getDay() == curDate.getDay()) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static Date getDate(int position) {
		if (dates != null && dates.length > 0 && 
			position >= 0 && position < dates.length) {
			return dates[position];
		} else {
			return null;
		}
	}
	
	public static String[] getHoursItems() {
		if (hours == null) {
			hours = new String[24];
			for (int i=0; i<24; i++) {
				hours[i] = i > 9 ? String.valueOf(i) : "0"+i;
			}
		}
		return hours;
	}
	
	public static String[] getMinutesItems() {
		if (minutes == null) {
			minutes = new String[60];
			for (int i=0; i<60; i++) {
				minutes[i] = i > 9 ? String.valueOf(i) : "0"+i;
			}
		}
		return minutes;
	}
	
	public static int getHours(long currentTimeMillis) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTimeInMillis(currentTimeMillis);
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getMinutes(long currentTimeMillis) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTimeInMillis(currentTimeMillis);
		return cal.get(Calendar.MINUTE);
	}
	
	public static int getAmpm(long currentTimeMillis) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTimeInMillis(currentTimeMillis);
		return cal.get(Calendar.AM_PM);
	}
	
	public static String[] getItems(int from, int to) {
		String[] items = new String[to - from + 1];
		for (int i=0; i<items.length; i++) {
			items[i] = String.valueOf(from + i);
		}
		return items;
	}
	
	public static final String[] getDaysInMonth(int month, int year) {
		if (month == 0 || month == 2 || month == 4 || month == 6 || month == 7 || month == 9 || month == 11) {
			if (days31 == null) {
				days31 = getItems(1, 31);
			}
			return days31;
		} else if (month == 3 || month == 5 || month == 8 || month == 10) {
			if (days30 == null) {
				days30 = getItems(1, 30);
			}
			return days30;
		} else {
			if (isLeapYear(year)) {
				if (days29 == null) {
					days29 = getItems(1, 29);
				}
				return days29;
			} else {
				if (days28 == null) {
					days28 = getItems(1, 28);
				}
				return days28;
			}
		}
	}
	
	public static String[] getYears(int currentYear) {
		if (years == null) {
			years = new String[currentYear*2];
			int startYear = START_YEAR;
			for (int i=0; i<years.length; i++) {
				years[i] = String.valueOf(startYear);
				startYear++;
			}
		}
		return years;
	}
	
	public static boolean isLeapYear(int year) {
		year += START_YEAR;
		return ((year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0));
	}
	
	public static final String getReadableFilesize(long bytes) {
		String s = " B";
		String size = bytes + s;
		
		while (bytes > 1024) {
			if (s.equals(" B"))	s = " KB";
			else if (s.equals(" KB")) s = " MB";
			else if (s.equals(" MB")) s = " GB";
			else if (s.equals(" GB")) s = " TB";
			
			size = (bytes / 1024) + "." + (bytes % 1024) + s;
			bytes = bytes / 1024;
		}
		
		return size;
	}
	
	public static String getDurationText(Context context, Record record) {
		String durationText = "";
		if (record.getHours() != 0) {
			durationText += Utils.quantityText(record.getHours(), context.getResources().getStringArray(R.array.hours_array));
		}
		if (record.getHours() != 0 && record.getMinutes() != 0) {
			durationText += ' ';
		}
		if (record.getMinutes() != 0) {
			durationText += quantityText(record.getMinutes(), context.getResources().getStringArray(R.array.mins_array));
		}
		return durationText;
	}

}
