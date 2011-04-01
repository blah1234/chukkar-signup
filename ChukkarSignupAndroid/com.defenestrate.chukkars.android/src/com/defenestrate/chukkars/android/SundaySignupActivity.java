package com.defenestrate.chukkars.android;

import com.defenestrate.chukkars.android.entity.Day;


public class SundaySignupActivity extends SignupActivity 
{
	/** Called when the activity is first created. */
    @Override
    public void onStart()
	{
    	// The activity is about to become visible.
		super.onStart();
		loadPlayers(Day.SUNDAY);
	}
}