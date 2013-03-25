package com.thepegeekapps.easypd.screen;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
import com.thepegeekapps.easypd.R;
import com.thepegeekapps.easypd.model.Record;
import com.thepegeekapps.easypd.model.Viewing;
import com.thepegeekapps.easypd.utils.Utils;
import com.thepegeekapps.easypd.view.wheel.OnWheelChangedListener;
import com.thepegeekapps.easypd.view.wheel.WheelView;
import com.thepegeekapps.easypd.view.wheel.adapters.ArrayWheelAdapter;

public class HoursScreen extends BaseScreen implements OnClickListener {
	
	public static final String TAG = HoursScreen.class.getSimpleName();
	
	public static final String APP_KEY = "ol6cfqia6x4d7zs";
    public static final String APP_SECRET = "zw3icu9fiprses5";
    public static final String ACCOUNT_PREFS_NAME = "prefs";
    public static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    public static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    public static final AccessType ACCESS_TYPE = AccessType.DROPBOX;   
	
	protected TextView titleView;
	protected Button rightBtn;
	protected Button viewingBtn;
	protected TextView total;
	
	protected Viewing viewing;
	protected String[] viewingItems;
	
	protected List<Record> records;
	
	protected DropboxAPI<AndroidAuthSession> mApi;
	protected boolean showConfirmUploadingDialog;
	protected boolean loggedIn;
	protected boolean showDropboxDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hours_screen);
		
		viewing = new Viewing();
		viewingItems = getResources().getStringArray(R.array.viewing_array);
		
		AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
		
		initializeViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		records = dbStorage.getRecords();
		applyViewing();
		
		AndroidAuthSession session = mApi.getSession();
		if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                loggedIn = true;
                
                if (showConfirmUploadingDialog) {
                	showConfirmUploadingDialog = false;
                	List<Record> exportRecords = getViewingRecords();
					if (exportRecords == null || exportRecords.isEmpty()) {
						showDialog(R.string.error, R.string.no_records);
					} else {
						ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
						NetworkInfo info = conManager.getActiveNetworkInfo();
						if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
							showDialog(R.string.error, R.string.no_connection);
						} else {
							if (loggedIn) {
								new DropboxUploadTask(exportRecords).execute((Void[]) null);
				            } else {
				            	showConfirmUploadingDialog = true;
				                mApi.getSession().startAuthentication(this);
				            }
						}
					}
                }
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
	}
	
	protected void showConfirmUploadingDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.export_to_dropbox)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					tryExportToDropbox();
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
	
	protected void tryExportToDropbox() {
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || info.getState() != NetworkInfo.State.CONNECTED) {
			showDialog(R.string.error, R.string.no_connection);
		} else {
			List<Record> exportRecords = getViewingRecords();
			if (exportRecords == null || exportRecords.isEmpty()) {
				showDialog(R.string.error, R.string.no_records);
			} else {
				if (loggedIn) {
					new DropboxUploadTask(exportRecords).execute((Void[]) null);
	            } else {
	            	showConfirmUploadingDialog = true;
	                mApi.getSession().startAuthentication(this);
	            }
			}							
		}
	}
	
	protected AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
	
	protected String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    protected void storeKeys(String key, String secret) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    protected void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
	
	protected void initializeViews() {
		titleView = (TextView) findViewById(R.id.titleView);
		titleView.setText(getString(R.string.pd_hours));
		
		rightBtn = (Button) findViewById(R.id.btnRight1);
		rightBtn.setText(R.string.export);
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setOnClickListener(this);
		
		total = (TextView) findViewById(R.id.total);
		viewingBtn = (Button) findViewById(R.id.viewingBtn);
		viewingBtn.setOnClickListener(this);
		
	}
	
	protected void applyViewing() {
		viewingBtn.setText(getString(R.string.viewing, viewingItems[viewing.getViewing()]));
		total.setText(getTotalTime());
	}
	
	protected String getTotalTime() {
		int hours = 0;
		int minutes = 0;
		for (Record record : records) {
			switch (viewing.getViewing()) {
			case Viewing.VIEW_ALL:
				hours += record.getHours();
				minutes += record.getMinutes();
				break;
			case Viewing.VIEW_INTERNAL:
				if (record.getType() == Record.TYPE_INTERNAL) {
					hours += record.getHours();
					minutes += record.getMinutes();
				}
				break;
			case Viewing.VIEW_EXTERNAL:
				if (record.getType() == Record.TYPE_EXTERNAL) {
					hours += record.getHours();
					minutes += record.getMinutes();
				}
				break;
			case Viewing.VIEW_BETWEEN_DATES:
				Date date = new Date(record.getStartDate());
				if (date.after(viewing.getFrom()) && date.before(viewing.getTo())) {
					hours += record.getHours();
					minutes += record.getMinutes();
				}
				break;
			}
		}
		hours += (int) (minutes / 60);
		minutes = minutes % 60;
		return Utils.quantityText(hours, getResources().getStringArray(R.array.hours_array)) + " " +
			Utils.quantityText(minutes, getResources().getStringArray(R.array.mins_array));
	}
	
	protected List<Record> getViewingRecords() {
		if (viewing.getViewing() == Viewing.VIEW_ALL) {
			return records;
		}
		List<Record> viewingRecords = new ArrayList<Record>();
		for (Record record : records) {
			switch (viewing.getViewing()) {
			case Viewing.VIEW_INTERNAL:
				if (record.getType() == Record.TYPE_INTERNAL) {
					viewingRecords.add(record);
				}
				break;
			case Viewing.VIEW_EXTERNAL:
				if (record.getType() == Record.TYPE_EXTERNAL) {
					viewingRecords.add(record);
				}
				break;
			case Viewing.VIEW_BETWEEN_DATES:
				Date date = new Date(record.getStartDate());
				if (date.after(viewing.getFrom()) && date.before(viewing.getTo())) {
					viewingRecords.add(record);
				}
				break;
			}
		}
		return viewingRecords;
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
				ArrayWheelAdapter<String> fromDayAdapter = new ArrayWheelAdapter<String>(HoursScreen.this, daysInMonth);
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
				ArrayWheelAdapter<String> fromDayAdapter = new ArrayWheelAdapter<String>(HoursScreen.this, daysInMonth);
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
				ArrayWheelAdapter<String> toDayAdapter = new ArrayWheelAdapter<String>(HoursScreen.this, daysInMonth);
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
				ArrayWheelAdapter<String> toDayAdapter = new ArrayWheelAdapter<String>(HoursScreen.this, daysInMonth);
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
				flipper.setInAnimation(HoursScreen.this, R.anim.slide_in_left);
				flipper.setOutAnimation(HoursScreen.this, R.anim.slide_out_left);
				flipper.showNext();
			}
		});
		
		Button fromBtn = (Button) dialogView.findViewById(R.id.fromBtn);
		fromBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flipper.setInAnimation(HoursScreen.this,  R.anim.slide_in_right);
				flipper.setOutAnimation(HoursScreen.this, R.anim.slide_out_right);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.viewingBtn:
			showSelectViewingDialog();
			break;
		case R.id.btnRight1:
			showConfirmUploadingDialog();
			break;
		}
	}
	
	class DropboxUploadTask extends AsyncTask<Void, Long, Boolean> {
		
		protected ProgressDialog dialog;
		protected List<Record> viewingRecords;
		
		protected String filename;
		
		public DropboxUploadTask(List<Record> viewingRecords) {
			this.viewingRecords = viewingRecords;
			dialog = new ProgressDialog(HoursScreen.this);
    		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		dialog.setMessage(getString(R.string.uploading));
    		dialog.setIndeterminate(true);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String rootFolder = "/Easy PD";
				try { mApi.createFolder(rootFolder); } catch (Exception e) { e.printStackTrace(); }
				
				filename = "Professional Development Record - " + Utils.formatRecordFilename(System.currentTimeMillis()) + ".csv";
				String recordPath = rootFolder + "/" + filename;
				String recordsCSV = getRecordsCSV();
				
				ByteArrayInputStream bais = new ByteArrayInputStream(recordsCSV.getBytes());
				try {
					mApi.putFileOverwrite(recordPath, bais, recordsCSV.getBytes().length, new ProgressListener() {
						@Override
						public void onProgress(long bytes, long total) {
							publishProgress(bytes, total);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (bais != null)
						bais.close();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Long... values) {
			String message = String.format(getString(R.string.upload_pattern), 
					filename, Utils.getReadableFilesize(values[0]), Utils.getReadableFilesize(values[1]));
			dialog.setMessage(message);
			super.onProgressUpdate(values);
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			if (!result.booleanValue()) {
				showDialog(R.string.error, R.string.upload_error);
			} else {
				showDialog(R.string.success, R.string.upload_success);
			}
		}
		
		protected String getRecordsCSV() {
			String result = getString(R.string.activity) + ',' +
					getString(R.string.event_type) + ',' +
					getString(R.string.location) + ',' +
					getString(R.string.date) + ',' +
					getString(R.string.duration) + ',' +
					getString(R.string.desc) + '\n';
			for (Record record : viewingRecords) {
				result += record.getName() + ',' +
						record.getTypeName(HoursScreen.this) + ',' +
						record.getLocation() + ',' +
						"\"" + Utils.formatRecordDatetime(record.getStartDate()) + "\"" + ',' +
						"\"" + Utils.getDurationText(HoursScreen.this, record) + "\"" + ',' +
						"\"" + record.getExplanation() + "\"" + '\n';
			}
			return result;
		}
		
	}

}
