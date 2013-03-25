package com.thepegeekapps.easypd.screen;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.thepegeekapps.easypd.R;
import com.thepegeekapps.easypd.utils.Utils;

@SuppressWarnings("deprecation")
public class MainScreen extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		initTabs();
	}
	
	protected void initTabs() {
		TabHost tabHost = getTabHost();
		Resources res = getResources();
		
		TabSpec newRecordTabSpec = tabHost.newTabSpec("new_record")
			.setIndicator(getString(R.string.add_record), res.getDrawable(R.drawable.ic_tab_new_record))
			.setContent(new Intent(this, NewRecordScreen.class));
		
		TabSpec recordsTabSpec = tabHost.newTabSpec("records")
				.setIndicator(getString(R.string.records), res.getDrawable(R.drawable.ic_tab_records))
				.setContent(new Intent(this, RecordsScreen.class));
				
		TabSpec hoursTabSpec = tabHost.newTabSpec("hours")
				.setIndicator(Utils.capitalize(getString(R.string.hours)), res.getDrawable(R.drawable.ic_tab_hours))
				.setContent(new Intent(this, HoursScreen.class));
		
		TabSpec aboutTabSpec = tabHost.newTabSpec("about")
				.setIndicator(getString(R.string.about), res.getDrawable(R.drawable.ic_tab_about))
				.setContent(new Intent(this, AboutScreen.class));
		
		tabHost.addTab(newRecordTabSpec);
		tabHost.addTab(recordsTabSpec);
		tabHost.addTab(hoursTabSpec);
		tabHost.addTab(aboutTabSpec);
	}

}
