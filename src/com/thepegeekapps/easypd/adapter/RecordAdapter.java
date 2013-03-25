package com.thepegeekapps.easypd.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thepegeekapps.easypd.R;
import com.thepegeekapps.easypd.model.Record;
import com.thepegeekapps.easypd.model.Sorting;
import com.thepegeekapps.easypd.model.Viewing;
import com.thepegeekapps.easypd.utils.Utils;
import com.thepegeekapps.easypd.view.VerticalTextView;

public class RecordAdapter extends BaseAdapter {
	
	protected Context context;
	protected List<Record> records;
	protected List<Record> shownRecords;
	
	protected Viewing viewing;
	protected Sorting sorting;
	
	public RecordAdapter(Context context, List<Record> records) {
		this.context = context;
		setRecords(records);
	}

	@Override
	public int getCount() {
		return shownRecords != null ? shownRecords.size() : 0;
	}

	@Override
	public Record getItem(int position) {
		return shownRecords != null ? shownRecords.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.record_list_item, null);
			holder = new ViewHolder();
			holder.type = (VerticalTextView) convertView.findViewById(R.id.type);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.duration = (TextView) convertView.findViewById(R.id.duration);
			holder.location = (TextView) convertView.findViewById(R.id.location);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag(); 
		}
		
		Record record = shownRecords.get(position);
		
		String typeText = record.getType() == Record.TYPE_INTERNAL ? context.getString(R.string.internal) : context.getString(R.string.external);
		int typeColor = record.getType() == Record.TYPE_INTERNAL ? Color.parseColor("#0000ff") : Color.parseColor("#ff0000");
		holder.type.setText(typeText);
		holder.type.setTextColor(typeColor);
		
		holder.name.setText(record.getName());
		
		String dateText = Utils.formatDatetime(record.getStartDate());
		int commaPosition = dateText.indexOf(',');
		dateText = (commaPosition == -1) ? dateText : dateText.substring(0, commaPosition) + '\n' + dateText.substring(commaPosition+1);
		holder.date.setText(dateText);
		
		String durationText = "";
		if (record.getHours() != 0) {
			durationText += Utils.quantityText(record.getHours(), context.getResources().getStringArray(R.array.hours_array));
		}
		if (record.getHours() != 0 && record.getMinutes() != 0) {
			durationText += '\n';
		}
		if (record.getMinutes() != 0) {
			durationText += Utils.quantityText(record.getMinutes(), context.getResources().getStringArray(R.array.mins_array));
		}
		holder.duration.setText(durationText);
		
		holder.location.setText(record.getLocation());
		
		return convertView;
	}
	
	class ViewHolder {
		VerticalTextView type;
		TextView name;
		TextView date;
		TextView duration;
		TextView location;
	}
	
	public List<Record> getRecords() {
		return records;
	}
	
	public void setRecords(List<Record> records) {
		this.records = (records != null) ? records : new ArrayList<Record>();
		initShownRecords();
		notifyDataSetChanged();
	}
	
	public Viewing getViewing() {
		return viewing;
	}
	
	public void setViewing(Viewing viewing) {
		this.viewing = viewing;
		initShownRecords();
		notifyDataSetChanged();
	}
	
	public Sorting getSorting() {
		return sorting;
	}
	
	public void setSorting(Sorting sorting) {
		this.sorting = sorting;
		initShownRecords();
		notifyDataSetChanged();
	}
	
	public void initShownRecords() {
		if (records == null || records.isEmpty()) {
			shownRecords = new ArrayList<Record>();
			return;
		}
		if (shownRecords == null) {
			shownRecords = new ArrayList<Record>();
		} else {
			if (!shownRecords.isEmpty())
				shownRecords.clear();
		}
		if (viewing == null || viewing.getViewing() == Viewing.VIEW_ALL) {
			shownRecords.addAll(records);
		} else {
			for (Record record : records) {
				switch (viewing.getViewing()) {
				case Viewing.VIEW_INTERNAL:
					if (record.getType() == Viewing.VIEW_INTERNAL)
						shownRecords.add(record);
					break;
				case Viewing.VIEW_EXTERNAL:
					if (record.getType() == Viewing.VIEW_EXTERNAL)
						shownRecords.add(record);
					break;
				case Viewing.VIEW_BETWEEN_DATES:
					Date date = new Date(record.getStartDate());
					if (date.after(viewing.getFrom()) && date.before(viewing.getTo()))
						shownRecords.add(record);
					break;
				}
			}
		}
		sortRecords();
	}
	
	protected void sortRecords() {
		if (shownRecords != null && !shownRecords.isEmpty() && sorting != null) {
			Collections.sort(shownRecords, new Comparator<Record>() {
				@Override
				public int compare(Record lhs, Record rhs) {
					int result = 0;
					switch (sorting.getSortMode()) {
					case Sorting.SORT_ACTIVITY:
						result = lhs.getName().compareTo(rhs.getName());
						break;
					case Sorting.SORT_DATE:
						result = lhs.getStartDate() < rhs.getStartDate() ? -1 :
							lhs.getStartDate() > rhs.getStartDate() ? 1 : 0;
						break;
					case Sorting.SORT_DURATION:
						result = lhs.getDurationInMillis() < rhs.getDurationInMillis() ? -1 :
							lhs.getDurationInMillis() > rhs.getDurationInMillis() ? 1 : 0;
						break;
					case Sorting.SORT_LOCATION:
						if (!TextUtils.isEmpty(lhs.getLocation()) && !TextUtils.isEmpty(rhs.getLocation())) {
							result = lhs.getLocation().compareTo(rhs.getLocation());
						} else if (!TextUtils.isEmpty(lhs.getLocation())) {
							result = 1;
						} else if (!TextUtils.isEmpty(rhs.getLocation())) {
							result = -1;
						} else {
							result = 0;
						}
						break;
					case Sorting.SORT_TYPE:
						result = (lhs.getType() == Record.TYPE_INTERNAL && rhs.getType() == Record.TYPE_EXTERNAL) ? -1 :
							(lhs.getType() == Record.TYPE_EXTERNAL && rhs.getType() == Record.TYPE_INTERNAL) ? 1 : 0;
						break;
					}
					return getSortOrderResult(result);
				}
			});
		}
	}
	
	protected int getSortOrderResult(int result) {
		if (result == 0 || sorting.getOrderMode() == Sorting.ORDER_ASC) {
			return result;
		} else {
			return result > 0 ? -1 : 1;
		}
	}

}
