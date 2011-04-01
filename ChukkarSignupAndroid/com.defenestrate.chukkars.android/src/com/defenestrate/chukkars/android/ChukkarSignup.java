package com.defenestrate.chukkars.android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ChukkarSignup extends TabActivity
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static final String TAB_INDEX_KEY = "TAB_INDEX_KEY"; 


	//////////////////////////// Activity METHODS //////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.signup_tabs);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, SaturdaySignupActivity.class);
	    intent.putExtra(TAB_INDEX_KEY, 0);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("day0").setIndicator(
	    	res.getString(R.string.day0_init_tab_title), res.getDrawable(R.drawable.ic_tab_sat) ).setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, SundaySignupActivity.class);
	    intent.putExtra(TAB_INDEX_KEY, 1);
	    
	    spec = tabHost.newTabSpec("day1").setIndicator(
	    	res.getString(R.string.day1_init_tab_title), res.getDrawable(R.drawable.ic_tab_sun) ).setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
}
