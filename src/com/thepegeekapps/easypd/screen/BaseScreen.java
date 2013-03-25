package com.thepegeekapps.easypd.screen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.thepegeekapps.easypd.R;
import com.thepegeekapps.easypd.storage.DatabaseStorage;
import com.thepegeekapps.easypd.storage.ImageStorage;
import com.thepegeekapps.easypd.storage.RecordStorage;

public class BaseScreen extends Activity {
	
	protected DatabaseStorage dbStorage;
	protected ImageStorage imgStorage;
	protected RecordStorage recordStorage;
	
	protected ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbStorage = DatabaseStorage.getInstance(this);
		imgStorage = ImageStorage.getInstance(this);
		recordStorage = RecordStorage.getInstance();
	}
	
	protected void showDialog(int titleResId, int messageResId) {
		showDialog(titleResId, messageResId, null);
	}
	
	protected void showDialog(int titleResId, int messageResId, DialogInterface.OnClickListener okListener) {
		showDialog(getString(titleResId), getString(messageResId), okListener);
	}
	
	protected void showDialog(String title, String message, DialogInterface.OnClickListener okListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
			.setMessage(message)
			.setPositiveButton(R.string.ok, okListener)
			.create()
			.show();
	}
	
	protected void showProgressDialog(String message) {
//		handler.sendEmptyMessage(PROGRESS_DIALOG_SHOW);
//		handler.obtainMessage(PROGRESS_DIALOG_SHOW, message).sendToTarget();
		if (progressDialog == null) {
			 progressDialog = new ProgressDialog(BaseScreen.this);
			 progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		 }
		 progressDialog.setMessage(message);
		 progressDialog.show();
	}
	
	protected void hideProgressDialog() {
//		handler.sendEmptyMessage(PROGRESS_DIALOG_HIDE);
//		handler.obtainMessage(PROGRESS_DIALOG_HIDE, null).sendToTarget();
		if (progressDialog != null && progressDialog.isShowing())
			 progressDialog.dismiss();
	}

}
