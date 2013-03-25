package com.thepegeekapps.easypd.screen;

import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.thepegeekapps.easypd.R;
import com.thepegeekapps.easypd.adapter.RecordAdapter;
import com.thepegeekapps.easypd.model.Record;
import com.thepegeekapps.easypd.model.Sorting;
import com.thepegeekapps.easypd.model.Viewing;
import com.thepegeekapps.easypd.utils.Utils;
import com.thepegeekapps.easypd.view.wheel.OnWheelChangedListener;
import com.thepegeekapps.easypd.view.wheel.WheelView;
import com.thepegeekapps.easypd.view.wheel.adapters.ArrayWheelAdapter;

public class RecordsScreen extends BaseScreen implements OnClickListener {
	
	public static final int MENU_EDIT = 0;
	public static final int MENU_DELETE = 1;
	
	protected TextView titleView;
	protected Button rightBtn1;
	protected Button rightBtn2;
	protected Button viewingBtn;
	protected ListView list;
	
	protected List<Record> records;
	protected RecordAdapter adapter;
	
	protected Sorting sorting;
	protected String[] sortingItems;
	protected String[] sortOrderItems;
	protected Viewing viewing;
	protected String[] viewingItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.records_screen);
		
		sorting = new Sorting();
		sortingItems = getResources().getStringArray(R.array.sorting_array);
		sortOrderItems = getResources().getStringArray(R.array.sort_order_array);
		viewing = new Viewing();
		viewingItems = getResources().getStringArray(R.array.viewing_array);
		
		initializeViews();
		
		records = dbStorage.getRecords();
		adapter = new RecordAdapter(this, records);
		list.setAdapter(adapter);
		
		applyViewing();
		applySorting();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		records = dbStorage.getRecords();
		adapter.setViewing(viewing);
		adapter.setSorting(sorting);
		adapter.setRecords(records);
	}
	
	protected void initializeViews() {
		titleView = (TextView) findViewById(R.id.titleView);
		titleView.setText(getString(R.string.pd_records));
		
		rightBtn1 = (Button) findViewById(R.id.btnRight1);
		int textResId = (sorting.getOrderMode() == Sorting.ORDER_ASC) ? R.string.asc : R.string.desc; 
		rightBtn1.setText(textResId);
		rightBtn1.setVisibility(View.VISIBLE);
		rightBtn1.setOnClickListener(this);
		
		rightBtn2 = (Button) findViewById(R.id.btnRight2);
		rightBtn2.setText(getString(R.string.sort));
		rightBtn2.setVisibility(View.VISIBLE);
		rightBtn2.setOnClickListener(this);
		
		viewingBtn = (Button) findViewById(R.id.viewingBtn);
		viewingBtn.setOnClickListener(this);
		
		list = (ListView) findViewById(R.id.list);
		registerForContextMenu(list);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Record record = adapter.getItem(info.position);
		menu.setHeaderTitle(record.getName());
//		menu.add(0, MENU_EDIT, 0, R.string.edit);
		menu.add(0, MENU_DELETE, 0, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_EDIT:
			// TODO
			return true;
		case MENU_DELETE:
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			Record record = adapter.getItem(info.position);
			dbStorage.deleteRecord(record);
			records = dbStorage.getRecords();
			adapter.setRecords(records);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRight1:
			sorting.toogleOrderMode();
			applySorting();
			break;
		case R.id.btnRight2:
			showSelectSortingDialog();
			break;
		case R.id.viewingBtn:
			showSelectViewingDialog();
			break;
		}
	}
	
	protected void showSelectSortingDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_method)
			.setItems(sortingItems, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sorting.setSortMode(which);
					applySorting();
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}
	
	protected void showSelectViewingDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.view)
			.setItems(viewingItems, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					viewing.setViewing(which);
					if (which == Viewing.VIEW_BETWEEN_DATES) {
						showSelectBetweenDatesDialog();
					} else {
						applyViewing();
					}
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}
	
	@SuppressWarnings("deprecation")
	protected void showSelectBetweenDatesDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.select_between_dates_dialog, null);
		
		// FROM
		
		final WheelView fromDayWheel = (WheelView) dialogView.findViewById(R.id.fromDayWheel);
		ArrayWheelAdapter<String> fromDayAdapter = new ArrayWheelAdapter<String>(this, 
				Utils.getDaysInMonth(viewing.getFrom().getMonth(), viewing.getFrom().getYear()));
		fromDayAdapter.setTextSize(18);
		fromDayWheel.setViewAdapter(fromDayAdapter);
		fromDayWheel.setCurrentItem(viewing.getFrom().getDate());
		fromDayWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				viewing.getFrom().setDate(newValue);
			}
		});
		
		final WheelView fromMonthWheel = (WheelView) dialogView.findViewById(R.id.fromMonthWheel);
		ArrayWheelAdapter<String> fromMonthAdapter = new ArrayWheelAdapter<String>(this, getResources().getStringArray(R.array.months_array));
		fromMonthAdapter.setTextSize(18);
		fromMonthWheel.setViewAdapter(fromMonthAdapter);
		fromMonthWheel.setCurrentItem(viewing.getFrom().getMonth());
		fromMonthWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				viewing.getFrom().setMonth(newValue);
				String[] daysInMonth = Utils.getDaysInMonth(viewing.getFrom().getMonth(), viewing.getFrom().getYear());
				ArrayWheelAdapter<String> fromDayAdapter = new ArrayWheelAdapter<String>(RecordsScreen.this, daysInMonth);
				fromDayAdapter.setTextSize(18);
				fromDayWheel.setViewAdapter(fromDayAdapter);
				if (viewing.getFrom().getDate() < daysInMonth.length)
					fromDayWheel.setCurrentItem(viewing.getFrom().getDate());
				else
					fromDayWheel.setCurrentItem(daysInMonth.length-1);
			}
		});
		
		final WheelView fromYearWheel = (WheelView) dialogView.findViewById(R.id.fromYearWheel);
		ArrayWheelAdapter<String> fromYearAdapter = new ArrayWheelAdapter<String>(this, 
				Utils.getYears(viewing.getFrom().getYear()));
		fromYearAdapter.setTextSize(18);
		fromYearWheel.setViewAdapter(fromYearAdapter);
		fromYearWheel.setCurrentItem(viewing.getFrom().getYear());
		fromYearWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				viewing.getFrom().setYear(newValue);
				String[] daysInMonth = Utils.getDaysInMonth(fromMonthWheel.getCurrentItem(), viewing.getFrom().getYear());
				ArrayWheelAdapter<String> fromDayAdapter = new ArrayWheelAdapter<String>(RecordsScreen.this, daysInMonth);
				fromDayAdapter.setTextSize(18);
				fromDayWheel.setViewAdapter(fromDayAdapter);
				if (viewing.getFrom().getDate() < daysInMonth.length)
					fromDayWheel.setCurrentItem(viewing.getFrom().getDate());
				else
					fromDayWheel.setCurrentItem(daysInMonth.length-1);
			}
		});
		
		// TO
		
		final WheelView toDayWheel = (WheelView) dialogView.findViewById(R.id.toDayWheel);
		ArrayWheelAdapter<String> toDayAdapter = new ArrayWheelAdapter<String>(this, 
				Utils.getDaysInMonth(viewing.getTo().getMonth(), viewing.getTo().getYear()));
		toDayAdapter.setTextSize(18);
		toDayWheel.setViewAdapter(toDayAdapter);
		toDayWheel.setCurrentItem(viewing.getTo().getDate());
		toDayWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				viewing.getTo().setDate(newValue);
			}
		});
		
		final WheelView toMonthWheel = (WheelView) dialogView.findViewById(R.id.toMonthWheel);
		ArrayWheelAdapter<String> toMonthAdapter = new ArrayWheelAdapter<String>(this, getResources().getStringArray(R.array.months_array));
		toMonthAdapter.setTextSize(18);
		toMonthWheel.setViewAdapter(toMonthAdapter);
		toMonthWheel.setCurrentItem(viewing.getTo().getMonth());
		toMonthWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				viewing.getTo().setMonth(newValue);
				String[] daysInMonth = Utils.getDaysInMonth(viewing.getTo().getMonth(), viewing.getTo().getYear());
				ArrayWheelAdapter<String> toDayAdapter = new ArrayWheelAdapter<String>(RecordsScreen.this, daysInMonth);
				toDayAdapter.setTextSize(18);
				toDayWheel.setViewAdapter(toDayAdapter);
				if (viewing.getTo().getDate() < daysInMonth.length)
					toDayWheel.setCurrentItem(viewing.getTo().getDate());
				else
					toDayWheel.setCurrentItem(daysInMonth.length-1);
			}
		});
		
		final WheelView toYearWheel = (WheelView) dialogView.findViewById(R.id.toYearWheel);
		ArrayWheelAdapter<String> toYearAdapter = new ArrayWheelAdapter<String>(this, 
				Utils.getYears(viewing.getTo().getYear()));
		toYearAdapter.setTextSize(18);
		toYearWheel.setViewAdapter(toYearAdapter);
		toYearWheel.setCurrentItem(viewing.getTo().getYear());
		toYearWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				viewing.getTo().setYear(newValue);
				String[] daysInMonth = Utils.getDaysInMonth(fromMonthWheel.getCurrentItem(), viewing.getTo().getYear());
				ArrayWheelAdapter<String> toDayAdapter = new ArrayWheelAdapter<String>(RecordsScreen.this, daysInMonth);
				toDayAdapter.setTextSize(18);
				toDayWheel.setViewAdapter(toDayAdapter);
				if (viewing.getTo().getDate() < daysInMonth.length)
					toDayWheel.setCurrentItem(viewing.getTo().getDate());
				else
					toDayWheel.setCurrentItem(daysInMonth.length-1);
			}
		});
		
		
		final ViewFlipper flipper = (ViewFlipper) dialogView.findViewById(R.id.flipper);
		
		Button toBtn = (Button) dialogView.findViewById(R.id.toBtn);
		toBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flipper.setInAnimation(RecordsScreen.this, R.anim.slide_in_left);
				flipper.setOutAnimation(RecordsScreen.this, R.anim.slide_out_left);
				flipper.showNext();
			}
		});
		
		Button fromBtn = (Button) dialogView.findViewById(R.id.fromBtn);
		fromBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flipper.setInAnimation(RecordsScreen.this,  R.anim.slide_in_right);
				flipper.setOutAnimation(RecordsScreen.this, R.anim.slide_out_right);
				flipper.showPrevious();
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView)
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				viewing.getFrom().setMonth(fromMonthWheel.getCurrentItem());
				viewing.getTo().setMonth(toMonthWheel.getCurrentItem());
				applyViewing();
				dialog.dismiss();
			}
		})
		.create()
		.show();
	}
	
	protected void applyViewing() {
		viewingBtn.setText(getString(R.string.viewing, viewingItems[viewing.getViewing()]));
		adapter.setViewing(viewing);
	}
	
	protected void applySorting() {
		rightBtn1.setText(sortOrderItems[sorting.getOrderMode()]);
		adapter.setSorting(sorting);
	}

}
