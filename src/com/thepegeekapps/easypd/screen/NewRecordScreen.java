package com.thepegeekapps.easypd.screen;

import java.util.Date;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.thepegeekapps.easypd.R;
import com.thepegeekapps.easypd.model.Record;
import com.thepegeekapps.easypd.utils.Utils;
import com.thepegeekapps.easypd.view.SwitchView;
import com.thepegeekapps.easypd.view.SwitchView.OnStateChangedListener;
import com.thepegeekapps.easypd.view.wheel.OnWheelChangedListener;
import com.thepegeekapps.easypd.view.wheel.WheelView;
import com.thepegeekapps.easypd.view.wheel.adapters.ArrayWheelAdapter;

public class NewRecordScreen extends BaseScreen implements OnClickListener, OnStateChangedListener {
	
	public static final int ADD_IMAGE_REQUEST_CODE = 0;
	
	public static final int MODE_CAPTURE_FROM_CAMERA = 0;
	public static final int MODE_SELECT_FROM_LIBRARY = 1;
	
	protected TextView titleView;
	protected Button rightBtn;
	protected EditText name;
	protected EditText location;
	protected SwitchView type;
	protected TextView startDate;
	protected TextView duration;
	protected EditText explanation;
	protected Button selectActionBtn;
	protected ImageView image;
	protected ImageView noImage;
	
	protected Record record;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_record_screen);
		initializeViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (recordStorage.getRecord() == null) {
			record = new Record();
			rightBtn.setText(R.string.add);
		} else {
			record = recordStorage.getRecord();
			rightBtn.setText(R.string.save);
		}
		updateViews();
	}
	
	protected void initializeViews() {
		titleView = (TextView) findViewById(R.id.titleView);
		titleView.setText(getString(R.string.new_record));
		
		rightBtn = (Button) findViewById(R.id.btnRight1);
		rightBtn.setText(getString(R.string.add));
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setOnClickListener(this);
		
		name = (EditText) findViewById(R.id.name);
		location = (EditText) findViewById(R.id.location);
		type = (SwitchView) findViewById(R.id.type);
		type.setOnStateChangedListener(this);
		startDate = (TextView) findViewById(R.id.startDate);
		startDate.setOnClickListener(this);
		duration = (TextView) findViewById(R.id.duration);
		duration.setOnClickListener(this);
		explanation = (EditText) findViewById(R.id.explanation);
		selectActionBtn = (Button) findViewById(R.id.selectActionBtn);
		selectActionBtn.setOnClickListener(this);
		image = (ImageView) findViewById(R.id.image);
		noImage = (ImageView) findViewById(R.id.noImage);
	}
	
	protected void updateViews() {
		updateName();
		updateLocation();
		updateType();
		updateStartDate();
		updateDuration();
		updateExplanation();
		updateImage();
	}
	
	protected void updateName() {
		name.setText(record.getName());
	}
	
	protected void updateLocation() {
		location.setText(record.getLocation());
	}
	
	protected void updateType() {
		type.setState(record.getType());
	}
	
	protected void updateStartDate() {
		startDate.setText(Utils.formatDatetime(record.getStartDate()));
	}
	
	protected void updateDuration() {
		String durationText = "";
		if (record.getHours() != 0) {
			durationText += Utils.quantityText(record.getHours(), getResources().getStringArray(R.array.hours_array));
		}
		if (record.getMinutes() != 0) {
			durationText += " " + Utils.quantityText(record.getMinutes(), getResources().getStringArray(R.array.mins_array));
		}
		duration.setText(durationText);
	}
	
	protected void updateExplanation() {
		explanation.setText(record.getExplanation());
	}
	
	protected void updateImage() {
		if (!TextUtils.isEmpty(record.getImageUrl())) {
			image.setImageBitmap(imgStorage.loadBitmap(record.getImageUrl()));
			image.setVisibility(View.VISIBLE);
			noImage.setVisibility(View.INVISIBLE);
		} else {
			image.setVisibility(View.INVISIBLE);
			noImage.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRight1:
			saveRecord();
			break;
		case R.id.startDate:
			showSelectStartDateDialog();
			break;
		case R.id.duration:
			showSelectDurationDialog();
			break;
		case R.id.selectActionBtn:
			showSelectActionDialog(ADD_IMAGE_REQUEST_CODE);
			break;
		}
	}
	
	public void saveRecord() {
		fillRecordFields();
		if (TextUtils.isEmpty(record.getName())) {
			showDialog(R.string.error, R.string.activity_name_empty, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					name.requestFocus();
				}
			});
			return;
		}
		boolean saved = (recordStorage.getRecord() == null) ? dbStorage.addRecord(record) : dbStorage.updateRecord(record);
		if (saved) {
			showDialog(R.string.success, (recordStorage.getRecord() == null) ? R.string.record_added : R.string.record_edited);
			recordStorage.setRecord(null);
			record = new Record();
			rightBtn.setText(R.string.add);
			updateViews();
		} else {
			showDialog(R.string.error, (recordStorage.getRecord() == null) ? R.string.record_not_added  :R.string.record_not_edited);
		}
	}
	
	protected void fillRecordFields() {
		record.setName(name.getText().toString());
		record.setLocation(location.getText().toString());
		record.setExplanation(explanation.getText().toString());
	}

	@Override
	public void onStateChanged(int newState) {
		record.setType(newState);
	}
	
	@SuppressWarnings("deprecation")
	protected void showSelectStartDateDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.select_start_date_dialog, null);
		
		final Date date = new Date(record.getStartDate());
		
		final WheelView dayWheel = (WheelView) dialogView.findViewById(R.id.dayWheel);
		ArrayWheelAdapter<String> dayAdapter = new ArrayWheelAdapter<String>(this, 
				Utils.getDaysInMonth(date.getMonth(), date.getYear()));
		dayAdapter.setTextSize(18);
		dayWheel.setViewAdapter(dayAdapter);
		dayWheel.setCurrentItem(date.getDate()-1);
		
		final WheelView monthWheel = (WheelView) dialogView.findViewById(R.id.monthWheel);
		ArrayWheelAdapter<String> monthAdapter = new ArrayWheelAdapter<String>(this, getResources().getStringArray(R.array.months_array));
		monthAdapter.setTextSize(18);
		monthWheel.setViewAdapter(monthAdapter);
		monthWheel.setCurrentItem(date.getMonth());
		monthWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				date.setMonth(newValue);
				String[] daysInMonth = Utils.getDaysInMonth(date.getMonth(), date.getYear());
				ArrayWheelAdapter<String> dayAdapter = new ArrayWheelAdapter<String>(NewRecordScreen.this, daysInMonth);
				dayAdapter.setTextSize(18);
				dayWheel.setViewAdapter(dayAdapter);
				if (date.getDate() < daysInMonth.length)
					dayWheel.setCurrentItem(date.getDate());
				else
					dayWheel.setCurrentItem(daysInMonth.length-1);
			}
		});
		
		final WheelView yearWheel = (WheelView) dialogView.findViewById(R.id.yearWheel);
		ArrayWheelAdapter<String> yearAdapter = new ArrayWheelAdapter<String>(this, 
				Utils.getYears(date.getYear()));
		yearAdapter.setTextSize(18);
		yearWheel.setViewAdapter(yearAdapter);
		yearWheel.setCurrentItem(date.getYear());
		yearWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				date.setYear(newValue);
				String[] daysInMonth = Utils.getDaysInMonth(monthWheel.getCurrentItem(), date.getYear());
				ArrayWheelAdapter<String> dayAdapter = new ArrayWheelAdapter<String>(NewRecordScreen.this, daysInMonth);
				dayAdapter.setTextSize(18);
				dayWheel.setViewAdapter(dayAdapter);
				if (date.getDate() < daysInMonth.length)
					dayWheel.setCurrentItem(date.getDate());
				else
					dayWheel.setCurrentItem(daysInMonth.length-1);
			}
		});
		
		final WheelView ampmWheel = (WheelView) dialogView.findViewById(R.id.ampmWheel);
		ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<String>(this, Utils.ampmItems);
		ampmAdapter.setTextSize(18);
		ampmWheel.setViewAdapter(ampmAdapter);
		ampmWheel.setCurrentItem(Utils.getAmpm(date.getTime()));
		ampmWheel.setEnabled(false);
		
		final WheelView hoursWheel = (WheelView) dialogView.findViewById(R.id.hoursWheel);
		ArrayWheelAdapter<String> hoursAdapter = new ArrayWheelAdapter<String>(this, Utils.getHoursItems());
		hoursAdapter.setTextSize(18);
		hoursWheel.setViewAdapter(hoursAdapter);
		hoursWheel.setCurrentItem(Utils.getHours(date.getTime()));
		hoursWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				date.setHours(newValue);
				ampmWheel.setCurrentItem(newValue < 12 ? 0 : 1, true);
			}
		});
		
		final WheelView minutesWheel = (WheelView) dialogView.findViewById(R.id.minutesWheel);
		ArrayWheelAdapter<String> minutesAdapter = new ArrayWheelAdapter<String>(this, Utils.getMinutesItems());
		minutesAdapter.setTextSize(18);
		minutesWheel.setViewAdapter(minutesAdapter);
		minutesWheel.setCurrentItem(Utils.getMinutes(date.getTime()));
		minutesWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				date.setMinutes(newValue);
			}
		});
		
		
		final ViewFlipper flipper = (ViewFlipper) dialogView.findViewById(R.id.flipper);
		
		Button timeBtn = (Button) dialogView.findViewById(R.id.timeBtn);
		timeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flipper.setInAnimation(NewRecordScreen.this, R.anim.slide_in_left);
				flipper.setOutAnimation(NewRecordScreen.this, R.anim.slide_out_left);
				flipper.showNext();
			}
		});
		
		Button dateBtn = (Button) dialogView.findViewById(R.id.dateBtn);
		dateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flipper.setInAnimation(NewRecordScreen.this,  R.anim.slide_in_right);
				flipper.setOutAnimation(NewRecordScreen.this, R.anim.slide_out_right);
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
				date.setDate(dayWheel.getCurrentItem()+1);
				record.setStartDate(date.getTime());
				updateStartDate();
				dialog.dismiss();
			}
		})
		.create()
		.show();
	}
	
	public void showSelectDurationDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.select_duration_dialog, null);
		
		final WheelView hoursWheel = (WheelView) dialogView.findViewById(R.id.hours);
		ArrayWheelAdapter<String> hoursAdapter = new ArrayWheelAdapter<String>(this, Utils.getHoursItems());
		hoursAdapter.setTextSize(18);
		hoursWheel.setViewAdapter(hoursAdapter);
		hoursWheel.setCurrentItem(record.getHours());
		
		final WheelView minutesWheel = (WheelView) dialogView.findViewById(R.id.minutes);
		ArrayWheelAdapter<String> minutesAdapter = new ArrayWheelAdapter<String>(this, Utils.getMinutesItems());
		minutesAdapter.setTextSize(18);
		minutesWheel.setViewAdapter(minutesAdapter);
		minutesWheel.setCurrentItem(record.getMinutes());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				record.setHours(hoursWheel.getCurrentItem());
				record.setMinutes(minutesWheel.getCurrentItem());
				updateDuration();
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
	
	public void showSelectActionDialog(final int requestCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		PackageManager pm = getPackageManager();
		String[] items = null;
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			items = new String[] {getString(R.string.take_photo), getString(R.string.select_from_library)};
		} else {
			items = new String[] {getString(R.string.select_from_library)};
		}
		if (record.hasImage()) {
			String[] newItems = new String[items.length + 1];
			for (int i=0; i<items.length; i++) {
				newItems[i] = items[i];
			}
			newItems[newItems.length-1] = getString(R.string.remove_photo);
			items = newItems;
		}
		
		builder.setTitle(R.string.select_option)
		.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog alert = (AlertDialog) dialog;
				String selectedItem = (String) alert.getListView().getAdapter().getItem(which);
				if (selectedItem.equals(getString(R.string.take_photo))) {
					Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		            startActivityForResult(Intent.createChooser(cameraIntent, "Take image"), requestCode);
				} else if (selectedItem.equals(getString(R.string.select_from_library))) {
					Intent intent = new Intent(Intent.ACTION_PICK);
					intent.setType("image/*");
					startActivityForResult(intent, requestCode);
				} else if (selectedItem.equals(getString(R.string.remove_photo))) {
					imgStorage.saveBitmap(null, record.getImageUrl());
					record.setImageUrl(null);
					updateImage();
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
	
	public String getRealPathFromURI(Uri contentUri) {
	    String[] proj = { MediaStore.Images.Media.DATA };
	    Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == ADD_IMAGE_REQUEST_CODE) {
			(new AddImageTask(data)).execute((Void[]) null);
		}
	}
	
	class AddImageTask extends AsyncTask<Void, Void, Boolean> {
		
		protected Intent data;
		
		public AddImageTask(Intent data) {
			this.data = data;
		}
		
		@Override
		protected void onPreExecute() {
			showProgressDialog(getString(R.string.importing));
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Bitmap bitmap = null;
			try {
				if (data != null && data.hasExtra("data")) {
					bitmap = (Bitmap) data.getExtras().get("data");
				} else {
					bitmap = BitmapFactory.decodeFile(getRealPathFromURI(data.getData()));
				}				
				if (bitmap != null) {
					int targetW = image.getVisibility() == View.VISIBLE ? image.getWidth() : noImage.getWidth();
					int targetH = image.getVisibility() == View.VISIBLE ? image.getHeight() : noImage.getHeight();
//					int scaledW = bitmap.getWidth();
//					int scaledH = bitmap.getHeight();
//					while (scaledW > targetW || scaledH > targetH) {
//						scaledW--;
//						scaledH--;
//					}
					bitmap = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true);
				}
				if (bitmap != null) {
					record.setImageUrl(record.toString());
					imgStorage.saveBitmap(bitmap, record.toString());
					bitmap.recycle();
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			hideProgressDialog();
			if (result) {
				updateImage();
			} else {
				Toast.makeText(NewRecordScreen.this, R.string.add_image_error, Toast.LENGTH_SHORT).show();
			}
		}
		
	}

}
