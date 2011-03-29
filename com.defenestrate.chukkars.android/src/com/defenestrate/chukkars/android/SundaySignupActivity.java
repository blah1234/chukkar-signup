package com.defenestrate.chukkars.android;

import android.os.Bundle;

import com.defenestrate.chukkars.android.entity.Day;



public class SundaySignupActivity extends SignupActivity 
{
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        loadPlayers(Day.SUNDAY);
    }
}